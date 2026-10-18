extends Control

@onready var save_button: Button = $VBoxContainer/save

func _on_play_pressed():
	get_tree().paused = false
	self.visible = false

func _on_save_pressed():
	Save.save(Global.loaded_save_index)
	save_button.text = "Saved!"

func _on_exit_pressed():
	get_tree().change_scene_to_file("res://scenes/UI/main_menu.tscn")
