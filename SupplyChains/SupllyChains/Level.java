import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* Level - the main game world
*
* Functions:
* - Level()                 constructor
* - getCameraX -Y()         returns camera offsets
* - act()                   Main loop
* - handleCameraMovement()  Moves camera
* - generateTerrain()       Randomizes the map
* - generateRiver()         Makes river
* - placeTileRandomly()     Helper function for randomizing the map
* - spawnVehicle()          Helper function for creation of the vehicle
* - getDrillColor()         Helper function to check adjacent drill colors
* - isAdjacentToPickup()    Helper function to check for valid route
* - isAdjacentToDropoff()   Helper function to check for valid route
* - loadTiles()             Extracts all tiles out of spritesheet
* - drawMap()               Draws tiles in the viewport
* - getAnimatedTileId()     Returns next frame for animated tiles
* - highlightTile()         Highlights the tile that would be effected if effect applied
* - drawRoads()             Helper function for left and right click helps with dragging and pressing
* - handleLeftClick()       Handles every function that uses leftclick
* - handleRightClick        Handles every function that uses Rightclick
* - selectTile()            Does quick selection and rotation
* - updateSelectedTile()    Helper function for updating the selected tile
* - rotate()                Helper function for rotating selected tile returns next rotation 
* - rotateThrough()         Helper function for rotating selected tile
* - isValidPosition()       Helper function for checking if space is valid
*
*/

public class Level extends World
{
    // Display & rendering constants
    private static final int tileSize = 32;
    public static final int worldWidth = 1280;
    public static final int worldHight = 720;
    
    // Camera
    private static int cameraX = 0;
    private static int cameraY = 0;
    private static final int cameraSpeed = 5;

    // Ui coordinates
    private static final int UIPanel_X = 1550;
    private static final int UIPanel_Y = 650;

    //All tiles
    public static GreenfootImage[] tiles;

    // Tile placement / removal tracking
    private int effected_col;
    private int effected_row;
    public static int selectedTile = 1;
    private boolean building = false;
    private boolean removing = false;
    private boolean rKeyWasDown = false;

    // Animation
    private int animationCounter = 0;
    private static final int animationSpeed = 15;

    // Terrain Generation
    private static final int forest_SpawnChance = 10;
    private static final int house_SpawnChance = 3;
    private static final int resource_SpawnChance = 1;

    // TILE IDS - Terrain
    private static final int grass = 0;
    private static final int forest = 56;
    private static final int house = 57;

    // TILE IDS - Roads
    private static final int road_V_gravel = 1;
    private static final int road_H_gravel = 2;
    private static final int cross_gravel = 3;
    private static final int tJunction_up_gravel = 4;
    private static final int tJunction_right_gravel = 5;
    private static final int tJunction_down_gravel = 6;
    private static final int tJunction_left_gravel = 7;
    private static final int corner_NE_gravel = 8;
    private static final int corner_NW_gravel = 9;
    private static final int corner_SW_gravel = 10;
    private static final int corner_SE_gravel = 11;

    private static final int road_V_cement = 16;
    private static final int road_H_cement = 17;
    private static final int cross_cement = 18;
    private static final int tJunction_up_cement = 19;
    private static final int tJunction_right_cement = 20;
    private static final int tJunction_down_cement = 21;
    private static final int tJunction_left_cement = 22;
    private static final int corner_NE_cement = 23;
    private static final int corner_NW_cement = 24;
    private static final int corner_SW_cement = 25;
    private static final int corner_SE_cement = 26;

    private static final int road_V_asphalt = 32;
    private static final int road_H_asphalt = 33;
    private static final int cross_asphalt = 34;
    private static final int tJunction_up_asphalt = 35;
    private static final int tJunction_right_asphalt = 36;
    private static final int tJunction_down_asphalt = 37;
    private static final int tJunction_left_asphalt = 38;
    private static final int corner_NE_asphalt = 39;
    private static final int corner_NW_asphalt = 40;
    private static final int corner_SW_asphalt = 41;
    private static final int corner_SE_asphalt = 42;

    // TILE IDS - Depots
    private static final int depot_left_gravel = 12;
    private static final int depot_down_gravel = 13;
    private static final int depot_right_gravel = 14;
    private static final int depot_up_gravel = 15;

    private static final int depot_left_cement = 28;
    private static final int depot_down_cement = 29;
    private static final int depot_right_cement = 30;
    private static final int depot_up_cement = 27;

    private static final int depot_left_asphalt = 44;
    private static final int depot_down_asphalt = 45;
    private static final int depot_right_asphalt = 31;
    private static final int depot_up_asphalt = 43;

