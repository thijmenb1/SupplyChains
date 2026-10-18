extends Control

@export var speed: float = 30.0
@export var pause_time: float = 2.0

@onready var label: Label = $Label

var _dir: int = 1
var _wait: float = 0.0

func _ready() -> void:
	clip_contents = true

func _process(delta: float) -> void:
	label.size = label.get_minimum_size()
	var overflow: float = label.size.x - size.x
	if overflow <= 0.0:
		label.position.x = 0.0
		return
	if _wait > 0.0:
		_wait -= delta
		return
	label.position.x -= speed * delta * _dir
	if label.position.x <= -overflow:
		label.position.x = -overflow
		_dir = -1
		_wait = pause_time
	elif label.position.x >= 0.0:
		label.position.x = 0.0
		_dir = 1
		_wait = pause_time
