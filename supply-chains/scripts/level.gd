extends Node2D

@export var noiseHeigthText : NoiseTexture2D
@export var noiseTempText: NoiseTexture2D
@export var noiseMoistText: NoiseTexture2D

@export var TerrainLayer : TileMapLayer
@export var RoadLayer: TileMapLayer
@export var DecorationLayer: TileMapLayer
@export var PreviewLayer: TileMapLayer
@export var camera: Camera2D

#generation vars
var noise_alt: Noise
var noise_temp: Noise
var noise_moist: Noise

var chunk_size: int = 16
var render_distance: int = 14
var loaded_chunks: Dictionary = {} # Keeps track of already generated chunks
var world_size : int = 300   #chunks

#atlas vars
var sourceId := 1
var water_atlas := Vector2i(0,0)
var grass_atlas := Vector2i(2,0)
var grass_alt_atlas := Vector2i(4,1)
var sand_atlas := Vector2i(3,0)
var forrestGrass_atlas= Vector2i(1,0)
var snow_atlas := Vector2i(5,0)
var savanne_atlas := Vector2i(6,0)
var savanne_alt_atlas := Vector2i(3, 1)
var toendra_atlas := Vector2i(7,0)
var mountain_atlas := Vector2i(7,1)
var coldWater_atlas := Vector2i(5,1)
var coldSand_atlas := Vector2i(6,1)
var tree_atlas := Vector2i(0,1)

var selected_terrain: int = 0

var selectedCell

func _ready():
	# Randomize seeds
	noiseHeigthText.noise.seed = randi()
	noiseTempText.noise.seed = randi()
	noiseMoistText.noise.seed = randi()
	
	noise_alt = noiseHeigthText.noise
	noise_temp = noiseTempText.noise
	noise_moist = noiseMoistText.noise


func _process(_delta):
	selectedCell = RoadLayer.local_to_map(RoadLayer.get_global_mouse_position())
	if camera:
		update_chunks_around_camera()
	
	if Input.is_action_just_pressed("select_1"):
		selected_terrain = 0
	if Input.is_action_just_pressed("select_2"):
		selected_terrain = 1
	if Input.is_action_just_pressed("select_3"):
		selected_terrain = 2
	if Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		paint_road()
	if Input.is_mouse_button_pressed(MOUSE_BUTTON_RIGHT):
		erase_road()
	
	if PreviewLayer:
		PreviewLayer.clear()
		if not Input.is_mouse_button_pressed(MOUSE_BUTTON_RIGHT):
			previewTile()
	
func paint_road():
	RoadLayer.set_cells_terrain_connect([selectedCell], 0, selected_terrain, false)

func erase_road():
	RoadLayer.erase_cell(selectedCell)


func update_chunks_around_camera() -> void:
	var cam_tile_pos = TerrainLayer.local_to_map(camera.global_position)
	var cam_chunk_x = floor(float(cam_tile_pos.x) / chunk_size)
	var cam_chunk_y = floor(float(cam_tile_pos.y) / chunk_size)
	
	for x in range(cam_chunk_x - render_distance, cam_chunk_x + render_distance + 1):
		for y in range(cam_chunk_y - render_distance, cam_chunk_y + render_distance + 1):
			var chunk_key = Vector2i(x, y)
			if not loaded_chunks.has(chunk_key):
				generate_chunk(x, y)
				loaded_chunks[chunk_key] = true


func generate_chunk(chuck_x : int, chunk_y: int) -> void:
	var start_x = chuck_x * chunk_size
	var start_y = chunk_y * chunk_size
	
	for x in range(start_x, start_x + chunk_size):
		for y in range(start_y, start_y + chunk_size):
			setBiome(x,y)



func setBiome(x: int, y: int) -> void:
	var altitude = noise_alt.get_noise_2d(x,y)
	var base_temp = noise_temp.get_noise_2d(x,y)
	var moisture = noise_moist.get_noise_2d(x,y)
	
	# 0 at the middle, 1 at the poles
	var latitude = abs((float(y) / (world_size * chunk_size)) * 2.0 - 1.0)
	var temperature = base_temp
	temperature += (1.0 - latitude) * 0.4
	temperature -= max(altitude, 0.0) * 0.4
	var coords = Vector2i(x, y)
	
	if altitude <= -0.10:
		if temperature < -0.55:
			TerrainLayer.set_cell(coords, sourceId, coldWater_atlas)
		else:
			TerrainLayer.set_cell(coords, sourceId, water_atlas)
	elif between(altitude, -0.10, -0.05):
		if temperature < -0.55:
			TerrainLayer.set_cell(coords, sourceId, coldSand_atlas)
		else:
			TerrainLayer.set_cell(coords, sourceId, sand_atlas)
	elif between(altitude, -0.05, 0.45):
		if temperature < -0.55:
			TerrainLayer.set_cell(coords, sourceId, snow_atlas)
		elif temperature < -0.25:
			TerrainLayer.set_cell(coords, sourceId, toendra_atlas)
		elif temperature < 0.30:
			if moisture < -0.2:
				var random: float = randf()
				if random <= 0.15:
					TerrainLayer.set_cell(coords, sourceId, grass_alt_atlas)
				else:
					TerrainLayer.set_cell(coords, sourceId, grass_atlas)
			else:
				var random: float = randf()
				if random <= 0.15:
					DecorationLayer.set_cell(coords, sourceId, tree_atlas)
				TerrainLayer.set_cell(coords, sourceId, forrestGrass_atlas)
		else:
			if moisture < -0.15:
				TerrainLayer.set_cell(coords, sourceId, sand_atlas)
			else:
				var random: float = randf()
				if random <= 0.15:
					TerrainLayer.set_cell(coords, sourceId, savanne_alt_atlas)
				else:
					TerrainLayer.set_cell(coords, sourceId, savanne_atlas)
	elif altitude > 0.45:
		if temperature < -0.2:
			TerrainLayer.set_cell(coords, sourceId, snow_atlas)
		else:
			TerrainLayer.set_cell(coords, sourceId, mountain_atlas)
	
func between(val : float, start: float, end: float) -> bool:
	if val >= start and val < end:
		return true
	return false

func previewTile():
	if not PreviewLayer:
		return
	PreviewLayer.set_cells_terrain_connect([selectedCell], 0, selected_terrain, false)
