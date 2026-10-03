extends Panel

@onready var VehicleSpriteFile = preload("res://assets/Vehicles/trucks_topdown_spritesheet.png")
@onready var TrailerSpriteFile = preload("res://assets/Vehicles/trailers_topdown_spritesheet.png")

@onready var VehicleSprite: TextureRect = $VehicleSprite
@onready var VehicleNameLabel: Label = $VehicleName
@onready var VehicleCoordsText: Label = $CoordsText

@export var VehicleInstance: Node2D
@export var VehicleType: String
@export var VehicleNumber: int

@export var TowingVehicle: VehicleBody
@onready var trailerUI: Panel

var vehicleSpecs: VehicleData

func _ready() -> void:
	if VehicleInstance != null:
		VehicleType = VehicleInstance.vehicleType
		VehicleNumber = VehicleInstance.vehicleNumber
	
	vehicleSpecs = Global.getVehicleProperties(VehicleType)
	
	var isTrailer = bool(vehicleSpecs.general.is_trailer)

	var atlas_texture := AtlasTexture.new()
	if isTrailer:
		atlas_texture.atlas = TrailerSpriteFile
	else:
		atlas_texture.atlas = VehicleSpriteFile
	atlas_texture.region = vehicleSpecs.general.atlas_region
	VehicleSprite.texture = atlas_texture

	if VehicleNumber != null and VehicleNumber > 1:
		VehicleNameLabel.text = vehicleSpecs.general.vehicle_name + " " + str(VehicleNumber)
	else:
		VehicleNameLabel.text = vehicleSpecs.general.vehicle_name

func _process(_delta):
	if VehicleInstance != null:
		var x := int(VehicleInstance.global_position.x / 32)
		var y := int(VehicleInstance.global_position.y / 32)
		VehicleCoordsText.text = "coords:\n(" + str(x) + "," + str(y) + ")"
	else:
		VehicleCoordsText.visible = false

func _on_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		if TowingVehicle != null:
			trailerUI = $"../../.."
			var trailer_id: String = VehicleType + "*" + str(VehicleNumber)
			var trailer_node = VehicleManager.get_vehicle(trailer_id)
			
			if trailer_node and TowingVehicle.rearAttatchmentPoint:
				trailer_node.couple_to(TowingVehicle, TowingVehicle.rearAttatchmentPoint)
				trailerUI.visible = false
				Global.show_popup("Successfully coupled %s to %s" % [trailer_id, TowingVehicle.vehicleID])
			else:
				Global.show_popup("Coupling failed")
		else:
			if VehicleInstance != null:
				VehicleManager.focus_on_vehilce(VehicleType + "*" + str(VehicleNumber))
