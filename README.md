# Supply Chains - Godot

A supply chain management simulation game built in Godot, where you transport resources, manage vehicles, and build efficient logistics systems.

> **⏰ Game Jam Status:** Active development | Deadline: 2 days remaining

## About the Project

Supply Chains is a strategic logistics and resource-management game built in Godot 4. Players are tasked with:

- Transporting resources between production and destination points
- Managing a growing vehicle fleet
- Building and improving infrastructure
- Creating efficient delivery routes
- Optimizing profits while balancing costs and logistics

The project focuses on simulation, route planning, vehicle management, and resource flow rather than a traditional Java/legacy web app stack.

## Built With

- Godot 4
- GDScript
- AStar pathfinding for route and grid logic

## Project Structure

### Core Game Systems

- `supply-chains/scripts/Singletons/Global.gd`
  - Shared game state
  - Money, resources, vehicle garage, and route data
- `supply-chains/scripts/Singletons/GridManager.gd`
  - Grid placement logic
  - Building occupancy checks
  - A* pathfinding and road weight management
- `supply-chains/scripts/Vehicles/`
  - Vehicle logic, engines, attachments, and behavior
- `supply-chains/scripts/UI/`
  - Shop, trade, garage, route management, HUD, and selection panels
- `supply-chains/scripts/Factories/`
  - Factory and production logic
- `supply-chains/assets/`
  - Art, tiles, and vehicle assets

### Main Features

- Vehicle management and garage system
- Trailer and attachment support
- Factory and route-based resource flow
- Trade order system
- Map generation with terrain and road systems
- Real-time route calculation and pathfinding

## Known Limitations & In-Progress Features ⚠️

Since I am a solo dev a lot of features did not get polished or straight up dont work i have tryed my best to make the game as fun as posible with thes limitations but not everything is perfect.
Things that dont work (yet):
- the articulated dumptruck. just didnt have time to make the articulation work

**Game Jam Project | Godot 4 | Solo Dev | 2 Days to Launch 🚀**
