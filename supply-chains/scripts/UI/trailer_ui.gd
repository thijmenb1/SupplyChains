extends Panel

const VEHICLE_TILE_SCENE = preload("res://scenes/UI/vehicleTile.tscn")
@onready var vbox: VBoxContainer = $ScrollContainer/VBoxContainer

@export var TowingVehicle: VehicleBody

var first_refresh: bool = false

func _ready():
	refresh_trailer_ui()

func refresh_trailer_ui():
	for child in vbox.get_children():
		child.queue_free()
	
	for instance in Global.garage["flatbedTrailer"]:
		var new_card = VEHICLE_TILE_SCENE.instantiate()
		new_card.VehicleInstance = instance
		new_card.TowingVehicle = TowingVehicle
		print(new_card.TowingVehicle)
		vbox.add_child(new_card)
	
	for instance in Global.garage["bulkTrailer"]:
		var new_card = VEHICLE_TILE_SCENE.instantiate()
		new_card.VehicleInstance = instance
		new_card.TowingVehicle = TowingVehicle
		print(new_card.TowingVehicle)
		vbox.add_child(new_card)