    // TILE IDS - Resources
    private static final int sand = 58;
    private static final int gravel = 59;
    private static final int steel_oreDeposit = 60;
    private static final int copper_oreDeposit = 61;
    private static final int sulfur_oreDeposit = 62;

    // TILE IDS - Factories
    private static final int factory_steel = 76;
    private static final int factory_copper = 77;
    private static final int factory_sulfur = 78;
    private static final int factory_cable = 79;
    private static final int factory_copperSulfate = 80;
    private static final int factory_hardenedSteel = 81;
    private static final int factory_PCB = 82;
    private static final int factory_cement = 83;

    // Tile IDS - Storages Places
    private static final int warehouse = 84;
    private static final int bulkStorage_plant = 85;

    // TILE IDS - Rivers
    private static final int river_V = 48;
    private static final int river_H = 49;
    private static final int riverCrossing_V_cement = 50;
    private static final int riverCrossing_H_cement = 51;
    private static final int riverCrossing_V_asphalt = 46;
    private static final int riverCrossing_H_asphalt = 47;
    private static final int riverCorner_NE = 52;
    private static final int riverCorner_NW = 53;
    private static final int riverCorner_SW = 54;
    private static final int riverCorner_SE = 55;

    // TILE IDS - Drills & Animation
    private static final int drill = 67;
    private static final int drill_steel_1_1 = 68;
    private static final int drill_steel_1_2 = 64;
    private static final int drill_copper_1_1 = 69;
    private static final int drill_copper_1_2 = 65;
    private static final int drill_sulfur_1_1 = 70;
    private static final int drill_sulfur_1_2 = 66;
    private static final int drill_steel_2 = 72;
    private static final int drill_copper_2 = 73;
    private static final int drill_sulfur_2 = 74;
    private static final int excavator = 75;

    private static final int[] gravel_tiles = {road_H_gravel, road_V_gravel, cross_gravel, tJunction_down_gravel, tJunction_left_gravel, tJunction_right_gravel, tJunction_up_gravel, corner_NE_gravel, corner_NW_gravel, corner_SE_gravel, corner_SW_gravel, depot_down_gravel, depot_left_gravel, depot_right_gravel, depot_up_gravel};
    private static final int[] cement_tiles = {road_H_cement, road_V_cement, cross_cement, tJunction_down_cement, tJunction_left_cement, tJunction_right_cement, tJunction_up_cement, corner_NE_cement, corner_NW_cement, corner_SE_cement, corner_SW_cement, depot_down_cement, depot_left_cement, depot_right_cement, depot_up_cement};
    private static final int[] asphalt_tiles = {road_H_asphalt, road_V_asphalt, cross_asphalt, tJunction_down_asphalt, tJunction_left_asphalt, tJunction_right_asphalt, tJunction_up_asphalt, corner_NE_asphalt, corner_NW_asphalt, corner_SE_asphalt, corner_SW_asphalt, depot_down_asphalt, depot_left_asphalt, depot_right_asphalt, depot_up_asphalt};

    public static int base_row = 1;
    public static int base_col = 1;

    public static int[] storedResources = {100, 100, 100, 100, 100, 100, 100, 100, 100, 100}; // 1 = steel_ore, 2 = copper_ore, 3 = sulfur_ore, 4 = cables, 5 = copperSulfate, 6 = hardenedSteel, 7 = PCB, 8 = cement, 9 = gravel , 10 = sand

    // Direction vectors
    private static final int[][] adjacentDirections = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public static int[][] map = new int[100][100];
    
    public static HashMap <String, Integer> garage = new HashMap<>();
    public static ArrayList<Route> routes = new ArrayList<>();
    public static HashMap<String, Factory> factories = new java.util.HashMap<>();
    
    // Route is a inner class that handles the routes
    public static class Route 
    {
        private int startRow;
        private int startCol;
        private int endRow;
        private int endCol;
        private int vehicleCount;
        
        public Route(int startRow, int startCol, int endRow, int endCol, int vehicleCount)
        {
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
            this.vehicleCount = vehicleCount;
        }
        
        public int      getStartRow()                       { return startRow; }
        public void     setStartRow(int startRow)           { this.startRow = startRow; }
        public int      getStartCol()                       { return startCol; }
        public void     setStartCol(int startCol)           { this.startCol = startCol; }
        public int      getEndRow()                         { return endRow; }
        public void     setEndRow(int endRow)               { this.endRow = endRow; }
        public int      getEndCol()                         { return endCol; }
        public void     setEndCol(int endCol)               { this.endCol = endCol; }
        public int      getVehicleCount()                   { return vehicleCount; }
        public void     setVehicleCount(int count)          { this.vehicleCount = count; }
    }

