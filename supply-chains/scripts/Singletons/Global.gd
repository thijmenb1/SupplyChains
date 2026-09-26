extends Node

# Consts
const TILE_SIZE = 32

# General vars
var mouseIsOVerUI : bool = false
var vehicle_ui_selected: String
var vehicle_ui_open: bool = false
var factory_ui_selected: String
var factory_ui_open: bool = false

var routes: Array[Dictionary]
var route_color: Array[Color] = [Color(0.0, 0.404, 0.624, 1.0), Color(1.0, 0.0, 0.0, 1.0)]
var _routes_dirty: bool = false

func mark_routes_dirty() -> void:
	_routes_dirty = true

# Finace vars

var money: float = 100000
var debt: float
var maxDebt: int = 100

var trade_orders: Array[Dictionary]

#Think its not needed anymore
"""
var resources := {
	"ironBeam": 999,
	"gravel": 999,
	"sand": 999,
	"fule": 999,
	"coal": 999,
	"steelBeam": 999,
	"PCBPallet": 999
}
var resourcesDebt := {
	"ironBeam": 0,
	"copperSpool": 0,
	"sulferPallet": 0,
	"cabelSpool": 0,
	"copperSulfideIBC": 0,
	"steelBeam": 0,
	"PCBPallet": 0
}
"""

var garage := {
	"valtra_s416": [],
	"ford_7810": [],
	"liebherr_ta230_lintronic": [],
	"flatbedTruck": [],
	"cementMixerTruck": [],
	"Vögele_Super_1603-3i": [],
	"Komatsu_D65PXI": [],
	"Volvo_EC300DL": [],
	"flatbedTrailer": [],
	"bulkTrailer": []
}

enum BuildStage {
	PLACE_RESOURCE_BOX,
	PLACE_RUNWAY,
	PLACE_TERMINAL,
	PLACE_BASE_TILES,
	READY
}

var build_stage: BuildStage = BuildStage.PLACE_RESOURCE_BOX
var starter_vehicles_spawned: bool = false

const STARTER_TRACTOR: String = "ford_7810"
const STARTER_TRAILER: String = "flatbedTrailer"

func spawn_starter_vehicles() -> void:
	if starter_vehicles_spawned:
		return
	starter_vehicles_spawned = true
	
	var tractor := spawn_vehicle(STARTER_TRACTOR)
	var trailer := spawn_vehicle(STARTER_TRAILER)
	
	if tractor != null and trailer != null  and tractor.attachments != null:
		trailer.couple_to(tractor, tractor.rearAttatchmentPoint)



func advance_build_stage() -> void:
	match build_stage:
		BuildStage.PLACE_RESOURCE_BOX:
			build_stage = BuildStage.PLACE_RUNWAY
		BuildStage.PLACE_RUNWAY:
			build_stage = BuildStage.PLACE_TERMINAL
		BuildStage.PLACE_TERMINAL:
			build_stage = BuildStage.PLACE_BASE_TILES
		BuildStage.PLACE_BASE_TILES:
			build_stage = BuildStage.READY

# Tile tracking vars
var selcted_tile : Vector2i = Vector2i(1, 2)
var selected_factory_type: String = "gaspower"
var baseTiles : Array[Vector2i] = []
var baseBuild: bool = false
var factorys: Array[FactoryInstance] = []
var construction_sites: Array[ConstructionSite] = []
var airstrips: Array[Dictionary] = []
var occupied_airstrips: Array = []
var taxiways: Array[Dictionary] = []
var clickMode: String
var selected_terrain: int

# Time vars
const SECONDS_PER_MINUTE = 60
const MINUTES_PER_HOUR = 60
const HOURS_PER_DAY = 24
const MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR
const START_HOURS: int = 8

const SPEED_TIERS: Array[float] = [0.0, 1.0, 4.0, 60.0]	#60.0 just for dev should be 12.0

var speed_index: int = 1
var speed_tier: float = SPEED_TIERS[speed_index]

var elapsed_game_seconds: float = START_HOURS * MINUTES_PER_HOUR * SECONDS_PER_MINUTE

var minute : int 
var hour: int
var day : int
var month: String
var year: int

