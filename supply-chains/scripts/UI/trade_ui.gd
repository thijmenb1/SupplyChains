extends Panel

const TRADE_TILE_SCENE := preload("res://scenes/UI/tradeTile.tscn")
@onready var Vbox : VBoxContainer = $ScrollContainer/VBoxContainer

func _process(delta):
	if Vbox.get_child_count() != Global.trade_orders.size():
		populate()

func _open_order_count() -> int:
	var count := 0
	for order in Global.trade_orders:
		if not order["accepted"]:
			count += 1
	return count

func populate() -> void:
	for child in Vbox.get_children():
		child.queue_free()
	
	for i in Global.trade_orders.size():
		var order = Global.trade_orders[i]
		if order == null or order == {}:
			push_error("order is empty", order)
			return
		
		if order["accepted"] == true:
			continue
		var new_card = TRADE_TILE_SCENE.instantiate()
		new_card.order = order
		new_card.index = i
		Vbox.add_child(new_card)
