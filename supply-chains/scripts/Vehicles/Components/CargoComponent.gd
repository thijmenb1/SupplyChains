extends Node
class_name  CargoComponent

signal cargo_changed

var vehicle: CharacterBody2D
var transport: VehicleTransport
var cargo: Dictionary = {}
var cargoWeight: float = 0.0
var cargoVolume: float = 0.0

const CARGO_FRAMES: Dictionary = {
	"flatbedTruck": {
		"ironBeam": 1,
		"copperWire": 2,
		"goldBars": 3,
		"copperIngots": 4,
		"steelBeam": 5,
		"goldWire": 6,
		"crudeOil": 7,
		"fuel": 8,
	},
	"flatbedTrailer": {
		"ironBeam": 1,
		"copperWire": 2,
		"goldBars": 3,
		"copperIngots": 4,
		"steelBeam": 5,
		"goldWire": 6,
		"crudeOil": 7,
		"fuel": 8,
	},
	"bulkTrailer": {
		"ironOre": 1,
		"copperOre": 2,
		"goldOre": 3,
		"asphalt": 4,
		"coal": 5,
		"gravel": 6,
		"sand": 7
	}
}

func setup(t_vehicle: CharacterBody2D, t_transport: VehicleTransport) -> void:
	vehicle = t_vehicle
	transport = t_transport

func load_vehicle(loading_cargo: String, quantity: float) -> void:
	var cargo_props: Dictionary = Global.getCargoProperties(loading_cargo)
	if cargo_props.get("type") != transport.cargoType:
		Global.show_popup(loading_cargo + " is not supported for type " + str(transport.cargoType))
		return
	
	var added_weight: float = cargo_props.get("Weight", 0) * quantity
	if cargoWeight + added_weight <= transport.cargo_weight_capacity and cargoVolume + quantity <= transport.cargo_volume_capacity:
		cargoWeight += added_weight
		cargoVolume += quantity
		cargo[loading_cargo] = cargo.get(loading_cargo, 0.0) + quantity
		cargo_changed.emit()
	elif cargoVolume + quantity > transport.cargo_volume_capacity:
		Global.show_popup("No space left for this cargo")
	else:
		Global.show_popup("The cargo is too heavy to load")

func unload_vehicle(unloading_cargo: String, quantity: float) -> void:
	var cargo_props: Dictionary = Global.getCargoProperties(unloading_cargo)
	if cargo.get(unloading_cargo, 0) > 0:
		cargo[unloading_cargo] -= quantity
		cargoWeight -= cargo_props.get("Weight", 0) * quantity
		cargoVolume -= quantity
		cargo_changed.emit()
	else:
		Global.show_popup("The vehicle doesn't contain: " + unloading_cargo)

func get_primary_cargo() -> String:
	var best: String = ""
	var best_qty: float = 0.001
	for cargo_name in cargo:
		if cargo[cargo_name] > best_qty:
			best = cargo_name
			best_qty = cargo[cargo_name]
	return best

func get_sprite_frame(vehicle_type: String) -> int:
	var frames: Dictionary = CARGO_FRAMES.get(vehicle_type, {})
	return frames.get(get_primary_cargo(), 0)
