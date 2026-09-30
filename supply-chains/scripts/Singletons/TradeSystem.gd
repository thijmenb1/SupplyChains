extends Node

const PLANE_SCENE: PackedScene = preload("res://scenes/Vehicles/delivery_plane.tscn")

const NAMES: Array = [
	"James", "Sarah", "Michael", "Emma", "David", "Lisa", "Robert", "Jennifer", "Christopher", "Maria",
	"Daniel", "Amanda", "Matthew", "Sophie", "Joseph", "Rachel", "Andrew", "Elena", "Kevin", "Victoria",
	"Brian", "Jessica", "Ryan", "Lauren", "Jason", "Hannah", "Jacob", "Olivia", "Justin", "Abigail",
	"Brandon", "Grace", "Samuel", "Chloe", "Timothy", "Mia", "Eric", "Charlotte", "Stephen", "Amelia",
	"Paul", "Isabelle", "Mark", "Ella", "Donald", "Harper", "George", "Evelyn", "Kenneth", "Scarlett",
]

const SURENAMES: Array = [
	"Mitchell", "Chen", "Torres", "Johnson", "Brown", "Anderson", "Garcia", "Williams", "Davis", "Rodriguez",
	"Lee", "Martinez", "Thompson", "Taylor", "White", "Green", "Harris", "Jackson", "Martin", "Clark",
	"Lopez", "Allen", "Young", "King", "Scott", "Baker", "Nelson", "Carter", "Hall", "Rivera",
	"Murphy", "Campbell", "Parker", "Edwards", "Evans", "Collins", "Morris", "Rogers", "Jenkins", "Stewart",
	"Peterson", "Sanchez", "Bennett", "Sanders", "Powell", "Long", "Patterson", "Hughes", "Flores",
]

const EASTEREGG_NAME: String = "Thijmen Bruins"

const TRADE_BASE_PRICES: Dictionary = {
	"ironBeam": 18.0,
	"copperIngots": 12.0,
	"goldBars": 36.0,
	"steelBeam": 30.0,
	"copperWire": 30.0,
	"goldWire": 90.0,
	"fuel": 50.0
#	"PCBPallet": 250.0,
}

const MAX_ORDERS := 10
const MIN_ORDERS := 2
const ORDER_GENERATION_PROBEBILITY := 0.05
const MIN_ORDER_LIFETIME := 3600 #in game sec (1h)
const MAX_ORDER_LIFETIME := 86400 #in game sec (1d)
const MIN_QUANTITY := 1
const MAX_QUANTITY := 15

var _next_order_id: int = 0
var _last_minute: int = -1

const PLANE_SPAWN_MIN_DIST := 3000.0
const PLANE_SPAWN_MAX_DIST := 8000.0

func _ready() -> void:
	if Global.trade_orders.size() < MIN_ORDERS:
		generate_order()
		generate_order()

func _process(_delta: float) -> void:
	_expire_old_orders()
	if Global.elapsed_game_seconds / 60 >= _last_minute:
		_last_minute = ceil(Global.elapsed_game_seconds / 60)
		if randf() <= ORDER_GENERATION_PROBEBILITY:
			generate_order()

func _expire_old_orders() -> void:
	for i in range(Global.trade_orders.size() -1, -1, -1):
		if Global.trade_orders[i]["accepted"]:
			continue
		if Global.elapsed_game_seconds >= Global.trade_orders[i]["expires_at"]:
			Global.trade_orders.remove_at(i)

func generate_order() -> Dictionary:
	var resource_keys: Array = TRADE_BASE_PRICES.keys()
	var resource: String = resource_keys[randi_range(0, resource_keys.size() -1)]
	var base_price: float = TRADE_BASE_PRICES.get(resource, 10.0)
	var price_variance: float = randf_range(0.85, 1.2)
	
	var order: Dictionary = {
		"id": _next_order_id,
		"trader": generate_name(),
		"resource": resource,
		"quantity": randi_range(MIN_QUANTITY, MAX_QUANTITY),
		"price_per_unit": snapped(base_price * price_variance, 0.01),
		"expires_at": Global.elapsed_game_seconds + randi_range(MIN_ORDER_LIFETIME, MAX_ORDER_LIFETIME),
		"accepted": false
	}
	_next_order_id += 1
	Global.trade_orders.append(order)
	return order

func generate_name() -> String:
	var name_num: int = randi_range(0, 50)
	var surename_num: int = randi_range(0, 48)
	
	if name_num == 50:
		if randf() >= 0.5:
			return EASTEREGG_NAME
		else:
			name_num = randi_range(0, 49)
	
	var firstname: String = NAMES[name_num]
	var surename: String = SURENAMES[surename_num]
	return firstname + " " + surename

func get_free_airstrip() -> int:
	for i in Global.airstrips.size():
		if not Global.occupied_airstrips.has(i):
			return i
	return -1

func get_terminal_stock(resource: String) -> int:
	var terminal: FactoryInstance = Global.get_warehouse()
	if terminal == null:
		return 0
	return int(terminal.outputResources.get(resource, 0))

func has_stock_for_order(order: Dictionary) -> bool:
	return get_terminal_stock(order["resource"]) >= int(order["quantity"])

func accept_order(order: Dictionary) -> bool:
	if order.get("accepted", false):
		return false
	if Global.elapsed_game_seconds >= order["expires_at"]:
		return false
	
	var terminal: FactoryInstance = Global.get_warehouse()
	if terminal == null:
		print("No cargo terminal built, cant accept orders")
		return false
	if not has_stock_for_order(order):
		print("Not enough %s in cargo terminal" % order["resource"])
		return false
	
	var airstrip: int = get_free_airstrip()
	if airstrip == -1:
		print("No unoccupied airstrips available")
		return false
	if spawn_delivery_plane(order, airstrip) == null:
		return false
	
	terminal.withdraw_resource(order["resource"], int(order["quantity"]))
	return true

func spawn_delivery_plane(order: Dictionary, airstrip_index: int) -> DeliveryPlane:
	if Global.vehicle_layer == null:
		push_error("Global.vehicle_layer not set, can't spawn plane")
		return null
	
	var plane := PLANE_SCENE.instantiate() as DeliveryPlane
	Global.vehicle_layer.add_child(plane)
	var angle: float = randf() * TAU
	var dist: float = randf_range(PLANE_SPAWN_MIN_DIST, PLANE_SPAWN_MAX_DIST)
	if not plane.setup(order, airstrip_index, angle, dist):
		plane.queue_free()
		return null
	
	Global.occupied_airstrips.append(airstrip_index)
	order["accepted"] = true
	order["airstrip"] = airstrip_index
	order["place"] = str(airstrip_index + 1)
	order["loaded"] = 0
	plane.delivery_finished.connect(_on_delivery_finished)
	return plane

func _on_delivery_finished(order: Dictionary, airstrip_index: int) -> void:
	var payout: float = snapped(order["quantity"] * order["price_per_unit"], 0.01)
	Global.money += payout
	Global.occupied_airstrips.erase(airstrip_index)
	for i in Global.trade_orders.size():
		if Global.trade_orders[i]["id"] == order["id"]:
			Global.trade_orders.remove_at(i)
			break
