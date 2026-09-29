extends Control

@onready var save1_name: Label = $CenterContainer/VBoxContainer/Save1/ContainsSave/SaveName
@onready var save1_saveDate: Label = $"CenterContainer/VBoxContainer/Save1/ContainsSave/SaveDate and time"
@onready var save1_moneyInSave: Label = $CenterContainer/VBoxContainer/Save1/ContainsSave/moneyInSave
@onready var save1_TimeInSave: Label = $CenterContainer/VBoxContainer/Save1/ContainsSave/TimeInSave
@onready var save1_new: Control = $CenterContainer/VBoxContainer/Save1/Create_new
@onready var save1_has: Control = $CenterContainer/VBoxContainer/Save1/ContainsSave

var save1_hasSave: bool

@onready var save2_name: Label = $CenterContainer/VBoxContainer/Save2/ContainsSave/SaveName
@onready var save2_saveDate: Label = $"CenterContainer/VBoxContainer/Save2/ContainsSave/SaveDate and time"
@onready var save2_moneyInSave: Label = $CenterContainer/VBoxContainer/Save2/ContainsSave/moneyInSave
@onready var save2_TimeInSave: Label = $CenterContainer/VBoxContainer/Save2/ContainsSave/TimeInSave
@onready var save2_new: Control = $CenterContainer/VBoxContainer/Save2/Create_new
@onready var save2_has: Control = $CenterContainer/VBoxContainer/Save2/ContainsSave

var save2_hasSave: bool

@onready var save3_name: Label = $CenterContainer/VBoxContainer/Save3/ContainsSave/SaveName
@onready var save3_saveDate: Label = $"CenterContainer/VBoxContainer/Save3/ContainsSave/SaveDate and time"
@onready var save3_moneyInSave: Label = $CenterContainer/VBoxContainer/Save3/ContainsSave/moneyInSave
@onready var save3_TimeInSave: Label = $CenterContainer/VBoxContainer/Save3/ContainsSave/TimeInSave
@onready var save3_new: Control = $CenterContainer/VBoxContainer/Save3/Create_new
@onready var save3_has: Control = $CenterContainer/VBoxContainer/Save3/ContainsSave

var save3_hasSave: bool

func _ready():
	Save.refresh_slots()
	_populate_slot(0, save1_has, save1_new, save1_name, save1_saveDate, save1_moneyInSave, save1_TimeInSave)
	_populate_slot(1, save2_has, save2_new, save2_name, save2_saveDate, save2_moneyInSave, save2_TimeInSave)
	_populate_slot(2, save3_has, save3_new, save3_name, save3_saveDate, save3_moneyInSave, save3_TimeInSave)

func _populate_slot(slot: int, has_box: Control, new_box: Control, name_l: Label, date_l: Label, money_l: Label, time_l: Label) -> void:
	var info: Dictionary = Global.save_games[slot]
	var has_save: bool = info.get("exists", false)
	has_box.visible = has_save
	new_box.visible = not has_save
	if not has_save:
		return
	name_l.text = info.get("name", "Slot %d" % (slot + 1))
	date_l.text = _format_real_date(info.get("timestamp", 0))
	money_l.text = "$%.0f" % info.get("money", 0.0)
	time_l.text = "%s %d - %02d:%02d" % [info.get("month", ""), info.get("year", -2026) + 2026, info.get("hour", 0), info.get("minute", 0)]


func _handle_slot_click(event: InputEvent, slot: int) -> void:
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		if Global.save_games[slot].get("exists", false):
			Save.load_game(slot)
		else:
			Save.start_new_game(slot)

func _on_save_1_gui_input(event):
	_handle_slot_click(event, 0)

func _on_save_2_gui_input(event):
	_handle_slot_click(event, 1)

func _on_save_3_gui_input(event):
	_handle_slot_click(event, 2)

func _format_real_date(unix_time: int) -> String:
	var bias_minutes: int = Time.get_time_zone_from_system().get("bias", 0)
	var dt: Dictionary = Time.get_datetime_dict_from_unix_time(unix_time + bias_minutes * 60)
	return "%02d-%02d-%04d  %02d:%02d" % [dt.day, dt.month, dt.year, dt.hour, dt.minute]
