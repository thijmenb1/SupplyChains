extends HBoxContainer

@onready var checkbox = $CheckBox

const ICON_SIZE = Vector2i(6,6)
const ICON_DISPLAY_SIZE = Vector2i(12,12)
const FONT_SIZE = 12

func setup(recipe: Dictionary, atlas_positions: Dictionary, atlas: Texture2D) -> void:
	var inputs = {}
	var outputs = {}
	for resource_key in recipe.keys():
		if recipe[resource_key] < 0:
			inputs[resource_key] = abs(recipe[resource_key])
		else:
			outputs[resource_key] = recipe[resource_key]

	var arrow = $Arrow
	var first := true
	for resource_name in inputs.keys():
		if not first:
			_add_plus()
		first = false
		_add_resource(resource_name, inputs[resource_name], atlas_positions, atlas)
	move_child(arrow, -1)

	first = true
	for resource_name in outputs.keys():
		if not first:
			_add_plus()
		first = false
		_add_resource(resource_name, outputs[resource_name], atlas_positions, atlas)

func _add_plus() -> void:
	var label = Label.new()
	label.text = "+"
	label.add_theme_font_size_override("font_size", FONT_SIZE)
	add_child(label)

func _add_resource(resource_name: String, amount: int, atlas_positions: Dictionary, atlas: Texture2D) -> void:
	if not atlas_positions.has(resource_name):
		push_warning("No icon for " + resource_name)
		return
	var container = HBoxContainer.new()
	
	var texture_rect = TextureRect.new()
	var atlas_texture = AtlasTexture.new()
	atlas_texture.atlas = atlas
	atlas_texture.region = Rect2(atlas_positions[resource_name], ICON_SIZE)
	texture_rect.texture = atlas_texture
	texture_rect.custom_minimum_size = ICON_DISPLAY_SIZE
	container.add_child(texture_rect)
	
	var label = Label.new()
	label.text = str(amount)
	label.add_theme_font_size_override("font_size", FONT_SIZE)
	container.add_child(label)
	
	add_child(container)
