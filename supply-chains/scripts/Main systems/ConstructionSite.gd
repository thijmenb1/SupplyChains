extends RefCounted
class_name ConstructionSite

var kind: String
var cells: Array[Vector2i] = []
var anchor: Vector2i
var grid_pos: Vector2i
var result: Dictionary = {}

var paths: Dictionary = {}

var required_resources: Dictionary = {}
var deliverd_resources: Dictionary = {}

var build_time: float = 5.0
var build_time_left: float = 0.0
var is_complete: bool = false

func _init(t_kind: String, t_cells: Array[Vector2i], cost_def: Dictionary, t_result: Dictionary) -> void:
	kind = t_kind
	cells = t_cells
	anchor = cells[0]
	grid_pos = anchor
	result = t_result

	required_resources = (cost_def.get("resources", {}) as Dictionary).duplicate()
	for res in required_resources:
		deliverd_resources[res] = 0
	
	build_time = cost_def.get("build_time", 5.0)
	build_time_left = build_time

func deposit_resource(resource: String, amount: int) -> void:
	if not required_resources.has(resource):
		return
	var still_needed: int = required_resources[resource] - deliverd_resources.get(resource, 0)
	var accepted: int = min(still_needed, amount)
	if accepted <= 0:
		return
	deliverd_resources[resource] = deliverd_resources.get(resource, 0) + accepted

func withdraw_resource(_resource: String, _amount: int) -> int:
	return 0

func is_fully_supplied() -> bool:
	for res in required_resources:
		if deliverd_resources.get(res, 0) < required_resources[res]:
			return false
	return true

func get_resource_progress(resource: String) -> float:
	if not required_resources.has(resource) or required_resources[resource] <= 0:
		return 1.0
	return float(deliverd_resources.get(resource, 0)) / float(required_resources[resource])

func get_overall_progress() -> float:
	var total_needed := 0
	var total_have := 0
	for res in required_resources:
		total_needed += required_resources[res]
		total_have += min(deliverd_resources.get(res, 0), required_resources[res])
	if total_needed == 0:
		return 1.0
	return float(total_have) / float(total_needed)

func update(delta: float) -> bool:
	if is_complete:
		return false
	if not is_fully_supplied():
		return false
	build_time_left -= delta
	if build_time_left <= 0.0:
		is_complete = true
		return true
	return false