func set_speed_index(new_index: int) -> void:
	speed_index = clampi(new_index, 0, SPEED_TIERS.size() -1)
	speed_tier = SPEED_TIERS[speed_index]

func speed_up() -> void:
	set_speed_index(speed_index + 1)

func slow_down() -> void:
	set_speed_index(speed_index -1)

var previous_speed: int = 1
func toggle_pause() -> void:
	if speed_index != 0:
		previous_speed = speed_index
		set_speed_index(0)
	else:
		set_speed_index(previous_speed)

const BUILDING_DEFS: Dictionary = {
	"blast":		{"display": "Blast furnace ",	"size": Vector2i(4,2),	"tile": Tiles.BLAST_FURANCE,	"paths": "res://scripts/Factories/paths/blast_path.tscn",		"dock_reverse": true},
	"refinary":		{"display": "Refinery ",		"size": Vector2i(4,2),	"tile": Tiles.REFINARY,			"paths": "res://scripts/Factories/paths/refinary_path.tscn",	"dock_reverse": false},
	"coalpower":	{"display": "Coal plant ",		"size": Vector2i(2,2),	"tile": Tiles.COAL_POWER,		"paths": "res://scripts/Factories/paths/coalpower_path.tscn",	"dock_reverse": true},
	"solarpanels":	{"display": "Solar panels ",	"size": Vector2i(2,2),	"tile": Tiles.SOLAR_FARM,		"paths": null,													"dock_reverse": true},
	"steelmill":	{"display": "Steel mill ",		"size": Vector2i(2,2),	"tile": Tiles.STEEL_MILL,		"paths": "res://scripts/Factories/paths/steelmill_path.tscn",	"dock_reverse": true},
	"wiremill":		{"display": "Wire mill ",		"size": Vector2i(2,2),	"tile": Tiles.WIRE_MILL,		"paths": "res://scripts/Factories/paths/wiremill_path.tscn",	"dock_reverse": true},
	"gaspower":		{"display": "Generators ",		"size": Vector2i(2,2),	"tile": Tiles.DIESEL_GENERATOR,	"paths": "res://scripts/Factories/paths/gaspower_path.tscn",	"dock_reverse": true},
	"cementMixing":	{"display": "Cement mixer ",	"size": Vector2i(2,2),	"tile": Tiles.CONCRETE_PLANT,	"paths": null,													"dock_reverse": true},
	"cargoTerminal":{"display": "Cargo terminal ",	"size": Vector2i(4,3),	"tile": Tiles.CARGO_TERMINAL,	"paths": null,													"dock_reverse": true},
	"resourceBox":	{"display": "Resource box ",	"size": Vector2i(1,1),	"tile": Tiles.START_BOX,		"paths": null,													"dock_reverse": false},
}

const BUILDING_CONSTRUCTION_COST: Dictionary = {
	"blast":		{"resources": {"ironBeam": 4, "gravel": 5},	"build_time": 20.0},
	"refinary":		{"resources": {"ironBeam": 6, "gravel": 5},	"build_time": 25.0},
	"coalpower":	{"resources": {"ironBeam": 8, "gravel": 5},	"build_time": 15.0},
	"solarpanels":	{"resources": {"ironBeam": 10},				"build_time": 12.0},
	"steelmill":	{"resources": {"ironBeam": 8, "gravel": 5},	"build_time": 18.0},
	"wiremill":		{"resources": {"ironBeam": 8},				"build_time": 15.0},
	"gaspower":		{"resources": {"ironBeam": 8},				"build_time": 12.0},
	"cementMixing":	{"resources": {"ironBeam": 5, "gravel": 5},	"build_time": 15.0},
	"cargoTerminal":{"resources": {"ironBeam": 10,},			"build_time": 30.0},
	"base":			{"resources": {"gravel": 5},				"build_time": 10.0},
	"resourceBox":	{"resources": {},							"build_time": 0.0},
}

const ROAD_CONSTRUCTION_COST: Dictionary = {
	Tiles.SINGEL_ROAD_GRAVEL: {"resources": {"gravel": 1}, "building_time": 3.0},
	Tiles.SINGEL_ROAD_CEMENT: {"resources": {"cement": 1}, "building_time": 4.0},
	Tiles.SINGEL_ROAD_ASPHALT: {"resources": {"asphalt": 1}, "building_time": 5.0}
}

