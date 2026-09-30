extends CharacterBody2D
class_name VehicleBody

@onready var animatedSprite: AnimatedSprite2D = $VehicleSprite
@onready var rearAttatchmentPoint: Marker2D = $RearAttatchmentPoint
@onready var frontAttatchmentPoint: Marker2D = $FrontAttatchmentPoint
@onready var pathDebugLine: Line2D = $PathDebugLine
@onready var front_left_wheel: Node2D = $VehicleSprite/frontLeftTireSprite
@onready var front_right_wheel: Node2D = $VehicleSprite/frontRightTireSprite

var vehicleSpecs: VehicleData
var vehicleType: String
var vehicleNumber: int
var vehicleID: String
var parked: bool = false

var drivetrain: DrivetrainComponent
var attachments: AttachmentComponent
var cargo: CargoComponent
var movement: PathMovementComponent

func _ready():
	vehicleID = vehicleType + "*" + str(vehicleNumber)
	VehicleManager.register_vehicle(vehicleID, self)
	
	vehicleSpecs = Global.getVehicleProperties(vehicleType)
	if vehicleSpecs == null:
		push_error("Vehicle type is not supported " + str(vehicleType))
		return
		
	_setup_sprite()
	_build_components()
	
	if pathDebugLine:
		pathDebugLine.top_level = true
		pathDebugLine.global_position = Vector2.ZERO
		pathDebugLine.global_rotation = false
	
	if has_node("DayNightLayer"):
		get_node("DayNightLayer").game_second_passed.connect(_on_second_passed)

func _setup_sprite() -> void:
	if vehicleType != "valtra_s416" and vehicleType != "ford_7810":
		front_left_wheel.visible = false
		front_right_wheel.visible = false
	if animatedSprite.sprite_frames.has_animation(vehicleType):
		animatedSprite.play(vehicleType)

func _build_components() -> void:
	if vehicleSpecs.attachments != null:
		attachments = AttachmentComponent.new()
		add_child(attachments)
		attachments.setup(self, vehicleSpecs.attachments)
	
	if vehicleSpecs.engine != null:
		drivetrain = DrivetrainComponent.new()
		add_child(drivetrain)
		drivetrain.setup(self, vehicleSpecs.engine, vehicleSpecs.mobility, vehicleSpecs.durability)
		drivetrain.out_of_fuel.connect(_on_out_of_fuel)
	
	if vehicleSpecs.transport != null:
		cargo = CargoComponent.new()
		add_child(cargo)
		cargo.setup(self, vehicleSpecs.transport)
	
	if vehicleSpecs.dimensions != null and drivetrain != null:
		movement = PathMovementComponent.new()
		add_child(movement)
		movement.setup(self, vehicleSpecs.dimensions, vehicleSpecs.mobility, drivetrain, attachments, front_left_wheel, front_right_wheel, pathDebugLine)

func get_total_weight() -> float:
	var weight := float(vehicleSpecs.mobility.weight)
	if cargo:
		weight += cargo.cargoWeight
	if attachments:
		weight += attachments.pullingWeight
	return weight

func _physics_process(delta: float) -> void:
	if movement:
		movement.tick(delta * Global.speed_tier)

func _on_second_passed() -> void:
	if drivetrain and not parked:
		drivetrain.tick_fuel()

func _on_out_of_fuel() -> void:
	velocity = Vector2.ZERO
	if movement:
		movement.current_speed = 0.0
	print(vehicleType + "_", vehicleNumber, " is out of fuel!")

func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		var clickable_rect = Rect2(Vector2(-12, -6.5), Vector2(24, 13))
		if clickable_rect.has_point(get_local_mouse_position()):
			Global.vehicle_ui_selected = vehicleID
			Global.set_deferred("vehicle_ui_open", true)
