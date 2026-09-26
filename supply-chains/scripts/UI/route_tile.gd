extends Panel

@onready var NameLabel: Label = $Label
@onready var routeColor: Panel = $RouteColor
@onready var style_box : StyleBoxFlat = routeColor.get_theme_stylebox("panel").duplicate()

var number: int

func _ready():
	NameLabel.text = "route " + str(number)
	if Global.route_color.size() > number -1:
		style_box.bg_color = Global.route_color[number - 1]
	else:
		var color: Color = Color(randf(), randf(), randf())
		Global.route_color.append(color)
		style_box.bg_color = color

func _on_delete_route_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		Global.remove_route(number -1)
