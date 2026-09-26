extends Node

var vehicles: Dictionary = {}
@onready var camera: Camera2D

func _ready() -> void:
	print("VehicleManager loaded")

func register_vehicle(vehicle_id: String, Vehicle: Node2D) -> void:
	vehicles[vehicle_id] = Vehicle
	print("Registered vehicle: ", vehicle_id)

func get_vehicle(vehicle_id: String) -> Node2D:
	if vehicles.has(vehicle_id):
		return vehicles[vehicle_id]
	push_error("Vehicle not found: ", vehicle_id)
	return null

func get_vehicle_specs(vehicle_id: String) -> Dictionary:
	var Vehicle = get_vehicle(vehicle_id)
	if not Vehicle:
		return {}
	
	var specs: Dictionary = {"totalWeight": Vehicle.get_total_weight()}
	if Vehicle.drivetrain:
		specs["fuel"] = Vehicle.drivetrain.fuel
		specs["healt"] = Vehicle.drivetrain.health
		specs["engineLoad"] = Vehicle.drivetrain.engineLoad
	if Vehicle.cargo:
		specs["cargo"] = Vehicle.cargo.cargo
		specs["cargoWeight"] = Vehicle.cargo.cargoWeight
		specs["cargoVolume"] = Vehicle.cargo.cargoVolume
	if Vehicle.attachments:
		specs["attachmentPoints"] = Vehicle.attachments.points
		specs["pullingWeight"] = Vehicle.attachments.pullingWeight
	specs["totalWeigth"] = Vehicle.get_total_weight()
	return specs

func remove_vehicle(vehicle_id: String) -> void:
	vehicles.erase(vehicle_id)

func get_all_vehicles() -> Dictionary:
	return vehicles.duplicate()

func focus_on_vehilce(vehicle_id: String) -> void:
	var Vehicle = get_vehicle(vehicle_id)
	if Vehicle != null:
		var Vehilce_coords = Vehicle.global_position
		camera.following = true
		camera.follow_vehilce(Vehilce_coords)
	else:
		print(vehicle_id)
	
	