    // Factory is a inner class that handles the factories
    public static class Factory
    {
        private String factoryType;
        private String outputResource;  // What resources the factory can produce

        private HashMap<String, Integer> inputInventory;      // Resource name, Current count stored
        private HashMap<String, Integer> recipeRequirements;  // Resource name, Amount needed to craft

        private float constructionTime;
        private int processedOutputs;

        public Factory(String factoryType)
        {
            this.constructionTime = 180f;
            this.factoryType = factoryType;
            this.processedOutputs = 0;
            this.inputInventory = new HashMap<>();
            this.recipeRequirements = new HashMap<>();

            setRecipe();
        }

        private void setRecipe()
        {
            switch (factoryType) {
                case "steel":
                    recipeRequirements.put("steel_ore", 2);
                    this.outputResource = "steel_beam";
                    break;
            
                case "copper":
                    recipeRequirements.put("copper_ore", 2);
                    this.outputResource = "copper_spool";
                    break;
                
                case "sulfur":
                    recipeRequirements.put("sulfur_ore", 2);
                    this.outputResource = "sulfur_pallet";
                    break;

                case "cable":
                    recipeRequirements.put("copper_spool", 1);
                    this.outputResource = "cable_spool";
                    constructionTime = 360f;
                    break;
                
                case "copperSulfate":
                    recipeRequirements.put("copper_ore", 1);
                    recipeRequirements.put("sulfur_pallet", 1);
                    this.outputResource = "copperSulfate_IBC";
                    break;

                case "hardenedSteel":
                    recipeRequirements.put("steel_beam", 1);
                    recipeRequirements.put("copperSulfate_IBC", 1);
                    this.outputResource = "hardenedSteel_beam";
                    break;
                
                case  "PCB":
                    recipeRequirements.put("steel_beam", 1);
                    recipeRequirements.put("copper_spool", 2);
                    this.outputResource = "PCB_pallet";
                    break;

                case "cement":
                    recipeRequirements.put("gravel", 1);
                    recipeRequirements.put("sand", 1);
                    this.outputResource = "cement";
                    break;

                default:
                    System.out.println("No valid factoryType found");
                    System.out.println(factoryType + "Is not a valid type");
                    break;
            }          
        }


        public boolean acceptsResource(String resourceName)
        {
            return this.recipeRequirements.containsKey(resourceName);
        }

        public void addResource(String resourceName)
        {
            if (acceptsResource(resourceName))
            {
                int currentAmount = inputInventory.getOrDefault(resourceName, 0);
                int maxBuffer = recipeRequirements.get(resourceName) * 3; // Buffer up to 3 batches max

                if (currentAmount < maxBuffer)
                {
                    inputInventory.put(resourceName, currentAmount + 1);
                }
            }
        }

        public boolean hasAllIngredients()
        {
            for (String resName : recipeRequirements.keySet())
            {
                int stored = inputInventory.getOrDefault(resName, 0);
                int needed = recipeRequirements.get(resName);
                if (stored < needed)
                {
                    return false;
                }
            }
            return true;
        }

        private void consumeIngredients()
        {
            for (String resName : recipeRequirements.keySet())
            {
                int currentAmount = inputInventory.get(resName);
                int needed = recipeRequirements.get(resName);
                inputInventory.put(resName, currentAmount - needed);
            }
        }

        public String pickupResource()
        {
            if (this.processedOutputs > 0)
            {
                this.processedOutputs--;
                return this.outputResource;
            }
            return null;
        }

        public void processesResources()
        {   if (hasAllIngredients())
            {
                if (constructionTime > 0)
                {
                    constructionTime--;
                }

                if (constructionTime == 180)
                {
                    consumeIngredients();
                }

                if (constructionTime <= 0)
                {
                    processedOutputs++;
                    constructionTime = 180;
                }
            }
        }

        public void act()
        {
            processesResources();
        }

        public int getStoredResources()
        {
            int total = 0;
            for (int count : inputInventory.values())
            {
                total += count;
            }
            return total;
        }

        public float    getConstructionTime()       {return constructionTime; }
        public int      getProcessedResources()     {return processedOutputs;}
        public String   getOutputResource()         {return outputResource; }
    }