const AIRSTRIP_COST: Dictionary = {
	"airstrip": {"resources": {}, "building_time": 5.0},
	"taxiway": {"resources": {}, "building_time": 5.0}
}

var free_buildings_left: Dictionary = {
	"base": 4,
	"resourceBox": 1,
}

func get_road_construction_cost(terrain: int) -> Dictionary:
	return ROAD_CONSTRUCTION_COST.get(terrain, {"resources": {}, "build_time": 2.0})

func get_area_construction_cost(per_tile_resources: Dictionary, build_time: float, tile_count: int) -> Dictionary:
	var resources: Dictionary = {}
	for res in per_tile_resources:
		resources[res] = per_tile_resources[res] * tile_count
	return {"resources": resources, "build_time": build_time}

func _bake_factory_paths(factory_type: String, grid_pos: Vector2i) -> Dictionary:
	if not BUILDING_DEFS.has(factory_type):
		return {}
	if BUILDING_DEFS[factory_type].get("paths", null) == null:
		return {}
	var template: PackedScene = load(BUILDING_DEFS[factory_type]["paths"])
	var root: Node2D = template.instantiate()
	var world_offset: Vector2 = Vector2(grid_pos) * TILE_SIZE
	var reverse_in: bool = BUILDING_DEFS[factory_type].get("dock_reverse", true)
	
	var result: Dictionary= {}
	for child in root.get_children():
		if child is Path2D:
			var pts: Array[Vector2] = []
			for local_pt in child.curve.tessellate():
				pts.append(local_pt + world_offset)
			var key: String = child.name.to_lower().trim_prefix(root.name.to_lower() + "_")
			result[key] = {
				"points": pts,
				"start_tangent": (pts[1] - pts[0]).normalized(),
				"reverse_in": reverse_in
			}
	root.queue_free()
	return result


func create_factory(factory_type: String, grid_pos: Vector2i) -> FactoryInstance:
	var same_type_count := 0
	for existing in factorys:
		if existing.factory_type == factory_type:
			same_type_count += 1
	
	if not BUILDING_DEFS.has(factory_type):
		push_error("Unknown factory type: " + factory_type)
		return null
	
	var def: Dictionary = BUILDING_DEFS[factory_type]
	var size: Vector2i = def["size"]
	
	var factory_name := factory_type + "*" + str(same_type_count)
	var new_factory := FactoryInstance.new(factory_name, grid_pos, size)
	new_factory.paths = _bake_factory_paths(factory_type, grid_pos)
	print(new_factory.paths)
	
	GridManager.register_building(new_factory, grid_pos, size)
	GridManager.set_footprint_astar_weight(grid_pos, size, 1.0)
	factorys.append(new_factory)
	update_path_reachability()
	mark_routes_dirty()
	return new_factory

func get_factory_at(grid_pos: Vector2i) -> FactoryInstance:
	for factory_data in factorys:
		if factory_data["grid_pos"] == grid_pos:
			return factory_data
	return null

func get_construction_site_at(grid_pos: Vector2i) -> ConstructionSite:
	for site in construction_sites:
		if grid_pos in site.cells:
			return site
	return null

func get_building_or_site_at(grid_pos: Vector2i):
	var factory := get_factory_at(grid_pos)
	if factory != null:
		return factory
	return get_construction_site_at(grid_pos)

func start_construction(kind: String, cells: Array[Vector2i], cost_def: Dictionary, result: Dictionary) -> ConstructionSite:
	if not GridManager.can_place_on_cells(cells):
		print("Cannot build here, a cell is already occupied")
		return null
	
	var site := ConstructionSite.new(kind, cells, cost_def, result)
	
	if kind == "factory":
		site.paths = _bake_factory_paths(result["factory_type"], site.anchor)
	
	GridManager.register_on_cells(site, cells)
	GridManager.set_cells_astar_weight(cells, 1.0)
	
	if construction_layer:
		_paint_ghost(site)
	
	construction_sites.append(site)
	update_path_reachability()
	mark_routes_dirty()
	_auto_route_construction_site(site)
	return site

