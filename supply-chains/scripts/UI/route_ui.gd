extends Control

const ROUTE_TILE_SCENE = preload("res://scenes/UI/route_tile.tscn")
const ROUTE_LINE_SCENE = preload("res://scenes/UI/route_line.tscn")

@onready var source: OptionButton = $RouteUI_Create/source
@onready var destination: OptionButton = $RouteUI_Create/Destination
@onready var resource: OptionButton = $RouteUI_Create/Resource

@onready var RouteUI_View: Panel = $RouteUI_View
@onready var TileVbox: VBoxContainer = $RouteUI_View/ScrollContainer/VBoxContainer

@onready var RouteUI_Create: Panel = $RouteUI_Create

var _last_factory_count: int = -1
var _last_source_id: int = -1
var _last_destination_id: int = -1

var source_factory: String

func _process(_delta):
	if Global.factorys.size() != _last_factory_count:
		_last_factory_count = Global.factorys.size()
		_populate_source_dropdown()
		_populate_destination_dropdown()
	elif source.get_selected_id() != _last_source_id:
		_populate_destination_dropdown()
	elif destination.get_selected_id() != _last_destination_id:
		_populate_resource_dropdown()
	
	_last_source_id = source.get_selected_id()
	
	if TileVbox.get_child_count() != Global.active_route_count():
		_route_view_update()

func _populate_source_dropdown():
	var previous_id := source.get_selected_id()
	source.clear()
	for i in range(Global.factorys.size()):
		var factory = Global.factorys[i]
		if Global.SUITBEL_DILIVERY_FACTORYS.get(factory.factory_type, null) == null:
			continue
		source.add_item(factory.factory_name, i)
	
	var idx := source.get_item_index(previous_id)
	if idx != -1:
		source.select(idx)

func _populate_destination_dropdown():
	var previous_id := destination.get_selected_id()
	destination.clear()
	var source_id = source.get_selected_id()
	
	if source_id == -1 or source_id >= Global.factorys.size():
		return
	
	source_factory = Global.factorys[source_id].factory_type
	var allowed: Array = Global.SUITBEL_DILIVERY_FACTORYS.get(source_factory, null)
	if allowed == null:
		return
	
	for i in range(Global.factorys.size()):
		if i == source_id:
			continue
		var factory = Global.factorys[i]
		if not allowed.has(factory.factory_type):
			print("no suitible factory avalible", allowed, "    ", factory.factory_type)
			continue
		destination.add_item(factory.factory_name, i)

func _populate_resource_dropdown():
	var previous_text: String = ""
	if resource.get_selected() != -1:
		previous_text = resource.get_item_text(resource.get_selected())
	resource.clear()
	
	var destination_id := destination.get_selected_id()
	if destination_id == -1 or destination_id >= Global.factorys.size():
		return
	var destination_type: String = Global.factorys[destination_id].factory_type
	var accepted_resources: Array = Global.get_accepted_resources(destination_type)
	
	for i in accepted_resources.size():
		resource.add_item(accepted_resources[i], i)
	
	for i in resource.item_count:
		if resource.get_item_text(i) == previous_text:
			resource.select(i)
			break


func _on_create_route_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		if source.get_selected_id() == -1 or destination.get_selected_id() == -1:
			print("Can't create route: select a source and destination first")
			return
		print("creating route")
		Global.routes.append(
			{
				"source": Global.factorys[source.get_selected_id()],
				"destination": Global.factorys[destination.get_selected_id()],
				"resource": resource.get_item_text(resource.get_selected_id()),
			})
		Global.mark_routes_dirty()
		var new_line_offroad = ROUTE_LINE_SCENE.instantiate()
		var new_line_onroad = ROUTE_LINE_SCENE.instantiate()
		new_line_offroad.route_index = (Global.routes.size() -1)
		new_line_offroad.road_mode = "offroad"
		new_line_onroad.route_index = (Global.routes.size() - 1)
		new_line_onroad.road_mode = "onroad"
		Global.route_layer.add_child(new_line_onroad)
		Global.route_layer.add_child(new_line_offroad)

func _route_view_update():
	for child in TileVbox.get_children():
		child.queue_free()
	
	for i in Global.routes.size():
		if Global.routes[i].is_empty():
			continue
		var new_card = ROUTE_TILE_SCENE.instantiate()
		new_card.number = i + 1
		TileVbox.add_child(new_card)

func _on_route_button_pressed():
	RouteUI_Create.visible = ! RouteUI_Create.visible


func _on_cancel_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		RouteUI_Create.visible = false