    // Constructor
    public Level()
    {
        super(worldWidth, worldHight, 1, false);
        loadTiles();
        generateTerrain();
        generateRiver();
        placeTileRandomly(warehouse);
        drawMap();
        addObject(new ui(), UIPanel_X, UIPanel_Y);
    }
    
    // Returns camera offsets
    public int getCameraX() { return cameraX; }
    public int getCameraY() { return cameraY; }
    
    // Main loop
    public void act()
    {
        selectTile();
        handleCameraMovement();

        animationCounter++;

        for (Factory factory : factories.values())
        {
            factory.act();
        }

        drawMap();
        drawRoads();
        highlightTile();
    }
    
    // Camera movement clamped to map edges
    private void handleCameraMovement()
    {
        int newCameraX = cameraX;
        int newCameraY = cameraY;
        
        if (Greenfoot.isKeyDown("w"))
        {
            newCameraY -= cameraSpeed;
        }
        if (Greenfoot.isKeyDown("s"))
        {
            newCameraY += cameraSpeed;
        }
        if (Greenfoot.isKeyDown("a"))
        {
            newCameraX -= cameraSpeed;
        }
        if (Greenfoot.isKeyDown("d"))
        {
            newCameraX += cameraSpeed;
        }
        
        // Clamp camera to map bounds
        int maxCameraX = (map[0].length * tileSize) - worldWidth;
        int maxCameraY = (map.length * tileSize) - worldHight;
        
        cameraX = Math.max(0, Math.min(newCameraX, maxCameraX));
        cameraY = Math.max(0, Math.min(newCameraY, maxCameraY));
    }

    // Randomizes the map
    private void generateTerrain()
    {
        // Track if each resource has been placed yet
        boolean[] resourcePlaced = new boolean[5]; //0 = sand, 1 = gravel, 2 = steel_ore, 3 = copper_ore, 4 = sulfur_ore

        for (int row = 0; row < map.length; row++)
        {
            for (int col = 0; col < map[row].length; col++)
            {
                int random = Greenfoot.getRandomNumber(100);

                if (random < forest_SpawnChance)
                {
                    map[row][col] = forest;
                }
                else if (random < forest_SpawnChance + house_SpawnChance)
                {
                    map[row][col] = house;
                }
                else if (random < forest_SpawnChance + house_SpawnChance + resource_SpawnChance)
                {
                    // Pick a random resource type
                    int resourceIndex = Greenfoot.getRandomNumber(5);
                    map[row][col] = sand + resourceIndex;
                    resourcePlaced[resourceIndex] = true;
                }
                else
                {
                    map[row][col] = grass;
                }
            }
        }

        // Guarantee at least 1 of each resource type
        for (int i = 0; i < 5; i++)
        {
            if (!resourcePlaced[i])
            {
                placeTileRandomly(sand + i);
            }
        }
    }

    // Generates a winding river across the map
    private void generateRiver()
    {
        int row = Greenfoot.getRandomNumber(map.length - 6) + 3;
        int col = 0;

        while (col < map[0].length)
        {
            map[row][col] = river_H;
            int turnChance = Greenfoot.getRandomNumber(100);

            // 20% chance to go up
            if (turnChance < 20 && row > 2 && col < map[0].length - 1)
            {
                map[row][col] = riverCorner_NW;
                int verticalLength = 1 + Greenfoot.getRandomNumber(3);
                for (int i = 0; i < verticalLength && row > 0; i++)
                {
                    row--;
                    map[row][col] = river_V;
                }
                if (col + 1 < map[0].length)
                {
                    map[row][col] = riverCorner_SE;
                }
            }
            // 20% chance to go down
            else if (turnChance > 80 && row < map.length - 3 && col < map[0].length - 1)
            {
                map[row][col] = riverCorner_SW;
                int verticalLength = 1 + Greenfoot.getRandomNumber(3);
                for (int i = 0; i < verticalLength && row < map.length - 1; i++)
                {
                    row++;
                    map[row][col] = river_V;
                }
                map[row][col] = riverCorner_NE;
            }

            col++;
        }
    }

    // Helper function for placing tiles on a random grass cell
    private void placeTileRandomly(int tileType)
    {
        while (true)
        {
            int row = Greenfoot.getRandomNumber(map.length);
            int col = Greenfoot.getRandomNumber(map[0].length);

            if (map[row][col] == grass)
            {
                map[row][col] = tileType;
                return;
            }
        }
    }

    // Vehicle spawning
    public void spawnVehicle(int pickupRow, int pickupCol, int dropoffRow, int dropoffCol, int routeNumber, int vehicleNumber)
    {
        int x = pickupCol * 32 + 16;
        int y = pickupRow * 32 + 16;
        
        addObject(new Vehicle(pickupRow, pickupCol, dropoffRow, dropoffCol, routeNumber, vehicleNumber), x, y);        
    }

