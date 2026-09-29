extends Control

@onready var progress_bar: ProgressBar = $CenterContainer/VBoxContainer/ProgressBar
@onready var label: Label = $CenterContainer/VBoxContainer/Label

var _loading: bool = false

func  _ready() -> void:
	progress_bar.max_value = 100
	progress_bar.value = 0
	ResourceLoader.load_threaded_request(Save.LEVEL_SCENE)
	_loading = true

func _process(delta) -> void:
	if not _loading:
		return
	var progress: Array = []
	var status := ResourceLoader.load_threaded_get_status(Save.LEVEL_SCENE, progress)
	match status:
		ResourceLoader.THREAD_LOAD_IN_PROGRESS:
			if progress.size() > 0:
				progress_bar.value = progress[0] * 100.0
		ResourceLoader.THREAD_LOAD_LOADED:
			_loading = false
			label.text = "Generationg world..."
			await get_tree().process_frame
			var scene: PackedScene = ResourceLoader.load_threaded_get(Save.LEVEL_SCENE)
			get_tree().change_scene_to_packed(scene)
		ResourceLoader.THREAD_LOAD_FAILED, ResourceLoader.THREAD_LOAD_INVALID_RESOURCE:
			_loading = false
			label.text = "Failed to load level."
			push_error("Threaded load failed for: " + Save.LEVEL_SCENE)
