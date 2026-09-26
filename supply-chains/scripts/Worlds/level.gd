extends Node2D

@export var noiseHeigthText : NoiseTexture2D
@export var noiseTempText: NoiseTexture2D
@export var noiseMoistText: NoiseTexture2D
@export var noiseOreText: NoiseTexture2D

@export var TerrainLayer: TileMapLayer
@export var ConstructionLayer: TileMapLayer
@export var OreLayer: TileMapLayer
@export var RoadLayer: TileMapLayer
@export var PreviewLayer: TileMapLayer
@export var camera: Camera2D

@export var TreeScene: PackedScene
@export var TreeLayer: Node2D

var upgrade_size: Vector2i = Vector2i.ONE


#generation vars
var noise_alt: Noise
var noise_temp: Noise
var noise_moist: Noise
var noise_ore: Noise

var chunk_size: int = 16
var render_distance: int = 14
var loaded_chunks: Dictionary = {} # Keeps track of already generated chunks
var world_size : int = 300   #chunks

 
#temp vars
var selected_terrainSet: int = 0

var selectedCell

var is_dragging_left: bool = false
var is_dragging_right: bool = false
var last_drag_cell: Vector2i = Vector2i(-9999, -9999)

# Airstrip vars
var airstrip_start: Vector2i = Vector2i(-9999, -9999)
var airstrip_dragging: bool = false
var airstrip_edit_index: int = -1
var airstrip_edit_axis: String = ""
const AIRSTRIP_GRAB_TOLERANCE: int = 2
const AIRSTRIP_MIN_LENGTH: int = 30
const AIRSTRIP_WIDTH: int = 3
const AIRSTRIP_MIN_DRAG: int = 5

# Taxiway vars
var taxiway_start: Vector2i = Vector2i(-9999, -9999)
var taxiway_dragging: bool = false
const TAXIWAY_WIDTH: int = 2

func _ready():
	var total_grid_span = world_size * chunk_size
	GridManager.setup_astar_grid(total_grid_span, total_grid_span)
	
	# Randomize seeds
	noiseHeigthText.noise.seed = randi()
	noiseTempText.noise.seed = randi()
	noiseMoistText.noise.seed = randi()
	noiseOreText.noise.seed = randi()
	
	noise_alt = noiseHeigthText.noise
	noise_temp = noiseTempText.noise
	noise_moist = noiseMoistText.noise
	noise_ore = noiseOreText.noise
	
	selected_terrainSet = 0
	Global.build_stage = Global.BuildStage.PLACE_RESOURCE_BOX
	Global.starter_vehicles_spawned = false
	Global.baseBuild = false

	Global.vehicle_layer = $VehicleLayer
	Global.route_layer = $RouteLayer
	Global.road_layer = $RoadLayer
	Global.construction_layer = $ConstructionLayer

func _apply_build_stage_tool() -> void:
	match Global.build_stage:
		Global.BuildStage.PLACE_RESOURCE_BOX:
			Global.selected_factory_type = "resourceBox"
			Global.clickMode = "place_factory"
			Global.baseBuild = false
		Global.BuildStage.PLACE_RUNWAY:
			selected_terrainSet = 0
			Global.selected_terrain = Tiles.AIRSTRIP_DIRT
			Global.clickMode = "place_airstrip"
			Global.baseBuild = false
		Global.BuildStage.PLACE_TERMINAL:
			Global.selected_factory_type = "cargoTerminal"
			Global.clickMode = "place_factory"
			Global.baseBuild = false
		Global.BuildStage.PLACE_BASE_TILES:
			selected_terrainSet =0
			Global.selcted_tile = Vector2i(0,19)
			Global.clickMode = "place_terrainSet"
			Global.baseBuild = false
		Global.BuildStage.READY:
			Global.baseBuild = true


