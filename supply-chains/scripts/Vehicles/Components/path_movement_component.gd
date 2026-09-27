extends Node
class_name PathMovementComponent

const ON_ROAD_ARRIVAL_RADIUS: float = 12.0
const OFFROAD_ARRIVAL_RADIUS: float = 32.0
const SHIFT_DURATION: float = 0.5
const GEAR_SHIFT_SPEED_THRESHOLD: float = 4.0

var vehicle: VehicleBody
var drivetrain: DrivetrainComponent
var attachments: AttachmentComponent
var pathfinding: VehicleDimensions
var mobility: VehicleMobility

var front_left_wheel: Node2D
var front_rigth_wheel: Node2D
var pathDebugLine: Line2D

var home_tile: Vector2i = Vector2i.ZERO
var has_home_tile: bool = false
var returning_to_base: bool = false

var max_steer_angle_deg: float = 35.0
var steer_speed: float = 5.0
var current_steer_angle: float = 0.0
var current_speed: float = 0.0
var desired_reversing := false
var is_reversing := false
var is_shifting := false
var shift_timer: float = 0.0
var prev_articulation: float = 0.0
var lookahead_distance: float = 48.0
var on_road_lookahead_distance: float = 14.0
var trailer_reverse_baseline: float = 0.0

var current_path: Array[Vector2] = []
var path_index: int = 0
var assigned_route_index: int = -1
var targetTile: Vector2i
var firstLocationTile: Vector2i
var secondLocationTile: Vector2i

enum ROUTE_STAGE {NONE, TO_SOURCE, AT_SOURCE, TO_DEST, AT_DEST}
var route_stage: ROUTE_STAGE = ROUTE_STAGE.NONE
var route_waiting: bool = false
var route_wait_timer: float = 0.0
const ROUTE_DOCK_WAIT_DURATION: float = 5.0

enum PATHPHASE {NONE, TO_CORRIDOR, TO_STAGING}
var path_phase: PATHPHASE = PATHPHASE.NONE
var _pending_path_key: String = ""
var _pending_path_data: Dictionary = {}
var _stage_brake_latched: bool = false

var docking_active: bool = false
var docking_points: Array[Vector2] = []
var dock_distance: float = 0.0
var docking_direction: int = 1  
const DOCK_SPEED: float = 20
const HITCH_OFFSET: float = 24

const DOCK_POS_TOLETANCE: float = 6.0
const DOCK_ANGLE_TOLERANCE: float = deg_to_rad(8.0)

func setup(t_vehicle: VehicleBody, t_pathfinding: VehicleDimensions, t_mobility: VehicleMobility, t_drivetrain: DrivetrainComponent, t_attachments: AttachmentComponent, t_left_wheel: Node2D, t_right_wheel: Node2D, t_debug_line: Line2D) -> void:
	vehicle = t_vehicle
	pathfinding = t_pathfinding
	mobility = t_mobility
	drivetrain = t_drivetrain
	attachments = t_attachments
	front_left_wheel = t_left_wheel
	front_rigth_wheel = t_right_wheel
	pathDebugLine = t_debug_line

func _is_on_road() -> bool:
	return _is_tile_on_road(vehicle.global_position)

func _is_tile_on_road(world_pos: Vector2) -> bool:
	var tile: Vector2i = Vector2i((world_pos / Global.TILE_SIZE).floor())
	return GridManager.astar.get_point_weight_scale(tile) <= GridManager.ROAD_WEIGHT_THRESHOLD

func _arrival_radius_for(pos: Vector2) -> float:
	return ON_ROAD_ARRIVAL_RADIUS if _is_tile_on_road(pos) else OFFROAD_ARRIVAL_RADIUS

func _waypoint_pass_direction(i: int) -> Vector2:
	if i + 1 < current_path.size():
		return (current_path[i + 1] - current_path[i]).normalized()
	elif i > 0:
		return (current_path[i] - current_path[i - 1]).normalized()
	return Vector2.ZERO

