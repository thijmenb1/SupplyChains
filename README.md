# roadbalencer

## Gameplaly
In this game you have to transport resources mange vehicles and build a supply chain.

## Navigation
### Level.java:
- class Route               inner class that helps with routes for vehicles
- class Factory             keeps track off all the factories and there stats
- Level()                   constructor runs on world creation
- act()                     main loop it runs every frame
- handleCameraMovement()    moves the camera around the map with wasd
- generateTerrain()         generates the terrain with different tiles
- generateRiver()           generates a river with curves crossing over the map
- placeTileRandomly()       helper function for placing tiles in a random row and col
- loadTiles()               helper function to extract all tiles form the spritesheet
- drawMap()                 draws all tiles visible on screen
- getAnimatedTileId()       helper function for animated tiles
- getUpgradedTileId()       helper function for upgrading roads
- getPrice()                helper function that returns the build cost of a tile
- payResourceCost()         helper function that looks if the player has enough resource to pay and then pays
- refundResourceCost        helper function that adds the cost off a building
- 