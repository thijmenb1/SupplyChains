extends Panel

@export var mode: String
@export var order: Dictionary
@export var index: int

@onready var nameLabel: Label = $Name
@onready var timeLabel1: Label = $landing_hover/Label2
@onready var timeLabel2: Label = $landing/Label2
@onready var landingText_hover: Label = $landing_hover/Label3
@onready var landingText: Label = $landing/Label3
@onready var loadedText1: Label = $loading_hover/Label2
@onready var loadedText2: Label = $loading/Label2
@onready var loadingText: Label = $loading/Label3
@onready var loadingText_hover: Label = $loading_hover/Label3
@onready var resourceText1: TextureRect = $loading_hover/TextureRect
@onready var resourceText2: TextureRect = $loading/TextureRect

@onready var landing_hover: Control = $landing_hover
@onready var landing: Control = $landing
@onready var loading_hover: Control = $loading_hover
@onready var loading: Control = $loading

@onready var resourceAtlas: AtlasTexture = preload("res://assets/resources_atlas.tres")

const ATLAS_VAL_IMG: Dictionary = {
	"ironBeam": Vector2i(0,0),
	"copperIngots": Vector2i(30,0),
	"goldBars": Vector2i(24,0),
	"steelBeam": Vector2i(48,0),
	"copperWire": Vector2i(36,0),
	"goldWire": Vector2i(42,0),
	"PCBPallet": Vector2i(54, 0),
}

func _process(delta):
	if order.get("stage", "landing") in ["loading", "leaving"] and mode.begins_with("landing"):
		mode = mode.replace("landing", "loading")
	
	match mode:
		"landing_hover":
			landing_hover.visible = true
			landing.visible = false
			loading_hover.visible = false 
			loading.visible = false
		"landing":
			landing_hover.visible = false
			landing.visible = true
			loading_hover.visible = false 
			loading.visible = false
		"loading_hover": 
			landing_hover.visible = false
			landing.visible = false
			loading_hover.visible = true 
			loading.visible = false
		"loading":
			landing_hover.visible = false
			landing.visible = false
			loading_hover.visible = false 
			loading.visible = true
	nameLabel.text = order["trader"]
	var remaining: float = max(order["arrival_time"] - Global.elapsed_game_seconds, 0.0)
	var remaining_min: int = remaining / 60
	var remaining_sec: int = remaining - (remaining_min * 60)
	timeLabel1.text = str(remaining_min) + ":" + (str(remaining_sec) if remaining_sec >= 10 else "0" + str(remaining_sec))
	timeLabel2.text = str(remaining_min) + ":" + (str(remaining_sec) if remaining_sec >= 10 else "0" + str(remaining_sec))
	landingText_hover.text = "to land on runway " + order["place"]
	landingText.text = "to land on R" + order["place"]
	loadedText1.text = str(order.get("loaded", 0)) + "/" + str(order["quantity"])
	loadedText2.text = str(order.get("loaded", 0)) + "/" + str(order["quantity"])
	loadingText.text = "loaded at terminal " + order["place"]
	loadingText_hover.text = "loaded at T" + order["place"]
	var texture := AtlasTexture.new()
	texture.atlas = resourceAtlas.atlas
	texture.region = Rect2(ATLAS_VAL_IMG[order["resource"]], Vector2i(6,6))
	resourceText1.texture = texture
	resourceText2.texture = texture
	order["mode"] = mode

func _on_mouse_entered():
	if mode == "landing":
		mode = "landing_hover"
	elif mode == "loading":
		mode = "loading_hover"

func _on_mouse_exited():
	if mode == "landing_hover":
		mode = "landing"
	elif mode == "loading_hover":
		mode = "loading"