func _process(_delta):
	selectedCell = RoadLayer.local_to_map(RoadLayer.get_global_mouse_position())
	if camera:
		update_chunks_around_camera()
	
	if PreviewLayer:
		PreviewLayer.clear()
		if not Input.is_mouse_button_pressed(MOUSE_BUTTON_RIGHT):
			previewTile()
	
		_apply_build_stage_tool()
	
	if Global.build_stage == Global.BuildStage.READY:
		if Input.is_action_just_pressed("select_1"):
			selected_terrainSet = 0
			Global.selected_terrain = Tiles.SINGEL_ROAD_GRAVEL
			Global.selcted_tile = Vector2i(1,5)
			Global.clickMode = "place_terrainSet"
		if Input.is_action_just_pressed("select_2"):
			selected_terrainSet = 0
			Global.selected_terrain = Tiles.SINGEL_ROAD_CEMENT
			Global.selcted_tile = Vector2i(1,7)
			Global.clickMode = "place_terrainSet"
		if Input.is_action_just_pressed("select_3"):
			selected_terrainSet = 0
			Global.selected_terrain = Tiles.SINGEL_ROAD_ASPHALT
			Global.selcted_tile = Vector2i(1,9)
			Global.clickMode = "place_terrainSet"
		if Input.is_action_just_pressed("select_4"):
			selected_terrainSet = 0
			Global.selected_terrain = Tiles.BASE
			Global.selcted_tile = Vector2i(0,19)
			Global.clickMode = "place_terrainSet"
		if Input.is_action_just_pressed("select_5"):
			Global.selcted_tile = Vector2i(0,0)
			Global.selected_factory_type = "gaspower"
			Global.clickMode = "place_factory"
	if Input.is_action_just_pressed("esc"):
		Global.clickMode = "highlight"
	if Input.is_action_just_pressed("speedUpTime"):
		Global.speed_up()
	if Input.is_action_just_pressed("slowDownTime"):
		Global.slow_down()
	if Input.is_action_just_pressed("pause"):
		Global.toggle_pause()


func _unhandled_input(event: InputEvent) -> void:
	selectedCell = RoadLayer.local_to_map(RoadLayer.get_global_mouse_position())

	if event is InputEventMouseButton:
		if event.button_index == MOUSE_BUTTON_LEFT:
			if Global.clickMode == "place_airstrip":
				if event.pressed:
					start_airstrip_drag()
				else:
					commit_airstrip_drag()
					
			elif Global.clickMode == "place_taxiway":
				if event.pressed:
					start_taxiway_drag()
				else:
					commit_taxiway_drag()
			if Global.clickMode == "place_factory":
				place_factory()
			else:
				paint_road()
				last_drag_cell = selectedCell
				is_dragging_left = event.pressed
				
		elif event.button_index == MOUSE_BUTTON_RIGHT:
			if Global.clickMode == "place_factory":
				demolish_factory()
				return
			
			erase_road()
			last_drag_cell = selectedCell
			is_dragging_right = event.pressed

	elif event is InputEventMouseMotion:
		if selectedCell != last_drag_cell:
			last_drag_cell = selectedCell
			
			if is_dragging_left and Global.clickMode != "place_upgrade":
				paint_road()
			elif is_dragging_right and Global.clickMode != "place_upgrade":
				erase_road()

	elif event is InputEventMouseMotion:
		var current_cell = RoadLayer.local_to_map(RoadLayer.get_global_mouse_position())
		
		if current_cell != last_drag_cell:
			last_drag_cell = current_cell
			
			if is_dragging_left and Global.clickMode != "place_upgrade":
				paint_road()
			elif is_dragging_right and Global.clickMode != "place_upgrade":
				erase_road()
	
	