func _has_crossed_waypoint(i: int) -> bool:
	var pass_dir: Vector2 = _waypoint_pass_direction(i)
	if pass_dir == Vector2.ZERO:
		return false
	return (vehicle.global_position - current_path[i]).dot(pass_dir) >= 0.0

func start_docking_job(factory: FactoryInstance, purpose: String) -> bool:
	var can_go_offroad: bool = float(mobility.offroad_capability) > 0.6
	var best_key: String = ""
	var best_data: Dictionary = {}
	var best_dist: float = INF

	for key in factory.paths.keys():
		if not key.begins_with(purpose):
			continue
		var data: Dictionary = factory.paths[key]
		if not can_go_offroad and not data.get("reachable", false):
			continue
		var corridor_tile: Vector2i = Global._corridor_tile_for_path_data(data)
		var dist: float = vehicle.global_position.distance_to(Vector2(corridor_tile) * Global.TILE_SIZE)
		if dist < best_dist:
			best_dist = dist
			best_key = key
			best_data = data
	
	print(best_key)
	
	if best_key == "":
		print("Cannot reach destination")
		return false

	_pending_path_key = best_key
	_pending_path_data = best_data
	path_phase = PATHPHASE.TO_CORRIDOR
	navigate_to_tile(Vector2i(best_data["corridor_tile"]))
	return true


func _advance_path_index() -> void:
	var advanced := false
	var max_index: int = current_path.size() - 1 if path_phase == PATHPHASE.TO_STAGING else current_path.size()
	while path_index < max_index:
		var wp: Vector2 = current_path[path_index]
		var dist: float = vehicle.global_position.distance_to(wp)
		var radius: float = _arrival_radius_for(wp)

		if dist <= radius or _has_crossed_waypoint(path_index):
			path_index += 1
			advanced = true
			continue

		break

	if advanced:
		update_debug_path_line()

func _get_lookahead_target() -> Vector2:
	var last_index: int = current_path.size() - 1
	if last_index < 0:
		return vehicle.global_position
	if path_index > last_index:
		return current_path[last_index]

	var effective_lookahead: float = on_road_lookahead_distance if _is_on_road() else lookahead_distance
	var remaining: float = effective_lookahead
	var from_pos: Vector2 = vehicle.global_position
	for idx in range(path_index, current_path.size()):
		var wp: Vector2 = current_path[idx]
		var seg_len: float = from_pos.distance_to(wp)
		if seg_len >= remaining:
			if seg_len <= 0.001:
				return wp
			return from_pos.lerp(wp, remaining / seg_len)
		remaining -= seg_len
		from_pos = wp

	return current_path[last_index]

