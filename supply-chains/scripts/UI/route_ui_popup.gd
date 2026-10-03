extends Panel

@onready var RouteDropdown: OptionButton = $OptionButton
@onready var popUpUI: Panel = $".."

var _last_signature: String = ""

func _process(_delta):
	# routes.size() + active count catches add, remove, and remove+add in the same frame
	var sig := str(Global.routes.size()) + ":" + str(Global.active_route_count())
	if sig != _last_signature:
		_last_signature = sig
		_populate_routes_dropdown()

func _populate_routes_dropdown() -> void:
	var previous_id := RouteDropdown.get_selected_id()
	RouteDropdown.clear()
	for i in Global.routes.size():
		if Global.routes[i].is_empty():
			continue
		# id = real index into Global.routes, text = what the player sees
		RouteDropdown.add_item("Route " + str(i + 1), i)
	
	var idx := RouteDropdown.get_item_index(previous_id)
	if idx != -1:
		RouteDropdown.select(idx)

func _on_panel_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		if RouteDropdown.item_count == 0 or RouteDropdown.get_selected_id() == -1:
			return
		var route_index: int = RouteDropdown.get_selected_id()
		VehicleManager.get_vehicle(Global.vehicle_ui_selected).movement.assign_route(route_index)
		visible = false
