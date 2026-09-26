extends VBoxContainer

@onready var cardScene := preload("res://scenes/UI/accepted_order_card.tscn")

func _process(delta):
	var accepted: Array = []
	for order in Global.trade_orders:
		if order["accepted"] == true:
			accepted.append(order)

	if get_child_count() != accepted.size():
		for child in get_children():
			child.queue_free()

		for i in accepted.size():
			var order = accepted[i]
			var card = cardScene.instantiate()
			card.order = order
			card.mode = "landing"
			card.index = i
			add_child(card)