func tick(delta: float) -> void:
	if route_waiting:
		_tick_route_wait(delta)
		return
	if docking_active:
		_tick_docking(delta)
		return
	if assigned_route_index == -1 and not returning_to_base and current_path.is_empty() and path_phase == PATHPHASE.NONE and has_home_tile:
		_maybe_start_return_to_base()
	if path_phase == PATHPHASE.TO_STAGING:
		if _ready_for_dock_handoff():
			docking_points = (_pending_path_data["points"] as Array[Vector2]).duplicate()
			dock_distance = 0.0
			docking_direction = 1
			docking_active = true
			path_phase = PATHPHASE.NONE
			_stage_brake_latched = false
			var dock_hitch = attachments.get_hitch() if attachments else null
			if dock_hitch != null and dock_hitch.is_coupled:
				dock_hitch.set_physics_process(false)          # <-- add
			return
		elif _stage_brake_latched and abs(current_speed) <= DOCK_HANDOFF_SPEED_THRESHOLD:
			var target_pos: Vector2 = _pending_path_data["points"][0]
			var to_target: Vector2 = target_pos - vehicle.global_position
			if to_target.length() > 0.5:
				vehicle.global_position += to_target.normalized() * min(20.0 * delta, to_target.length())
			return
 
	if not current_path.is_empty() and drivetrain.fuel > 0 and not vehicle.parked:
		_advance_path_index()

	if current_path.is_empty() or path_index >= current_path.size() or drivetrain.fuel <= 0 or vehicle.parked:
		current_speed = move_toward(current_speed, 0.0, drivetrain.get_braking_rate() * delta)
		vehicle.velocity = Vector2.RIGHT.rotated(vehicle.rotation) * current_speed * Global.speed_tier
		vehicle.move_and_slide()
		if not current_path.is_empty() and path_index >= current_path.size() and path_phase != PATHPHASE.TO_STAGING:
			current_path.clear()
			handelArival()
		return
	
	var target_pos: Vector2 = _get_lookahead_target()
	var local_target: Vector2 = vehicle.to_local(target_pos)
	var desired_angle: float = local_target.angle()
	var max_steer_rad: float = deg_to_rad(max_steer_angle_deg)

	var reverse_threshold_high: float = deg_to_rad(105.0)
	var reverse_threshold_low: float = deg_to_rad(75.0)

	if path_phase == PATHPHASE.TO_STAGING:
		desired_reversing = _pending_path_data.get("reverse_in", false)
	else:
		if not desired_reversing and abs(desired_angle) > reverse_threshold_high:
			desired_reversing = true
		elif desired_reversing and abs(desired_angle) < reverse_threshold_low:
			desired_reversing = false

	var shifting_gears: bool = is_shifting or (desired_reversing != is_reversing)

	if is_shifting:
		current_speed = move_toward(current_speed, 0.0, drivetrain.get_braking_rate() * delta)
		shift_timer += delta
		if shift_timer >= SHIFT_DURATION:
			is_reversing = desired_reversing
			is_shifting = false
			shift_timer = 0.0
			shifting_gears = false
			var shift_hitch = attachments.get_hitch() if attachments else null
			if is_reversing and shift_hitch != null and shift_hitch.is_coupled:
				trailer_reverse_baseline = 0.0
				prev_articulation = wrapf(vehicle.rotation - (shift_hitch as CharacterBody2D).rotation - trailer_reverse_baseline, -PI, PI)
	elif desired_reversing != is_reversing:
		current_speed = move_toward(current_speed, 0.0, drivetrain.get_braking_rate() * delta)
		if abs(current_speed) <= GEAR_SHIFT_SPEED_THRESHOLD:
			var aligned: bool = attachments.trailer_aligned_enough() if attachments else true
			if desired_reversing and not aligned:
				current_speed = move_toward(current_speed, 30.0, drivetrain.get_acceleration_rate() * delta)
				current_steer_angle = move_toward(current_steer_angle, 0.0, steer_speed * delta)
			else:
				is_shifting = true
				shift_timer = 0.0
				current_speed = 0.0

	if is_reversing:
		desired_angle = wrapf(desired_angle + PI, -PI, PI)

	var target_steer: float = clamp(desired_angle, -max_steer_rad, max_steer_rad)

	var hitched_trailer = attachments.get_hitch() if attachments else null
	var has_coupled_trailer: bool = hitched_trailer != null and hitched_trailer.is_coupled


	if path_phase == PATHPHASE.TO_STAGING and not has_coupled_trailer:
		target_steer = 0.0
	elif is_reversing and has_coupled_trailer:
		var trailer_node: CharacterBody2D = hitched_trailer as CharacterBody2D
		var trailer_local_target: Vector2 = trailer_node.to_local(target_pos)
		var trailer_heading_error: float = wrapf(trailer_local_target.angle() + PI, -PI, PI)
		var max_articulation_cmd: float = deg_to_rad(40.0)
		var target_articulation: float = clamp(-trailer_heading_error * 0.6, -max_articulation_cmd, max_articulation_cmd)
		var current_articulation: float = wrapf(vehicle.rotation - trailer_node.rotation - trailer_reverse_baseline, -PI, PI)
		var articulation_rate: float = wrapf(current_articulation - prev_articulation, -PI, PI) / delta

		prev_articulation = current_articulation
		var articulation_error: float = wrapf(target_articulation - current_articulation, -PI, PI)
		var counter_steer: float = -(articulation_error * 2.5) + (articulation_rate * 0.3)
		target_steer = clamp(counter_steer, -max_steer_rad, max_steer_rad)
	elif has_coupled_trailer:
		target_steer = clamp(desired_angle, -max_steer_rad, max_steer_rad)

	var effective_steer_speed: float = steer_speed
	if is_reversing and not has_coupled_trailer:
		effective_steer_speed = steer_speed * 0.4
	elif has_coupled_trailer and not is_reversing:
		var trailer_node2: CharacterBody2D = hitched_trailer as CharacterBody2D
		var swing_severity: float = clamp(abs(wrapf(vehicle.rotation - trailer_node2.rotation, -PI, PI)) / deg_to_rad(85.0), 0.0, 1.0)
		effective_steer_speed = lerp(steer_speed, steer_speed * 0.35, swing_severity)
	current_steer_angle = move_toward(current_steer_angle, target_steer, effective_steer_speed * delta)

	if front_left_wheel:
		front_left_wheel.rotation = current_steer_angle
	if front_rigth_wheel:
		front_rigth_wheel.rotation = current_steer_angle

	if not shifting_gears:
		var target_max_speed: float = drivetrain.get_calculated_speed()
		if is_reversing:
			target_max_speed *= 0.5

		var steer_ratio: float = abs(target_steer) / max_steer_rad
		var corner_speed_factor: float = lerp(1.0, 0.35, clamp(steer_ratio, 0.0, 1.0))
		target_max_speed *= corner_speed_factor

		var corner_braking_mult: float = 1.0

		if not is_reversing and path_index + 1 < current_path.size():
			const CORNER_SCAN_WAYPOINTS: int = 8
			var scan_prev_pos: Vector2 = vehicle.global_position
			var cumulative_dist: float = 0.0
			var tightest_corner_target_speed: float = target_max_speed
			var tightest_corner_severity: float = 0.0
			var tightest_corner_dist: float = 0.0

			var scan_end: int = min(path_index + CORNER_SCAN_WAYPOINTS, current_path.size() - 1)
			for c in range(path_index, scan_end):
				var corner_pos: Vector2 = current_path[c]
				cumulative_dist += scan_prev_pos.distance_to(corner_pos)
				scan_prev_pos = corner_pos

				var incoming_dir: Vector2 = corner_pos - (current_path[c - 1] if c > path_index else vehicle.global_position)
				var outgoing_dir: Vector2 = current_path[c + 1] - corner_pos
				if incoming_dir.length() > 0.01 and outgoing_dir.length() > 0.01:
					var turn_angle: float = abs(incoming_dir.normalized().angle_to(outgoing_dir.normalized()))
					var turn_severity: float = clamp(turn_angle / deg_to_rad(90.0), 0.0, 1.0)
					var this_corner_speed: float = lerp(target_max_speed, target_max_speed * 0.3, turn_severity)
					if this_corner_speed < tightest_corner_target_speed:
						tightest_corner_target_speed = this_corner_speed
						tightest_corner_severity = turn_severity
						tightest_corner_dist = cumulative_dist

			if tightest_corner_severity > 0.0:
				const CORNER_BRAKING_BOOST: float = 2.0
				const MIN_BRAKING_LEAD_DISTANCE: float = ON_ROAD_ARRIVAL_RADIUS * 4
				const BRAKING_SAFETY_MARGIN: float = 3.0
				corner_braking_mult = lerp(1.0, CORNER_BRAKING_BOOST, tightest_corner_severity)
				var braking_distance: float = max((max(current_speed * current_speed - tightest_corner_target_speed * tightest_corner_target_speed, 0.0) / (2.0 * drivetrain.get_braking_rate())) * BRAKING_SAFETY_MARGIN, MIN_BRAKING_LEAD_DISTANCE * tightest_corner_severity)
				if tightest_corner_dist <= braking_distance:
					target_max_speed = min(target_max_speed, tightest_corner_target_speed)

		if path_phase == PATHPHASE.TO_STAGING and not current_path.is_empty():
			var stage_target: Vector2 = current_path[current_path.size() - 1]
			var dist_to_stage: float = vehicle.global_position.distance_to(stage_target)
			const STAGE_BRAKING_SAFETY_MARGIN: float = 1.2
			var stage_braking_distance: float = ((current_speed * current_speed) / (2.0 * drivetrain.get_braking_rate())) * 1.05
			var crossed_stage: bool = _has_crossed_waypoint(current_path.size() - 1)
			if not _stage_brake_latched and (dist_to_stage <= stage_braking_distance or crossed_stage):
				_stage_brake_latched = true
			if _stage_brake_latched:
				target_max_speed = 0.0
		
		var approaching_dock: bool = (path_phase == PATHPHASE.TO_CORRIDOR) or (path_phase == PATHPHASE.NONE and assigned_route_index != -1 and not route_waiting and not docking_active)
		if approaching_dock and not current_path.is_empty():
			var final_wp: Vector2 = current_path[current_path.size() - 1]
			var dist_to_final: float = vehicle.global_position.distance_to(final_wp)
			var final_arrival_radius: float = _arrival_radius_for(final_wp)
			var approach_braking_distance: float = (current_speed * current_speed) / (2.0 * drivetrain.get_braking_rate()) + final_arrival_radius
			if dist_to_final <= approach_braking_distance:
				target_max_speed = 0.0

		var rate: float = drivetrain.get_acceleration_rate() if target_max_speed > current_speed else drivetrain.get_braking_rate() * corner_braking_mult
		current_speed = move_toward(current_speed, target_max_speed, rate * delta)

	var speed_direction: float = -1.0 if is_reversing else 1.0
	vehicle.velocity = Vector2.RIGHT.rotated(vehicle.rotation) * (current_speed * speed_direction) * Global.speed_tier
	vehicle.move_and_slide()

	if abs(current_steer_angle) > 0.001:
		var wheel_base: float = pathfinding.wheelbase_pixel
		var angular_velocity: float = (current_speed / wheel_base) * tan(current_steer_angle) * speed_direction
		vehicle.rotation += angular_velocity * delta

