extends Node
class_name  CargoComponent

var vehicle: CharacterBody2D
var transport: VehicleTransport
var cargo: Dictionary = {}
var cargoWeight: float = 0.0
var cargoVolume: float = 0.0

func setup(t_vehicle: CharacterBody2D, t_transport: VehicleTransport) -> void:
	vehicle = t_vehicle
	transport = t_transport

func load_vehicle(loading_cargo: String, quantity: float) -> void:
	var cargo_props: Dictionary = Global.getCargoProperties(loading_cargo)
	if cargo_props.get("type") != transport.cargoType:
		print(loading_cargo + " is not supported for type " + str(transport.cargoType))
		return
	
	var added_weight: float = cargo_props.get("Weight", 0) * quantity
	if cargoWeight + added_weight <= transport.cargo_weight_capacity and cargoVolume + quantity <= transport.cargo_volume_capacity:
		cargoWeight += added_weight
		cargoVolume += quantity
		cargo[loading_cargo] = cargo.get(loading_cargo, 0.0) + quantity
	elif cargoVolume + quantity > transport.cargo_volume_capacity:
		print("No space left for this cargo")
	else:
		print("The cargo is too heavy to load")

func unload_vehicle(unloading_cargo: String, quantity: float) -> void:
	var cargo_props: Dictionary = Global.getCargoProperties(unloading_cargo)
	if cargo.get(unloading_cargo, 0) > 0:
		cargo[unloading_cargo] -= quantity
		cargoWeight -= cargo_props.get("Weight", 0) * quantity
		cargoVolume -= quantity
	else:
		print("The vehicle doesnt contain: " + unloading_cargo)
