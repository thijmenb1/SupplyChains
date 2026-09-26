extends Camera2D

@export var speed: float = 1000.0
@export var zoom_speed: float = 0.15
@export var min_zoom: float = 0.1
@export var max_zoom: float = 3.0

var target_zoom: float = 1.0
var following: bool = false

func _ready():
	VehicleManager.camera = self

func _process(delta: float) -> void:
	# 1. Handle Movement
	var input_dir = Vector2.ZERO
	
	if Input.is_key_pressed(KEY_W) or Input.is_key_pressed(KEY_UP):
		input_dir.y -= 1
		following = false
	if Input.is_key_pressed(KEY_S) or Input.is_key_pressed(KEY_DOWN):
		input_dir.y += 1
		following = false
	if Input.is_key_pressed(KEY_A) or Input.is_key_pressed(KEY_LEFT):
		input_dir.x -= 1
		following = false
	if Input.is_key_pressed(KEY_D) or Input.is_key_pressed(KEY_RIGHT):
		input_dir.x += 1
		following = false
		
	# Normalize to prevent faster diagonal movement
	input_dir = input_dir.normalized()
	
	# Move the camera
	position += input_dir * speed * delta

	# 2. Smooth Zoom Interpolation
	zoom = zoom.lerp(Vector2(target_zoom, target_zoom), 10 * delta)

func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventMouseButton:
		if event.is_pressed():
			if event.button_index == MOUSE_BUTTON_WHEEL_UP:
				target_zoom = clamp(target_zoom + zoom_speed, min_zoom, max_zoom)
			elif event.button_index == MOUSE_BUTTON_WHEEL_DOWN:
				target_zoom = clamp(target_zoom - zoom_speed, min_zoom, max_zoom)

func follow_vehilce(vehicle_posistion: Vector2) -> void:
	if following:
		if zoom <= Vector2(1,1):
			target_zoom = 1.5
		global_position = vehicle_posistion
