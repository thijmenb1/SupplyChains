extends CanvasModulate

@export var color_gradient: Gradient
signal game_second_passed(total_seconds: int)

const MONTHS: Array = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]

var last_second: int = -1

func _process(delta: float) -> void:
	Global.elapsed_game_seconds += delta * Global.speed_tier
	
	var total_minutes := int(Global.elapsed_game_seconds / Global.SECONDS_PER_MINUTE)
	var current_day_minutes := total_minutes % Global.MINUTES_PER_DAY
	
	var gradient_value := float(current_day_minutes) / Global.MINUTES_PER_DAY
	self.color = color_gradient.sample(gradient_value)
	
	Global.day = int(total_minutes / Global.MINUTES_PER_DAY)
	Global.hour = int(current_day_minutes / Global.MINUTES_PER_HOUR)
	Global.minute = current_day_minutes % Global.MINUTES_PER_HOUR
	Global.year = Global.day / 12
	Global.month = MONTHS[Global.day % 12]
	
	var total_seconds := int(Global.elapsed_game_seconds)
	if total_seconds != last_second:
		game_second_passed.emit(total_seconds)
		last_second = total_seconds