    // Returns if their are neighboring tiles with a output 
    private boolean isAdjacentToPickup(int row, int col)
    {
        for (int[] dir : adjacentDirections)
        {
            int r = row + dir[0];
            int c = col + dir[1];
            if (!isValidPosition(r, c)) continue;
            int t = map[r][c];
            // Drills (all variants) and factories count as pickup sources
            if (t == drill_steel_1_1 || t == drill_steel_1_2 || t == drill_copper_1_1 || t == drill_copper_1_2 || t == drill_sulfur_1_1 || t == drill_sulfur_1_2 || t == factory_steel || t == factory_copper || t == factory_sulfur)
            {
                return true;
            }
        }
        return false;
    }

    // Returns if their are neighboring tiles with a input
    private boolean isAdjacentToDropoff(int row, int col)
    {
        for (int[] dir : adjacentDirections)
        {
            int r = row + dir[0];
            int c = col + dir[1];
            if (!isValidPosition(r, c)) continue;
            int t = map[r][c];
            // Sell point and factories are valid dropoff destinations
            if (t == factory_steel || t == factory_copper || t == factory_sulfur)
            {
                return true;
            }
            else if (t == warehouse)
            {   
                return true;
            }
        }
        return false;
    }

    // Extracts all tiles out of spritesheet
    private void loadTiles()
    {
        final int TILEMAP_COLS = 4;
        final int totalTiles = 88;
        GreenfootImage sheet = new GreenfootImage("tilemap_concept.png");
        tiles = new GreenfootImage[totalTiles];

        for (int i = 0; i < totalTiles; i++)
        {
            int col = i % TILEMAP_COLS;
            int row = i / TILEMAP_COLS;
            GreenfootImage tile = new GreenfootImage(tileSize, tileSize);
            tile.drawImage(sheet, -(col * tileSize), -(row * tileSize));
            tiles[i] = tile;
        }
    }

    // Draws all tiles visible on screen
    private void drawMap()
    {
        GreenfootImage bg = getBackground();
        
        // Calculate which tiles are visible
        int startCol = cameraX / tileSize;
        int endCol = (cameraX + worldWidth) / tileSize + 1;
        int startRow = cameraY / tileSize;
        int endRow = (cameraY + worldHight) / tileSize + 1;
        
        // Clamp to map bounds
        startCol = Math.max(0, startCol);
        endCol = Math.min(map[0].length, endCol);
        startRow = Math.max(0, startRow);
        endRow = Math.min(map.length, endRow);
        
        for (int row = startRow; row < endRow; row++)
        {
            for (int col = startCol; col < endCol; col++)
            {
                int tileId = getAnimatedTileId(map[row][col]);
                int screenX = col * tileSize - cameraX;
                int screenY = row * tileSize - cameraY;
                bg.drawImage(tiles[tileId], screenX, screenY);
            }
        }
    }

    // Returns next frame for animated tiles
    private int getAnimatedTileId(int tileId)
    {
        int frame = (animationCounter / animationSpeed) % 2;
        if (tileId == drill_steel_1_1 || tileId == drill_steel_1_2) {
            return frame == 0 ? drill_steel_1_1 : drill_steel_1_2;
        } else if (tileId == drill_copper_1_1 || tileId == drill_copper_1_2) {
            return frame == 0 ? drill_copper_1_1 : drill_copper_1_2;
        } else if (tileId == drill_sulfur_1_1 || tileId == drill_sulfur_1_2) {
            return frame == 0 ? drill_sulfur_1_1 : drill_sulfur_1_2;
        }
        return tileId;
    }

    public static int getUpgradedTileId(int current, int selected)
    {
        if (selected == road_V_cement)
        {
            for (int i = 0; i < gravel_tiles.length; i++)
            {
                if (current == gravel_tiles[i]) return cement_tiles[i];
            }
        }
        if (selected == road_V_asphalt)
        {
            for (int i = 0; i < gravel_tiles.length; i++)
            {
                if (current == gravel_tiles[i] || current == cement_tiles[i]) return asphalt_tiles[i];
            }
        }
        return -1;
    }

