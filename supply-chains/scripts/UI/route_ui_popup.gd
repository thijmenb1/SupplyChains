extends Panel

@onready var RouteDropdown: OptionButton = $OptionButton
@onready var popUpUI: Panel = $".."

var last_route_count

func _process(_delta):
	if last_route_count != Global.routes.size():
		last_route_count = Global.routes.size()
		_populate_routes_dropdown()

func _populate_routes_dropdown():
	RouteDropdown.clear()
	for i in Global.routes.size():
		RouteDropdown.add_item("Route " + str(i + 1))

func _on_panel_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		var route_index: int = RouteDropdown.get_item_index(RouteDropdown.get_selectable_item())
		print(route_index)
		VehicleManager.get_vehicle(Global.vehicle_ui_selected).movement.assign_route(route_index)
		visible = false
