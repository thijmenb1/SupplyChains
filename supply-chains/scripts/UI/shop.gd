extends Control

@onready var uiMain : CanvasLayer = $"../../.."

@onready var VEHICLE_ATLAS = preload("res://assets/Vehicles/Vehicle_atlas.tres")
@onready var TRAILER_ATLAS = preload("res://assets/Vehicles/Trailer_atlas.tres")
@onready var ABILITY_ATLAS = preload("res://assets/ability_icons.png")

@onready var VehicleImg: TextureRect = $VehicleImg/TextureRect
@onready var VehicleNameLabel: Label = $"Vehicle name"
@onready var VehicleDescriptionLabel: Label = $VehicleDescription

@onready var stat_card: Panel = $"VboxContainer/stat card"
@onready var stat_card_label: Label = $"VboxContainer/stat card/Label"
@onready var stat_card_texture: TextureRect = $"VboxContainer/stat card/TextureRect"
@onready var stat_card_2: Panel = $"VboxContainer/stat card2"
@onready var stat_card_label_2: Label = $"VboxContainer/stat card2/Label"
@onready var stat_card_texture_2: TextureRect = $"VboxContainer/stat card2/TextureRect"
@onready var stat_card_3: Panel = $"VboxContainer/stat card3"
@onready var stat_card_label_3: Label = $"VboxContainer/stat card3/Label"
@onready var stat_card_texture_3: TextureRect = $"VboxContainer/stat card3/TextureRect"
@onready var stat_card_4: Panel = $"VboxContainer/stat card4"
@onready var stat_card_label_4: Label = $"VboxContainer/stat card4/Label"
@onready var stat_card_texture_4: TextureRect = $"VboxContainer/stat card4/TextureRect"
@onready var stat_card_5: Panel = $"VboxContainer/stat card5"
@onready var stat_card_label_5: Label = $"VboxContainer/stat card5/Label"
@onready var stat_card_texture_5: TextureRect = $"VboxContainer/stat card5/TextureRect"

@onready var gargage: Panel = $"../../GargeUi"
@onready var trailerUI: Panel = $"../../VehicleUI/TrailerUI"

var selectedVehicle: String
var VehicleName: String
var VehicleDescription: String
var isTrailer: bool
var vehicleValues: VehicleData
	
func _process(_delta):
	if selectedVehicle != uiMain.selectedVehicle:
		selectedVehicle = uiMain.selectedVehicle
		vehicleValues = Global.getVehicleProperties(selectedVehicle)
		setVehiclePorperties()
		updateVisuals()

func setVehiclePorperties():
	VehicleName = vehicleValues.general.vehicle_name
	VehicleDescription = vehicleValues.general.vehicle_description
	isTrailer = vehicleValues.general.is_trailer
	if isTrailer:
		TRAILER_ATLAS.region = vehicleValues.general.atlas_region
	else:
		VEHICLE_ATLAS.region = vehicleValues.general.atlas_region


func updateVisuals():
	VehicleNameLabel.text = VehicleName
	VehicleDescriptionLabel.text = VehicleDescription
	_seting_stat_card()
	if isTrailer:
		VehicleImg.texture = TRAILER_ATLAS
	else:
		VehicleImg.texture = VEHICLE_ATLAS

func _seting_stat_card():
	if not isTrailer:
		stat_card.visible = true
		stat_card_2.visible = true
		stat_card_3.visible = true
		stat_card_4.visible = true
		stat_card_5.visible = true
		
		
		stat_card_label.text = str(vehicleValues.engine.horse_power) + " hp"
		stat_card_texture.texture = load("res://assets/UI/Icons/engineIcon.png")
		
		stat_card_label_2.text = str(vehicleValues.engine.fuel_capacity) + " L"
		stat_card_texture_2.texture = load("res://assets/UI/Icons/gas-pump-solid.png")
		
		stat_card_label_3.text = str(vehicleValues.mobility.vehicle_speed) + " kmh"
		stat_card_texture_3.texture = load("res://assets/UI/Icons/gauge-high-solid.png")
		
		if vehicleValues.transport != null:
			stat_card_label_4.text = str(vehicleValues.transport.cargo_volume_capacity) + " pallets" if vehicleValues.transport.cargoType == "Flatbed" else str(vehicleValues.transport.cargo_volume_capacity) + " m³" 
			stat_card_texture_4.texture = load("res://assets/UI/Icons/cubes-solid.png")
		elif vehicleValues.equipment != null:
			stat_card_label_4.text = str(vehicleValues.equipment.working_width) + " m" if vehicleValues.general.vehicle_class == "Crawler Dozer" or vehicleValues.general.vehicle_class == "Asphalt Paver" else str(vehicleValues.equipment.capacity) + " m³"
			stat_card_texture_4.texture = load("res://assets/UI/Icons/workingWidth.png") if vehicleValues.general.vehicle_class == "Crawler Dozer" or vehicleValues.general.vehicle_class == "Asphalt Paver" else load("res://assets/UI/Icons/cubes-solid.png")
		else:
			stat_card_label_4.text = str(vehicleValues.attachments.connection_count) + " points"
			stat_card_texture_4.texture = load("res://assets/UI/Icons/linkIcon.png")
			
		stat_card_label_5.text = str(vehicleValues.mobility.weight) + " kg"
		stat_card_texture_5.texture = load("res://assets/UI/Icons/weight-hanging-solid.png")
		
	else:
		stat_card_label.text = str(vehicleValues.mobility.weight) + " kg"
		stat_card_texture.texture = load("res://assets/UI/Icons/weight-hanging-solid.png")
		stat_card_label_2.text = str(vehicleValues.transport.cargo_volume_capacity) + " pallets" if vehicleValues.transport.cargoType == "Flatbed" else str(vehicleValues.transport.cargo_volume_capacity) + " m³" 
		stat_card_texture_2.texture = load("res://assets/UI/Icons/cubes-solid.png")
		stat_card_3.visible = false
		stat_card_4.visible = false
		stat_card_5.visible = false

func _on_buy_button_pressed():
	if not Global.has_free_base_tile():
		return
	
	if Global.pay_cost(60.0):
		Global.spawn_vehicle(selectedVehicle)
		gargage.refresh_garage_ui()
		trailerUI.refresh_trailer_ui()

func _on_finance_button_pressed():
	if not Global.has_free_base_tile():
		return
	
	if Global.addDebt(60.0):
		Global.spawn_vehicle(selectedVehicle)
		gargage.refresh_garage_ui()
		trailerUI.refresh_trailer_ui()
