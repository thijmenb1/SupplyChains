extends Node

var occupied_cells: Dictionary = {}
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
