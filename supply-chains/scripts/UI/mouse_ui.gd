extends Panel

@onready var Label_entety : RichTextLabel = $RichTextLabel
@onready var uiMain: CanvasLayer = $".."

var height: float = 50.0
var hoverSubject: String
var TextString: String
var selectedVehicle: String
var selectedVehicleStats: VehicleData

func _ready() -> void:
	visible = false
	global_position = get_global_mouse_position() - Vector2(0, height)

func _input(event) -> void:
	if event is InputEventMouseMotion:
		global_position = get_global_mouse_position() - Vector2(0, height)

func _process(_delta):
	size = Label_entety.size + Vector2(10, 10)
	height = size.y
	Label_entety.text = TextString
	if selectedVehicle != uiMain.selectedVehicle && selectedVehicle != null:
		selectedVehicle = uiMain.selectedVehicle
		selectedVehicleStats = Global.getVehicleProperties(selectedVehicle)
	update_text()
	
func update_text() -> void:
	if selectedVehicle == "":
		return
	
	if not selectedVehicleStats.general.is_trailer:
		match hoverSubject:
			"stat_1":
				TextString = "[b]Engine[/b]" + "\n   Torque: " + str(selectedVehicleStats.engine.torque) + " nm"
			
			"stat_2":
				TextString = "[b]Fuel[/b]" + "\n   Fuel type: " + str(selectedVehicleStats.engine.fuel_type) + "\n   Fuel consumption: " + str(selectedVehicleStats.engine.fuel_consumption) + " L/h"
			
			"stat_3":
				TextString = "[b]Mobility[/b]" + "\n   Turning radius: " + str(selectedVehicleStats.mobility.turning_radius) + " m" + "\n   Traction: " + str(int(selectedVehicleStats.mobility.traction * 100)) + "%" + "\n   Drivetrain: " + str(selectedVehicleStats.mobility.drive_type)
			
			"stat_4":
				if selectedVehicleStats.transport != null:
					TextString = "[b]Transport[/b]" + "\n   Cargo type: " + str(selectedVehicleStats.transport.cargoType) + "\n   Max cargo weigth: " + str(selectedVehicleStats.transport.cargo_weight_capacity) + " kg"
				elif selectedVehicleStats.equipment != null:
					match selectedVehicleStats.general.vehicle_class:
						"Crawler Dozer":
							TextString = "[b]Equipment[/b]" + "\n   Blade capacity: " + str(selectedVehicleStats.equipment.capacity) + " m³"
						"Heavy crawler Excavator":
							TextString = "[b]Equipment[/b]"
						"Asphalt Paver":
							TextString = "[b]Equipment[/b]" + "\n   Hopper capacity: " + str(selectedVehicleStats.equipment.capacity) + " m³" + "\n   Min paving width: " + str(selectedVehicleStats.equipment.min_working_width) + " m" + "\n   Max paving width: " + str(selectedVehicleStats.equipment.max_working_width) + " m" + "\n   Work speed: " + str(selectedVehicleStats.equipment.working_speed) + " km/h"
						_:
							push_error(selectedVehicleStats.general.vehicle_class, " is not recoginzed")
				else:
					TextString = "[b]Connections[/b]"
					if selectedVehicleStats.attachments.front_3point:
						TextString += "\n   Front 3-point:" + "\n      Max lift capacity: " + str(selectedVehicleStats.attachments.front_3point_lift_capacity) + " kg" + "\n      Max PTO horsepower: " + str(selectedVehicleStats.attachments.front_pto_power) + " hp"
					if selectedVehicleStats.attachments.rear_3point:
						TextString += "\n   rear 3-point:" + "\n      Max lift capacity: " + str(selectedVehicleStats.attachments.rear_3point_lift_capacity) + " kg" + "\n      Max PTO horsepower: " + str(selectedVehicleStats.attachments.rear_pto_power) + " hp"
					if selectedVehicleStats.attachments.hitch_type == Global.HitchType.PIN:
						TextString += "\n   rear hitch:" + "\n      Hitch type: " + str(selectedVehicleStats.attachments.hitch_type) + "\n   Max towing weight: " + str(selectedVehicleStats.attachments.max_tow_weight) + " kg" + "\n   Hydraulic flow: " + str(selectedVehicleStats.attachments.hydraulic_flow) + " L/min"
			
			"stat_5":
				TextString = "[b]Weigth[/b]" + "\n   Contact area: " + str(selectedVehicleStats.mobility.contact_area) + " m²" + "\n   Offroad capability: " + str(int(selectedVehicleStats.mobility.offroad_capability * 100)) + " %"
	else:
		if hoverSubject == "stat_2":
			TextString = "[b]Transport[/b]" + "\n   Cargo type: " + str(selectedVehicleStats.transport.cargoType) + "\n   Max cargo weigth: " + str(selectedVehicleStats.transport.cargo_weight_capacity) + " kg"
		else:
			TextString = "[b]Weigth[/b]"


func _on_stat_card_mouse_entered():
	visible = true
	hoverSubject = "stat_1"

func _on_stat_card_mouse_exited():
	visible = false

func _on_stat_card_2_mouse_entered():
	visible = true
	hoverSubject = "stat_2"

func _on_stat_card_2_mouse_exited():
	visible = false

func _on_stat_card_3_mouse_entered():
	visible = true
	hoverSubject = "stat_3"

func _on_stat_card_3_mouse_exited():
	visible = false

func _on_stat_card_4_mouse_entered():
	visible = true
	hoverSubject = "stat_4"

func _on_stat_card_4_mouse_exited():
	visible = false

func _on_stat_card_5_mouse_entered():
	visible = true
	hoverSubject = "stat_5"

func _on_stat_card_5_mouse_exited():
	visible = false
