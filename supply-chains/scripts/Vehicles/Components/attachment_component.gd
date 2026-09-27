extends Node
class_name AttachmentComponent

const MAX_ARTICULATION_FOR_REVERSE: float = deg_to_rad(20.0)

var vehicle: VehicleBody
var points: Dictionary
var pullingWeight: int = 0

func setup(t_vehicle: VehicleBody, attachment_points: VehicleAttachmentPoints) -> void:
	vehicle = t_vehicle
	if attachment_points.front_3point:
		points["front3Point"] = null
	if attachment_points.rear_3point:
		points["rear3Point"] = null
	if attachment_points.hitch_type:
		points["hitch"] = null
	vehicle.frontAttatchmentPoint.position = attachment_points.front_attachment_point_coords
	vehicle.rearAttatchmentPoint.position = attachment_points.rear_attachment_point_coords

func get_hitch() -> CharacterBody2D:
	return points.get("hitch", null)

func has_coupled_trailer() -> bool:
	var hitch = get_hitch()
	return hitch != null and hitch.is_coupled

func trailer_aligned_enough() -> bool:
	var hitch = get_hitch()
	if hitch == null or not hitch.is_coupled:
		return true
	var articulation: float = wrapf(vehicle.rotation - hitch.rotation, -PI, PI)
	return abs(articulation) < MAX_ARTICULATION_FOR_REVERSE
