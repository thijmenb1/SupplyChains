extends Node
class_name DrivetrainComponent

const PIXEL_PER_METER: float = 5.0

signal out_of_fuel

var vehicle: VehicleBody
var engine: VehicleEngine
var mobility: VehicleMobility

var health: int
var fuel: float
var totalHorsePower: float
var engineLoad: int

func setup(t_vehicle: VehicleBody, t_engine: VehicleEngine, t_mobility: VehicleMobility, t_durability: VehicleDurability) -> void:
	vehicle = t_vehicle
	engine = t_engine
	mobility = t_mobility
	health = t_durability.health
	fuel = float(engine.fuel_capacity)
	totalHorsePower = float(engine.horse_power)

func get_calculated_speed() -> float:
	var total_weight: float = vehicle.get_total_weight()
	var raw_speed = mobility.vehicle_speed
	var kmh_speed := float(raw_speed) if raw_speed != null else 20.0
	var power_to_weigth: float = totalHorsePower / max(total_weight, 1.0)
	var standard_ratio: float = totalHorsePower / float(mobility.weight)
	var load_preformance_factor: float = clamp(power_to_weigth / standard_ratio, 0.25, 1.0)
	var adjusted_kmh: float = kmh_speed * load_preformance_factor
	return (adjusted_kmh * (1.0 / 3.6)) * PIXEL_PER_METER

func get_acceleration_rate() -> float:
	var total_weigth: float = vehicle.get_total_weight()
	var traction := float(mobility.traction)
	var power_ratio: float = totalHorsePower / max(total_weigth, 1.0)
	const ACCEL_SCALLER: float = 8000.0
	return max(power_ratio * traction * ACCEL_SCALLER, 150.0)

func get_braking_rate() -> float:
	return clamp(200000.0 / max(vehicle.get_total_weight(), 1.0), 50.0, 400.0)

func update_engine_load() -> void:
	if engine.is_empty():
		engineLoad = 0
		return
	
	var total_weight: float = vehicle.get_total_weight()
	var empty_weight := float(mobility.weight)
	var weight_ratio: float = total_weight / empty_weight
	var speed_ratio: float = vehicle.velocity.length() / max(get_calculated_speed(), 1.0)
	var movement_load_factor: float = lerp(0.1, 0.85, speed_ratio)
	var required_hp: float = (totalHorsePower * movement_load_factor) * clamp(weight_ratio, 1.0, 2.5)
	engineLoad = int(clamp(required_hp, totalHorsePower * 0.1, totalHorsePower))

func tick_fuel() -> void:
	if vehicle.velocity.length() < 1.0 or engine.is_empty():
		return
	
	update_engine_load()
	var fuel_per_hour: float = float(engine.fuel_consumption)
	var load_factor: float = 0.3 + ( 0.7 * (float(engineLoad) / totalHorsePower))
	var fuel_per_second: float = (fuel_per_hour / 3600.0) * load_factor
	fuel = max(0.0, fuel - fuel_per_second)
	if fuel <= 0.0:
		out_of_fuel.emit()
