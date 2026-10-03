extends Node

var occupied_cells: Dictionary = {}
var building_solid_cells: Dictionary = {}
var road_colors: Dictionary = {}

const ROAD_WEIGHT_THRESHOLD: float = 3.0
var astar := AStarGrid2D.new()


func setup_astar_grid(width_tiles: int, heigth_tiles: int) -> void:
	astar.region = Rect2i(-width_tiles / 2, -heigth_tiles / 2, width_tiles, heigth_tiles)
	astar.cell_size = Vector2(Global.TILE_SIZE, Global.TILE_SIZE)
	astar.offset = Vector2(Global.TILE_SIZE / 2.0, Global.TILE_SIZE / 2.0)
	astar.default_compute_heuristic = AStarGrid2D.HEURISTIC_OCTILE
	astar.default_estimate_heuristic = AStarGrid2D.HEURISTIC_OCTILE
	astar.diagonal_mode = AStarGrid2D.DIAGONAL_MODE_NEVER
	astar.update()

func can_place_building(grid_pos: Vector2i, size: Vector2i) -> bool:
	return can_place_on_cells(get_footprint_cells(grid_pos, size))

func register_building(occupant, grid_pos: Vector2i, size: Vector2i) -> bool:
	return register_on_cells(occupant, get_footprint_cells(grid_pos, size))

func remove_building(grid_pos: Vector2i, size: Vector2i) -> void:
	remove_from_cells(get_footprint_cells(grid_pos, size))

func get_building_at(grid_pos: Vector2i):
	return occupied_cells.get(grid_pos, null)

func get_footprint_cells(grid_pos: Vector2i, size: Vector2i) -> Array[Vector2i]:
	var cells: Array[Vector2i]
	for x in range(grid_pos.x, grid_pos.x + size.x):
		for y in range(grid_pos.y, grid_pos.y + size.y):
			cells.append(Vector2i(x,y))
	return cells

func road_atlas_for_color(road_layer: TileMapLayer, cell: Vector2i, road_type: int) -> Vector2i:
	var atlas: Vector2i = road_layer.get_cell_atlas_coords(cell)
	if atlas.x < 0:
		return atlas
	var row_offset: int = (atlas.y - 5) % 2
	var base_row: int = Tiles.ROAD_COLOR_ROW[road_type]
	return Vector2i(atlas.x, base_row + row_offset)

func repaint_road_colors(road_layer: TileMapLayer, cell: Vector2i) -> void:
	for c in [cell, cell + Vector2i.UP, cell + Vector2i.DOWN, cell + Vector2i.LEFT, cell + Vector2i.RIGHT]:
		if not road_colors.has(c):
			continue
		var new_atlas := road_atlas_for_color(road_layer, c, road_colors[c])
		if new_atlas.x >= 0:
			road_layer.set_cell(c, Tiles.ROAD_SOURCE, new_atlas)

func can_place_on_cells(cells: Array[Vector2i]) -> bool:
	for cell in cells:
		if occupied_cells.has(cell):
			return false
	return true

func register_on_cells(occupant, cells: Array[Vector2i]) -> bool:
	if not can_place_on_cells(cells):
		return false
	for cell in cells:
		occupied_cells[cell] = occupant
	return true

func remove_from_cells(cells: Array[Vector2i]) -> void:
	for cell in cells:
		occupied_cells.erase(cell)

func set_cells_astar_weight(cells: Array[Vector2i], weight: float) -> void:
	for cell in cells:
		astar.set_point_weight_scale(cell, weight)

func set_footprint_astar_weight(grid_pos: Vector2i, size: Vector2i, weight: float) -> void:
	set_cells_astar_weight(get_footprint_cells(grid_pos, size), weight)

func set_cells_astar_solid(cells: Array[Vector2i], solid: bool) -> void:
	for cell in cells:
		if not astar.is_in_boundsv(cell):
			continue
		if solid:
			if not building_solid_cells.has(cell):
				building_solid_cells[cell] = astar.is_point_solid(cell)
			astar.set_point_solid(cell, true)
		elif building_solid_cells.has(cell):
			astar.set_point_solid(cell, building_solid_cells[cell])
			building_solid_cells.erase(cell)

func set_footprint_astar_solid(grid_pos: Vector2i, size: Vector2i, solid: bool) -> void:
	set_cells_astar_solid(get_footprint_cells(grid_pos, size), solid)

func _open_ends(start: Vector2i, end: Vector2i) -> Array[Vector2i]:
	var opened: Array[Vector2i] = []
	for cell in [start, end]:
		if building_solid_cells.has(cell) and astar.is_in_boundsv(cell) and astar.is_point_solid(cell):
			astar.set_point_solid(cell, false)
			opened.append(cell)
	return opened

func _close_ends(opened: Array[Vector2i]) -> void:
	for cell in opened:
		astar.set_point_solid(cell, true)

func get_id_path_open_ends(start: Vector2i, end: Vector2i) -> Array[Vector2i]:
	var opened := _open_ends(start, end)
	var result: Array[Vector2i] = astar.get_id_path(start, end)
	_close_ends(opened)
	return result

func get_point_path_open_ends(start: Vector2i, end: Vector2i) -> PackedVector2Array:
	var opened := _open_ends(start, end)
	var result: PackedVector2Array = astar.get_point_path(start, end)
	_close_ends(opened)
	return result

func _has_fully_paved_path(start: Vector2i, end: Vector2i) -> bool:
	var raw_points = get_id_path_open_ends(start, end)
	if raw_points.is_empty():
		return false
	for id in raw_points:
		if astar.get_point_weight_scale(id) > ROAD_WEIGHT_THRESHOLD:
			return false
	return true

func paved_connected(from_cells: Array[Vector2i], to_cells: Dictionary) -> bool:
	var visited: Dictionary = {}
	var queue: Array[Vector2i] = []
	for c in from_cells:
		visited[c] = true
		queue.append(c)
	var head: int = 0
	while head < queue.size():
		var cell: Vector2i = queue[head]
		head += 1
		for d in [Vector2i.UP, Vector2i.DOWN, Vector2i.LEFT, Vector2i.RIGHT]:
			var n: Vector2i = cell + d
			if visited.has(n):
				continue
			if to_cells.has(n):
				return true
			if not astar.is_in_boundsv(n) or astar.is_point_solid(n):
				continue
			if astar.get_point_weight_scale(n) > ROAD_WEIGHT_THRESHOLD:
				continue
			visited[n] = true
			queue.append(n)
	return false