func handelArival() -> void:
	if path_phase == PATHPHASE.TO_CORRIDOR:
		path_phase = PATHPHASE.TO_STAGING
		var stage_point: Vector2 = _pending_path_data["points"][0]
		var tangent: Vector2 = _pending_path_data["start_tangent"]
		var reverse_in: bool = _pending_path_data.get("reverse_in", false)
		current_path = [stage_point - tangent * 40.0, stage_point]
		path_index = 0
		vehicle.rotation = tangent.angle() + (PI if reverse_in else 0.0)
		is_reversing = reverse_in
		desired_reversing = reverse_in
		is_shifting = false
		shift_timer = 0.0
		current_steer_angle = 0.0
		_stage_brake_latched = false
		current_speed = 0.0
		var flip_hitch = attachments.get_hitch() if attachments else null
		if flip_hitch != null and flip_hitch.is_coupled:
			var tow_point_node = flip_hitch.get_node_or_null("TowPoint")
			var tow_offset: Vector2 = tow_point_node.position if tow_point_node else Vector2(-20, 0)
			var trailer_rot: float = wrapf(vehicle.rotation + (PI if reverse_in else 0.0), -PI, PI)
			flip_hitch.rotation = trailer_rot
			flip_hitch.global_position = flip_hitch.hitch_marker.global_position - tow_offset.rotated(trailer_rot)
			trailer_reverse_baseline = PI if reverse_in else 0.0
			prev_articulation = 0.0
		_stage_brake_latched = false
		update_debug_path_line()
		return
	if assigned_route_index != -1:
		_handel_route_arrival()
		return
	if returning_to_base:
		returning_to_base = false
		current_speed = 0.0
		return
	if targetTile == firstLocationTile:
		targetTile = secondLocationTile
	elif targetTile == secondLocationTile:
		targetTile = firstLocationTile
	else:
		print("Error: something wrong with nav coords")
	navigate_to_tile(targetTile)