    public static int[] getPrice(int tileId)
    {
        if (grass < tileId && tileId <road_V_cement)
        {
            return new int[] {0, 0, 0, 0, 0, 0, 0, 0, 1, 0};
        }
        else if (tileId == road_V_cement)
        {
            return new int[] {0, 0, 0, 0, 0, 0, 0, 1, 0, 0};
        }
        else if (tileId == road_V_asphalt)
        {
            System.out.println("asphalt not implemented jet");
            return null;
        }
        else if (tileId == riverCrossing_H_cement || tileId == riverCrossing_V_cement)
        {
            return new int[] {0, 0, 0, 0, 0, 0, 0, 2, 0, 0};
        }
        else if (tileId == drill)
        {
            return new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }
        else if (factory_steel - 1 < tileId || tileId < factory_cement + 1)
        {
            return new int[] {1, 0, 0, 0, 0, 0, 0, 1, 0, 0};
        }
        else if (tileId == -1)
        {
            return new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }
        return null;
    }

    public static boolean payResourceCost(int[] cost)
    {
        if (cost == null) return true;

        for (int i = 0; i < storedResources.length; i++)
        {
            if (storedResources[i] < cost[i])
            {
                System.out.println("Not enough resources to build this");
                return false;
            }
        }

        for (int i = 0; i < storedResources.length; i++)
        {
            storedResources[i] -= cost[i];
        }
        return true;
    }

    public static void refundResourceCost(int[] cost)
    {
        if (cost == null) return;

        for (int i = 0; i < storedResources.length; i++)
        {
            storedResources[i] += cost[i];
        }
    }

    // Passes click trough to the right function and helps with dragging
    private void drawRoads()
    {
        MouseInfo mouse = Greenfoot.getMouseInfo();

        if (mouse == null)
        {
            return;
        }

        //  Convert screen coordinates to tile coordinates
        effected_col = (mouse.getX() + cameraX) / tileSize;
        effected_row = (mouse.getY() + cameraY) / tileSize;

        if (!isValidPosition(effected_row, effected_col)) return;
        
        // Begin dragging
        if (Greenfoot.mousePressed(null))
        {
            if (mouse.getButton() == 1)
            {
                if (!ui.isMouseOverUI(mouse.getX() - cameraX, mouse.getY() - cameraY))
                {
                    building = true;
                }
            }

            if (mouse.getButton() == 3)
            {
                if (!ui.isMouseOverUI(mouse.getX() - cameraX, mouse.getY() - cameraY))
                {
                    removing = true;
                }
            }
        }

        // Continue the function while dragging 
        if (Greenfoot.mouseDragged(null))
        {
            if (ui.isMouseOverUI(mouse.getX() - cameraX, mouse.getY() - cameraY))
            {
                building = false;
                removing = false;
                return;
            }
            if (building)
            {
                handleLeftClick();
            }

            if (removing)
            {
                handleRightClick();
            }
        }

        // Single click actions and drag clean-up
        if (Greenfoot.mouseClicked(null))
        {
            if (mouse.getButton() == 1)
            {
                if (!ui.isMouseOverUI(mouse.getX() - cameraX, mouse.getY() - cameraY))
                {
                    handleLeftClick();
                } 
            }

            if (mouse.getButton() == 3)
            {
                if (!ui.isMouseOverUI(mouse.getX() - cameraX, mouse.getY() - cameraY))  
                {
                    handleRightClick();
                }
            }

            building = false;

            removing = false;
        }
    }

