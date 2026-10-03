extends Panel

@onready var NameLabel: Label = $Label
@onready var progressbar1: ProgressBar = $ProgressBar
@onready var progressText1: Label = $ProgressBar/ProgressText
@onready var progressWeigth1: Panel = $ProgressBar/Panel
@onready var progressbar2: ProgressBar = $ProgressBar2
@onready var progressText2: Label = $ProgressBar2/ProgressText
@onready var progressWeigth2: Panel = $ProgressBar2/Panel

@onready var FuelText: Label = $FuelIcon/Label
@onready var WeightText: Label = $WeightIcon/Label
@onready var HealthText: Label = $HealthIcon/Label
@onready var EngineText: Label = $EngineLoadIcon/Label

@onready var TrailerUI: Panel = $TrailerUI
@onready var TrailerUIButton: Panel = $CopleTrailer
@onready var RouteUI: Panel = $RouteUI

func _process(_delta: float) -> void:
	if Global.vehicle_ui_selected == "":
		return
	
	self.visible = Global.vehicle_ui_open
	
	var vehicle_split = Global.vehicle_ui_selected.split("*")
	var vehicle_type = vehicle_split[0]
	var _vehicle_number = vehicle_split[1]
	var vehicle_specs: VehicleData = Global.getVehicleProperties(vehicle_type)
	var vehicle_values: Dictionary = VehicleManager.get_vehicle_specs(Global.vehicle_ui_selected)
		
	NameLabel.text = vehicle_specs.general.vehicle_name
		
	var points: Dictionary = vehicle_values.get("attachmentPoints", {})

	var can_have_trailer: bool = points.has("hitch")
	TrailerUIButton.visible = can_have_trailer

	var attatchment = null
	for key in ["hitch", "front3Point", "rear3Point"]:
		if points.get(key, null) != null:
			attatchment = points[key]
			break

	if attatchment != null:
		progressbar2.visible = true
		var att_specs: VehicleData = Global.getVehicleProperties(attatchment.vehicleType)
		progressbar2.max_value = att_specs.transport.cargo_volume_capacity
		progressbar2.value = attatchment.cargo.cargoVolume
		progressText2.text = str(int(attatchment.cargo.cargoVolume))
		progressWeigth2.visible = attatchment.cargo.cargoWeight >= att_specs.transport.cargo_weight_capacity
	else:
		progressbar2.visible = false
		progressWeigth2.visible = false
	
	progressbar1.position = Vector2(427, 15)
	
	FuelText.text = str(vehicle_values.get("fuel")) + "L"
	WeightText.text = str(int(vehicle_values.get("totalWeigth"))) + "kg"
	HealthText.text = str(vehicle_values.get("healt"))
	EngineText.text = str(vehicle_values.get("engineLoad")) + "/" + str(vehicle_specs.engine.horse_power) + "hp"
		
	if vehicle_specs.transport != null:
		progressbar1.visible = true
		progressbar1.max_value = parse_capacity(vehicle_specs.transport.cargo_volume_capacity)
		progressbar1.value = vehicle_values.get("cargoVolume", 0)
		progressText1.text = str(int(progressbar1.value)) + " / " + str(int(progressbar1.max_value))
		if vehicle_values.get("cargoWeight") >= vehicle_specs.transport.cargo_weight_capacity:
			progressWeigth1.visible = true
		else:
			progressWeigth1.visible = false
	elif vehicle_specs.equipment != null:
		progressbar1.visible = true
		progressbar1.max_value = parse_capacity(vehicle_specs.equipment.capacity)
		progressbar1.value = vehicle_values.get("cargoVolume", 0)
		progressText1.text = str(int(progressbar1.value)) + " / " + str(int(progressbar1.max_value))
		progressWeigth1.visible = false
	else:
		progressbar1.value = 0
		progressText1.text = "n/a"
		progressWeigth1.visible = false

func parse_capacity(value) -> float:
	if value is String:
		if value.is_valid_int():
			return float(int(value))
		return 0.0
	return float(value)


func _on_route_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		var vehicle_type: String = Global.vehicle_ui_selected.split("*")[0]
		if Global.is_excavator(vehicle_type):
			RouteUI.visible = false
			Global.mining_select_vehicle = Global.vehicle_ui_selected
			Global.clickMode = "select_mining_area"
			Global.vehicle_ui_open = false
			return
		RouteUI.visible = !RouteUI.visible
			
func _on_cople_trailer_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		TrailerUI.refresh_trailer_ui()
		var target_vehicle = VehicleManager.get_vehicle(Global.vehicle_ui_selected)
		var hitched_trailer = target_vehicle.attachments.points.get("hitch", null) if target_vehicle else null
		
		if hitched_trailer != null and hitched_trailer.is_coupled:
			hitched_trailer.decouple(get_tree().current_scene)
			TrailerUI.visible = false
		elif TrailerUI.visible:
			TrailerUI.visible = false
		else:
			TrailerUI.TowingVehicle = target_vehicle
			
			for child in TrailerUI.find_children("*", "", true, false):
				if "TowingVehicle" in child:
					child.TowingVehicle = target_vehicle
			
			TrailerUI.visible = true
