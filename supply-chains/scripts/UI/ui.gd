extends CanvasLayer

"""
@onready var recourceBar: Panel = $Control/ResourceBar
@onready var ironBarLab: Label = $Control/ResourceBar/TextureRect/Label
@onready var gravelLab: Label = $Control/ResourceBar/TextureRect2/Label
@onready var sandLab: Label = $Control/ResourceBar/TextureRect3/Label
@onready var fuleLab: Label = $Control/ResourceBar/TextureRect4/Label
@onready var coalLab: Label = $Control/ResourceBar/TextureRect5/Label
@onready var steelBarLab: Label = $Control/ResourceBar/TextureRect6/Label
@onready var PCBCrateLab: Label = $Control/ResourceBar/TextureRect7/Label
@onready var Lab1: Label = $Control/ResourceBar/TextureRect8/Label
@onready var lab2: Label = $Control/ResourceBar/TextureRect9/Label
"""

@onready var selectedTileUI: Control = $Control/selectedTileUI
@onready var selectedTileImg: TextureRect = $Control/selectedTileUI/TextureRect
@onready var selectedTilePanel: Panel = $Control/selectedTileUI/SelcetedTile
@onready var selectedTileExstendedPanel: Panel = $Control/selectedTileUI/SelctedTileExtended

@onready var extenedTileUI: Control = $Control/extendedTileUI
@onready var extendedTileUIPanel_factory: Panel = $Control/extendedTileUI/Factory
@onready var extendedTileUIPanel_base: Panel = $Control/extendedTileUI/Base
@onready var extendedTileUI_factory: VBoxContainer = $Control/extendedTileUI/Factory/FactoryTiles
@onready var extendedTileUI_base: VBoxContainer = $Control/extendedTileUI/Base/BaseTiles
@onready var extendedTileUISelcter_factory: ColorRect = $Control/extendedTileUI/Factory/TextureRect
@onready var extendedTileUISelcter_base: ColorRect = $Control/extendedTileUI/Base/TextureRect

@onready var overlayUi: Panel = $Control/OverlayUI
@onready var heightmapButton: CheckBox = $Control/OverlayUI/CheckBox
@onready var tempuratureButton: CheckBox = $Control/OverlayUI/CheckBox2
@onready var humidatyButton: CheckBox = $Control/OverlayUI/CheckBox3
@onready var level: Node2D = $".."

@onready var bottomLeftButtons: Control = $Control/bottomLeftButtons

@onready var garageUI: Panel = $Control/GargeUi
@onready var tradeUI: Panel = $Control/TradeUi
@onready var shop: Panel = $Control/Shop
@onready var shopGeneral: ScrollContainer = $Control/Shop/generalShop
@onready var shopViewing: Control = $"Control/Shop/Shop viewing"

@onready var routeUI: Control = $Control/RouteUI

@onready var monthLabel: Label = $Control/DateUI/Month
@onready var yearLabel: Label = $Control/DateUI/Year
@onready var timeLabel: Label = $Control/TimeUI/time
@onready var speedLabel: Label = $Control/SpeedUI/Label

@onready var money: Label = $Control/MoneyUI/Money

@onready var popUpText: Label = $Control/PopUPText

var selectedVehicle: String
var currentShopMode: shopMode = shopMode.General
enum shopMode {None, General, Viewing}

func _ready() -> void:
	for i in extendedTileUI_factory.get_child_count():
		var texture_rect = extendedTileUI_factory.get_child(i)
		
		if texture_rect is TextureRect:
			texture_rect.gui_input.connect(_on_texture_rect_gui_input_factory.bind(i))
	for i in extendedTileUI_base.get_child_count():
		var texture_rect = extendedTileUI_base.get_child(i)
		
		if texture_rect is TextureRect:
			texture_rect.gui_input.connect(_on_texture_rect_gui_input_base.bind(i))


func _process(_delta):
	if Global.clickMode == "highlight":
		selectedTileUI.visible = false
		extenedTileUI.visible = false
	else:
		selectedTileUI.visible = true
		extenedTileUI.visible = true
	
	if Global.baseBuild:
#		recourceBar.visible = true
		bottomLeftButtons.visible = true
	else:
