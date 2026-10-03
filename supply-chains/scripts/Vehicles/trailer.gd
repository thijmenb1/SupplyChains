extends CharacterBody2D
class_name Trailer

@onready var animatedSprite: AnimatedSprite2D = $AnimatedSprite2D

var vehicleType: String = "flatbedTrailer"
var vehicleNumber: int
var vehicleID: String
@export var hitch_type: Global.HitchType = Global.HitchType.PIN

var vehicleSpecs: VehicleData
var cargo: CargoComponent

var is_coupled: bool = false
var towing_vehicle: CharacterBody2D = null
var hitch_marker: Marker2D = null

var base_position: Vector2
var has_base_position: bool = false

func _ready():
	animatedSprite.animation = vehicleType
	vehicleID = vehicleType + "*" + str(vehicleNumber)
	VehicleManager.register_vehicle(vehicleID, self)
	
	vehicleSpecs = Global.getVehicleProperties(vehicleType)
	if vehicleSpecs != null and vehicleSpecs.transport != null:
		cargo = CargoComponent.new()
		add_child(cargo)
		cargo.setup(self, vehicleSpecs.transport)
		cargo.cargo_changed.connect(update_cargo_sprite)
		update_cargo_sprite()

func update_cargo_sprite() -> void:
	if cargo == null or not animatedSprite.sprite_frames.has_animation(vehicleType):
		return
	var frame_count: int = animatedSprite.sprite_frames.get_frame_count(vehicleType)
	if frame_count <= 1:
		return
	animatedSprite.frame = clampi(cargo.get_sprite_frame(vehicleType), 0, frame_count -1)

func couple_to(Vehicle: VehicleBody, target_hitch: Marker2D) -> bool:
	towing_vehicle = Vehicle
	hitch_marker = target_hitch
	is_coupled = true
	
	add_collision_exception_with(Vehicle)
	
	Vehicle.attachments.points["hitch"] = self
	
	var Trailer_weight: int = 4000
	Vehicle.attachments.pullingWeight += Trailer_weight
	
	if hitch_type == Global.HitchType.THREE_POINT:
		get_parent().remove_child(self)
		Vehicle.add_child(self)
		global_position = hitch_marker.global_position
		rotation = Vector2.ZERO.angle()
	
	return true

func decouple(vehicleChildNode: Node2D) -> void:
	if not is_coupled:
		return
	
	if not towing_vehicle:
		return

	remove_collision_exception_with(towing_vehicle)

	towing_vehicle.attachments.points["hitch"] = null
	
	var Trailer_weight: int = 4000
	towing_vehicle.attachments.pullingWeight -= Trailer_weight
	
	if hitch_type == Global.HitchType.THREE_POINT:
		var global_pos_backup = global_position
		get_parent().remove_child(self)
		vehicleChildNode.add_child(self)
		global_position = global_pos_backup
	
	is_coupled = false
	towing_vehicle = null
	hitch_marker = null
	return_to_base()

func _physics_process(delta: float) -> void:
	if not is_coupled or not hitch_marker:
		return
	
	match hitch_type:
		Global.HitchType.PIN, Global.HitchType.BALL, Global.HitchType.FIFTH_WHEEL:
			follow_pivoted(delta)
		Global.HitchType.THREE_POINT:
			pass

func follow_pivoted(delta: float) -> void:
	if not towing_vehicle or not hitch_marker:
		return
		
	var hitch_pos: Vector2 = hitch_marker.global_position
	var tow_point_node = get_node_or_null("TowPoint")
	var tow_offset: Vector2 = tow_point_node.position if tow_point_node else Vector2(-20, 0)
	var hitch_length: float = max(tow_offset.length(), 1.0)
	
	var tow_speed: float = 0.0
	if "movement" in towing_vehicle and towing_vehicle.movement:
		tow_speed = towing_vehicle.movement.current_speed * Global.speed_tier
		if towing_vehicle.movement.is_reversing:
			tow_speed = -tow_speed
	

	var articulation: float = wrapf(towing_vehicle.rotation - rotation, -PI, PI)
	var angular_rate: float = (tow_speed / hitch_length) * sin(articulation)
	rotation += angular_rate * delta
	
	var max_articulation: float = deg_to_rad(85.0)
	var clamped_articulation: float = clamp(wrapf(rotation - towing_vehicle.rotation, -PI, PI), -max_articulation, max_articulation)
	rotation = towing_vehicle.rotation + clamped_articulation
	
	global_position = hitch_pos - tow_offset.rotated(rotation)
	

func set_base(pos: Vector2) -> void:
	base_position = pos
	has_base_position = true

func return_to_base() -> void:
	if not has_base_position:
		return
	global_position = base_position
	global_rotation = 0.0
	velocity = Vector2.ZERO
