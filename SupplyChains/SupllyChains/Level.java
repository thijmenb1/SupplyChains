import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.HashMap;

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
* - isAdjacentToDropoff()    Helper function to check for valid route
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
    private static final int TILE_SIZE = 32;
    public static final int WORLD_WIDTH = 1280;
    public static final int WORLD_HEIGHT = 720;
    
    // Camera
    private static int cameraX = 0;
    private static int cameraY = 0;
    private static final int CAMERA_SPEED = 5;

    // Ui coordinates
    private static final int UI_PANEL_X = 1550;
    private static final int UI_PANEL_Y = 650;

    //All tiles
    public static GreenfootImage[] tiles;

    // Tile placement / removal tracking
    private int effected_col;
    private int effected_row;
    public static int selected_tile = 1;
    private boolean building = false;
    private boolean removing = false;
    private boolean rKeyWasDown = false;

    // Animation
    private int animationCounter = 0;
    private static final int ANIMATION_SPEED = 15;

    // Terrain Generation
    private static final int FOREST_SPAWN_CHANCE = 10;
    private static final int HOUSE_SPAWN_CHANCE = 3;
    private static final int RESOURCE_SPAWN_CHANCE = 1;

    // Direction vectors
    private static final int[][] ADJACENT_DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    // TILE IDS - Terrain
    private static final int GRASS = 0;
    private static final int FOREST = 32;
    private static final int HOUSE = 33;

    // TILE IDS - Roads
    private static final int ROAD_V = 1;
    private static final int ROAD_H = 2;
    private static final int CROSS = 3;
    private static final int T_JUNCTION_UP = 4;
    private static final int T_JUNCTION_RIGHT = 5;
    private static final int T_JUNCTION_DOWN = 6;
    private static final int T_JUNCTION_LEFT = 7;
    private static final int CORNER_NE = 8;
    private static final int CORNER_SE = 9;
    private static final int CORNER_SW = 10;
    private static final int CORNER_NW = 11;

    // TILE IDS - Depots
    private static final int DEPOT_PICKUP_LEFT = 12;
    private static final int DEPOT_PICKUP_DOWN = 13;
    private static final int DEPOT_PICKUP_RIGHT = 14;
    private static final int DEPOT_PICKUP_UP = 15;

    // TILE IDS - Resources & Factories
    private static final int RESOURCE_1 = 16;
    private static final int RESOURCE_2 = 17;
    private static final int RESOURCE_3 = 18;
    private static final int FACTORY_1 = 20;
    private static final int FACTORY_2 = 21;
    private static final int FACTORY_3 = 22;
    private static final int SELLPOINT = 23;

    // TILE IDS - Rivers
    private static final int RIVER_V = 24;
    private static final int RIVER_H = 25;
    private static final int RIVER_CROSSING_V = 26;
    private static final int RIVER_CROSSING_H = 27;
    private static final int RIVER_CORNER_NE = 28;
    private static final int RIVER_CORNER_NW = 29;
    private static final int RIVER_CORNER_SW = 30;
    private static final int RIVER_CORNER_SE = 31;

    // TILE IDS - Drills & Animation
    private static final int DRILL = 39;
    private static final int DRILL_RESOURCE_1 = 40;
    private static final int DRILL_RESOURCE_1_2 = 36;
    private static final int DRILL_RESOURCE_2 = 41;
    private static final int DRILL_RESOURCE_2_2 = 37;
    private static final int DRILL_RESOURCE_3 = 42;
    private static final int DRILL_RESOURCE_3_2 = 38;

    // Costs
    private static final int COST_GRASS = 1;
    private static final int COST_FOREST = 2;
    private static final int COST_HOUSE = 5;
    private static final int COST_RIVER_CROSSING = 3;
    private static final int COST_DRILL = 5;
    private static final int COST_FACTORY = 5;
    private static final int REFUND_FOREST = 1;
    private static final int REFUND_HOUSE = 4;

    // Quick select keys
    public static final int[] QUICK_SELECT_TILES = {
        ROAD_V,
        CORNER_NW,
        ROAD_H,
        T_JUNCTION_UP,
        DEPOT_PICKUP_RIGHT,
        DRILL,
        FACTORY_1
    };

    public static int money = 100;

    public static int[][] map = new int[100][100];
    
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
        private String routeColor;
        private boolean Sell;
        
        public Route(int startRow, int startCol, int endRow, int endCol, int vehicleCount, String routeColor, boolean Sell)
        {
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
            this.vehicleCount = vehicleCount;
            this.routeColor = routeColor;
            this.Sell = Sell;
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
        public String   getRouteColor()                     { return routeColor; }
        public void     setRouteColor(String routeColor)    { this.routeColor = routeColor; }
        public boolean  getSell()                           { return Sell;}
        public void     setSell(boolean Sell)               { this.Sell = Sell;}
    }

    // Factory is a inner class that handles the factories
    public static class Factory
    {
        private int storedResources;
        private float constructionTime;
        private int processedResources;
        private String factoryColor;

        public Factory(int storedResources, int constructionTime, String factoryColor)
        {
            this.storedResources = storedResources;
            this.constructionTime = constructionTime;
            this.factoryColor = factoryColor;
        }

        public boolean pickupResource()
        {
            if (this.processedResources > 0)
            {
                this.processedResources--;
                return true;
            }
            return false;
        }

        public void processesResources()
        {   if (storedResources > 0)
            {
                if (constructionTime > 0)
                {
                    constructionTime--;
                }

                if (constructionTime <= 0)
                {
                    System.out.println(constructionTime);
                    storedResources--;
                    processedResources++;
                    constructionTime = 360;
                }
            }
        }

        public void act()
        {
            processesResources();
        }

        public int      getStoredResources()        {return storedResources; }
        public float    getConstructionTime()       {return constructionTime; }
        public String   getFactoryColor()           {return factoryColor; }
        public void     addResource()               {this.storedResources++; }
        public int      getProcessedResources()     {return processedResources;}
    }

    // Constructor
    public Level()
    {
        super(WORLD_WIDTH, WORLD_HEIGHT, 1, false);
        loadTiles();
        generateTerrain();
        generateRiver();
        placeTileRandomly(SELLPOINT);
        drawMap();
        addObject(new ui(), UI_PANEL_X, UI_PANEL_Y);
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
        
        if (Greenfoot.isKeyDown("w")|| Greenfoot.isKeyDown("up"))
        {
            newCameraY -= CAMERA_SPEED;
        }
        if (Greenfoot.isKeyDown("s")|| Greenfoot.isKeyDown("down"))
        {
            newCameraY += CAMERA_SPEED;
        }
        if (Greenfoot.isKeyDown("a")|| Greenfoot.isKeyDown("left"))
        {
            newCameraX -= CAMERA_SPEED;
        }
        if (Greenfoot.isKeyDown("d")|| Greenfoot.isKeyDown("right"))
        {
            newCameraX += CAMERA_SPEED;
        }
        
        // Clamp camera to map bounds
        int maxCameraX = (map[0].length * TILE_SIZE) - WORLD_WIDTH;
        int maxCameraY = (map.length * TILE_SIZE) - WORLD_HEIGHT;
        
        cameraX = Math.max(0, Math.min(newCameraX, maxCameraX));
        cameraY = Math.max(0, Math.min(newCameraY, maxCameraY));
    }

    // Randomizes the map
    private void generateTerrain()
    {
        // Track if each resource has been placed yet
        boolean[] resourcePlaced = new boolean[3]; //0 = resource16, 1 = resource17, 2 = resource18

        for (int row = 0; row < map.length; row++)
        {
            for (int col = 0; col < map[row].length; col++)
            {
                int random = Greenfoot.getRandomNumber(100);

                if (random < FOREST_SPAWN_CHANCE)
                {
                    map[row][col] = FOREST;
                }
                else if (random < FOREST_SPAWN_CHANCE + HOUSE_SPAWN_CHANCE)
                {
                    map[row][col] = HOUSE;
                }
                else if (random < FOREST_SPAWN_CHANCE + HOUSE_SPAWN_CHANCE + RESOURCE_SPAWN_CHANCE)
                {
                    // Pick a random resource type (16, 17, or 18)
                    int resourceIndex = Greenfoot.getRandomNumber(3);
                    map[row][col] = 16 + resourceIndex;
                    resourcePlaced[resourceIndex] = true;
                }
                else
                {
                    map[row][col] = GRASS;
                }
            }
        }

        // Guarantee at least 1 of each resource type
        for (int i = 0; i < 3; i++)
        {
            if (!resourcePlaced[i])
            {
                placeTileRandomly(16 + i);
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
            map[row][col] = RIVER_H;
            int turnChance = Greenfoot.getRandomNumber(100);

            // 20% chance to go up
            if (turnChance < 20 && row > 2 && col < map[0].length - 1)
            {
                map[row][col] = RIVER_CORNER_NW;
                int verticalLength = 1 + Greenfoot.getRandomNumber(3);
                for (int i = 0; i < verticalLength && row > 0; i++)
                {
                    row--;
                    map[row][col] = RIVER_V;
                }
                if (col + 1 < map[0].length)
                {
                    map[row][col] = RIVER_CORNER_SE;
                }
            }
            // 20% chance to go down
            else if (turnChance > 80 && row < map.length - 3 && col < map[0].length - 1)
            {
                map[row][col] = RIVER_CORNER_SW;
                int verticalLength = 1 + Greenfoot.getRandomNumber(3);
                for (int i = 0; i < verticalLength && row < map.length - 1; i++)
                {
                    row++;
                    map[row][col] = RIVER_V;
                }
                map[row][col] = RIVER_CORNER_NE;
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

            if (map[row][col] == GRASS)
            {
                map[row][col] = tileType;
                return;
            }
        }
    }

    // Vehicle spawning
    public void spawnVehicle(int pickupRow, int pickupCol, int dropoffRow, int dropoffCol, int routeNumber, int vehicleNumber)
    {
        String color = getDrillColor(pickupRow, pickupCol);
        int x = pickupCol * 32 + 16;
        int y = pickupRow * 32 + 16;
        
        routes.get(ui.selectedRouteIndex).setRouteColor(color);
        addObject(new Vehicle(pickupRow, pickupCol, dropoffRow, dropoffCol, color, true, routeNumber, vehicleNumber, routes.get(ui.selectedRouteIndex).getSell()), x, y);        
    }
    
    // Checking adjacent tiles for color
    private String getDrillColor(int row, int col)
    {
        for (int[] dir : ADJACENT_DIRECTIONS)
        {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (isValidPosition(newRow, newCol))
            {
                int tile = map[newRow][newCol];
                
                if (tile == DRILL_RESOURCE_1 || tile == DRILL_RESOURCE_1_2 || tile == RESOURCE_1 || tile == FACTORY_1)
                {
                    return "red";
                }
                else if (tile == DRILL_RESOURCE_2 || tile == DRILL_RESOURCE_2_2 || tile == RESOURCE_2 || tile == FACTORY_2)
                {
                    return "blue";
                }
                else if (tile == DRILL_RESOURCE_3 || tile == DRILL_RESOURCE_3_2 || tile == RESOURCE_3 || tile == FACTORY_3)
                {
                    return "yellow";
                }
            }
        }
        
        return "red"; // Default to red if no adjacent drill found
    }

    // Returns if their are neighboring tiles with a output 
    private boolean isAdjacentToPickup(int row, int col)
    {
        for (int[] dir : ADJACENT_DIRECTIONS)
        {
            int r = row + dir[0];
            int c = col + dir[1];
            if (!isValidPosition(r, c)) continue;
            int t = map[r][c];
            // Drills (all variants) and factories count as pickup sources
            if (t == DRILL_RESOURCE_1 || t == DRILL_RESOURCE_1_2 || t == DRILL_RESOURCE_2 || t == DRILL_RESOURCE_2_2 || t == DRILL_RESOURCE_3 || t == DRILL_RESOURCE_3_2 || t == FACTORY_1 || t == FACTORY_2 || t == FACTORY_3)
            {
                return true;
            }
        }
        return false;
    }

    // Returns if their are neighboring tiles with a input
    private boolean isAdjacentToDropoff(int row, int col)
    {
        for (int[] dir : ADJACENT_DIRECTIONS)
        {
            int r = row + dir[0];
            int c = col + dir[1];
            if (!isValidPosition(r, c)) continue;
            int t = map[r][c];
            // Sell point and factories are valid dropoff destinations
            if (t == FACTORY_1 || t == FACTORY_2 || t == FACTORY_3)
            {
                return true;
            }
            else if (t == SELLPOINT)
            {   
                routes.get(ui.selectedRouteIndex).setSell(true);
                return true;
            }
        }
        return false;
    }

    // Extracts all tiles out of spritesheet
    private void loadTiles()
    {
        final int TILEMAP_COLS = 4;
        final int TOTAL_TILES = 44;
        GreenfootImage sheet = new GreenfootImage("tilemap_concept.png");
        tiles = new GreenfootImage[TOTAL_TILES];

        for (int i = 0; i < TOTAL_TILES; i++)
        {
            int col = i % TILEMAP_COLS;
            int row = i / TILEMAP_COLS;
            GreenfootImage tile = new GreenfootImage(TILE_SIZE, TILE_SIZE);
            tile.drawImage(sheet, -(col * TILE_SIZE), -(row * TILE_SIZE));
            tiles[i] = tile;
        }
    }

    // Draws all tiles visible on screen
    private void drawMap()
    {
        GreenfootImage bg = getBackground();
        
        // Calculate which tiles are visible
        int startCol = cameraX / TILE_SIZE;
        int endCol = (cameraX + WORLD_WIDTH) / TILE_SIZE + 1;
        int startRow = cameraY / TILE_SIZE;
        int endRow = (cameraY + WORLD_HEIGHT) / TILE_SIZE + 1;
        
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
                int screenX = col * TILE_SIZE - cameraX;
                int screenY = row * TILE_SIZE - cameraY;
                bg.drawImage(tiles[tileId], screenX, screenY);
            }
        }
    }

    // Returns next frame for animated tiles
    private int getAnimatedTileId(int tileId)
    {
        int frame = (animationCounter / ANIMATION_SPEED) % 2;
        if (tileId == DRILL_RESOURCE_1 || tileId == DRILL_RESOURCE_1_2) {
            return frame == 0 ? DRILL_RESOURCE_1 : DRILL_RESOURCE_1_2;
        } else if (tileId == DRILL_RESOURCE_2 || tileId == DRILL_RESOURCE_2_2) {
            return frame == 0 ? DRILL_RESOURCE_2 : DRILL_RESOURCE_2_2;
        } else if (tileId == DRILL_RESOURCE_3 || tileId == DRILL_RESOURCE_3_2) {
            return frame == 0 ? DRILL_RESOURCE_3 : DRILL_RESOURCE_3_2;
        }
        return tileId;
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
        effected_col = (mouse.getX() + cameraX) / TILE_SIZE;
        effected_row = (mouse.getY() + cameraY) / TILE_SIZE;

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

        // Route making mode
        if (ui.activeTab == 1)
        {
            int tile = map[effected_row][effected_col];
            if (tile >= 12 && tile <= 15)
            {
                if (ui.routeStep == 0)
                {
                    if (!isAdjacentToPickup(effected_row, effected_col))
                    {
                        System.out.println("Pickup depot must be next to a drill or factory!");
                        return;
                    }
                    routes.get(ui.selectedRouteIndex).setStartRow(effected_row);
                    routes.get(ui.selectedRouteIndex).setStartCol(effected_col);
                    ui.routeStep = 1;
                }
                else if (ui.routeStep == 1)
                {
                    if (effected_row == routes.get(ui.selectedRouteIndex).getStartRow()
                        && effected_col == routes.get(ui.selectedRouteIndex).getStartCol())
                    {
                        System.out.println("Cannot select the same depot for pickup and dropoff!");
                    }
                    else if (ui.routeExists(routes.get(ui.selectedRouteIndex).getStartRow(),
                                            routes.get(ui.selectedRouteIndex).getStartCol(),
                                            effected_row, effected_col))
                    {
                        System.out.println("Route already exists!");
                    }
                    // NEW: second depot must be adjacent to a dropoff (sell point or factory)
                    else if (!isAdjacentToDropoff(effected_row, effected_col))
                    {
                        System.out.println("Dropoff depot must be next to a sell point or factory!");
                    }
                    else
                    {
                        routes.get(ui.selectedRouteIndex).setEndRow(effected_row);
                        routes.get(ui.selectedRouteIndex).setEndCol(effected_col);
                        ui.routeStep = 2;
                    }
                }
            }
            return;
        }

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
        
        if (currentTile == selected_tile) return;

        if (ui.isOpen)
        {
            switch (currentTile)
            {
                // Terrain tiles can be replaced by roads/depots or factories
                case GRASS:
                case FOREST:
                case HOUSE:
                    int cost = (currentTile == FOREST) ? COST_FOREST : (currentTile == HOUSE)  ? COST_HOUSE : COST_GRASS;

                    boolean isFactory = (selected_tile == FACTORY_1 || selected_tile == FACTORY_2 || selected_tile == FACTORY_3);

                    if (selected_tile != DRILL)
                    {
                        money -= isFactory ? COST_FACTORY : cost;
                        map[effected_row][effected_col] = selected_tile;

                        if (isFactory)
                        {
                            String coordinateKey = effected_row + "," + effected_col;
                            String factoryColor = (selected_tile == FACTORY_1) ? "red" : (selected_tile == FACTORY_2) ? "blue" : "yellow";
                            factories.put(coordinateKey, new Factory(0, 360, factoryColor));
                        }
                    }
                    break;
                
                // Building a river crossing
                case RIVER_V:
                    if (selected_tile == ROAD_H)
                    {
                        money -= COST_RIVER_CROSSING;
                        map[effected_row][effected_col] = RIVER_CROSSING_H;
                    }
                    break;

                case RIVER_H:
                    if (selected_tile == ROAD_V)
                    {
                        money -= COST_RIVER_CROSSING;
                        map[effected_row][effected_col] = RIVER_CROSSING_V;
                    }
                    break;

                //  Placing a drill
                case RESOURCE_1:
                    if (selected_tile == DRILL)
                    {
                        money -= COST_DRILL;
                        map[effected_row][effected_col] = DRILL_RESOURCE_1;
                    }
                    break;

                case RESOURCE_2:
                    if (selected_tile == DRILL)
                    {
                        money -= COST_DRILL;
                        map[effected_row][effected_col] = DRILL_RESOURCE_2;
                    }
                    break;
                
                case RESOURCE_3:
                    if (selected_tile == DRILL)
                    {
                        money -= COST_DRILL;
                        map[effected_row][effected_col] = DRILL_RESOURCE_3;
                    }
                    break;
                
                // Cannot place on these tiles
                case RIVER_CORNER_NE:
                case RIVER_CORNER_NW:
                case RIVER_CORNER_SW:
                case RIVER_CORNER_SE:
                case RIVER_CROSSING_V:
                case RIVER_CROSSING_H:
                case DRILL_RESOURCE_1:
                case DRILL_RESOURCE_2:
                case DRILL_RESOURCE_3:
                    break;
                
                // If there is already something build it overwrites
                default:
                    map[effected_row][effected_col] = selected_tile;
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
            case GRASS:
                break;
            
            case RIVER_CROSSING_V:
                money += COST_RIVER_CROSSING;
                map[effected_row][effected_col] = RIVER_H;
                break;
            
            case RIVER_CROSSING_H:
                money += COST_RIVER_CROSSING;
                map[effected_row][effected_col] = RIVER_V;
                break;
            
            case HOUSE:
                money -= REFUND_HOUSE;
                map[effected_row][effected_col] = GRASS;
                break;
            
            case FOREST:
                money -= REFUND_FOREST;
                map[effected_row][effected_col] = GRASS;
                break;
        
            case DRILL_RESOURCE_1:
                money += COST_DRILL;
                map[effected_row][effected_col] = RESOURCE_1;
                break;
            
            case DRILL_RESOURCE_2:
                money += COST_DRILL;
                map[effected_row][effected_col] = RESOURCE_2;
                break;
            
            case DRILL_RESOURCE_3:
                money += COST_DRILL;
                map[effected_row][effected_col] = RESOURCE_3;
                break;

            case FACTORY_1:
            case FACTORY_2:
            case FACTORY_3:
                money += COST_FACTORY;
                map[effected_row][effected_col] = GRASS;
                factories.remove(effected_row + "," + effected_col);
                break;

            // cannot be removed
            case RIVER_V:
            case RIVER_H:
            case RIVER_CORNER_NE:
            case RIVER_CORNER_NW:
            case RIVER_CORNER_SW:
            case RIVER_CORNER_SE:
            case RESOURCE_1:
            case RESOURCE_2:
            case RESOURCE_3:
                break;
            
            default:
                money++;
                map[effected_row][effected_col] = GRASS;
        }
    }

    // Hotkeys for selecting tiles
    private void selectTile()
    {
        updateSelectedTile("1", ROAD_V);
        updateSelectedTile("2", CORNER_NW);
        updateSelectedTile("3", CROSS);
        updateSelectedTile("4", T_JUNCTION_UP);
        updateSelectedTile("5", DEPOT_PICKUP_RIGHT);
        updateSelectedTile("6", DRILL);
        updateSelectedTile("7", FACTORY_1);

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
            selected_tile = tileId;
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
        int screenX = effected_col * TILE_SIZE - cameraX;
        int screenY = effected_row * TILE_SIZE - cameraY;
        getBackground().fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
    }

    // Cycles through the rotations of the selected tile
    private void rotate()
    {
        selected_tile = rotateThrough(
            selected_tile,
            new int[]{ROAD_V, ROAD_H},
            new int[]{T_JUNCTION_UP, T_JUNCTION_DOWN, T_JUNCTION_LEFT, T_JUNCTION_RIGHT},
            new int[]{CORNER_NW, CORNER_SW, CORNER_SE, CORNER_NE},
            new int[]{DEPOT_PICKUP_RIGHT, DEPOT_PICKUP_DOWN, DEPOT_PICKUP_LEFT, DEPOT_PICKUP_UP},
            new int[]{FACTORY_1, FACTORY_2, FACTORY_3}
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
}