#		recourceBar.visible = false
		bottomLeftButtons.visible = false
	
	money.text = "€" + format_number_with_separators(Global.money)
	
	"""
	ironBarLab.text = str(Global.resources["ironBeam"])
	gravelLab.text = str(Global.resources["gravel"]) 
	sandLab.text = str(Global.resources["sand"])
	fuleLab.text = str(Global.resources["fule"])
	coalLab.text = str(Global.resources["coal"])
	steelBarLab.text = str(Global.resources["steelBeam"])
	PCBCrateLab.text = str(Global.resources["PCBPallet"]) 
	"""
	displaySelected()
	if Global.selcted_tile.y == 0 or Global.selcted_tile.y == 2:
		extendedTileUIPanel_factory.visible = true
		extendedTileUIPanel_base.visible = false
		if Global.selcted_tile.x == 0:
			extendedTileUISelcter_factory.position = Vector2(5.5, 5.5)
	elif Global.selcted_tile == Vector2i(0,12) or Global.selcted_tile == Vector2i(0,19) or Global.selcted_tile == Vector2i(9,11):
		extendedTileUIPanel_factory.visible = false
		extendedTileUIPanel_base.visible = true
		if Global.selcted_tile.x == 0:
			extendedTileUISelcter_base.position = Vector2(5.5, 5.5)
	else:
		extendedTileUIPanel_factory.visible = false
		extendedTileUIPanel_base.visible = false
	
	if currentShopMode == shopMode.General:
		shopGeneral.visible = true
		shopViewing.visible = false
	elif currentShopMode == shopMode.Viewing:
		shopGeneral.visible = false
		shopViewing.visible = true
	else:
		shop.visible = false
	setTime()
	
	if Global.baseTiles.size() == 0:
		popUpText.text = "You first have to place a base"
	elif Global.baseTiles.size() < 4:
		popUpText.text = "Your base must be atleast 4 tiles"
	else:
		popUpText.text = ""

func _on_texture_rect_mouse_entered():
	selectedTileExstendedPanel.visible = true
	
func _on_texture_rect_mouse_exited():
	selectedTileExstendedPanel.visible = false
	
func _on_texture_rect_gui_input_factory(event: InputEvent, index: int) -> void:
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		var selectedX: float
		var selectedY: float
		Global.selcted_tile = Tiles.factories[index]["tile"]
		Global.selected_factory_type = Tiles.factories[index]["name"]
		selectedY = 5.5 + (32 * index) + (4 * index)
		selectedX = 5.5
		extendedTileUISelcter_factory.position = Vector2(selectedX, selectedY)

func _on_texture_rect_gui_input_base(event: InputEvent, index: int) -> void:
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		var selectedX: float
		var selectedY: float
		match index:
			0:
				Global.selcted_tile = Vector2i(0,19)
				Global.selected_terrain = Tiles.BASE
				Global.clickMode = "place_terrainSet"
			1:
				Global.selcted_tile = Vector2i(9,11)
				Global.selected_factory_type = "cargoTerminal"
				Global.clickMode = "place_factory"
			2:
				Global.selcted_tile = Vector2i(0,12)
				Global.selected_terrain = Tiles.AIRSTRIP_DIRT
				Global.clickMode = "place_airstrip"
			3: 
				Global.selcted_tile = Vector2i(8,12)
				Global.selected_terrain = Tiles.AIRSTRIP_DIRT
				Global.clickMode = "place_taxiway"
		
		selectedY = 5.5 + (32 * index) + (4 * index)
		selectedX = 5.5
		extendedTileUISelcter_base.position = Vector2(selectedX, selectedY)

func displaySelected() -> void:
	if selectedTileImg.texture is AtlasTexture:
		var atlas = selectedTileImg.texture as AtlasTexture
		var atlasCoords: Vector2
		
		if Global.selcted_tile.y == 0 or Global.selcted_tile.y == 2:
			for factory in Tiles.factories:
				if Global.selcted_tile == factory["tile"]:
					atlasCoords = Vector2((factory["icon"].x * 32), (factory["icon"].y * 32))
		else:
			atlasCoords = Vector2((Global.selcted_tile.x * 32), (Global.selcted_tile.y * 32))
		
		atlas.region.position = atlasCoords


func _on_ovelay_button_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
#		overlayUi.visible = !overlayUi.visible
		pass

