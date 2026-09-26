extends Node2D

@onready var flatbed: VehicleBody = $Vehicle
@onready var bulk_trailer: trailer = $Trailer
@onready var road_layer: TileMapLayer = $RoadLayer # Adjust to your RoadLayer node name/path

func _ready() -> void:

	GridManager.setup_astar_grid(100, 100)

	var region := GridManager.astar.region
	for x in range(region.position.x, region.position.x + region.size.x):
		for y in range(region.position.y, region.position.y + region.size.y):
			var coords := Vector2i(x, y)
			if road_layer.get_cell_source_id(coords) != -1:
				GridManager.astar.set_point_weight_scale(coords, 1.0)
			else:
				GridManager.astar.set_point_weight_scale(coords, 5.0)

	flatbed.vehicleType = "flatbedTruck"
	flatbed.vehicleNumber = 1
	
	flatbed.firstLocationTile = Vector2i(1.5, 0.5)
	flatbed.SecondLocationTile = Vector2i(15, 10)
	flatbed.targetTile = flatbed.SecondLocationTile
	
	flatbed.navigate_to_tile(flatbed.targetTile)
	
	bulk_trailer.global_position = flatbed.global_position + Vector2(0, 30)
	bulk_trailer.trailer_name = "bulkTrailer"
	bulk_trailer.hitch_type = Global.HitchType.PIN
	
	if flatbed.rearAttatchmentPoint:
		bulk_trailer.couple_to(flatbed, flatbed.rearAttatchmentPoint)
		print("succesfully coupled flatbed Trailer to faltbedTruck!")
	else:
		push_error("faltbedTruck is missing rearAttatchmentPoint")