func _maybe_start_return_to_base() -> void:
	var current_tile: Vector2i = Vector2i((vehicle.global_position / Global.TILE_SIZE).floor())
	if current_tile == home_tile:
		return
	returning_to_base = true
	navigate_to_tile(home_tile)

func _handel_route_arrival() -> void:
	var route_data: Dictionary = Global.routes[assigned_route_index]
	if route_stage == ROUTE_STAGE.TO_DEST:
		var unload_dock: Dictionary = route_data["unload_dock"]
		if unload_dock.is_empty():
			_arrive_no_dock(ROUTE_STAGE.AT_DEST)
		else:
			_route_dock_at(unload_dock)
	elif route_stage == ROUTE_STAGE.TO_SOURCE:
		var load_dock: Dictionary = route_data.get("load_dock", {})
		if load_dock.is_empty():
			_arrive_no_dock(ROUTE_STAGE.AT_SOURCE)
		else:
			_route_dock_at(load_dock)

func _route_dock_at(dock_data: Dictionary) -> void:
	if dock_data.is_empty():
		push_error("Route " + str(assigned_route_index) + " has no dock data for stage " + str(route_stage))
		path_phase = PATHPHASE.NONE
		return
	_pending_path_data = dock_data
	path_phase = PATHPHASE.TO_STAGING
	var stage_point: Vector2 = dock_data["points"][0]
	var tangent: Vector2 = dock_data["start_tangent"]
	var reverse_in: bool = dock_data.get("reverse_in", false)
	current_path = [stage_point - tangent * 40.0, stage_point]
	path_index = 0
	vehicle.rotation = tangent.angle() + (PI if reverse_in else 0.0)
	is_reversing = reverse_in
	desired_reversing = reverse_in
	is_shifting = false
	shift_timer = 0.0
	current_steer_angle = 0.0
	_stage_brake_latched = false
	current_speed = 0.0
	var flip_hitch = attachments.get_hitch() if attachments else null
	if flip_hitch != null and flip_hitch.is_coupled:
		var tow_point_node = flip_hitch.get_node_or_null("TowPoint")
		var tow_offset: Vector2 = tow_point_node.position if tow_point_node else Vector2(-20, 0)
		var trailer_rot: float = wrapf(vehicle.rotation + (PI if reverse_in else 0.0), -PI, PI)
		flip_hitch.rotation = trailer_rot
		flip_hitch.global_position = flip_hitch.hitch_marker.global_position - tow_offset.rotated(trailer_rot)
		trailer_reverse_baseline = PI if reverse_in else 0.0
		prev_articulation = 0.0
	_stage_brake_latched = false
	update_debug_path_line()