func paint_road():
	if Global.clickMode == "highlight":
		return
	if Global.mouseIsOVerUI: return
	if GridManager.get_building_at(selectedCell) != null: return
	if selectedCell in Global.baseTiles:
		if not selectedCell in Global.occupied_base_tiles:
			Global.baseTiles.erase(selectedCell)
		else:
			return
	
	for existing in Global.factorys:
		if existing["grid_pos"] == selectedCell:
			Global.factorys.erase(existing)

	if Global.clickMode == "place_terrainSet":
		if Global.selected_terrain in [Tiles.SINGEL_ROAD_GRAVEL, Tiles.SINGEL_ROAD_CEMENT, Tiles.SINGEL_ROAD_ASPHALT]:
			var cost: Dictionary = Global.get_road_construction_cost(Global.selected_terrain)
			var result: Dictionary = {
				"terrain_set": selected_terrainSet,
				"terrain": Tiles.ROAD_TERRAIN,
				"is_colored_road": true,
				"road_color": Global.selected_terrain
			}
			Global.start_construction("road", [selectedCell], cost, result)
		else:
			RoadLayer.set_cells_terrain_connect([selectedCell], selected_terrainSet, Global.selected_terrain, false)
			GridManager.astar.set_point_weight_scale(selectedCell, 1.0)
			if Global.selected_terrain == Tiles.BASE && !Global.baseTiles.has(selectedCell):
				Global.baseTiles.append(selectedCell)
				if Global.build_stage == Global.BuildStage.PLACE_BASE_TILES and Global.baseTiles.size() >= 4:
					Global.advance_build_stage()
					Global.spawn_starter_vehicles()
	
	elif Global.clickMode == "place_tile":
		RoadLayer.set_cell(selectedCell, Tiles.ROAD_SOURCE, Global.selcted_tile)
#		GridManeger.astar.set_point_weight_scale(selectedCell, 1.0)

	Global.update_path_reachability()
	Global.mark_routes_dirty()

func erase_road():
	var site := Global.get_construction_site_at(selectedCell)
	if site != null:
		Global.cancel_construction(site)
		return
	
	if GridManager.get_building_at(selectedCell) != null: return
	
	if selectedCell in Global.baseTiles:
		if not selectedCell in Global.occupied_base_tiles:
			Global.baseTiles.erase(selectedCell)
		else:
			return
	
	GridManager.road_colors.erase(selectedCell)
	RoadLayer.erase_cell(selectedCell)
	GridManager.astar.set_point_weight_scale(selectedCell, 5.0)
	Global.update_path_reachability()
	Global.mark_routes_dirty()

func start_airstrip_drag():
	if Global.mouseIsOVerUI: return
	
	var grab = find_airstrip_end_grab(selectedCell)
	if grab["index"] != -1:
		airstrip_edit_index = grab["index"]
		airstrip_edit_axis = grab["axis"]
		airstrip_start = grab["anchor"]
	else:
		airstrip_edit_index = -1
		airstrip_edit_axis = ""
		airstrip_start = selectedCell
	
	airstrip_dragging = true

func commit_airstrip_drag():
	if not airstrip_dragging: return
	airstrip_dragging = false
	
	var raw_footprint = get_airstrip_footprint(airstrip_start, selectedCell, false)
	
	if airstrip_edit_index == -1 and raw_footprint["length"] < AIRSTRIP_MIN_DRAG:
		print("Drag at least ", AIRSTRIP_MIN_DRAG, " tiles to place an airstrip")
		reset_airstrip_drag_state()
		return
	
	var footprint = get_airstrip_footprint(airstrip_start, selectedCell, true, airstrip_edit_axis)
	if not is_airstrip_valid(footprint["cells"]):
		print("Airstrip placement rejected incaid cell in footprint")
		reset_airstrip_drag_state()
		return
	
	if airstrip_edit_index != -1:
		var old_strip = Global.airstrips[airstrip_edit_index]
		for cell in old_strip["cells"]:
			RoadLayer.erase_cell(cell)
			GridManager.astar.set_point_weight_scale(cell, 5.0)
		Global.airstrips[airstrip_edit_index] = footprint
		RoadLayer.set_cells_terrain_connect(footprint["cells"], selected_terrainSet, Global.selected_terrain, false)
		for cell in footprint["cells"]:
			GridManager.astar.set_point_weight_scale(cell, 1.0)
		Global.mark_routes_dirty()
		print("Airstrip extended: ", footprint["length"], " tiles")
	else:
		var cost: Dictionary = Global.get_area_construction_cost(Global.AIRSTRIP_COST.airstrip["resources"], Global.AIRSTRIP_COST.airstrip["building_time"], footprint["cells"].size())
		var result: Dictionary = {
			"terrain_set": selected_terrainSet,
			"terrain": Global.selected_terrain,
			"footprint": footprint,
		}
		var site := Global.start_construction("airstrip", footprint["cells"], cost, result)
		if site != null and Global.build_stage == Global.BuildStage.PLACE_RUNWAY:
			Global.advance_build_stage()
		print("Airstrip construction started: ", footprint["length"], " tiles")
	
	reset_airstrip_drag_state()

