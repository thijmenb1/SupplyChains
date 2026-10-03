extends Node

const SAVE_DIR: String = "user://saves"
const SAVE_PASS: String = "kadfsjklfdsaljkoipThijmendfsajklfdskljfdsajkl"
const LEVEL_SCENE: String = "res://scenes/Worlds/level.tscn"
const LOADING_SCENE: String = "res://scenes/UI/loading_screen.tscn"

const SLOT_COUNT: int = 3
const AUTOSAVE_INTERVAL: float = 180.0
var autosave_enabled: bool = false
var _autosave_timer: float = 0.0

func _process(delta) -> void:
	if not autosave_enabled:
		return 
	_autosave_timer += delta
	if _autosave_timer >= AUTOSAVE_INTERVAL:
		_autosave_timer = 0.0
		save()
		Global.show_popup("Autosaved slot %d" % Global.loaded_save_index, 1.5)

func _slot_path(slot: int) -> String:
	return "%s/slot_%d.save" % [SAVE_DIR, slot]

func _slot_key(slot: int) -> String:
	return "savegame_%d" % slot

func _ready():
	DirAccess.make_dir_recursive_absolute(SAVE_DIR)
	refresh_slots()

func save(slot: int = -1) -> void:
	if slot < 0:
		slot = Global.loaded_save_index
	var data: Dictionary = {
		"save_name": "Slot %d" % (slot +1),
		"timestamp": int(Time.get_unix_time_from_system()),
		"money": Global.money,
		"game_seconds": Global.elapsed_game_seconds,
		"hour": Global.hour,
		"minute": Global.minute,
		"day": Global.day,
		"month": Global.month,
		"year": Global.year,
		"state": _collect_state(),
	}
	var f := FileAccess.open_encrypted_with_pass(_slot_path(slot), FileAccess.WRITE, SAVE_PASS)
	if f == null:
		push_error("Failed to open save slot %d for writing" % slot)
		return
	f.store_var(data)
	f.close()
	Global.loaded_save_index = slot
	refresh_slots()

func load_game(slot: int = 0) -> void:
	var data = recover(slot)
	
	if data.is_empty():
		Global.show_popup("No save file found in slot %d" % slot)
		return
	Global.loaded_save_index = slot
	autosave_enabled = true
	_reset_runtime_state()
	Global.pending_load = data.get("state", {})
	get_tree().change_scene_to_file(LOADING_SCENE)

func recover(slot: int = 0) -> Dictionary:
	if not FileAccess.file_exists(_slot_path(slot)):
		return {}
	var f := FileAccess.open_encrypted_with_pass(_slot_path(slot), FileAccess.READ, SAVE_PASS)
	if f == null:
		push_error("Failed to open save slot %d (wrong password or corrupt file)" % slot)
		return {}
	var data = f.get_var()
	f.close()
	return data

func refresh_slots() -> void:
	Global.save_games.clear()
	for slot in SLOT_COUNT:
		var data = recover(slot)
		var info: Dictionary = {"slot": slot, "exists": false}
		if data is Dictionary and not data.is_empty():
			info["exists"] = true
			info["name"] = data.get("save_name", "Slot %d" % (slot + 1))
			info["timestamp"] = data.get("timestamp", 0)
			info["money"] = data.get("money", 0.0)
			info["game_seconds"] = data.get("game_seconds", 0.0)
			info["hour"] = data.get("hour", 0)
			info["minute"] = data.get("minute", 0)
			info["day"] = data.get("day", 1)
			info["month"] = data.get("month", "")
			info["year"] = data.get("year", 0)
		Global.save_games.append(info)

func delete_slot(slot: int) -> void:
	if OS.get_name() == "Web":
		Engine.get_singleton("javaScript").eval("localStorage.removeItem('" + _slot_key(slot) + "'); sessionStorage.removeItem('" + _slot_key(slot) + "');")
	elif FileAccess.file_exists(_slot_path(slot)):
		DirAccess.remove_absolute(ProjectSettings.globalize_path(_slot_path(slot)))
	refresh_slots()

