extends Node2D

@onready var animatedSprite: AnimatedSprite2D = $AnimatedSprite2D

var head : CharacterBody2D
var hithcDistance: float = 24.0

func _physics_process(delta) -> void:
	if not is_instance_valid(head):
		queue_free()
		return
	
	var head_pos = head.global_position
	var distance = global_position.distance_to(head_pos)
	
	if distance > hithcDistance:
		var dirTo_head
		global_position = head_pos - (dirTo_head * hithcDistance)
		look_at(head_pos)
	