func reset_airstrip_drag_state():
	airstrip_start = Vector2i(-9999, -9999)
	airstrip_edit_index = -1
	airstrip_edit_axis = ""

func get_airstrip_footprint(start: Vector2i, end: Vector2i, enforce_min: bool, forced_axis: String = "") -> Dictionary:
	return _get_strip_footprint(start, end, enforce_min, forced_axis, AIRSTRIP_WIDTH, AIRSTRIP_MIN_LENGTH)

func get_taxiway_footprint(start: Vector2i, end: Vector2i) -> Dictionary:
	return _get_strip_footprint(start, end, false, "", TAXIWAY_WIDTH, 1)

func _get_strip_footprint(start: Vector2i, end: Vector2i, enforce_min: bool, forced_axis: String, width: int, min_length: int) -> Dictionary:
	var diff: Vector2i = end - start
	var direction: Vector2i
	var raw_length: int
	
	var use_x_axis: bool
	if forced_axis == "x":
		use_x_axis = true
	elif forced_axis == "y":
		use_x_axis = false
	else:
		use_x_axis = abs(diff.x) >= abs(diff.y)
	
	if use_x_axis:
		direction = Vector2i(1 if diff.x >= 0 else -1, 0)
		raw_length = abs(diff.x) + 1
	else:
		direction = Vector2i(0, 1 if diff.y >= 0 else -1)
		raw_length = abs(diff.y) + 1
	
	var length: int = max(raw_length, min_length) if enforce_min else raw_length
	var perp: Vector2i = Vector2i(-direction.y, direction.x)
	var half: int = width / 2
	var cells: Array[Vector2i] = []
	for i in range(length):
		for w in range(width):
			cells.append(start + direction * i + perp * (w - half))
	return {
		"start": start,
		"direction": direction,
		"length": length,
		"width": width,
		"cells": cells
	}

func find_airstrip_end_grab(cell: Vector2i) -> Dictionary:
	for i in range(Global.airstrips.size()):
		var strip: Dictionary = Global.airstrips[i]
		var dir: Vector2i = strip["direction"]
		var perp: Vector2i = Vector2i(-dir.y, dir.x)
		var local: Vector2i = cell - strip["start"]
		var along: int = local.x * dir.x + local.y * dir.y
		var across: int = local.x * perp.x + local.y * perp.y
		
		if abs(across) > 1:
			continue
		var length: int = strip["length"]
		var axis: String = "x" if dir.x != 0 else "y"
		
		if along <= AIRSTRIP_GRAB_TOLERANCE:
			return{"index": i, "anchor": strip["start"] + dir * (length - 1), "axis": axis}
		elif along >= length - 1 - AIRSTRIP_GRAB_TOLERANCE:
			return{"index": i, "anchor": strip["start"], "axis": axis}
	return {"index": -1, "anchor": Vector2i(-9999, -9999), "axis": ""}

func is_airstrip_valid(cells: Array) -> bool:
	for cell in cells:
		if cell in Global.baseTiles:
			return false
		if GridManager.astar.is_in_boundsv(cell) and GridManager.astar.is_point_solid(cell):
			return false
	return true
	
func start_taxiway_drag():
	if Global.mouseIsOVerUI: return
	taxiway_start = selectedCell
	taxiway_dragging = true

func commit_taxiway_drag():
	if not taxiway_dragging: return
	taxiway_dragging = false
	
	var footprint = get_taxiway_footprint(taxiway_start, selectedCell)
	if not is_airstrip_valid(footprint["cells"]):
		print("Taxiway placement rejected, invalid cell in footprint")
		reset_taxiway_drag_state()
		return
	
	var cost: Dictionary = Global.get_area_construction_cost(Global.AIRSTRIP_COST["taxiway"]["resources"], Global.AIRSTRIP_COST["taxiway"]["building_time"], footprint["cells"].size())
	var result: Dictionary = {
		"terrain_set": selected_terrainSet,
		"terrain": Global.selected_terrain,
		"footprint": footprint,
	}
	Global.start_construction("taxiway", footprint["cells"], cost, result)
	
	print("Taxiway construction started: ", footprint["length"], " tiles")
	reset_taxiway_drag_state()

