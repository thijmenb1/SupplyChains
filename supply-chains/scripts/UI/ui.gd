extends CanvasLayer

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

@onready var pause_menu: Control = $Control/pause_menu

@onready var money: Label = $Control/MoneyUI/Money

@onready var popUpText: Label = $Control/PopUPText
@onready var skipTutorial: Label = $Control/skipTutorial

var selectedVehicle: String
var currentShopMode: shopMode = shopMode.General
enum shopMode {None, General, Viewing}

var popup_time_left: float = 0.0

const TUTORIAL_TOTAL: int = 14

func _ready() -> void:
	Global.popup_requested.connect(_on_popup_requested)
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
		bottomLeftButtons.visible = true
	else:
		bottomLeftButtons.visible = false
	
	money.text = "€" + format_number_with_separators(Global.money)
	
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
	
	if skipTutorial:
		skipTutorial.visible = Global.build_stage == Global.BuildStage.READY and not Global.first_trade_done
	else:
		print("there is something wrong")
	
	if popup_time_left > 0.0:
		popup_time_left -= _delta
		if popup_time_left <= 0.0:
			popUpText.text = ""	
	elif popup_time_left <= 0.0:
		if Global.build_stage != Global.BuildStage.READY:
			popUpText.text = get_tutorial_text()
		elif Global.baseTiles.size() == 0:
			popUpText.text = "You first have to place a base"
		elif Global.baseTiles.size() < Global.BASE_TILES_REQUIRED:
			popUpText.text = "Your base must be at least %d tiles" % Global.BASE_TILES_REQUIRED
		else:
			var hint: String = get_route_hint_text()
			if hint == "":
				hint = get_production_hint_text()
			popUpText.text = hint

func get_tutorial_text() -> String:
	var prefix: String = "Step %d/6: " % Global.get_tutorial_step()
	match Global.build_stage:
		Global.BuildStage.PLACE_RUNWAY:
			return prefix + "Build a runway. Click and drag on open ground (at least %d tiles long)" % level.AIRSTRIP_MIN_LENGTH
		Global.BuildStage.PLACE_RESOURCE_BOX:
			return prefix + "Place the resource box close to the runway (it holds the iron beams for the terminal)"
		Global.BuildStage.PLACE_TERMINAL:
			return prefix + "Place the cargo terminal close to the runway, your vehicles will build it from the box"
		Global.BuildStage.PLACE_TAXIWAY:
			if Global.airstrips.is_empty():
				return prefix + "Waiting for the runway to finish building..."
			return prefix + "Click and drag a taxiway to connect the terminal to the runway"
		Global.BuildStage.PLACE_BASE_TILES:
			return prefix + "Place %d base tiles for your vehicles to park (%d/%d)" % [Global.BASE_TILES_REQUIRED, Global.baseTiles.size(), Global.BASE_TILES_REQUIRED]
	return ""

func _step(number: int, text: String) -> String:
	return "Step %d/%d: %s" % [number, TUTORIAL_TOTAL, text]

func _building_type(b) -> String:
	if b is FactoryInstance:
		return b.factory_type
	if b is ConstructionSite:
		return b.result.get("factory_type", "")
	return ""

func _has_factory(type: String) -> bool:
	for f in Global.factorys:
		if f.factory_type == type:
			return true
	return false

func _has_site(type: String) -> bool:
	for s in Global.construction_sites:
		if s.result.get("factory_type", "") == type:
			return true
	return false

func _has_mine_with(resource: String) -> bool:
	for f in Global.factorys:
		if f.factory_type == "mine" and f.mine_resources.has(resource):
			return true
	return false

func _find_route(src_type: String, dst_type: String, resource: String) -> int:
	for i in Global.routes.size():
		var r: Dictionary = Global.routes[i]
		if r.is_empty():
			continue
		if _building_type(r.get("source")) != src_type or _building_type(r.get("destination")) != dst_type:
			continue
		if r.get("resource", "") != resource:
			continue
		if src_type == "mine" and not r["source"].mine_resources.has(resource):
			continue
		return i
	return -1

func _route_has_vehicle(route_index: int) -> bool:
	for vehicle in VehicleManager.get_all_vehicles().values():
		if vehicle == null:
			continue
		var movement = vehicle.get("movement")
		if movement != null and movement.assigned_route_index == route_index:
			return true
	return false

