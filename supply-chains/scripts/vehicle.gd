extends CharacterBody2D

@onready var navAgent: NavigationAgent2D = $NavigationAgent2D
@onready var animatedSprite: AnimatedSprite2D = $VehicleSprite

@export var speed: float = 150.0
var vehicleType: String
var cargo: Dictionary

var targetTile: Vector2i
var targetCoords: Vector2i = targetTile * 32
var firstLocationTile: Vector2i
var SecondLocationTile: Vector2i

func _ready():
	setVehicleSprite()

func setVehicleSprite() -> void:
	if animatedSprite.sprite_frames.has_animation(vehicleType):
		animatedSprite.play(vehicleType)

func _physics_process(delta) -> void:
	if navAgent.is_navigation_finished():
		handelArival()
	
	var next_path_pos: Vector2 = navAgent.get_next_path_position()
	var dir: Vector2 = global_position.direction_to(next_path_pos)
	
	velocity = dir * speed
	rotation = lerp_angle(rotation, dir.angle(), 12 * delta)
	
	move_and_slide()

func handelArival() -> void:
	if targetTile == firstLocationTile:
		targetTile = SecondLocationTile
	elif targetTile == SecondLocationTile:
		targetTile = firstLocationTile
	else:
		print("Error: something wong with nav coords")