    // Handles all left-clicking on the tiles
    private void handleLeftClick()
    {
        if (!isValidPosition(effected_row, effected_col)) return;

        if (!ui.isOpen)
        {
            String coordinateKey = effected_row + "," + effected_col;
            if (factories.containsKey(coordinateKey))
            {
                // Capture the exact mouse coordinates at the frame of the click
                MouseInfo mouse = Greenfoot.getMouseInfo();
                if (mouse != null)
                {
                    ui.factoryUIX = mouse.getX();
                    ui.factoryUIY = mouse.getY();
                }

                // Tell the UI which factory key it needs to pull real-time updates from
                ui.activeFactoryKey = coordinateKey;
                ui.clickedFactory = true;
                return;
            }
            else
            {
                ui.clickedFactory = false;
                ui.activeFactoryKey = ""; // Clear active factory if clicking away
            }
        }
        else
        {
            ui.clickedFactory = false;
        }

        // Build mode
        int currentTile = map[effected_row][effected_col];
        int[] cost = getPrice(selectedTile);
        
        if (currentTile == selectedTile) return;

        if (ui.isOpen)
        {
            switch (currentTile)
            {

                // Terrain tiles can be replaced by roads/depots or factories
                case grass:
                case forest:
                case house:

                    boolean isFactory = (selectedTile == factory_steel || selectedTile == factory_copper || selectedTile == factory_sulfur || selectedTile == factory_cable || selectedTile == factory_copperSulfate || selectedTile == factory_hardenedSteel || selectedTile == factory_PCB || selectedTile == factory_cement);

                    if (selectedTile!= drill)
                    {
                        if (payResourceCost(cost))
                        {   
                            map[effected_row][effected_col] = selectedTile;
                        
                            if (isFactory)
                            {
                                String coordinateKey = effected_row + "," + effected_col;
                                String factoryType = (selectedTile == factory_steel) ? "steel" : (selectedTile == factory_copper) ? "copper" : (selectedTile == factory_sulfur) ? "sulfur" : (selectedTile == factory_cable) ? "cable" : (selectedTile == factory_copperSulfate) ? "copperSulfate" : (selectedTile == factory_hardenedSteel) ? "hardenedSteel" : (selectedTile == factory_PCB) ? "PCB" : (selectedTile == factory_cement) ? "cement" : null;
                                factories.put(coordinateKey, new Factory(factoryType));
                            }
                        }
                    }
                    break;
                
                // Building a river crossing
                case river_V:
                    if (selectedTile == riverCrossing_H_cement)
                    {
                        if (payResourceCost(cost))
                        {
                            map[effected_row][effected_col] = riverCrossing_H_cement;
                        }
                    }
                    break;

                case river_H:
                    if (selectedTile == riverCrossing_V_cement)
                    {
                        if (payResourceCost(cost))
                        {
                            map[effected_row][effected_col] = riverCrossing_V_cement;
                        }
                    }
                    break;

                //  Placing a drill
                case steel_oreDeposit:
                    if (selectedTile == excavator)
                    {
                        map[effected_row][effected_col] = drill_steel_2;
                    }
                    if (selectedTile == drill)
                    {
                        if (payResourceCost(cost))
                        {
                            map[effected_row][effected_col] = drill_steel_1_1;
                        }
                    }
                    break;

                case copper_oreDeposit:
                    if (selectedTile == excavator)
                    {
                        map[effected_row][effected_col] = drill_copper_2;
                    }

                    if (selectedTile == drill)
                    {
                        if (payResourceCost(cost))
                        {
                            map[effected_row][effected_col] = drill_copper_1_1;
                        }
                    }
                    break;
                
                case sulfur_oreDeposit:
                    if (selectedTile == excavator)
                    {
                        map[effected_row][effected_col] = drill_sulfur_2;
                    }
                    if (selectedTile == drill)
                    {
                        if (payResourceCost(cost))
                        {
                            map[effected_row][effected_col] = drill_sulfur_1_1;
                        }
                    }
                    break;

                // Cannot place on these tiles
                case riverCorner_NE:
                case riverCorner_NW:
                case riverCorner_SW:
                case riverCorner_SE:
                case riverCrossing_V_asphalt:
                case riverCrossing_H_asphalt:
                case drill_steel_1_1:
                case drill_steel_1_2:
                case drill_copper_1_1:
                case drill_copper_1_2:
                case drill_sulfur_1_1:
                case drill_sulfur_1_2:
                    break;
                
                // If there is already something build it overwrites
                default:
                    if (selectedTile != excavator)
                    {
                        if (payResourceCost(cost))
                        {
                            map[effected_row][effected_col] = selectedTile;
                    
                        }
                    }
                    int upgradedTile = getUpgradedTileId(currentTile, selectedTile);

                    if (upgradedTile != -1)
                    {
                        if (payResourceCost(cost))
                        {
                            map[effected_row][effected_col] = upgradedTile;
                        }
                    }
                    break;
            }
        }
    }

    // Handles right-clicking on the map
    private void handleRightClick()
    {   
        if (!isValidPosition(effected_row, effected_col))
        {
            return;
        }

        int currentTile = map[effected_row][effected_col];

        if (ui.activeTab != 0)
        {
            return;
        }

        switch (currentTile)
        {
            case grass:
                break;
            
            case riverCrossing_V_cement:
            case riverCrossing_V_asphalt:
                refundResourceCost(getPrice(currentTile));
                map[effected_row][effected_col] = river_H;
                break;
            
            case riverCrossing_H_cement:
            case riverCrossing_H_asphalt:
                refundResourceCost(getPrice(currentTile));
                map[effected_row][effected_col] = river_V;
                break;

            case factory_steel:
            case factory_copper:
            case factory_sulfur:
            case factory_cable:
            case factory_copperSulfate:
            case factory_hardenedSteel:
            case factory_PCB:
            case factory_cement:
                refundResourceCost(getPrice(currentTile));
                map[effected_row][effected_col] = grass;
                factories.remove(effected_row + "," + effected_col);
                break;

            // cannot be removed
            case river_V:
            case river_H:
            case riverCorner_NE:
            case riverCorner_NW:
            case riverCorner_SW:
            case riverCorner_SE:
            case steel_oreDeposit:
            case copper_oreDeposit:
            case sulfur_oreDeposit:
                break;
            
            default:
                refundResourceCost(getPrice(currentTile));
                callConstructionCrew("dozer", effected_row, effected_col);
        }
    }