func _paint_ghost(site: ConstructionSite) -> void:
	if site.kind == "factory":
		for cell in get_building_stamp(site.result["factory_type"]):
			construction_layer.set_cell(site.anchor + cell["offset"], Tiles.ROAD_SOURCE, cell["atlas"])
	else:
		for cell in site.cells:
			construction_layer.set_cells_terrain_connect([cell], Tiles.ROAD_TERRAIN_SET, Tiles.CONSTRUCTION_MARKER)

func _clear_ghost(site: ConstructionSite) -> void:
	if site.kind == "factory":
		for cell in get_building_stamp(site.result["factory_type"]):
			construction_layer.erase_cell(site.anchor + cell["offset"])
	else:
		for cell in site.cells:
			construction_layer.erase_cell(cell)

func cancel_construction(site: ConstructionSite) -> void:
	construction_sites.erase(site)
	if construction_layer:
		_clear_ghost(site)
	GridManager.remove_from_cells(site.cells)
	GridManager.set_cells_astar_weight(site.cells, 5.0)
	_drop_routes_touching(site)
	update_path_reachability()
	mark_routes_dirty()

func _drop_routes_touching(building) -> void:
	for i in routes.size():
		var route: Dictionary = routes[i]
		if route.is_empty():
			continue
		if route.get("source") == building or route.get("destination") == building:
			remove_route(i)

func _finish_construction(site: ConstructionSite) -> void:
	construction_sites.erase(site)
	if construction_layer:
		_clear_ghost(site)
	GridManager.remove_from_cells(site.cells)
	_drop_routes_touching(site)
	_apply_construction_result(site)
	update_path_reachability()

func _apply_construction_result(site: ConstructionSite) -> void:
	match site.kind:
		"factory":
			var new_factory := create_factory(site.result["factory_type"], site.anchor)
			if new_factory and road_layer:
				for cell in get_building_stamp(site.result["factory_type"]):
					road_layer.set_cell(site.anchor + cell["offset"], Tiles.ROAD_SOURCE, cell["atlas"])
		"road":
			if road_layer:
				road_layer.set_cells_terrain_connect(site.cells, site.result["terrain_set"], site.result["terrain"], false)
			GridManager.set_cells_astar_weight(site.cells, 1.0)
			if site.result.get("is_colored_road", false) and road_layer:
				GridManager.road_colors[site.anchor] = site.result["road_color"]
				GridManager.repaint_road_colors(road_layer, site.anchor)
		"airstrip":
			airstrips.append(site.result["footprint"])
			if road_layer:
				road_layer.set_cells_terrain_connect(site.cells, site.result["terrain_set"], site.result["terrain"], false)
			GridManager.set_cells_astar_weight(site.cells, 1.0)
		"taxiway":
			airstrips.append(site.result["footprint"])
			if road_layer:
				road_layer.set_cells_terrain_connect(site.cells, site.result["terrain_set"], site.result["terrain"], false)
			GridManager.set_cells_astar_weight(site.cells, 1.0)
	mark_routes_dirty()

func get_building_stamp(building_type: String) -> Array:
	if building_type == "": return []
	var def: Dictionary = BUILDING_DEFS[building_type]
	var base_atlas: Vector2i = def["tile"]
	var size: Vector2i = def["size"]
	var stamp: Array = []
	for x in range(size.x):
		for y in range(size.y):
			stamp.append({"offset": Vector2i(x,y), "atlas": base_atlas + Vector2i(x,y)})
	return stamp

func get_resource_box() -> FactoryInstance:
	for factory in factorys:
		if factory.factory_type == "resourceBox":
			return factory
	return null

func get_warehouse() -> FactoryInstance:
	for factory in factorys:
		if factory.factory_type == "cargoTerminal":
			return factory
	return null

func get_resource_source() -> FactoryInstance:
	var warehouse := get_warehouse()
	if warehouse != null:
		return warehouse
	return get_resource_box()

func _auto_route_construction_site(site: ConstructionSite) -> void:
	var source := get_resource_box()
	if source == null:
		print("No resource box or cargo terminal built yet, can't auto route")
		return
	
	for resource in site.required_resources.keys():
		routes.append({
			"source": source,
			"destination": site,
			"resource": resource
		})
		mark_routes_dirty()