func _on_check_box_toggled(toggled_on):
	if toggled_on:
		tempuratureButton.set_pressed_no_signal(false)
		humidatyButton.set_pressed_no_signal(false)
		level.setOverlayMode(level.OverlayMode.HEIGHT)
	else:
		checkIfNoneActive()

func _on_check_box_2_toggled(toggled_on):
	if toggled_on:
		heightmapButton.set_pressed_no_signal(false)
		humidatyButton.set_pressed_no_signal(false)
		level.setOverlayMode(level.OverlayMode.TEMPERATURE)
	else:
		checkIfNoneActive()

func _on_check_box_3_toggled(toggled_on):
	if toggled_on:
		tempuratureButton.set_pressed_no_signal(false)
		heightmapButton.set_pressed_no_signal(false)
		level.setOverlayMode(level.OverlayMode.HUMIDITY)
	else:
		checkIfNoneActive()

func checkIfNoneActive() -> void:
	if not heightmapButton.button_pressed and not tempuratureButton.button_pressed and not humidatyButton.button_pressed:
		level.setOverlayMode(level.OverlayMode.NONE)


func _on_garage_menu_button_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		garageUI.visible = !garageUI.visible
		routeUI.visible = false
		tradeUI.visible = false


func _on_vehicle_tile_tractor_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		selectedVehicle = "valtra_s416"
		currentShopMode = shopMode.Viewing

func _on_vehicle_tile_articulatedDumpTruck_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		selectedVehicle = "liebherr_ta230_lintronic"
		currentShopMode = shopMode.Viewing

func _on_vehicle_tile_flatbedTruck_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		selectedVehicle = "flatbedTruck"
		currentShopMode = shopMode.Viewing

func _on_vehicle_tile_cementMixerTruck_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		selectedVehicle = "cementMixerTruck"
		currentShopMode = shopMode.Viewing

func _on_vehicle_tile_asphaltPaver_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed: 
		selectedVehicle = "Vögele_Super_1603-3i"
		currentShopMode = shopMode.Viewing

func _on_vehicle_tile_dozer_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		selectedVehicle = "Komatsu_D65PXI"
		currentShopMode = shopMode.Viewing

func _on_vehicle_tile_excavator_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		selectedVehicle = "Volvo_EC300DL"
		currentShopMode = shopMode.Viewing

func _on_vehicle_tile_flatbed_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		selectedVehicle = "flatbedTrailer"
		currentShopMode = shopMode.Viewing

func _on_vehicle_tile_bulkTrailer_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		selectedVehicle = "bulkTrailer"
		currentShopMode = shopMode.Viewing
		

func _on_vehicle_tile_ford_7810_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		selectedVehicle = "ford_7810"
		currentShopMode = shopMode.Viewing

func _on_shop_button_pressed():
	currentShopMode = shopMode.General
	shop.visible = true

func _on_shopExit_button_pressed():
	if currentShopMode == shopMode.Viewing:
		currentShopMode = shopMode.General
	else:
		currentShopMode = shopMode.None

func setTime() -> void:	
	if Global.speed_index == 1: 
		speedLabel.text = ">"
	elif Global.speed_index == 2:
		speedLabel.text = ">>"
	elif Global.speed_index == 3:
		speedLabel.text = ">>>"
	else:
		speedLabel.text = "P"
	yearLabel.text = str(2026+ Global.year)
	monthLabel.text = str(Global.month)
	if Global.minute >= 10:
		timeLabel.text = str(Global.hour) + ":" + str(Global.minute)
	else:
		timeLabel.text = str(Global.hour) + ":0" + str(Global.minute)


func _on_route_button_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		routeUI.visible = !routeUI.visible
		garageUI.visible = false
		tradeUI.visible = false

func format_number_with_separators(value: float, _decimals: int = 2) -> String:
	var formatted = "%.2f" % value
	
	var parts = formatted.split(".")
	var integer_part = parts[0]
	var decimal_part = parts[1] if parts.size() > 1 else "00"
	
	var result = ""
	var count = 0
	for i in range(integer_part.length() - 1, -1, -1):
		if count > 0 and count % 3 == 0:
			result = "." + result
		result = integer_part[i] + result
		count += 1
	
	return result + "," + decimal_part


func _on_trade_button_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		tradeUI.visible = !tradeUI.visible
		routeUI.visible = false
		garageUI.visible = false