func _start_middle_leg(forward: bool) -> void:
	route_stage = ROUTE_STAGE.TO_DEST if forward else ROUTE_STAGE.TO_SOURCE
	var route_data: Dictionary = Global.routes[assigned_route_index]
	var can_go_offroad: bool = float(mobility.offroad_capability) > 0.6
	var middle_path: Array = route_data.get("Onroad_route", []) if not can_go_offroad else route_data.get("Offroad_route", [])
	if can_go_offroad and middle_path.is_empty():
		middle_path = route_data.get("Offroad_route", [])
	current_path.clear()
	if forward:
		for p in middle_path:
			current_path.append(p)
	else:
		for i in range(middle_path.size() - 1, -1, -1):
			current_path.append(middle_path[i])
	path_index = 0
	update_debug_path_line()

func assign_route(route_index: int) -> void:
	assigned_route_index = route_index
	returning_to_base = false
	if route_index == -1:
		return
	var route_data: Dictionary = Global.routes[route_index]
	var dock_data: Dictionary = route_data.get("load_dock", {})
	if dock_data.is_empty():
		Global.mark_routes_dirty()
		route_data = Global.routes[route_index]
		dock_data = route_data.get("load_dock", {})
	route_stage = ROUTE_STAGE.TO_SOURCE
	if dock_data.is_empty():
		path_phase = PATHPHASE.NONE
		navigate_to_tile(route_data["source"].grid_pos)
	else:
		_pending_path_data = dock_data
		path_phase = PATHPHASE.TO_CORRIDOR
		navigate_to_tile(Vector2(dock_data["corridor_tile"]))

