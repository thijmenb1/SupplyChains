extends Node2D

var route_index: int
var road_mode: String
var path_points: PackedVector2Array
var color: Color

func _process(_delta):
	if route_index < 0 or route_index >= Global.routes.size() or Global.routes[route_index].is_empty():
		queue_free()
		return
	if Global.routes[route_index].get("Offroad_route", null) != null && road_mode == "offroad":
		path_points = Global.routes[route_index]["Offroad_route"]
		queue_redraw()
	elif Global.routes[route_index].get("Onroad_route", null) != null && road_mode == "onroad":
		path_points = Global.routes[route_index]["Onroad_route"]
		queue_redraw()

func _draw():
	if path_points.size() < 2:
		return
	
	for i in range(path_points.size() - 1):
		var start = path_points[i]
		var end = path_points[i + 1]
		
		var grid_pos = ((end - GridManager.astar.offset) / GridManager.astar.cell_size).round()
		var weight = GridManager.astar.get_point_weight_scale(grid_pos)
		
		if weight > GridManager.ROAD_WEIGHT_THRESHOLD:
			draw_dotted_line(start, end)
		else:
			draw_line(start, end, color, 2)

func draw_dotted_line(start: Vector2, end: Vector2):
	var current = start
	var direction = (end - start).normalized()
	var distance = start.distance_to(end)
	var traveled = 0.0
	var dash_length = 4
	var gap_length = 4
	
	while traveled < distance:
		var next = current + direction * dash_length
		draw_line(current, next, color, 2)
		traveled += dash_length + gap_length
		current = next + direction * gap_length
	