func _unassign_vehicles_from_route(route_index: int) -> void:
	for vehicle_type in garage:
		for vehicle in garage[vehicle_type]:
			if vehicle.movement.assinged_route_index == route_index:
				vehicle.movement.clear_route()

func remove_route(route_index: int) -> void:
	if route_index < 0 or route_index >= routes.size():
		return
	if routes[route_index].is_empty():
		return
	_unassign_vehicles_from_route(route_index)
	routes[route_index] = {}

func active_route_count() -> int:
	var count := 0
	for route in routes:
		if not route.is_empty():
			count += 1
	return count

func remove_factory(factory: FactoryInstance) -> void:
	GridManager.remove_building(factory.grid_pos, factory.size)
	GridManager.set_footprint_astar_weight(factory.grid_pos, factory.size, 5.0)
	mark_routes_dirty()
	update_path_reachability()
	factorys.erase(factory)

const FACTORY_ACCEPTED_RESOURCES: Dictionary = {
	"refinary": ["crudeOil"],
	"blast": ["ironOre", "copperOre", "goldOre"],
	"steelmill": ["ironBeam"],
	"wiremill": ["copperIngots", "goldBars"],
	"coalpower": ["coal"],
	"cementMixing": ["gravel", "sand", "water"],
	"chip": ["copperWire", "goldWire"],
	"gaspower": ["fuel"]
}

const SUITBEL_DILIVERY_FACTORYS: Dictionary = {
	"refinary": ["gaspower", "base"],
	"blast": ["steelmill", "wiremill", "base"],
	"steelmill": ["base"],
	"wiremill": ["chip", "base"],
	"coalpower": null,
	"cementMixing": ["base"],
	"chip": ["base"],
	"gaspower": null
}

func factory_accepts_resouce(factory_type: String, resource: String) -> bool:
	return resource in FACTORY_ACCEPTED_RESOURCES.get(factory_type, [])

func get_accepted_resources(factory_type: String) -> Array:
	return FACTORY_ACCEPTED_RESOURCES.get(factory_type, [])
	
func _process(delta):
	for factory in factorys:
		factory.update(delta)
	for site in construction_sites.duplicate():
		if site.update(delta):
			_finish_construction(site)
	if _routes_dirty:
		generate_route()
		_routes_dirty = false

func pay_cost(cost: float) -> bool:
	if money < cost:
		return false
	
	money -= cost
	
	return true
	
func addDebt(cost: float) -> bool:
	if debt + cost > maxDebt:
		return false
	
	debt += cost
	
	return true


enum  HitchType {PIN, BALL, FIFTH_WHEEL, THREE_POINT}

func getVehicleProperties(vehicle_type: String) -> VehicleData:
	var resource = load("res://scripts/Vehicles/VehicleTypes/%s.tres" % vehicle_type)
	if resource == null:
		push_error("Failed to load vehicle type: " + vehicle_type)
		return null
	return resource

func getCargoProperties(cargo: String) -> Dictionary:
	match  cargo:
		"ironOre": 
			return {
				"type": "Bulk",
				"Weight": 2500	#kg/m³
			}
		"copperOre":
			return {
				"type": "Bulk",
				"Weight": 2600	#kg/m³
			}
		"goldOre":
			return {
				"type": "Bulk",
				"Weight": 2700	#kg/m³
			}
		"coal":
			return {
				"type": "Bulk",
				"Weight": 1200	#kg/m³
			}
		"gravel":
			return {
				"type": "Bulk",
				"Weight": 1600	#kg/m³
			}
		"sand":
			return {
				"type": "Bulk",
				"Weight": 1600
			}
		"concrete":
			return {
				"type": "Cement",
				"Weight": 1500
			}
		"asphalt":
			return {
				"type": "Bulk",
				"Weight": 2400
			}
		"ironBeam":
			return {
				"type": "Flatbed",
				"Weigth": 7555
			}
		"copperIngots":
			return {
				"type": "Flatbed",
				"Weigth": 8500
			}
		"goldBars":
			return {
				"type": "Flatbed",
				"Weight": 18500
			}
		"steelBeam":
			return {
				"type": "Flatbed",
				"Weight": 7530
			}
		"copperWire":
			return {
				"type": "Flatbed",
				"Weight": 8500
			}
		"goldWire":
			return {
				"type": "Flatbed",
				"Weight": 18500
			}
		"PCBPallet":
			return {
				"type": "Flatbed",
				"Weight": 1800
			}
		"crudeOil":
			return {
				"type": "liquid",
				"Weight": 800
			}
		"fule":
			return {
				"type": "liquid",
				"Weight": 800
			}
		_:
			return {}