func _arrive_no_dock(stage: ROUTE_STAGE) -> void:
	current_speed = 0.0
	route_stage = stage
	route_waiting = true
	route_wait_timer = 0.0

func clear_route() -> void:
	var hitch = attachments.get_hitch() if attachments else null
	if hitch != null:
		hitch.set_physics_process(true)
	assigned_route_index = -1
	route_stage = ROUTE_STAGE.NONE
	route_waiting = false
	route_wait_timer = 0.0
	docking_active = false
	docking_direction = 1
	docking_points.clear()
	dock_distance = 0.0
	_pending_path_data = {}
	path_phase = PATHPHASE.NONE
	current_path.clear()
	path_index = 0
	_stage_brake_latched = false

func navigate_to_tile(destination_tile: Vector2i) -> void:
	targetTile = destination_tile
	var current_tile: Vector2i = Vector2i((vehicle.global_position / Global.TILE_SIZE).floor())
	var offroad_capability: float = float(mobility.offroad_capability)
	var can_go_offroad: bool = offroad_capability > 0.6
	if can_go_offroad:
		current_path.assign(GridManager.astar.get_point_path(current_tile, destination_tile))
	else:
		current_path = _get_road_only_path(current_tile, destination_tile)
		if current_path.is_empty():
			current_path.assign(GridManager.astar.get_point_path(current_tile, destination_tile))
	current_path = Global.chamfer_path_corners(current_path, Global.ON_ROAD_CHAMFER_DISTANCE)
	path_index = 0
	update_debug_path_line()

func _get_road_only_path(start: Vector2i, end: Vector2i) -> Array[Vector2]:
	var temp_path: Array[Vector2] = []
	var raw_points = GridManager.astar.get_id_path(start, end)
	if raw_points.is_empty():
		return temp_path
	for id in raw_points:
		if GridManager.astar.get_point_weight_scale(id) <= GridManager.ROAD_WEIGHT_THRESHOLD:
			temp_path.append(GridManager.astar.get_point_position(id))
		else:
			print("no road path availible")
			return []
	return temp_path

func update_debug_path_line() -> void:
	if not pathDebugLine:
		return
	pathDebugLine.clear_points()
	if current_path.is_empty() or path_index >= current_path.size():
		return
	pathDebugLine.add_point(vehicle.global_position)
	for i in range(path_index, current_path.size()):
		pathDebugLine.add_point(current_path[i])

const DOCK_HANDOFF_SPEED_THRESHOLD: float = 2.0

func _ready_for_dock_handoff() -> bool:
	if _pending_path_data.is_empty():
		return false
	var target_pos: Vector2 = _pending_path_data["points"][0]
	var pos_ok: bool = vehicle.global_position.distance_to(target_pos) <= DOCK_POS_TOLETANCE
	var stopped: bool = abs(current_speed) <= DOCK_HANDOFF_SPEED_THRESHOLD
	return pos_ok and stopped

func _tick_docking(delta: float) -> void:
	var reverse_in: bool = _pending_path_data.get("reverse_in", true)
	var path_len: float = _path_length(docking_points)
	dock_distance = clamp(dock_distance + DOCK_SPEED * delta * docking_direction, 0.0, path_len)
	var vehicle_pos: Vector2 = _sample_along(docking_points, dock_distance)
	var vehicle_tangent: Vector2 = _tangent_along(docking_points, dock_distance)
	vehicle.global_position = vehicle_pos
	vehicle.rotation = vehicle_tangent.angle() + (PI if reverse_in else 0.0)
	
	var hitched = attachments.get_hitch() if attachments else null
	if hitched != null and hitched.is_coupled:
		var trailer_dist: float = clamp(dock_distance + (HITCH_OFFSET if reverse_in else -HITCH_OFFSET), 0.0, path_len)
		var trailer_pos: Vector2 = _sample_along(docking_points, trailer_dist)
		var trailer_tangent: Vector2 = _tangent_along(docking_points, trailer_dist)
		(hitched as Node2D).global_position = trailer_pos
		(hitched as Node2D).rotation = trailer_tangent.angle()
	
	var finished: bool = (docking_direction > 0 and dock_distance >= path_len) or (docking_direction < 0 and dock_distance <= 0.0)
	if finished:
		docking_active = false
		var dock_hitch = attachments.get_hitch() if attachments else null
		if dock_hitch != null:
			dock_hitch.set_physics_process(true)
		_on_dock_leg_complete()

