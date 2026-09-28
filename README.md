# Supply Chains - Godot Edition

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
- Custom UI and simulation systems

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
- Custom UI panels for economy and logistics
- Map generation with terrain and road systems
- Real-time route visualization and pathfinding

## Known Limitations & In-Progress Features ⚠️

Since this is a solo jam project with a tight deadline, some systems are incomplete or partially functional:

### High Priority (Currently Working On)
- 🔧 **Route visualization** - Dotted line display for offroad sections works, but needs polish
- 🔧 **Vehicle pathfinding** - A* grid works but needs refinement for edge cases
- 🔧 **Factory production chains** - Core logic in place, some recipes still need balancing

### Medium Priority (Partial Implementation)
- ⏳ **AI vehicle behavior** - Vehicles spawn but don't autonomously follow routes yet
- ⏳ **Save/Load system** - Not implemented; game state only persists during session
- ⏳ **Resource prices** - Static pricing; dynamic market fluctuation not implemented
- ⏳ **Repair/maintenance** - Vehicles can break but repair system incomplete
- ⏳ **Advanced factory logistics** - Multi-input/multi-output factories partially done

### Lower Priority (Cosmetic/Polish)
- 📋 **Sound effects** - No audio implementation yet
- 📋 **Tutorial system** - Gameplay is intuitive but formal tutorial missing
- 📋 **Advanced vehicle customization** - Basic vehicle types only; upgrades limited

## Getting Started

### Requirements

- Godot 4.x
- A local project folder with the `supply-chains/` project files

### Running the Game

1. Clone the repository:
   ```bash
   git clone https://github.com/thijmenb1/SupplyChains.git
   ```

2. Open the `supply-chains` folder in Godot 4

3. Press F5 or click Run to start the project

## License

This project is distributed under the MIT License unless otherwise noted.

---

**Game Jam Project | Godot 4 | Solo Dev | 2 Days to Launch 🚀**