const abilityAtlasRegions: Dictionary = {
	"hitch": Rect2(Vector2(0, 0), Vector2(16, 16)),
	"offroad": Rect2(Vector2(16, 0), Vector2(16, 16)),
	"flatbed": Rect2(Vector2(32, 0), Vector2(16, 16)),
	"bulk": Rect2(Vector2(48, 0), Vector2(16, 16)),
	"paving": Rect2(Vector2(64, 0), Vector2(16, 16)),
	"dozerblade": Rect2(Vector2(80, 0), Vector2(16, 16)),
	"cement": Rect2(Vector2(96, 0), Vector2(16, 16)),
	"digging": Rect2(Vector2(112, 0), Vector2(16, 16)),
}
	
const VEHICLE_SCENE := preload("res://scenes/Vehicles/vehicle.tscn")
const TRAILER_SCENE := preload("res://scenes/Vehicles/trailer.tscn")

var vehicle_layer: Node2D
var road_layer: TileMapLayer
var construction_layer: TileMapLayer
var occupied_base_tiles: Dictionary

func get_free_base_tile() -> Vector2i:
	for tile in baseTiles:
		if not occupied_base_tiles.has(tile):
			return tile
	return Vector2i(-1,-1)

func has_free_base_tile()-> bool:
	return get_free_base_tile() != Vector2i(-1,-1)

func spawn_vehicle(vehicle_type: String) -> Node:
	if vehicle_layer == null:
		push_error("Global.vehicle_layer not set, cant spawn vehicle")
		return null
	
	var spawn_tile: Vector2i = get_free_base_tile()
	if spawn_tile == Vector2i(-1, -1):
		push_error("No free base tile can't spawn vehicle")
		return null
	
	var props: VehicleData = getVehicleProperties(vehicle_type)
	var is_trailer: bool = props.general.is_trailer
	var scene: PackedScene = TRAILER_SCENE if is_trailer else VEHICLE_SCENE
	
	var instance := scene.instantiate()
	instance.vehicleType = vehicle_type
	instance.vehicleNumber = garage[vehicle_type].size() + 1
	instance.global_position = spawn_tile * TILE_SIZE + Vector2i(TILE_SIZE / 2, TILE_SIZE / 2)
	
	occupied_base_tiles[spawn_tile] = true
	vehicle_layer.add_child(instance)
	garage[vehicle_type].append(instance)
	return instance

var route_layer: Node2D
const ON_ROAD_CHAMFER_DISTANCE: float = 12.0

func _corridor_tile_for_path_data(data: Dictionary) -> Vector2i:
	var staging_tile: Vector2i = Vector2i((data["points"][0] / TILE_SIZE).floor())
	var tangent: Vector2 = data["start_tangent"]
	return staging_tile - Vector2i(round(tangent.x), round(tangent.y))

func _path_total_length(pts: PackedVector2Array) -> float:
	var total := 0.0
	for i in range(pts.size() - 1):
		total += pts[i].distance_to(pts[i + 1])
	return total

func _get_factory_dock(factory: FactoryInstance, purpose: String, anchor_tile: Vector2i) -> Dictionary:
	var best_key: String = ""
	var best_data: Dictionary = {}
	var best_len: float = INF
	for key in factory.paths.keys():
		if not key.begins_with(purpose):
			continue
		var data: Dictionary = factory.paths[key]
		var corridor_tile: Vector2i = _corridor_tile_for_path_data(data)
		var path: PackedVector2Array = GridManager.astar.get_point_path(anchor_tile, corridor_tile)
		if path.is_empty():
			continue
		var length: float = _path_total_length(path)
		if length < best_len:
			best_len = length
			best_key = key
			best_data = {
				"points": (data["points"] as Array[Vector2]).duplicate(),
				"start_tangent": data["start_tangent"],
				"reverse_in": data["reverse_in"],
				"corridor_tile": corridor_tile,
			}
	return best_data

