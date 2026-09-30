extends RefCounted
class_name FactoryInstance

var factory_name: String
var factory_type: String
var grid_pos: Vector2i
var size: Vector2i
var inputResources: Dictionary = {}
var outputResources: Dictionary = {}
var is_crafting: bool = false
var craft_time_left: float = 0.0
var craft_time: float = 5.0
var recipeIndex: int = 0

var paths: Dictionary = {}

var mine_resources: Dictionary = {}

func _init(name: String, pos: Vector2i, sz: Vector2i) -> void:
	var name_split = name.split("*")
	var type = name_split[0]
	factory_name = name
	factory_type = type
	grid_pos = pos
	size = sz
	print(factory_type + " " + factory_name)
	
func update(delta: float) -> void:
	if factory_type == "mine":
		return
	if not is_crafting:
		start_auto_craft()
	if is_crafting and craft_time_left > 0:
		craft_time_left -= delta
		if craft_time_left <= 0:
			finish_craft()
			is_crafting = false

func start_auto_craft() -> void:
	for i in range(5):
		if can_craft(i):
			recipeIndex = i
			is_crafting = true
			craft_time_left = craft_time
			consume_resources(i)
			return

func can_craft(recipe_index: int) -> bool:
	var recipe := get_recipe(recipe_index)
	for resource in recipe:
		if recipe[resource] < 0 and inputResources.get(resource, 0) < abs(recipe[resource]):
			return false
	return true

func consume_resources(recipe_index: int) -> void:
	for resource in get_recipe(recipe_index):
		var amount = get_recipe(recipe_index)[resource]
		if amount < 0:
			inputResources[resource] = inputResources.get(resource, 0) + amount

func finish_craft() -> void:
	for resource in get_recipe(recipeIndex):
		var amount = get_recipe(recipeIndex)[resource]
		if amount > 0:
			outputResources[resource] = outputResources.get(resource, 0) + amount

func deposit_resource(resource: String, amount: int) -> void:
	if not Global.factory_accepts_resouce(factory_type, resource):
		return
	if factory_type == "cargoTerminal":
		outputResources[resource] = outputResources.get(resource, 0) + amount
	else:
		inputResources[resource] = inputResources.get(resource, 0) + amount

func withdraw_resource(resource: String, amount: int) -> int:
	var available: int = outputResources.get(resource, 0)
	var taken: int = min(available, amount)
	outputResources[resource] = available - taken
	return taken

func get_recipe(index: int, FactoryType: String = factory_type) -> Dictionary:
	match  FactoryType:
		"refinary":
					return {
						"crudeOil": -2,
						"fule": 1,
						"asphalt": 1,
					}
		"blast":
				if index == 0:
					return {
						"ironOre": -3,
						"ironBeam": 1
					}
				elif index == 1:
					return {
						"copperOre": -2,
						"copperIngots":  1
					}
				elif index == 2:
					return {
						"goldOre": -2,
						"goldBars": 1
					}
				return {}
		"steelmill":
				return {
					"ironBeam": -1,
					"electricity": -25,
					"steelBeam": 1
					}
		"wiremill":
				if index == 0:
					return {
							"copperIngots": -2,
							"copperWire": 1
						}
				return {
					"goldBars": -2,
					"electricity": -50,
					"goldWire": 1,
				}
		"coalpower":
				if index == 0:
					return {
					"coal": -2,
					"electricity": 45
					}
				elif index == 1:
					return {
					"coal": -3,
					"electricity": 65
					}
				elif index == 2:
					return {
					"coal": -4,
					"electricity": 80
					}
				return {
					"coal": -1,
					"electricity": 25
					}
		"cementMixing":
				return {
					"gravel": -1,
					"sand": -1,
					"cement": 1
					}
		"chip":
				return {
					"copperWire": -1,
					"goldWire": -2,
					"electricity": -50,
					"PCBPallet": 1
					}
		"mine": return {}
		_:
			return {}

func to_dict() -> Dictionary:
	return {
		"factory_name": factory_name,
		"factory_type": factory_type,
		"grid_pos": grid_pos,
		"inputResources": inputResources,
		"outputResources": outputResources,
		"is_crafting": is_crafting,
		"craft_time_left": craft_time_left,
		"craft_time": craft_time,
		"recipeIndex": recipeIndex,
		"mine_resources": mine_resources
	}

func apply_dict(d: Dictionary) -> void:
	factory_name = d.get("factory_name", factory_name)
	inputResources = d.get("inputResources", {}).duplicate()
	outputResources = d.get("outputResources", {}).duplicate()
	is_crafting = d.get("is_crafting", false)
	craft_time_left = d.get("craft_time_left", 0.0)
	craft_time = d.get("craft_time", craft_time)
	recipeIndex = d.get("recipeIndex", 0)
	mine_resources = d.get("mine_resources", {}).duplicate()

func mine_resource(amount: int, limit: int) -> void:
	var total: int = 0
	for r in mine_resources:
		total += mine_resources[r]
	if total <= 0:
		return
	var roll: int = randi() % total
	for r in mine_resources:
		roll -= mine_resources[r]
		if roll < 0:
			if outputResources.get(r, 0) < limit:
				outputResources[r] = outputResources.get(r, 0) + amount
			return
