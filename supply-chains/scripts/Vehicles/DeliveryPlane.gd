extends Node2D
class_name DeliveryPlane

signal delivery_finished(order: Dictionary, airstrip_index: int)

enum Phase { APPROACH, LANDING, LOADING, TAKEOFF }

const CRUISE_SPEED: float = 200.0
const LANDING_SPEED: float = 90.0
const MIN_ROLL_SPEED: float = 30.0
const TAKEOFF_ACCEL: float = 14.0
const APPROACH_LENGTH: float = 240.0
const LEAVE_DISTANCE: float = 5000.0
const LOAD_TIME: float = 20.0
const CRUISE_SCALE: float = 1.6

var order: Dictionary
var airstrip_index: int = -1
var phase: Phase = Phase.APPROACH
var speed: float = CRUISE_SPEED

var dir: Vector2 = Vector2.RIGHT
var entry: Vector2
var far_end: Vector2
var run_len: float = 1.0
var target: Vector2
var on_final: bool = false
var load_timer: float = 0.0
var takeoff_start: Vector2

func setup(t_order: Dictionary, t_airstrip_index: int, spawn_angle: float, spawn_distance: float) -> bool:
	order = t_order
	airstrip_index = t_airstrip_index
	
	var cells: Array = Global.airstrips[airstrip_index].get("cells", [])
	if cells.is_empty():
		return false
	
	var tile: float = float(Global.TILE_SIZE)
	var min_c: Vector2i = cells[0]
	var max_c: Vector2i = cells[0]
	for c: Vector2i in cells:
		min_c = Vector2i(mini(min_c.x, c.x), mini(min_c.y, c.y))
		max_c = Vector2i(maxi(max_c.x, c.x), maxi(max_c.y, c.y))
	
	var a: Vector2 = (Vector2(min_c) + Vector2(0.5, 0.5)) * tile
	var b: Vector2 = (Vector2(max_c) + Vector2(0.5, 0.5)) * tile
	var center: Vector2 = (a + b) * 0.5
	if (max_c.x - min_c.x) >= (max_c.y - min_c.y):
		a.y = center.y
		b.y = center.y
	else:
		a.x = center.x
		b.x = center.x 
	var axis: Vector2 = (b - a).normalized() if a.distance_to(b) > 1.0 else Vector2.RIGHT
	a -= axis * tile * 0.5
	b += axis * tile * 0.5
	
	global_position = center + Vector2.from_angle(spawn_angle) * spawn_distance
	
	if global_position.distance_to(a) <= global_position. distance_to(b):
		entry = a
		far_end = b
	else:
		entry = b
		far_end = a
	dir = (far_end - entry).normalized()
	run_len = maxf(entry.distance_to(far_end), 1.0)
	
	target = entry - dir * APPROACH_LENGTH
	rotation = (target - global_position).angle()
	scale = Vector2.ONE * CRUISE_SCALE
	z_index = 50
	_update_eta()
	return true

func _process(delta):
	var dt: float = delta * Global.speed_tier
	if dt <= 0.0:
		return
	match phase:
		Phase.APPROACH:
			_process_approach(dt)
		Phase.LANDING:
			_process_landing(dt)
		Phase.LOADING:
			_process_loading(dt)
		Phase.TAKEOFF:
			_process_takeoff(dt)
		
func _process_approach(dt: float) -> void:
	var to_target: Vector2 = target - global_position
	var dist: float = to_target.length()
	rotation = to_target.angle() if dist > 0.5 else dir.angle()
	
	if on_final:
		speed = LANDING_SPEED
		scale = Vector2.ONE * lerpf(1.0, CRUISE_SCALE, clampf(dist / APPROACH_LENGTH, 0.0, 1.0))
	
	var step: float = speed * dt
	if step >= dist:
		global_position = target
		if not on_final:
			on_final = true
			target = entry
		else:
			phase = Phase.LANDING
			scale = Vector2.ONE
	else:
		global_position += to_target / dist * step
	_update_eta()

func _process_landing(dt: float) -> void:
	rotation = dir.angle()
	var remaining: float = global_position.distance_to(far_end)
	if remaining <= 1.0:
		phase = Phase.LOADING
		load_timer = 0.0
		order["stage"] = "loading"
		order["loaded"] = 0
		return
	speed = clampf(LANDING_SPEED * remaining / run_len, MIN_ROLL_SPEED, LANDING_SPEED)
	global_position = global_position.move_toward(far_end, speed * dt)
	
func _process_loading(dt: float) -> void:
	load_timer += dt
	var progress: float = clampf(load_timer / LOAD_TIME, 0.0, 1.0)
	order["loaded"] = int(floor(order["quantity"] * progress))
	if progress >= 1.0:
		order["loaded"] = order["quantity"]
		order["stage"] = "leaving"
		dir = -dir
		rotation = dir.angle()
		speed = 0.0
		takeoff_start = global_position
		phase = Phase.TAKEOFF

func _process_takeoff(dt: float) -> void:
	speed = minf(speed + TAKEOFF_ACCEL * dt, CRUISE_SPEED)
	global_position += dir * speed * dt
	var travelled: float = global_position.distance_to(takeoff_start)
	var total: float = run_len + LEAVE_DISTANCE
	scale = Vector2.ONE * lerpf(1.0, CRUISE_SCALE, clampf((travelled - run_len * 0.5) / total, 0.0, 1.0))
	if travelled >= total:
		delivery_finished.emit(order, airstrip_index)
		queue_free()

func _update_eta() -> void:
	var dist: float = global_position.distance_to(target)
	if not on_final:
		dist += target.distance_to(entry) / LANDING_SPEED * CRUISE_SPEED
	order["arrival_time"] = Global.elapsed_game_seconds + dist / maxf(speed, 1.0)