func _collect_state() -> Dictionary:
	var factories_out: Array = []
	for f in Global.factorys:
		factories_out.append(f.to_dict())
	
	var sites_out: Array = []
	for s in Global.construction_sites:
		sites_out.append(s.to_dict())
	
	var routes_out: Array = []
	for r in Global.routes:
		if r.is_empty():
			routes_out.append({})
		else:
			routes_out.append({
				"source": _building_ref(r["source"]),
				"destination": _building_ref(r["destination"]),
				"resource": r["resource"]
			})
	
	var vehicle_out: Array = []
	for type in Global.garage:
		for v in Global.garage[type]:
			if is_instance_valid(v):
				vehicle_out.append(_vehicle_to_dict(v))
	return {
		"world_seed": Global.world_seed,
		"money": Global.money,
		"debt": Global.debt,
		"trade_orders": Global.trade_orders,
		"base_tiles": Global.baseTiles,
		"occupied_base_tiles": Global.occupied_base_tiles,
		"baseBuild": Global.baseBuild,
		"build_stage": Global.build_stage,
		"starter_vehicles_spawned": Global.starter_vehicles_spawned,
		"free_buildings_left": Global.free_buildings_left,
		"airstrips": Global.airstrips,
		"occupied_airstrips": Global.occupied_airstrips,
		"taxiways": Global.taxiways,
		"elapsed_game_seconds": Global.elapsed_game_seconds,
		"speed_index": Global.speed_index,
		"road_cells": _collect_road_cells(),
		"road_colors": GridManager.road_colors,
		"factorys": factories_out,
		"construction_sites": sites_out,
		"routes": routes_out,
		"vehicles": vehicle_out,
	}
	
func _collect_road_cells() -> Array:
	var out: Array = []
	var layer := Global.road_layer
	if layer == null:
		return out
	for c in layer.get_used_cells():
		out.append([c, layer.get_cell_source_id(c), layer.get_cell_atlas_coords(c), layer.get_cell_alternative_tile(c)])
	return out
	
func _building_ref(b) -> Dictionary:
	if b is FactoryInstance:
		return {"t": "factory", "pos": b.grid_pos}
	if b is ConstructionSite:
		return {"t": "site", "pos": b.grid_pos}
	return {}
	
func _vehicle_to_dict(v) -> Dictionary:
	var d: Dictionary = {
		"type": v.vehicleType,
		"number": v.vehicleNumber,
		"pos": v.global_position,
		"rot": v.global_rotation,
	}
	if v.cargo != null:
		d["cargo"] = {"items": v.cargo.cargo, "weight": v.cargo.cargoWeight, "volume": v.cargo.cargoVolume}
	if v is VehicleBody:
		d["parked"] = v.parked
		if v.drivetrain != null:
			d["fuel"] = v.drivetrain.fuel
			d["health"] = v.drivetrain.health
		if v.movement != null:
			d["home_tile"] = v.movement.home_tile
			d["has_home_tile"] = v.movement.has_home_tile
			d["route_index"] = v.movement.assigned_route_index
			d["mining"] = v.movement.mining_to_dict()
	elif v is Trailer and v.is_coupled and v.towing_vehicle != null:
		d["towing"] = v.towing_vehicle.vehicleID
		d["hitch_front"] = v.hitch_marker == v.towing_vehicle.frontAttatchmentPoint
	return d
	
func _reset_runtime_state() -> void:
	for f in Global.factorys.duplicate():
		GridManager.remove_building(f.grid_pos, f.size)
	for s in Global.construction_sites.duplicate():
		GridManager.remove_from_cells(s.cells)
	Global.factorys.clear()
	Global.construction_sites.clear()
	Global.routes.clear()
	Global.airstrips.clear()
	Global.occupied_airstrips.clear()
	Global.taxiways.clear()
	Global.baseTiles.clear()
	Global.occupied_base_tiles.clear()
	for type in Global.garage:
		Global.garage[type] = []
	VehicleManager.vehicles.clear()
	GridManager.road_colors.clear()
	
	