func _concat_points(a: Array[Vector2], b: Array[Vector2], c: Array[Vector2]) -> Array[Vector2]:
	var result: Array[Vector2] = []
	result.append_array(a)
	result.append_array(b)
	result.append_array(c)
	return result

func generate_route() -> void:
	for route in routes:
		if route.is_empty():
			continue
		var source = route["source"]
		var destination = route["destination"]
		
		var load_dock: Dictionary = {}
		if source is ConstructionSite:
			push_warning("Route source is a construction site, construction sites cant be picked up from")
		else:
			load_dock = _get_factory_dock(source, "load", destination.grid_pos)
		
		var unload_dock: Dictionary = {}
		if not destination is ConstructionSite:
			unload_dock = _get_factory_dock(destination, "unload", source.grid_pos)

		route["load_dock"] = load_dock
		route["unload_dock"] = unload_dock
		
		var path_start: Vector2i = load_dock["corridor_tile"] if not load_dock.is_empty() else source.grid_pos
		var path_end: Vector2i = unload_dock["corridor_tile"] if not unload_dock.is_empty() else destination.grid_pos
		
		var raw_offroad: PackedVector2Array = GridManager.astar.get_point_path(path_start, path_end)
		var raw_onroad: Array[Vector2] = _get_road_only_path(path_start, path_end)
		
		route["Offroad_route"] = chamfer_path_corners(raw_offroad, ON_ROAD_CHAMFER_DISTANCE)
		route["Onroad_route"] = chamfer_path_corners(raw_onroad, ON_ROAD_CHAMFER_DISTANCE)

func _get_road_only_path(start: Vector2i, end: Vector2i) -> Array[Vector2]:
	var temp_path: Array[Vector2] = []
	var raw_points = GridManager.astar.get_id_path(start, end)
	if raw_points.is_empty():
		return temp_path
	for id in raw_points:
		if GridManager.astar.get_point_weight_scale(id) <= GridManager.ROAD_WEIGHT_THRESHOLD:
			temp_path.append(GridManager.astar.get_point_position(id))
		else:
			break
	return temp_path

func chamfer_path_corners(path: Array[Vector2], chamfer_dist: float) -> Array[Vector2]:
	if path.size() < 3:
		return path

	var result: Array[Vector2] = []
	result.append(path[0])

	for i in range(1, path.size() - 1):
		var prev: Vector2 = path[i - 1]
		var corner: Vector2 = path[i]
		var next: Vector2 = path[i + 1]

		var incoming: Vector2 = corner - prev
		var outgoing: Vector2 = next - corner
		if incoming.length() < 0.01 or outgoing.length() < 0.01:
			result.append(corner)
			continue

		var incoming_dir: Vector2 = incoming.normalized()
		var outgoing_dir: Vector2 = outgoing.normalized()

		if incoming_dir.dot(outgoing_dir) > 0.999:
			result.append(corner)
			continue

		var trim: float = min(chamfer_dist, min(incoming.length(), outgoing.length()) * 0.5)
		result.append(corner - incoming_dir * trim)
		result.append(corner + outgoing_dir * trim)

	result.append(path[path.size() - 1])
	return result

func update_path_reachability() -> void:
	if baseTiles.is_empty():
		return
	var reference_tile: Vector2i = baseTiles[0]
	var all_buildings: Array = []
	all_buildings.append_array(factorys)
	all_buildings.append_array(construction_sites)
	for factory in factorys:
		for key in factory.paths.keys():
			var data: Dictionary = factory.paths[key]
			var corridor_tile: Vector2i = _corridor_tile_for_path_data(data)
			var path: Array[Vector2] = _get_road_only_path(reference_tile, corridor_tile)
			data["corridor_tile"] = corridor_tile
			data["reachable"] = not path.is_empty()

func _unhandled_input(event):
	if event.is_action_pressed("toggle_fullscreen"):
		if DisplayServer.window_get_mode() == DisplayServer.WINDOW_MODE_FULLSCREEN:
			DisplayServer.window_set_mode(DisplayServer.WINDOW_MODE_WINDOWED)
		else:
			DisplayServer.window_set_mode(DisplayServer.WINDOW_MODE_FULLSCREEN)
