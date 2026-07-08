import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.HashMap;

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
    private static final int animationSpeed = 5;

    // Terrain Generation
    private static final int forest_SpawnChance = 10;
    private static final int house_SpawnChance = 3;
    private static final int resource_SpawnChance = 1;

    // TILE IDS - Terrain
    private static final int grass = 0;
    private static final int forest = 64;
    private static final int house = 65;

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

    private static final int road_V_cement = 17;
    private static final int road_H_cement = 18;
    private static final int cross_cement = 19;
    private static final int tJunction_up_cement = 20;
    private static final int tJunction_right_cement = 21;
    private static final int tJunction_down_cement = 22;
    private static final int tJunction_left_cement = 23;
    private static final int corner_NE_cement = 24;
    private static final int corner_NW_cement = 25;
    private static final int corner_SW_cement = 26;
    private static final int corner_SE_cement = 27;

    private static final int road_V_asphalt = 33;
    private static final int road_H_asphalt = 34;
    private static final int cross_asphalt = 35;
    private static final int tJunction_up_asphalt = 36;
    private static final int tJunction_right_asphalt = 37;
    private static final int tJunction_down_asphalt = 38;
    private static final int tJunction_left_asphalt = 39;
    private static final int corner_NE_asphalt = 40;
    private static final int corner_NW_asphalt = 41;
    private static final int corner_SW_asphalt = 42;
    private static final int corner_SE_asphalt = 43;

    // TILE IDS - Depots
    private static final int depot_left_gravel = 12;
    private static final int depot_down_gravel = 13;
    private static final int depot_right_gravel = 14;
    private static final int depot_up_gravel = 15;

    private static final int depot_left_cement = 28;
    private static final int depot_down_cement = 29;
    private static final int depot_right_cement = 30;
    private static final int depot_up_cement = 31;

    private static final int depot_left_asphalt = 44;
    private static final int depot_down_asphalt = 45;
    private static final int depot_right_asphalt = 46;
    private static final int depot_up_asphalt = 47;

    // TILE IDS - Resources
    private static final int sand = 72;
    private static final int gravel = 73;
    private static final int steel_oreDeposit = 74;
    private static final int copper_oreDeposit = 75;
    private static final int sulfur_oreDeposit = 76;

    // TILE IDS - Factories
    private static final int factory_steel = 96;
    private static final int factory_copper = 97;
    private static final int factory_sulfur = 98;
    private static final int factory_cable = 99;
    private static final int factory_copperSulfate = 100;
    private static final int factory_hardenedSteel = 101;
    private static final int factory_PCB = 102;
    private static final int factory_cement = 103;

    // Tile IDS - Base components
    private static final int base = 104;
    private static final int base_V = 105;
    private static final int base_H = 106;
    private static final int base_cross = 107;
    private static final int base_W = 108;
    private static final int base_S = 109;
    private static final int base_N = 110;
    private static final int base_E = 111;
    private static final int base_SW = 112;
    private static final int base_SE = 113;
    private static final int base_NE = 114;
    private static final int base_NW = 115;
    private static final int base_ESW = 116;
    private static final int base_NES = 117;
    private static final int base_NEW = 118;
    private static final int base_NSW = 119;

    // Tile IDS - Base upgrades
    private static final int parkingSpot = 500;
    private static final int parkingSpot_long = 501;
    private static final int parkingSpot_wide = 502;
    private static final int bulkStorage = 503;

    // TILE IDS - Rivers
    private static final int river_V = 49;
    private static final int river_H = 50;
    private static final int riverCrossing_V_cement = 60;
    private static final int riverCrossing_H_cement = 61;
    private static final int riverCrossing_V_asphalt = 62;
    private static final int riverCrossing_H_asphalt = 63;
    private static final int riverCorner_NE = 56;
    private static final int riverCorner_NW = 57;
    private static final int riverCorner_SW = 58;
    private static final int riverCorner_SE = 59;

    // TILE IDS - Drills & Animation
    private static final int drill = 80;
    private static final int drill_steel_1_1 = 81;
    private static final int drill_steel_1_2 = 84;
    private static final int drill_copper_1_1 = 82;
    private static final int drill_copper_1_2 = 85;
    private static final int drill_sulfur_1_1 = 83;
    private static final int drill_sulfur_1_2 = 86;
    private static final int drill_steel_2 = 88;
    private static final int drill_copper_2 = 89;
    private static final int drill_sulfur_2 = 90;
    private static final int excavator = 91;

    private static final int[] gravel_tiles = {road_H_gravel, road_V_gravel, cross_gravel, tJunction_down_gravel, tJunction_left_gravel, tJunction_right_gravel, tJunction_up_gravel, corner_NE_gravel, corner_NW_gravel, corner_SE_gravel, corner_SW_gravel, depot_down_gravel, depot_left_gravel, depot_right_gravel, depot_up_gravel};
    private static final int[] cement_tiles = {road_H_cement, road_V_cement, cross_cement, tJunction_down_cement, tJunction_left_cement, tJunction_right_cement, tJunction_up_cement, corner_NE_cement, corner_NW_cement, corner_SE_cement, corner_SW_cement, depot_down_cement, depot_left_cement, depot_right_cement, depot_up_cement};
    private static final int[] asphalt_tiles = {road_H_asphalt, road_V_asphalt, cross_asphalt, tJunction_down_asphalt, tJunction_left_asphalt, tJunction_right_asphalt, tJunction_up_asphalt, corner_NE_asphalt, corner_NW_asphalt, corner_SE_asphalt, corner_SW_asphalt, depot_down_asphalt, depot_left_asphalt, depot_right_asphalt, depot_up_asphalt};

    public static boolean basePlaced = false;
    public static int base_row = 1;
    public static int base_col = 1;
    public static Base playerBase;

    public static int[] storedResources = {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}; // 1 = steel_ore, 2 = copper_ore, 3 = sulfur_ore, 4 = cables, 5 = copperSulfate, 6 = hardenedSteel, 7 = PCB, 8 = cement, 9 = gravel , 10 = sand
    public static int[] resourceDebt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // Tracks how many resources the player owes to the bank for financing vehicles
    public static int maxResourceDebt = 200; // Maximum amount of resources the player can lend from the bank before they are unable to finance more vehicles

    public static int[][] map = new int[100][100];
    
    public static HashMap <String, ArrayList<Vehicle>> garage = new HashMap<>();
    public static HashMap<String, Factory> factories = new HashMap<>();

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
            if(factoryType == null)
            {
                System.out.println("Error: factoryType is null!");
                return;
            }

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

                case "base":
                    break;

                default:
                    System.out.println("No valid factoryType found");
                    System.out.println(factoryType + "Is not a valid type");
                    ui.instance.message("a error has occurred, pls report this error", 180);
                    break;
            }          
        }


        public boolean acceptsResource(String resourceName)
        {
            if (factoryType.equals("warehouse"))
            {
                return true; // Warehouse accepts all resources
            }
            return this.recipeRequirements.containsKey(resourceName);
        }

        public void addResource(String resourceName)
        {
            if (this.factoryType.equals("warehouse"))
            {
                int index = getResourceIndex(resourceName);
                if (index != -1)
                {
                    Level.storedResources[index]++;
                }
                return;
            }

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

        public String pickupResource(String requestedResource)
        {   
            if (this.factoryType.equals("warehouse"))
            {
                int index = getResourceIndex(requestedResource);
                if (index != -1 && Level.storedResources[index] > 0)
                {
                    Level.storedResources[index]--;
                    return requestedResource;
                }
                return null; // No resources available
            }
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

    public static class Base {
        public static class baseUpgrade
        {
            private String type;
            private int posX;
            private int posY;
            private int widthPixels;
            private int heightPixels;

            public baseUpgrade(String type, int x, int y, int widthPixels, int heightPixels)
            {
                this.type = type;
                this.posX = x;
                this.posY = y;
                this.widthPixels = widthPixels;
                this. heightPixels = heightPixels;
            }

            public String getType() {return type; }
            public int getPosX() {return posX; }
            public int getPosY() {return posY; }
            public int getWidthPixels() {return widthPixels; }
            public int getHeightPixels() {return heightPixels; }
            
        }

        private ArrayList<String> structuralTiles; 
        private ArrayList<baseUpgrade> upgrades;

        public Base(int row, int col)
        {
            this.structuralTiles = new ArrayList<>();
            this.upgrades = new ArrayList<>();
            addStructuralTile(row, col);
        }

        public void addStructuralTile(int row, int col)
        {
            String key = row + "," + col;
            if (!structuralTiles.contains(key))
            {
                structuralTiles.add(key);
            } 
        }

        public void removeStructuralTile(int row, int col)
        {
            structuralTiles.remove(row + "," + col);
        }

        public boolean containsTile(int row, int col)
        {
            return structuralTiles.contains(row + "," + col);
        }

        public int getModularTileId(int row, int col)
        {
            boolean N = containsTile(row - 1, col);
            boolean S = containsTile(row + 1, col);
            boolean W = containsTile(row, col - 1);
            boolean E = containsTile(row, col + 1);

            if (!N && !E && !S && !W) return base;

            if (N && E && S && W) return base_cross;

            if (E && S && W) return base_N;
            if (N && S && W) return base_E;
            if (N && E && W) return base_S;
            if (N && E && S) return base_W;

            if (S && E) return base_NW;
            if (S && W) return base_NE;
            if (N && E) return base_SW;
            if (N && W) return base_SE;
            if (N && S) return base_V;
            if (E && W) return base_H;

            if (S) return base_NEW;
            if (N) return base_ESW;
            if (E) return base_NSW;
            if (W) return base_NES;

            return base;
        }
        
        public void updateVisuals(int[][] map)
        {
            for (String key : structuralTiles)
            {
                String[] coords = key.split(",");
                int r = Integer.parseInt(coords[0]);
                int c = Integer.parseInt(coords[1]);

                map[r][c] = getModularTileId(r, c);
            }
        }

        public void addUpgrade(baseUpgrade upgrade)
        {
            upgrades.add(upgrade);
        }


        public ArrayList<String> getTiles() { return structuralTiles; }
        public ArrayList<baseUpgrade> getUpgrades() { return upgrades; }
    }

    // Constructor
    public Level()
    {
        super(worldWidth, worldHight, 1, false);
        loadTiles();
        generateTerrain();
        generateRiver();
        drawMap();
        addObject(new ui(), UIPanel_X, UIPanel_Y);
    }
        
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

        if (null != playerBase)
        {
            playerBase.updateVisuals(map);
        }
        
        if (!basePlaced)
        {
            selectedTile = base;
            ui.instance.message("First make your base", 1);
        }

        drawMap();
        drawRoads();
        highlightTile();
        payDebt();
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

    // Returns camera offsets
    public int getCameraX() { return cameraX; }
    public int getCameraY() { return cameraY; }

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

    // Extracts all tiles out of spritesheet
    private void loadTiles()
    {
        final int TILEMAP_COLS = 8;
        final int totalTiles = 120;
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

        if (null != playerBase)
        {
            for (Base.baseUpgrade upgrade : playerBase.getUpgrades())
            {
                int width = 32;
                int height = 32;
                int variantIndex = 1; 

                // --- FIX: Read the structural upgrade's actual type, NOT the current hand selection ---
                switch (upgrade.getType())
                {
                    case "parkingSpace":      // From parkingSpot (500)
                        width = 13; height = 39; variantIndex = 0;
                        break;
                    case "parkingSpace_long": // From parkingSpot_long (501)
                        width = 13; height = 48; variantIndex = 1; 
                        break;
                    case "parkingSpace_wide": // From parkingSpot_wide (502)
                        width = 18; height = 38; variantIndex = 2;
                        break;
                    case "bulkStorage":      // From bulkStorage (503)
                        width = 14; height = 16; variantIndex = 3;
                        break;
                }

                // Load the upgrades sprite sheet
                GreenfootImage spriteSheetUpgrades = new GreenfootImage("baseUpgrades.png");

                // Calculate the row and column in the tilemap sheet
                int srcRow = variantIndex / 6;
                int srcCol = variantIndex % 6;
                int srcX = srcCol * 18;
                int srcY = srcRow * 48;

                // Create the cropped preview image
                GreenfootImage upgradeImg = new GreenfootImage(width, height);
                upgradeImg.drawImage(spriteSheetUpgrades, -srcX, -srcY);
                int screenX = upgrade.getPosX() - cameraX;
                int screenY = upgrade.getPosY() - cameraY;
                bg.drawImage(upgradeImg, screenX, screenY);
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

    public static void addDebt(int[] cost)
    {
        if (cost == null) return;

        for (int i = 0; i < resourceDebt.length; i++)
        {
            resourceDebt[i] += cost[i];
        }
    }

    private static void payDebt()
    {
        for (int i = 0; i < storedResources.length; i++)
        {
            if (storedResources[i] >= 2 && resourceDebt[i] > 0)
            {
                int payment = Math.min(storedResources[i] / 2, resourceDebt[i]);
                
                storedResources[i] -= payment;
                resourceDebt[i] -= payment;
            }
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
                MouseInfo mouse = Greenfoot.getMouseInfo();
                if (mouse != null)
                {
                    ui.factoryUIX = mouse.getX();
                    ui.factoryUIY = mouse.getY();
                }
                ui.activeFactoryKey = coordinateKey;
                ui.clickedFactory = true;
                return;
            }
            else
            {
                ui.clickedFactory = false;
                ui.activeFactoryKey = ""; 
            }
        }
        else
        {
            ui.clickedFactory = false;
        }

        if (ui.uiMode.equals("task") && ui.activeTab == 1 && ui.isOpen)
        {
            if (ui.routeStep == 1)
            {
                ui.location_1_row = effected_row;
                ui.location_1_col = effected_col;
            }
            else if (ui.routeStep ==2)
            {
                ui.location_2_row = effected_row;
                ui.location_2_col = effected_col;
            }
        }


        if (ui.activeTab != 0)
        {
            return;
        }

        // --- FIX: Handle Base component placement BEFORE entering terrain switch rules ---
        if (selectedTile == base)
        {
            int currentTile = map[effected_row][effected_col];
            // Don't build if it's an illegal terrain like rivers
            if (currentTile == river_V || currentTile == river_H || 
                currentTile == riverCorner_NE || currentTile == riverCorner_NW || 
                currentTile == riverCorner_SW || currentTile == riverCorner_SE) 
            {
                ui.instance.message("You cannot build on this tile", 90);
                return;
            }

            if (!basePlaced)
            {
                playerBase = new Base(effected_row, effected_col);
                base_row = effected_row;
                base_col = effected_col;
                basePlaced = true;
            }
            else
            {
                playerBase.addStructuralTile(effected_row, effected_col);
            }
            
            // Immediately enforce layout sync over global map
            playerBase.updateVisuals(map);
            return; 
        }

        if (selectedTile >= 500)
        {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse != null && null != playerBase && playerBase.containsTile(effected_row, effected_col))
            {
                // Mirror global base mesh formulas
                int baseOriginX = base_col * tileSize;
                int baseOriginY = base_row * tileSize;

                int absoluteMouseX = mouse.getX() + cameraX;
                int absoluteMouseY = mouse.getY() + cameraY;

                int globalOffsetMouseX = absoluteMouseX - baseOriginX;
                int globalOffsetMouseY = absoluteMouseY - baseOriginY;

                int snappedGlobalOffsetX = 2 + (Math.round((float)(globalOffsetMouseX - 2) / 13) * 13);
                int snappedGlobalOffsetY = 2 + (Math.round((float)(globalOffsetMouseY - 2) / 13) * 13);

                int placementX = baseOriginX + snappedGlobalOffsetX;
                int placementY = baseOriginY + snappedGlobalOffsetY;

                if (selectedTile == parkingSpot)
                {
                    Base.baseUpgrade parkingSpace = new Base.baseUpgrade("parkingSpace", placementX, placementY, 13, 39);
                    playerBase.addUpgrade(parkingSpace);
                }
                if (selectedTile == parkingSpot_long)
                {
                    Base.baseUpgrade parkingSpace_long = new Base.baseUpgrade("parkingSpace_long", placementX, placementY, 13, 48);
                    playerBase.addUpgrade(parkingSpace_long);
                }
                if (selectedTile == parkingSpot_wide)
                {
                    Base.baseUpgrade parkingSpace_wide = new Base.baseUpgrade("parkingSpace_wide", placementX, placementY, 18, 38);
                    playerBase.addUpgrade(parkingSpace_wide);
                }
                if (selectedTile == bulkStorage)
                {
                    Base.baseUpgrade bulkStorage = new Base.baseUpgrade("bulkStorage", placementX, placementY, 14, 16);
                    playerBase.addUpgrade(bulkStorage);
                }
            }
            return;
        }

        // Build mode for everything else
        int currentTile = map[effected_row][effected_col];
        int[] cost = getPrice(selectedTile);
        
        if (currentTile == selectedTile) return;

        if (ui.isOpen)
        {   
            switch (currentTile)
            {
                case grass:
                case forest:
                case house:
                    boolean isFactory = (factory_steel <= selectedTile && selectedTile <= factory_cement);

                    if (selectedTile != drill)
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
                
                case river_V:
                    if (selectedTile == riverCrossing_H_cement)
                    {
                        if (payResourceCost(cost)) map[effected_row][effected_col] = riverCrossing_H_cement;
                    }
                    break;

                case river_H:
                    if (selectedTile == riverCrossing_V_cement)
                    {
                        if (payResourceCost(cost)) map[effected_row][effected_col] = riverCrossing_V_cement;
                    }
                    break;

                case steel_oreDeposit:
                    if (selectedTile == excavator) map[effected_row][effected_col] = drill_steel_2;
                    if (selectedTile == drill && payResourceCost(cost)) map[effected_row][effected_col] = drill_steel_1_1;
                    break;

                case copper_oreDeposit:
                    if (selectedTile == excavator) map[effected_row][effected_col] = drill_copper_2;
                    if (selectedTile == drill && payResourceCost(cost)) map[effected_row][effected_col] = drill_copper_1_1;
                    break;
                
                case sulfur_oreDeposit:
                    if (selectedTile == excavator) map[effected_row][effected_col] = drill_sulfur_2;
                    if (selectedTile == drill && payResourceCost(cost)) map[effected_row][effected_col] = drill_sulfur_1_1;
                    break;

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
                    ui.instance.message("You cannot build on this tile", 90);
                    break;
                
                default:
                    if (selectedTile != excavator)
                    {
                        if (payResourceCost(cost)) map[effected_row][effected_col] = selectedTile;
                    }
                    int upgradedTile = getUpgradedTileId(currentTile, selectedTile);

                    if (upgradedTile != -1 && payResourceCost(cost))
                    {
                        map[effected_row][effected_col] = upgradedTile;
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
                // add construction task to dozer
                factories.remove(effected_row + "," + effected_col);
                break;

            case base:
            case base_N:
            case base_NE:
            case base_NES:
            case base_NEW:
            case base_NSW:
            case base_NW:
            case base_E:
            case base_ESW:
            case base_S:
            case base_SE:
            case base_SW:
            case base_W:
                if (playerBase != null && playerBase.containsTile(effected_row, effected_col))
                {
                    map[effected_row][effected_col] = grass;
                    playerBase.removeStructuralTile(effected_row, effected_col);
                
                    if (playerBase.structuralTiles.isEmpty())
                    {
                        playerBase = null;
                        basePlaced = false;
                    }
                    else
                    {
                        playerBase.updateVisuals(map);
                    }
                    return;
                }
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
                ui.instance.message("This tile cannot be removed", 90);
                break;
            
            default:
                // add construction task to dozer
        }
    }

    // Hotkeys for selecting tiles
    private void selectTile()
    {
        if (ui.isOpen && ui.activeTab == 0)
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
            else if (ui.activeSubTab == 3)
            {
                updateSelectedTile("1", base);
                updateSelectedTile("2", parkingSpot);
                updateSelectedTile("3", parkingSpot_long);
                updateSelectedTile("4", parkingSpot_wide);
                updateSelectedTile("5", bulkStorage);
            }

            if (Greenfoot.isKeyDown("r") && !rKeyWasDown)
            {
                rotate();
            }

            rKeyWasDown = Greenfoot.isKeyDown("r");
        }
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
        int screenX;
        int screenY;

        // If an upgrade is selected, snap relative to a global base coordinate mesh
        if (selectedTile >= 500 && ui.isOpen && ui.activeTab == 0)
        {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse == null) return;

            // 1. Establish global origin anchor point at the first placed base tile corner
            int baseOriginX = base_col * tileSize;
            int baseOriginY = base_row * tileSize;

            // 2. Figure out how far the mouse cursor is relative to that global base anchor
            int absoluteMouseX = mouse.getX() + cameraX;
            int absoluteMouseY = mouse.getY() + cameraY;

            int globalOffsetMouseX = absoluteMouseX - baseOriginX;
            int globalOffsetMouseY = absoluteMouseY - baseOriginY;

            // 3. Snap that global offset evenly into uniform 13px columns with a 2px offset border
            int snappedGlobalOffsetX = 2 + (Math.round((float)(globalOffsetMouseX - 2) / 13) * 13);
            int snappedGlobalOffsetY = 2 + (Math.round((float)(globalOffsetMouseY - 2) / 13) * 13);

            // 4. Transform back out to standard camera viewport view coordinates
            screenX = (baseOriginX + snappedGlobalOffsetX) - cameraX;
            screenY = (baseOriginY + snappedGlobalOffsetY) - cameraY;
        }
        else
        {
            // Standard tile grid validation and coordinate math
            if (effected_row < 0 || effected_row >= map.length || effected_col < 0 || effected_col >= map[0].length)
            {
                return;
            }
            screenX = effected_col * tileSize - cameraX;
            screenY = effected_row * tileSize - cameraY;
        }

        // Check if the selected tile ID is a valid index in the tiles array and has an image
        if (selectedTile >= 0 && selectedTile < tiles.length && tiles[selectedTile] != null && ui.isOpen && ui.activeTab == 0)
        {
            GreenfootImage previewImage;

            if (selectedTile == road_V_cement || selectedTile == road_V_asphalt)
            {
                int upgradeTileIndex = getUpgradedTileId(map[effected_row][effected_col], selectedTile);
                if (upgradeTileIndex != -1)
                {
                    previewImage = new GreenfootImage(tiles[upgradeTileIndex]);
                }
                else
                {
                    previewImage = new GreenfootImage(tiles[selectedTile]);
                }
            }
            else
            {
                previewImage = new GreenfootImage(tiles[selectedTile]);
            }
            
            // Set transparency: 0 is completely clear, 255 is completely solid
            previewImage.setTransparency(130); 
            
            // Draw the semi-transparent preview image
            getBackground().drawImage(previewImage, screenX, screenY);
        }
        else if (selectedTile >= 500 && ui.isOpen && ui.activeTab == 0)
        {
            int width = 32;
            int height = 32;
            int variantIndex = 1; // Default to the index used in drawMap()

            // Map the selected upgrade type to its corresponding dimensions
            switch (selectedTile)
            {
                case parkingSpot:      // 500
                    width = 13; height = 28; variantIndex = 0;
                    break;
                case parkingSpot_long: // 501
                    width = 13; height = 37; variantIndex = 1; // Change variantIndex if they are on different columns
                    break;
                case parkingSpot_wide: // 502
                    width = 18; height = 26; variantIndex = 2;
                    break;
                case bulkStorage:      // 503
                    width = 14; height = 16; variantIndex = 3;
                    break;
                default:
                    break;
            }

            // Load the upgrades sprite sheet
            GreenfootImage spriteSheetUpgrades = new GreenfootImage("baseUpgrades.png");

            // Calculate the row and column in the tilemap sheet
            int srcRow = variantIndex / 6;
            int srcCol = variantIndex % 6;
            int srcX = srcCol * 18;
            int srcY = srcRow * 48;

            // Create the cropped preview image
            GreenfootImage previewImage = new GreenfootImage(width, height);
            previewImage.drawImage(spriteSheetUpgrades, -srcX, -srcY);

            // Apply semi-transparency
            previewImage.setTransparency(130);

            // Draw the preview onto the world background
            getBackground().drawImage(previewImage, screenX, screenY);
        }
        else
        {
            // Fallback to the blue preview square if no tile is selected or if the sprite cant be found
            getBackground().setColor(new Color(0, 150, 255, 100));
            getBackground().fillRect(screenX, screenY, tileSize, tileSize);
        }
    }

    // Cycles through the rotations of the selected tile
    private void rotate()
    {
        selectedTile = rotateThrough(
            selectedTile,
            new int[]{road_V_gravel, road_H_gravel},
            new int[]{tJunction_right_gravel, tJunction_down_gravel, tJunction_left_gravel, tJunction_up_gravel},
            new int[]{corner_NW_gravel, corner_SW_gravel, corner_SE_gravel, corner_NE_gravel},
            new int[]{depot_right_gravel, depot_down_gravel, depot_left_gravel, depot_up_gravel},
            new int[]{parkingSpot, 506},
            new int[]{parkingSpot_long, 507},
            new int[]{parkingSpot_wide, 508}
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

    public void addVehicleToGarage(String type)
    {
        garage.putIfAbsent(type, new ArrayList<>());

        ArrayList<Vehicle> vehiclesOFType = garage.get(type);
        int vehicleNumber = vehiclesOFType.size() + 1;

        Vehicle newVehicle = new Vehicle(vehicleNumber, type, base_col, base_row);
        vehiclesOFType.add(newVehicle);
        spawnVehicle(newVehicle, vehicleNumber, type);
    }

        // Vehicle spawning
    public void spawnVehicle(Vehicle newVehicle, int vehicleNumber, String vehicleType)
    {
        int x = base_col * 32 + 16;
        int y = base_row * 32 + 16;
        
        addObject(newVehicle, x, y);        
    }

    private static int getResourceIndex(String resourceName)
    {
        switch (resourceName) {
            case "steel_ore":          return 0;
            case "copper_ore":         return 1;
            case "sulfur_ore":         return 2;
            case "cable_spool":        return 3;
            case "copperSulfate_IBC":  return 4;
            case "hardenedSteel_beam": return 5;
            case "PCB_pallet":         return 6;
            case "cement":             return 7;
            case "gravel":             return 8;
            case "sand":               return 9;
            default:                   return -1;
        }
    }
}