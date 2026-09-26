extends Panel

const VEHICLE_TILE_SCENE = preload("res://scenes/UI/vehicleTile.tscn")
@onready var vehicle_container: VBoxContainer = $ScrollContainer/VBoxContainer

func _process(_delta):
	if not Global.baseBuild:
		visible = false

func refresh_garage_ui() -> void:
	for child in vehicle_container.get_children():
		child.queue_free()

	for vehicle_type in Global.garage.keys():
		var amount := int(Global.garage[vehicle_type].size())
		for i in range(amount):
			var new_card = VEHICLE_TILE_SCENE.instantiate()
			new_card.VehicleInstance = Global.garage[vehicle_type][i]
			vehicle_container.add_child(new_card)