func get_production_hint_text() -> String:
	var terminal: FactoryInstance = Global.get_warehouse()
	if Global.first_trade_done or terminal == null:
		return ""
	
	var blast_built: bool = _has_factory("blast")
	var blast_site: bool = _has_site("blast")
	var gravel_stock: int = terminal.outputResources.get("gravel", 0)
	
	if not blast_built and not blast_site:
		if gravel_stock >= 5:
			return _step(9, "Build a blast furnace: select it in the build menu (or press 5) and place it near your base. It turns iron ore into iron beams (needs 4 iron beams and 5 gravel)")
		if not _has_mine_with("gravel"):
			return _step(7, "Buy an excavator in the shop, select it, choose mine and drag over an area with gravel. The blast furnace needs 5 gravel")
		var gravel_route: int = _find_route("mine", "cargoTerminal", "gravel")
		if gravel_route == -1:
			return _step(8, "Open the route menu and create a route from your mine to the cargo terminal for gravel")
		if not _route_has_vehicle(gravel_route):
			return _step(8, "Assign a hauling vehicle to %s (buy one in the shop if all are busy) so it brings the gravel to the terminal" % Global.get_route_display_name(gravel_route))
		return _step(8, "Gravel is on its way to the terminal (%d/5 in stock)" % gravel_stock)
	
	if blast_site and not blast_built:
		for i in Global.routes.size():
			var r: Dictionary = Global.routes[i]
			if r.is_empty() or not r.has("construction_type"):
				continue
			if not _route_has_vehicle(i):
				return _step(10, "Assign a vehicle to %s so it delivers the materials for the blast furnace (buy another vehicle if all are busy)" % Global.get_route_display_name(i))
		return _step(10, "Your vehicles are delivering the materials for the blast furnace, please wait")
	
	if not _has_mine_with("ironOre"):
		return _step(11, "Place a mine on iron ore. An excavator works one mine, so buy a second excavator, select it, choose mine and drag over the iron ore")
	
	var ore_route: int = _find_route("mine", "blast", "ironOre")
	if ore_route == -1:
		return _step(12, "Create a route from the iron ore mine to the blast furnace (resource: ironOre)")
	if not _route_has_vehicle(ore_route):
		return _step(12, "Assign a hauling vehicle to %s so it brings the iron ore to the blast furnace" % Global.get_route_display_name(ore_route))
	
	var beam_route: int = _find_route("blast", "cargoTerminal", "ironBeam")
	if beam_route == -1:
		return _step(13, "Create a route from the blast furnace to the cargo terminal (resource: ironBeam)")
	if not _route_has_vehicle(beam_route):
		return _step(13, "Assign a vehicle to %s so it brings the iron beams from the furnace to the terminal" % Global.get_route_display_name(beam_route))
	
	var stock: int = terminal.outputResources.get("ironBeam", 0)
	var wanted: int = -1
	for order in Global.trade_orders:
		if order["resource"] != "ironBeam":
			continue
		if order["accepted"]:
			return _step(14, "A plane is picking up your iron beams, you get paid when it has loaded")
		if wanted == -1 or order["quantity"] < wanted:
			wanted = order["quantity"]
	
	if wanted == -1:
		return _step(14, "Wait for a trader to ask for iron beams in the trade menu. Keep producing so you have stock ready")
	if stock >= wanted:
		return _step(14, "Open the trade menu and accept the iron beam order. A plane lands on your runway, picks up the beams and pays you")
	return _step(14, "A trader wants %d iron beams but the terminal has %d. Keep the production running" % [wanted, stock])

func get_route_hint_text() -> String:
	if Global.get_warehouse() != null:
		return ""
	var terminal_site: ConstructionSite = null
	for site in Global.construction_sites:
		if site.result.get("factory_type", "") == "cargoTerminal":
			terminal_site = site
	if terminal_site == null:
		return ""
	
	if terminal_site.is_fully_supplied():
		return ""
	
	var has_route: bool = false
	for vehicle in VehicleManager.get_all_vehicles().values():
		if vehicle == null:
			continue
		var movement = vehicle.get("movement")
		if movement != null and movement.assigned_route_index != -1:
			has_route = true
			break
	
	if has_route:
		var needed: int = 0
		var delivered: int = 0
		for res in terminal_site.required_resources:
			needed += terminal_site.required_resources[res]
			delivered += terminal_site.deliverd_resources.get(res, 0)
		return "Step 6/6: Your tractor is bringing the iron beams to the terminal (%d/%d delivered) \n press 9 too speed up time and us 8 to slow it back down" % [delivered, needed]
	
	var route_name: String = "the route"
	for i in Global.routes.size():
		var route: Dictionary = Global.routes[i]
		if not route.is_empty() and route.get("destination") == terminal_site:
			route_name = Global.get_route_display_name(i)
			break
	
	return "Step 6/6: Click your tractor, choose %s in its route menu and click to assign it, so it delivers the iron beams to the terminal" % route_name

func _on_popup_requested(message: String, duration: float) -> void:
	popUpText.text = message
	popup_time_left = duration

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
		Global.show_factory_cost(Global.selected_factory_type)
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
				Global.show_factory_cost("cargoTerminal")
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


func _on_menu_gui_input(event):
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		get_tree().paused = true
		pause_menu.visible = true

func _unhandled_key_input(event: InputEvent) -> void:
	if not event is InputEventKey or not event.pressed or event.echo:
		return
	if event.keycode != KEY_ENTER and event.keycode != KEY_KP_ENTER:
		return
	if Global.build_stage != Global.BuildStage.READY or Global.first_trade_done:
		return
	Global.first_trade_done = true
	popUpText.text = ""
	Global.show_popup("Tutorial skipped", 3.0)
