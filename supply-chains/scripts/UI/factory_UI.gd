extends Panel

@onready var NameLabel: Label = $Label
@onready var progressbar1: ProgressBar = $ProgressBar
@onready var progressText1: Label = $ProgressBar/ProgressText
@onready var progressbar2: ProgressBar = $ProgressBar2
@onready var progressText2: Label = $ProgressBar2/ProgressText

@onready var storedResourceUI: Panel = $StoredResourceUI
@onready var RecipeUI: Panel = $RecipeUI

const RESOUCE_ATLAS: Texture2D = preload("res://assets/resources.png")
const RECIPE_ROW: PackedScene = preload("res://scenes/UI/recipeRow.tscn")

const ATLAS_VAL_IMG: Dictionary = {
	"ironBeam": Vector2i(0, 0),
	"copperIngots": Vector2i(30, 0),
	"goldBars": Vector2i(24, 0),
	"steelBeam": Vector2i(48, 0),
	"copperWire": Vector2i(36, 0),
	"goldWire": Vector2i(42, 0),
	"PCBPallet": Vector2i(54, 0),
	"crudeOil": Vector2i(60,0),
	"fuel": Vector2i(18,0),
	"asphalt": Vector2i(66,0),
	"ironOre": Vector2i(72,0),
	"copperOre": Vector2i(78,0),
	"goldOre": Vector2i(84,0),
	"electricity": Vector2i(102,0),
	"coal": Vector2i(90,0),
	"cement": Vector2i(96,0),
	"gravel": Vector2i(6,0),
	"sand": Vector2i(12,0)
}

@onready var resource_textures: Array[TextureRect] = [
	$StoredResourceUI/TextureRect,
	$StoredResourceUI/TextureRect2,
	$StoredResourceUI/TextureRect3,
	$StoredResourceUI/TextureRect4,
	$StoredResourceUI/TextureRect5,
	$StoredResourceUI/TextureRect6
]

@onready var resource_labels: Array[Label] = [
	$StoredResourceUI/TextureRect/Label,
	$StoredResourceUI/TextureRect2/Label,
	$StoredResourceUI/TextureRect3/Label,
	$StoredResourceUI/TextureRect4/Label,
	$StoredResourceUI/TextureRect5/Label,
	$StoredResourceUI/TextureRect6/Label
]

const REFINERY_RECIPES = {
	0: {"crudeOil": -2, "fuel": 1, "asphalt": 1}
}

const BLAST_RECIPES = {
	0: {"ironOre": -3, "ironBeam": 1},
	1: {"copperOre": -2, "copperIngots": 1},
	2: {"goldOre": -2, "goldBars": 1}
}

const STEELMILL_RECIPES = {
	0: {"ironBeam": -1, "electricity": -25, "steelBeam": 1}
}

const WIREMILL_RECIPES = {
	0: {"copperIngots": -2, "copperWire": 1},
	1: {"goldBars": -2, "electricity": -50, "goldWire": 1}
}

const COALPOWER_RECIPES = {
	0: {"coal": -2, "electricity": 45},
	1: {"coal": -3, "electricity": 65},
	2: {"coal": -4, "electricity": 80},
	3: {"coal": -1, "electricity": 25}
}

const CEMENTMIXING_RECIPES = {
	0: {"gravel": -1, "sand": -1, "water": -1, "cement": 1}
}

const ALL_RECIPES = {
	"refinery": REFINERY_RECIPES,
	"blast": BLAST_RECIPES,
	"steelmill": STEELMILL_RECIPES,
	"wiremill": WIREMILL_RECIPES,
	"coalpower": COALPOWER_RECIPES,
	"cementmixing": CEMENTMIXING_RECIPES
}

@onready var _recipe_list: VBoxContainer = $RecipeUI/VBoxContainer
var _built_for: String

func update_recipe_ui(factory_instance: FactoryInstance) -> void:
	var key = factory_instance.factory_name + "|" + factory_instance.factory_type
	if key == _built_for:
		return
	_built_for = key

	for child in _recipe_list.get_children():
		child.queue_free()

	var recipes: Dictionary = ALL_RECIPES.get(factory_instance.factory_type, {})
	for i in recipes.keys():
		var row = RECIPE_ROW.instantiate()
		_recipe_list.add_child(row)
		row.setup(recipes[i], ATLAS_VAL_IMG, RESOUCE_ATLAS)

func _process(_delta: float) -> void:
	visible = Global.factory_ui_open
	if not Global.factory_ui_open or Global.factory_ui_selected == "":
		return
	
	var factoryInstance: FactoryInstance = null
	for factory in Global.factorys:
		if factory.factory_name == Global.factory_ui_selected:
			factoryInstance = factory
			break
	if factoryInstance == null:
		Global.factory_ui_open = false
		return
	
	
	var factory_split = Global.factory_ui_selected.split("*")
	NameLabel.text = factory_split[0] + " " + factory_split[1]
	
	var total_input: int = 0
	for amount in factoryInstance.inputResources.values():
		total_input += int(amount)
	var total_output: int = 0
	for amount in factoryInstance.outputResources.values():
		total_output += int(amount)
	
	progressText1.text = str(total_input) + "/30"
	progressText2.text = str(total_output) + "/30"
	
	update_resource_display(factoryInstance)
	update_recipe_ui(factoryInstance)

func _on_full_inventory_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		storedResourceUI.visible = !storedResourceUI.visible

func update_resource_display(factory_instance: FactoryInstance) -> void:
	var resources_to_display: Array = []
	
	for resource_name in factory_instance.inputResources:
		resources_to_display.append({
			"name": resource_name,
			"amount": factory_instance.inputResources[resource_name],
			"type": "input"
		})
	
	for resource_name in factory_instance.outputResources:
		resources_to_display.append({
			"name": resource_name,
			"amount": factory_instance.outputResources[resource_name],
			"type": "output"
		})

	for i in range(resource_textures.size()):
		resource_textures[i].visible = false
		resource_labels[i].text = ""
		resource_textures[i].texture = null
	
	for i in range(min(resources_to_display.size(), resource_textures.size())):
		var resource_data = resources_to_display[i]
		if not ATLAS_VAL_IMG.has(resource_data.name):
			continue
		
		resource_textures[i].visible = true
		resource_labels[i].text = str(resource_data.amount)
		var texture: AtlasTexture = AtlasTexture.new()
		texture.atlas = RESOUCE_ATLAS
		texture.region = Rect2(ATLAS_VAL_IMG[resource_data.name], Vector2(6,6))
		resource_textures[i].texture = texture

func _on_change_recipe_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		RecipeUI.visible = !RecipeUI.visible