func reset_taxiway_drag_state():
	taxiway_start = Vector2i(-9999, -9999)

func start_placing_factory(factory_type: String) -> void:
	Global.selected_factory_type = factory_type
	Global.clickMode = "place_factory"

func place_factory() -> void:
	if Global.mouseIsOVerUI: return
	var def: Dictionary = Global.BUILDING_DEFS[Global.selected_factory_type]
	var cells: Array[Vector2i] = GridManager.get_footprint_cells(selectedCell, def["size"])
	var cost: Dictionary = Global.BUILDING_CONSTRUCTION_COST.get(Global.selected_factory_type, {})
	var result: Dictionary = {"factory_type": Global.selected_factory_type}
	var site := Global.start_construction("factory", cells, cost, result)
	if site == null:
		return
	
	if Global.selected_factory_type == "resourceBox" and Global.build_stage == Global.BuildStage.PLACE_RESOURCE_BOX:
		Global.advance_build_stage()
	elif Global.selected_factory_type == "cargoTerminal" and Global.build_stage == Global.BuildStage.PLACE_TERMINAL:
		Global.advance_build_stage()
	

func demolish_factory() -> void:
	if Global.mouseIsOVerUI: return
	
	var site := Global.get_construction_site_at(selectedCell)
	if site != null:
		Global.cancel_construction(site)
		return
	
	var factory := Global.get_factory_at(selectedCell)
	if factory == null or not (factory is FactoryInstance): return

	for cell in Global.get_building_stamp(factory.factory_type):
		RoadLayer.erase_cell(factory.grid_pos + cell["offset"])
	
	Global.remove_factory(factory)

func update_chunks_around_camera() -> void:
	var cam_tile_pos = TerrainLayer.local_to_map(camera.global_position)
	var cam_chunk_x = floor(float(cam_tile_pos.x) / chunk_size)
	var cam_chunk_y = floor(float(cam_tile_pos.y) / chunk_size)
	
	for x in range(cam_chunk_x - render_distance, cam_chunk_x + render_distance + 1):
		for y in range(cam_chunk_y - render_distance, cam_chunk_y + render_distance + 1):
			var chunk_key = Vector2i(x, y)
			if not loaded_chunks.has(chunk_key):
				generate_chunk(x, y)
				loaded_chunks[chunk_key] = true

func generate_chunk(chuck_x : int, chunk_y: int) -> void:
	var start_x = chuck_x * chunk_size
	var start_y = chunk_y * chunk_size
	
	for x in range(start_x, start_x + chunk_size):
		for y in range(start_y, start_y + chunk_size):
			setBiome(x,y)
			placeOres(x,y)

func setBiome(x: int, y: int) -> void:
	var altitude = noise_alt.get_noise_2d(x,y)
	
	var base_temp = noise_temp.get_noise_2d(x,y)
	var moisture = noise_moist.get_noise_2d(x,y)
	
	const CLIMATE_HALF_HEIGHT = 6000.0
	var latitude = clamp(abs(y) / CLIMATE_HALF_HEIGHT, 0.0, 1.0)
	var temperature = base_temp + (1.0 - latitude) * 0.25 - max(altitude, 0.0) * 0.35
	temperature += noise_temp.get_noise_2d(x+4000, y+4000) * 0.08
	var coords = Vector2i(x, y)
	
	if altitude <= -0.10:
		if temperature < -0.55:
			TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.COLD_WATER_ATLAS)
		else:
			TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.WATER_ATLAS)
	elif between(altitude, -0.10, -0.05):
		if temperature < -0.55:
			TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.COLD_SAND_ATLAS)
		else:
			TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.SAND_ATLAS)
	elif between(altitude, -0.05, 0.45):
		if temperature < -0.45:
			TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.SNOW_ATLAS)
		elif temperature < -0.15:
			TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.TOUNDRA_ATLAS)
		elif temperature < 0.20:
			if moisture < -0.1:
				var random: float = randf()
				if random <= 0.15:
					TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.GRASS_ALT_ATLAS)
				else:
					TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.GRASS_ATLAS)
			else:
				var random: float = randf()
				if random <= 0.15:
					var tree = TreeScene.instantiate()
					tree.position = TerrainLayer.map_to_local(coords)
					TreeLayer.add_child(tree)
				TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.FORREST_ATLAS)
		else:
			if moisture < -0.05:
				TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.SAND_ATLAS)
			else:
				var random: float = randf()
				if random <= 0.15:
					TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.SAVANA_ALT_ATLAS)
				else:
					TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.SAVANA_ATLAS)
	elif altitude > 0.40:
		if temperature < -0.2:
			TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.SNOW_ATLAS)
		else:
			TerrainLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.MOUNTAIN_ATLAS)
			
	update_astar_cell_from_biome(coords, altitude)