    // Hotkeys for selecting tiles
    private void selectTile()
    {
        if (ui.activeSubTab == 0){
            updateSelectedTile("1", road_V_gravel);
            updateSelectedTile("2", corner_NW_gravel);
            updateSelectedTile("3", cross_gravel);
            updateSelectedTile("4", tJunction_up_gravel);
            updateSelectedTile("5", depot_right_gravel);
            updateSelectedTile("6", riverCrossing_V_cement);
            updateSelectedTile("7", road_V_cement);
            updateSelectedTile("8", road_V_asphalt);
        }
        else if (ui.activeSubTab == 1)
        {
            updateSelectedTile("1", factory_steel);
            updateSelectedTile("2", factory_copper);
            updateSelectedTile("3", factory_sulfur);
            updateSelectedTile("4", factory_cable);
            updateSelectedTile("5", factory_copperSulfate);
            updateSelectedTile("6", factory_hardenedSteel);
            updateSelectedTile("7", factory_PCB);
            updateSelectedTile("8", factory_cement);
        }
        else if (ui.activeSubTab == 2)
        {
            updateSelectedTile("1", excavator);
            updateSelectedTile("2", drill);
        }

        if (Greenfoot.isKeyDown("r") && !rKeyWasDown)
        {
            rotate();
        }

        rKeyWasDown = Greenfoot.isKeyDown("r");
    }

    // Updates the selected tile
    private void updateSelectedTile(String key, int tileId)
    {
        if (Greenfoot.isKeyDown(key))
        {
            selectedTile = tileId;
        }
    }

    // Highlights the tile the mouse is over and going to effect
    private void highlightTile()
    {
        if (effected_row < 0 || effected_row >= map.length || effected_col < 0 || effected_col >= map[0].length)
        {
            return;
        }
        getBackground().setColor(new Color(0, 150, 255, 100));
        int screenX = effected_col * tileSize - cameraX;
        int screenY = effected_row * tileSize - cameraY;
        getBackground().fillRect(screenX, screenY, tileSize, tileSize);
    }

    // Cycles through the rotations of the selected tile
    private void rotate()
    {
        selectedTile = rotateThrough(
            selectedTile,
            new int[]{road_V_gravel, road_H_gravel},
            new int[]{tJunction_right_gravel, tJunction_down_gravel, tJunction_left_gravel, tJunction_up_gravel},
            new int[]{corner_NW_gravel, corner_SW_gravel, corner_SE_gravel, corner_NE_gravel},
            new int[]{depot_right_gravel, depot_down_gravel, depot_left_gravel, depot_up_gravel}
        );
    }

    // Finds the rotation group of the selected tile and returns the next tile in the group
    private int rotateThrough(int current, int[]... cycles)
    {
        for (int[] cycle : cycles)
        {
            for (int i = 0; i < cycle.length; i++)
            {
                if (cycle[i] == current)
                {
                    return cycle[(i + 1) % cycle.length];
                }
            }
        }
        return current;
    }


    // Returns true if (row, col) is within the bounds of the map array
    private boolean isValidPosition(int row, int col)
        {
            return row >= 0 && row < map.length && col >= 0 && col < map[0].length;
        }


    public void callConstructionCrew(String type, int row, int col)
    {
        ConstructionVehicles existingWorkers = null;
        List<ConstructionVehicles> activeWorkers = getObjects(ConstructionVehicles.class);

        for (ConstructionVehicles worker : activeWorkers)
        {
            if (worker.getVehicleType().equals(type))
            {
                existingWorkers = worker;
                break;
            }
        }
        if (existingWorkers != null)
        {
            existingWorkers.addToWorkQueue(row, col);
        }
        else 
        {
            ArrayList<int[]> initialTask = new ArrayList<int[]>();
            initialTask.add(new int[]{row, col});
            
            ConstructionVehicles newWorker = new ConstructionVehicles(type, base_row, base_col, initialTask);
            addObject(newWorker, 0, 0);
        }
    }
}