extends Node
class_name ArticulationComponent

const ARTICULATE_SPEED: float = deg_to_rad(45.0)

var vehicle: VehicleBody
var rear_section: Node2D
var rear_sprite: AnimatedSprite2D
var rear_length: float
var max_articulation: float
var _half_extents: Vector2

func setup(t_vehicle: VehicleBody, dims: VehicleDimensions) -> void:
	vehicle = t_vehicle
	rear_length = maxf(float(dims.rear_length_pixel), 1.0)
	max_articulation = deg_to_rad(float(dims.max_articulation_deg))
	_half_extents = Vector2(rear_length, float(dims.width_pixel) * 0.5)
	
	rear_section = Node2D.new()
	rear_section.name = "RearSection"
	rear_section.position = Vector2(dims.hinge_offset_pixel, 0.0)
	vehicle.add_child(rear_section)
	vehicle.move_child(rear_section, 0)
	
	var front: AnimatedSprite2D = vehicle.animatedSprite
	var anim: String = vehicle.vehicleType + "_rear"
	rear_sprite = AnimatedSprite2D.new()
	rear_sprite.sprite_frames = front.sprite_frames
	rear_sprite.centered = front.centered
	rear_sprite.rotation = front.rotation
	rear_sprite.scale = front.scale
	rear_sprite.position = Vector2(-rear_length, 0.0)
	rear_section.add_child(rear_sprite)
	
	if front.sprite_frames.has_animation(anim):
		rear_sprite.animation = anim
	else:
		push_warning("Misssing SpriteFrames")
		rear_sprite.visible = false
	
func tick(delta: float, steer_ratio: float) -> void:
	var target: float = -clampf(steer_ratio, -1.0, 1.0) * max_articulation
	rear_section.rotation = move_toward(rear_section.rotation, target, ARTICULATE_SPEED * delta)

func set_rear_heading(global_angle: float) -> void:
	var rel: float = wrapf(global_angle - vehicle.global_rotation, -PI, PI)
	rear_section.rotation = clampf(rel, -max_articulation, max_articulation)
	
func snap_straight() -> void:
	rear_section.rotation = 0.0
	
func set_cargo_frame(frame: int) -> void:
	if rear_sprite == null or rear_sprite.sprite_frames == null:
		return
	var anim: StringName = rear_sprite.animation
	if not rear_sprite.sprite_frames.has_animation(anim):
		return
	var count: int = rear_sprite.sprite_frames.get_frame_count(anim)
	if count <= 1:
		return
	rear_sprite.frame = clampi(frame, 0, count - 1)
	
func mouse_over_rear() -> bool:
	var local: Vector2 = rear_section.get_local_mouse_position() + Vector2(rear_length, 0.0)
	return Rect2(-_half_extents, _half_extents * 2.0).has_point(local)
	
	
	
	
	
