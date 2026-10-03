extends Node

const TERAIN_SOURCE: int = 0
const ROAD_SOURCE: int = 1

const ROAD_TERRAIN_SET: int = 0
# not used for now
const TERAIN_TERRAIN_SET: int = 1

# Roads
const ROAD_TERRAIN: int = 0
const SINGEL_ROAD_GRAVEL : int = 10
const SINGEL_ROAD_CEMENT : int = 11
const SINGEL_ROAD_ASPHALT: int = 12
const AIRSTRIP_DIRT		 : int = 2
const CONSTRUCTION_MARKER: int = 4

const BASE: int = 1

const ROAD_COLOR_ROW := {
	SINGEL_ROAD_GRAVEL: 5,
	SINGEL_ROAD_CEMENT: 7,
	SINGEL_ROAD_ASPHALT: 9
}

# Terain
const WATER_ATLAS 		:= Vector2i(0,0)
const COLD_WATER_ATLAS	:= Vector2i(0,8)
const FORREST_ATLAS		:= Vector2i(0,1)
const GRASS_ATLAS		:= Vector2i(0,2)
const GRASS_ALT_ATLAS	:= Vector2i(1,2)
const SAND_ATLAS		:= Vector2i(0,3)
const COLD_SAND_ATLAS	:= Vector2i(0,9)
const SAVANA_ATLAS		:= Vector2i(0,4)
const SAVANA_ALT_ATLAS	:= Vector2i(1,4)
const SNOW_ATLAS		:= Vector2i(0,6)
const TOUNDRA_ATLAS		:= Vector2i(0,7)
const MOUNTAIN_ATLAS	:= Vector2i(0,10)

# Resource
const GOLD_ORE_ATLAS	:= Vector2i(0,17)
const IRON_ORE_ATLAS	:= Vector2i(1,17)
const COPPER_ORE_ATLAS	:= Vector2i(2,17)
const COAL_ORE_ATLAS	:= Vector2i(3,17)

const START_BOX := Vector2i(0,13)

# Factorys
const DIESEL_GENERATOR_ICON := Vector2i(0,0)
const DIESEL_GENERATOR		:= Vector2i(1,0)
const SOLAR_FARM_ICON		:= Vector2i(3,0)
const SOLAR_FARM			:= Vector2i(4,0)
const REFINARY_ICON			:= Vector2i(6,0)
const REFINARY				:= Vector2i(7,0)
const COAL_POWER_ICON		:= Vector2i(11,0)
const COAL_POWER			:= Vector2i(12,0)
const STEEL_MILL_ICON		:= Vector2i(14,0)
const STEEL_MILL			:= Vector2i(15,0)
const WIRE_MILL_ICON		:= Vector2i(17,0)
const WIRE_MILL				:= Vector2i(18,0)
const BLAST_FURNACE_ICON	:= Vector2i(0,2)
const BLAST_FURANCE			:= Vector2i(1,2)
const CONCRETE_PLANT_ICON	:= Vector2i(5,2)
const CONCRETE_PLANT		:= Vector2i(6,2)
const CARGO_TERMINAL 		:= Vector2i(9,11)
const PUMPJACK_ICON			:= Vector2i(8,2)
const PUMPJACK				:= Vector2i(9,2)

const factories = [
	{"tile": REFINARY,			"size": Vector2i(2,4),	"name": "refinary",		"icon": REFINARY_ICON},
	{"tile": STEEL_MILL,		"size": Vector2i(2,2),	"name": "steelmill",	"icon": STEEL_MILL_ICON},
	{"tile": WIRE_MILL, 		"size": Vector2i(2,2),	"name": "wiremill",		"icon": WIRE_MILL_ICON},
	{"tile": BLAST_FURANCE, 	"size": Vector2i(2,4),	"name": "blast",		"icon": BLAST_FURNACE_ICON},
	{"tile": CONCRETE_PLANT, 	"size": Vector2i(2,2),	"name": "cementMixing",	"icon": CONCRETE_PLANT_ICON},
	{"tile": PUMPJACK, 			"size": Vector2i(1,2),	"name": "pumpjack",		"icon": PUMPJACK_ICON},
	{"tile": DIESEL_GENERATOR,	"size": Vector2i(2,2),	"name": "gaspower",		"icon": DIESEL_GENERATOR_ICON},
	{"tile": SOLAR_FARM,		"size": Vector2i(2,2),	"name": "solarpanels",	"icon": SOLAR_FARM_ICON},
	{"tile": COAL_POWER,		"size": Vector2i(2,2),	"name": "coalpower",	"icon": COAL_POWER_ICON},
]
