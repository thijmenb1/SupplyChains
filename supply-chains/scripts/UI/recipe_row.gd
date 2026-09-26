extends HBoxContainer

@onready var checkbox = $CheckBox

const ICON_SIZE = Vector2i(6,6)
const ICON_DISPLAY_SIZE = Vector2i(12,12)
const FONT_SIZE = 12

func setup(recipe: Dictionary, atlas_positions: Dictionary, atlas: Texture2D) -> void:
	var inputs = {}
	var outputs = {}
	
	for resource_key in  recipe.keys():
		if recipe[resource_key] < 0:
			inputs[resource_key] = abs(recipe[resource_key])
		else:
			outputs[resource_key] = recipe[resource_key]
	
	for resource_name in inputs.keys():
		_add_resource(resource_name, inputs[resource_name], atlas_positions, atlas)
		var label = Label.new()
		label.text = " +"
		label.add_theme_font_size_override("font_size", FONT_SIZE)
		add_child(label)
	
	if inputs.size() > 0:
		remove_child(get_child(get_child_count( - 1)))
	
	var arrow = $Arrow
	move_child(arrow, get_child_count())
	
	for resource_name in outputs.keys():
		_add_resource(resource_name, outputs[resource_name], atlas_positions, atlas)
		var label = Label.new()
		label.text = " +"
		label.add_theme_font_size_override("font_size", FONT_SIZE)
		add_child(label)
	
	if outputs.size() > 0:
		remove_child(get_child(get_child_count() -1))

func _add_resource(resource_name: String, amount: int, atlas_positions: Dictionary, atlas: Texture2D) -> void:
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
