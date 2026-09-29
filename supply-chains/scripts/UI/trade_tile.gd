extends Panel

@onready var nameLabel: Label = $BuyerName
@onready var quantityLabel: Label = $quantity
@onready var resourceNameLabel: Label = $RecourceName
@onready var resourceSpriteText: TextureRect = $ResourceSpriteBackground/ResourceSprite
@onready var timeLeftLabel: Label = $"Not accepted/timeLeft"
@onready var perUnitPriceLabel: Label = $perUnitPrice
@onready var priceLabel: Label = $Price

@onready var not_accepted: Control = $"Not accepted"
@onready var landing: Control = $landing
@onready var timeLeftLanding: Label = $landing/timeLeft
@onready var runwayLabel: Label = $landing/Label3
@onready var loading: Control = $loading
@onready var loadedLabel: Label = $loading/Label2
@onready var loadingLabel: Label = $loading/Label3

@onready var resourceAtlas: AtlasTexture = preload("res://assets/resources_atlas.tres")

var order: Dictionary
var index: int
var time_remaining: Vector2i

const ATLAS_VAL_IMG: Dictionary = {
	"ironBeam": Vector2i(0,0),
	"copperIngots": Vector2i(30,0),
	"goldBars": Vector2i(24,0),
	"steelBeam": Vector2i(48,0),
	"copperWire": Vector2i(36,0),
	"goldWire": Vector2i(42,0),
	"PCBPallet": Vector2i(54, 0),
}

func _process(_delta):
	setup()

func setup() -> void:
	resourceNameLabel.text = order["resource"]
	nameLabel.text = order["trader"]
	quantityLabel.text = str(order["quantity"])
	perUnitPriceLabel.text = "€" + "%.2f/u" % order["price_per_unit"]
	priceLabel.text = "€" + str(order["quantity"] * order["price_per_unit"])
	var texture := AtlasTexture.new()
	texture.atlas = resourceAtlas.atlas
	texture.region = Rect2(ATLAS_VAL_IMG[order["resource"]], Vector2i(6,6))
	resourceSpriteText.texture = texture
	
	if order["accepted"] == false:
		not_accepted.visible = true
		landing.visible = false
		loading.visible = false
		time_remaining = order_time_remaining(order["expires_at"])
		timeLeftLabel.text = str(time_remaining.x) + ":" + str(time_remaining.y) if time_remaining.y >= 10 else str(time_remaining.x) + ":" + "0" + str(time_remaining.y)
	elif order.get("mode") == "landing" or order.get("mode") == "landing_hover":
		not_accepted.visible = false
		landing.visible = true
		loading.visible = false
		var remaining: float = max(order["arrival_time"] - Global.elapsed_game_seconds, 0.0)
		var remaining_min: int = remaining / 60
		var remaining_sec: int = remaining - (remaining_min * 60)
		timeLeftLanding.text = str(remaining_min) + ":" + (str(remaining_sec) if remaining_sec >= 10 else "0" + str(remaining_sec))
	elif order.get("mode") == "loading" or order.get("mode") == "loading_hover":
		not_accepted.visible = false
		landing.visible = false
		loading.visible = true

func order_time_remaining(expires_at: float) -> Vector2i:
	var remaining = expires_at - Global.elapsed_game_seconds
	var hours: int = remaining / 3600.0
	var minutes: int = (remaining - (hours * 3600)) / 60
	return Vector2i(hours, minutes)


func _on_button_pressed():
	order["arrival_time"] = Global.elapsed_game_seconds + randi_range(30, 300)
	var airport: int = -1
	for i in Global.airstrips.size():
		if Global.occupied_airstrips.has(i):
			airport = i
			Global.occupied_airstrips.append(i)
			break
	if airport != -1:
		order["accepted"] = true
		order["place"] = Vector2i(airport,1)
	else:
		print(Global.airstrips)
		print("No unoccupied airstrips avilable.")
