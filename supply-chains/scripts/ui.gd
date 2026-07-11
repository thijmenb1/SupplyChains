extends CanvasLayer

@onready var ironBarLab: Label = $Control/ResourceBar/TextureRect/Label
@onready var copperSpoolLab: Label = $Control/ResourceBar/TextureRect2/Label
@onready var sulferCrateLab: Label = $Control/ResourceBar/TextureRect3/Label
@onready var cabelSpoolLab: Label = $Control/ResourceBar/TextureRect4/Label
@onready var copperSulfideIBCLab: Label = $Control/ResourceBar/TextureRect5/Label
@onready var steelBarLab: Label = $Control/ResourceBar/TextureRect6/Label
@onready var PCBCrateLab: Label = $Control/ResourceBar/TextureRect7/Label
@onready var Lab1: Label = $Control/ResourceBar/TextureRect8/Label
@onready var lab2: Label = $Control/ResourceBar/TextureRect9/Label

@onready var selectedTileImg: TextureRect = $Control/SelcetedTile/TextureRect

func _process(_delta):
	ironBarLab.text = str(Global.resources["ironBeam"])
	copperSpoolLab.text = str(Global.resources["copperSpool"])
	sulferCrateLab.text = str(Global.resources["sulferPallet"])
	cabelSpoolLab.text = str(Global.resources["cabelSpool"])
	copperSulfideIBCLab.text = str(Global.resources["copperSulfideIBC"])
	steelBarLab.text = str(Global.resources["steelBeam"])
	PCBCrateLab.text = str(Global.resources["PCBPallet"])