func _on_dock_leg_complete() -> void:
	if docking_direction > 0:
		if assigned_route_index != -1:
			route_stage = ROUTE_STAGE.AT_SOURCE if route_stage == ROUTE_STAGE.TO_SOURCE else ROUTE_STAGE.AT_DEST
			route_waiting = true
			route_wait_timer = 0.0
		else:
			docking_points.clear()
			dock_distance = 0.0
			_pending_path_data = {}
	else:
		docking_points.clear()
		dock_distance = 0.0
		_pending_path_data = {}
		if assigned_route_index != -1:
			_start_middle_leg(route_stage == ROUTE_STAGE.AT_SOURCE)

func _tick_route_wait(delta: float) -> void:
	current_speed = move_toward(current_speed, 0.0, drivetrain.get_braking_rate() * delta)
	vehicle.velocity = Vector2.ZERO
	vehicle.move_and_slide()

	route_wait_timer += delta
	if route_wait_timer < ROUTE_DOCK_WAIT_DURATION:
		return

	route_waiting = false
	route_wait_timer = 0.0
	_handle_route_transfer()
	
	if docking_points.is_empty():
		_start_middle_leg(route_stage == ROUTE_STAGE.AT_SOURCE)
		return
	
	docking_direction = -1
	docking_active = true
	var wait_hitch = attachments.get_hitch() if attachments else null
	if wait_hitch != null and wait_hitch.is_coupled:
		wait_hitch.set_physics_process(false)

func _get_cargo_component() -> CargoComponent:
	if vehicle.cargo != null:
		return vehicle.cargo
	var hitch = attachments.get_hitch() if attachments else null
	if hitch != null and "cargo" in hitch and hitch.cargo != null:
		return hitch.cargo
	return null

func _handle_route_transfer() -> void:
	var route_data: Dictionary = Global.routes[assigned_route_index]
	var resource: String = route_data["resource"]
	var cargo_component := _get_cargo_component()
	if cargo_component == null:
		push_warning("No cargo component available to transfer " + resource)
	
	
	if route_stage == ROUTE_STAGE.AT_SOURCE:
		var source = route_data["source"]
		var free_volume: float = cargo_component.transport.cargo_volume_capacity - cargo_component.cargoVolume
		var amount: int = source.withdraw_resource(resource, int(free_volume))
		if amount > 0:
			cargo_component.load_vehicle(resource, amount)
	elif route_stage == ROUTE_STAGE.AT_DEST:
		var destination = route_data["destination"]
		var amount: float = cargo_component.cargo.get(resource, 0.0)
		if amount > 0:
			cargo_component.unload_vehicle(resource, amount)
			destination.deposit_resource(resource, int(amount))
	
func _path_length(pts: Array[Vector2]) -> float:
	var total := 0.0
	for i in range(pts.size() - 1):
		total += pts[i].distance_to(pts[i + 1])
	return total

func _sample_along(pts: Array[Vector2], dist: float) -> Vector2:
	var remaining := dist
	for i in range(pts.size() - 1):
		var seg: float = pts[i].distance_to(pts[i + 1])
		if remaining <= seg:
			return pts[i].lerp(pts[i + 1], remaining / seg) if seg > 0.001 else pts[i]
		remaining -= seg
	return pts[pts.size() - 1]

func _tangent_along(pts: Array[Vector2], dist: float) -> Vector2:
	var eps := 1.0
	var a: Vector2 = _sample_along(pts, max(dist - eps, 0.0))
	var b: Vector2 = _sample_along(pts, min(dist + eps, _path_length(pts)))
	return (b - a).normalized() if a != b else Vector2.RIGHT