func placeOres(x: int, y: int):
	var coords := Vector2i(x, y)
	var ore = noise_ore.get_noise_2d(x,y)
	var altitude = noise_alt.get_noise_2d(x,y)
	var rng = randf()
	
	if ore > -0.30 && altitude > 0.10:
		if rng <= 0.01:
			OreLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.GOLD_ORE_ATLAS)
		elif rng <= 0.4:
			OreLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.COAL_ORE_ATLAS)
		elif rng <= 0.7:
			OreLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.COPPER_ORE_ATLAS)
		else:
			OreLayer.set_cell(coords, Tiles.TERAIN_SOURCE, Tiles.IRON_ORE_ATLAS)
		


func between(val : float, start: float, end: float) -> bool:
	if val >= start and val < end:
		return true
	return false

func previewTile():
	if not PreviewLayer: return
	if Global.clickMode == "place_terrainSet":
		if Global.selected_terrain in [Tiles.SINGEL_ROAD_GRAVEL, Tiles.SINGEL_ROAD_CEMENT, Tiles.SINGEL_ROAD_ASPHALT]:
			PreviewLayer.set_cells_terrain_connect([selectedCell], selected_terrainSet, Tiles.ROAD_TERRAIN, false)
			var atlas = GridManager.road_atlas_for_color(PreviewLayer, selectedCell, Global.selected_terrain)
			if atlas.x >= 0:
				PreviewLayer.set_cell(selectedCell, Tiles.ROAD_SOURCE, atlas)
		else:
			PreviewLayer.set_cells_terrain_connect([selectedCell], selected_terrainSet, Global.selected_terrain, false)
	elif Global.clickMode == "place_tile":
		PreviewLayer.set_cell(selectedCell, Tiles.ROAD_SOURCE, Global.selcted_tile)
	elif Global.clickMode == "place_airstrip":
		var start = airstrip_start if airstrip_dragging else selectedCell
		var axis = airstrip_edit_axis if airstrip_dragging else ""
		var footprint = get_airstrip_footprint(start, selectedCell, false, axis)
		PreviewLayer.set_cells_terrain_connect(footprint["cells"], selected_terrainSet, Global.selected_terrain, false)
	elif Global.clickMode == "place_taxiway":
		var start = taxiway_start if taxiway_dragging else selectedCell
		var footprint = get_taxiway_footprint(start, selectedCell)
		PreviewLayer.set_cells_terrain_connect(footprint["cells"], selected_terrainSet, Global.selected_terrain, false)
	elif Global.clickMode == "place_factory":
		for cell in Global.get_building_stamp(Global.selected_factory_type):
			PreviewLayer.set_cell(selectedCell + cell["offset"], Tiles.ROAD_SOURCE, cell["atlas"])
	elif Global.clickMode == "highlight":
		PreviewLayer.set_cell(selectedCell, Tiles.TERAIN_SOURCE, Tiles.WATER_ATLAS)

func update_astar_cell_from_biome(coords: Vector2i, altitude: float):
	if altitude > 0.40:
		GridManager.astar.set_point_solid(coords, true)
	elif altitude < -0.10:
		GridManager.astar.set_point_solid(coords, true)
	else:
		GridManager.astar.set_point_weight_scale(coords, 5.0)