func apply_pending_load() -> void:
	var s: Dictionary = Global.pending_load
	if s.is_empty():
		return
	Global.pending_load = {}
	
	Global.money = s.get("money", Global.money)
	Global.debt = s.get("debt", 0.0)
	Global.trade_orders.assign(s.get("trade_orders", []))
	Global.baseTiles.assign(s.get("base_tiles", []))
	Global.occupied_base_tiles = s.get("occupied_base_tiles", {})
	Global.baseBuild = s.get("baseBuild", false)
	Global.build_stage = s.get("build_stage", Global.build_stage)
	Global.starter_vehicles_spawned = s.get("starter_vehicles_spawned", false)
	Global.free_buildings_left = s.get("free_buildings_left", Global.free_buildings_left)
	Global.airstrips.assign(s.get("airstrips", []))
	Global.occupied_airstrips = s.get("occupied_airstrips", [])
	Global.taxiways.assign(s.get("taxiways", []))
	Global.elapsed_game_seconds = s.get("elapsed_game_seconds", Global.elapsed_game_seconds)
	Global.set_speed_index(s.get("speed_index", 1))
	
	GridManager.road_colors.merge(s.get("road_colors", {}), true)
	for e in s.get("road_cells", []):
		Global.road_layer.set_cell(e[0], e[1], e[2], e[3])
		GridManager.astar.set_point_weight_scale(e[0], 1.0)
	
	for d in s.get("factorys", []):
		var f: FactoryInstance
		if d["factory_type"] == "mine":
			f = Global.create_mine_factory(
				d["grid_pos"],
				d.get("size", Vector2i.ONE),
				d.get("mine_resources", {}))
		else:
			f = Global.create_factory(
				d["factory_type"],
				d["grid_pos"],
				d.get("size", Vector2i.ZERO))   # ZERO = use the def's size
		if f != null:
			f.apply_dict(d)
			if Global.road_layer:
				for cell in Global.get_building_stamp(f.factory_type):
					Global.road_layer.set_cell(f.grid_pos + cell["offset"], Tiles.ROAD_SOURCE, cell["atlas"])
	
	for d in s.get("construction_sites", []):
		Global.restore_construction_site(ConstructionSite.from_dict(d))
	
	Global.routes.clear()
	for r in s.get("routes", []):
		var src = _resolve_ref(r.get("source", {}))
		var dst = _resolve_ref(r.get("destination", {}))
		if r.is_empty() or src == null or dst == null:
			Global.routes.append({})
		else:
			Global.routes.append({"source": src, "destination": dst, "resource": r["resource"]})
	
	Global.update_path_reachability()
	Global.generate_route()
	Global._routes_dirty = false
	
	_restore_vehicles(s.get("vehicles", []))

func _resolve_ref(ref: Dictionary):
	match ref.get("t", ""):
		"factory":
			return Global.get_factory_at(ref["pos"])
		"site":
			for site in Global.construction_sites:
				if site.grid_pos == ref["pos"]:
					return site
	return null
	
func _restore_vehicles(list: Array) -> void:
	var by_id: Dictionary = {}
	var pairs: Array = []
	for d in list:
		var v = Global.restore_vehicle(d["type"], d["number"], d["pos"], d["rot"])
		if v == null:
			continue
		by_id[v.vehicleID] = v
		pairs.append([v, d])
	
	for p in pairs:
		var v = p[0]
		var d: Dictionary = p[1]
		if d.has("cargo") and v.cargo != null:
			v.cargo.cargo = d["cargo"]["items"].duplicate()
			v.cargo.cargoWeight = d["cargo"]["weight"]
			v.cargo.cargoVolume = d["cargo"]["volume"]
		if v is VehicleBody:
			v.parked = d.get("parked", false)
			if v.drivetrain != null:
				v.drivetrain.fuel = d.get("fuel", v.drivetrain.fuel)
				v.drivetrain.health = d.get("health", v.drivetrain.health)
			if v.movement != null:
				v.movement.home_tile = d.get("home_tile", Vector2i.ZERO)
				v.movement.has_home_tile = d.get("has_home_tile", false)
	
	for p in pairs:
		var v = p[0]
		var d: Dictionary = p[1]
		if v is Trailer and d.get("towing", "") != "":
			var tow = by_id.get(d["towing"])
			if tow != null and tow.attachments != null:
				var marker: Marker2D = tow.frontAttatchmentPoint if d.get("hitch_front", false) else tow.rearAttatchmentPoint
				v.couple_to(tow, marker)
	
	for p in pairs:
		var v = p[0]
		var d: Dictionary = p[1]
		if v is VehicleBody and v.movement != null:
			var idx: int = d.get("route_index", -1)
			if idx >= 0 and idx < Global.routes.size() and not Global.routes[idx].is_empty():
				v.movement.assign_route(idx)
			elif d.has("mining"):
				v.movement.mining_from_dict(d["mining"])
	
func start_new_game(slot: int) -> void:
	Global.loaded_save_index = slot
	_reset_runtime_state()
	Global.money = 100000
	Global.debt = 0.0
	Global.build_stage = Global.BuildStage.PLACE_RESOURCE_BOX
	Global.free_buildings_left = {"base": 4, "resourceBox": 1}
	Global.elapsed_game_seconds = Global.START_HOURS * Global.MINUTES_PER_HOUR * Global.SECONDS_PER_MINUTE
	Global.pending_load = {}
	autosave_enabled = true
	get_tree().change_scene_to_file(LOADING_SCENE)
