import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Level extends World
{
    private static final int Tile_Size = 32;

    public static GreenfootImage[] tiles;

    private int effected_col;
    private int effected_row;

    private boolean rKeyWasDown = false;

    private int animationCounter = 0;
    private static final int[] DRILL_ANIMATION_FRAMES = {39, 40, 41, 42};
    private static final int ANIMATION_SPEED = 15;

    // TILE IDS
    private static final int GRASS = 0;

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

    private static final int DEPOT_PICKUP_LEFT = 12;
    private static final int DEPOT_PICKUP_DOWN = 13;
    private static final int DEPOT_PICKUP_RIGHT = 14;
    private static final int DEPOT_PICKUP_UP = 15;

    private static final int RESOURCE_1 = 16;
    private static final int RESOURCE_2 = 17;
    private static final int RESOURCE_3 = 18;

    private static final int FACTORY_1 = 20;
    private static final int FACTORY_2 = 21;
    private static final int FACTORY_3 = 22;

    private static final int RIVER_CROSSING_V = 26;
    private static final int RIVER_CROSSING_H = 27;

    private static final int RIVER_V = 24;
    private static final int RIVER_H = 25;

    private static final int RIVER_CORNER_NE = 28;
    private static final int RIVER_CORNER_NW = 29;
    private static final int RIVER_CORNER_SW = 30;
    private static final int RIVER_CORNER_SE = 31;

    private static final int FOREST = 32;
    private static final int HOUSE = 33;

    private static final int DRILL_RESOURCE_1_2 = 36;
    private static final int DRILL_RESOURCE_2_2 = 37;
    private static final int DRILL_RESOURCE_3_2 = 38;
    private static final int DRILL = 39;
    private static final int DRILL_RESOURCE_1 = 40;
    private static final int DRILL_RESOURCE_2 = 41;
    private static final int DRILL_RESOURCE_3 = 42;

    public static final int[] QUICK_SELECT_TILES = {
        ROAD_V,
        CORNER_NW,
        ROAD_H,
        T_JUNCTION_UP,
        DEPOT_PICKUP_RIGHT,
        DRILL
    };

    private static final int RESOURCE_FACTORY_PAIRS = 1;

    public static boolean game_started = false;

    public static int selected_tile = 1;

    public static int row_depot_pickup_1 = 5;
    public static int col_depot_pickup_1 = 5;

    public static int row_depot_dropof_1 = 18;
    public static int col_depot_dropof_1 = 18;

    private boolean firstDepotPlaced = false;
    private boolean secondDepotPlaced = false;
    private boolean vehicleSpawned = false;

    private boolean building = false;
    private boolean removing = false;

    public static int money = 999999;

    public static int[][] map = new int[24][50];

    public Level()
    {
        super(1600, 768, 1);

        loadTiles();

        generateTerrain();

        generateRiver();

        spawnResourcesAndFactories();

        drawMap();

        addObject(new ui(), 1550, 650);

    }
    public void act()
    {
        selectTile();

        animationCounter++;

        drawMap();

        drawRoads();

        highlightTile();

        if (Greenfoot.isKeyDown("space"))
        {
            game_started = true;
        }
    }
    private void generateTerrain()
    {
        for (int row = 0; row < map.length; row++)
        {
            for (int col = 0; col < map[row].length; col++)
            {
                int random = Greenfoot.getRandomNumber(100);

                // 10% forest
                if (random < 10)
                {
                    map[row][col] = FOREST;
                }

                // 3% houses
                else if (random < 13)
                {
                    map[row][col] = HOUSE;
                }

                // grass
                else
                {
                    map[row][col] = GRASS;
                }
            }
        }
    }
    private void generateRiver()
    {
        int row = Greenfoot.getRandomNumber(map.length - 6) + 3;

        int col = 0;

        while (col < map[0].length)
        {
            // draw horizontal river
            map[row][col] = RIVER_H;

            int turnChance = Greenfoot.getRandomNumber(100);

            // TURN UP
            if (turnChance < 20
                && row > 2
                && col < map[0].length - 1)
            {
                // first corner
                map[row][col] = RIVER_CORNER_NW;

                int verticalLength = 1 + Greenfoot.getRandomNumber(3);

                for (int i = 0; i < verticalLength && row > 0; i++)
                {
                    row--;

                    map[row][col] = RIVER_V;
                }

                // ending corner
                if (col + 1 < map[0].length)
                {
                    map[row][col] = RIVER_CORNER_SE;
                }
            }

            // TURN DOWN
            else if (turnChance > 80
                    && row < map.length - 3
                    && col < map[0].length - 1)
            {
                // first corner
                map[row][col] = RIVER_CORNER_SW;

                int verticalLength = 1 + Greenfoot.getRandomNumber(3);

                for (int i = 0; i < verticalLength
                    && row < map.length - 1; i++)
                {
                    row++;

                    map[row][col] = RIVER_V;
                }

                // ending corner
                map[row][col] = RIVER_CORNER_NE;
            }

            col++;
        }
    }
    private void spawnResourcesAndFactories()
    {
        for (int i = 0; i < RESOURCE_FACTORY_PAIRS; i++)
        {
            int resourceType = 16 + Greenfoot.getRandomNumber(3);

            int factoryType = 20 + (resourceType - 16);

            placeTileRandomly(resourceType);

            placeTileRandomly(factoryType);
        }
    }
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

    private void spawnVehicle()
    {
        int x = col_depot_pickup_1 * Tile_Size + Tile_Size / 2;
        int y = row_depot_pickup_1 * Tile_Size + Tile_Size / 2;
        
        String color = getDrillColor(row_depot_pickup_1, col_depot_pickup_1);
        
        addObject(
            new Vehicle(
                row_depot_pickup_1,
                col_depot_pickup_1,
                row_depot_dropof_1,
                col_depot_dropof_1,
                color,
                true
            ),
            x,
            y
        );
    }
    
    private String getDrillColor(int row, int col)
    {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions)
        {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (newRow >= 0 && newRow < map.length && newCol >= 0 && newCol < map[0].length)
            {
                int tile = map[newRow][newCol];
                
                if (tile == DRILL_RESOURCE_1 || tile == DRILL_RESOURCE_1_2 || tile == RESOURCE_1)
                {
                    return "red";
                }
                else if (tile == DRILL_RESOURCE_2 || tile == DRILL_RESOURCE_2_2 || tile == RESOURCE_2)
                {
                    return "blue";
                }
                else if (tile == DRILL_RESOURCE_3 || tile == DRILL_RESOURCE_3_2 || tile == RESOURCE_3)
                {
                    return "yellow";
                }
            }
        }
        
        return "red"; // Default to red if no adjacent drill found
    }
    private void loadTiles()
    {
        GreenfootImage sheet = new GreenfootImage("tilemap_concept.png");

        tiles = new GreenfootImage[44];

        for (int i = 0; i < 44; i++)
        {
            int col = i % 4;

            int row = i / 4;

            GreenfootImage tile = new GreenfootImage(Tile_Size, Tile_Size);

            tile.drawImage(sheet, -(col * Tile_Size), -(row * Tile_Size));

            tiles[i] = tile;
        }
    }
    private void drawMap()
    {
        GreenfootImage bg = getBackground();

        for (int row = 0; row < map.length; row++)
        {
            for (int col = 0; col < map[row].length; col++)
            {
                int tileId = map[row][col];

                if (tileId == DRILL_RESOURCE_1 || tileId == DRILL_RESOURCE_1_2) {
                    tileId = (animationCounter / ANIMATION_SPEED) % 2 == 0 ? DRILL_RESOURCE_1 : DRILL_RESOURCE_1_2;
                } else if (tileId == DRILL_RESOURCE_2 || tileId == DRILL_RESOURCE_2_2) {
                    tileId = (animationCounter / ANIMATION_SPEED) % 2 == 0 ? DRILL_RESOURCE_2 : DRILL_RESOURCE_2_2;
                } else if (tileId == DRILL_RESOURCE_3 || tileId == DRILL_RESOURCE_3_2) {
                    tileId = (animationCounter / ANIMATION_SPEED) % 2 == 0 ? DRILL_RESOURCE_3 : DRILL_RESOURCE_3_2;
                }

                bg.drawImage(
                    tiles[tileId],
                    col * Tile_Size,
                    row * Tile_Size
                );
            }
        }
    }
    private void drawRoads()
    {
        MouseInfo mouse = Greenfoot.getMouseInfo();

        if (mouse == null || game_started)
        {
            return;
        }

        effected_col = mouse.getX() / Tile_Size;

        effected_row = mouse.getY() / Tile_Size;

        if (effected_col < 0 || effected_col >= map[0].length || effected_row < 0 || effected_row >= map.length)
        {
            return;
        }

        if (Greenfoot.mousePressed(null))
        {
            if (mouse.getButton() == 1)
            {
                building = true;
            }

            if (mouse.getButton() == 3)
            {
                removing = true;
            }
        }

        if (Greenfoot.mouseDragged(null))
        {
            if (building)
            {
                handleLeftClick();
            }

            if (removing)
            {
                handleRightClick();
            }
        }

        if (Greenfoot.mouseClicked(null))
        {
            if (mouse.getButton() == 1)
            {
                handleLeftClick();
            }

            if (mouse.getButton() == 3)
            {
                handleRightClick();
            }

            building = false;

            removing = false;
        }
    }
    private void handleLeftClick()
    {
        if (effected_row < 0 || effected_row >= map.length || effected_col < 0 || effected_col >= map[0].length)
        {
            return;
        }

        if (money == 0)
        {
            return;
        }

        int currentTile = map[effected_row][effected_col];

        if (currentTile == selected_tile)
        {
            return;
        }

        switch (currentTile)
        {
            case GRASS:
                if (selected_tile != DRILL)
                {
                    money--;
                    map[effected_row][effected_col] = selected_tile;
                    if (selected_tile >= 12 && selected_tile <= 15)
                    {
                        checkAndSetDepot(effected_row, effected_col);
                    }
                }
                break;

            case FOREST:
                if (selected_tile != DRILL)
                {
                    money -= 2;
                    map[effected_row][effected_col] = selected_tile;
                    if (selected_tile >= 12 && selected_tile <= 15)
                    {
                        checkAndSetDepot(effected_row, effected_col);
                    }
                }
                break;

            case HOUSE:
                if (selected_tile != DRILL)
                {
                    money -= 5;
                    map[effected_row][effected_col] = selected_tile;
                    if (selected_tile >= 12 && selected_tile <= 15)
                    {
                        checkAndSetDepot(effected_row, effected_col);
                    }
                }
                break;

            case RIVER_V:
                if (selected_tile == ROAD_H)
                {
                    money -= 3;
                    map[effected_row][effected_col] = RIVER_CROSSING_H;
                }
                break;

            case RIVER_H:
                if (selected_tile == ROAD_V)
                {
                    money -= 3;
                    map[effected_row][effected_col] = RIVER_CROSSING_V;
                }
                break;

            case RESOURCE_1:
                if (selected_tile == DRILL)
                {
                    money -= 5;
                    map[effected_row][effected_col] = 40;
                    checkAndSetDepot(effected_row, effected_col);
                }
                break;

            case RESOURCE_2:
                if (selected_tile == DRILL)
                {
                    money -= 5;
                    map[effected_row][effected_col] = 41;
                    checkAndSetDepot(effected_row, effected_col);
                }
                break;
            
            case RESOURCE_3:
                if (selected_tile == DRILL)
                {
                    money -= 5;
                    map[effected_row][effected_col] = 42;
                    checkAndSetDepot(effected_row, effected_col);
                }
                break;
            
            case RIVER_CORNER_NE:
            case RIVER_CORNER_NW:
            case RIVER_CORNER_SW:
            case RIVER_CORNER_SE:
            case RIVER_CROSSING_V:
            case RIVER_CROSSING_H:
            case DRILL_RESOURCE_1:
            case DRILL_RESOURCE_2:
            case DRILL_RESOURCE_3:
            case FACTORY_1:
            case FACTORY_2:
            case FACTORY_3:
                break;

            default:
                map[effected_row][effected_col] = selected_tile;
                if (selected_tile >= 12 && selected_tile <= 15)
                {
                    checkAndSetDepot(effected_row, effected_col);
                }
        }
    }

    private void checkAndSetDepot(int row, int col)
    {
        int tile = map[row][col];

        // If a depot was placed, check if adjacent to drill or factory
        if (tile >= 12 && tile <= 15)
        {
            boolean nextToDrill = isAdjacentTo(row, col, new int[]{DRILL, DRILL_RESOURCE_1, DRILL_RESOURCE_2, DRILL_RESOURCE_3});
            boolean nextToFactory = isAdjacentTo(row, col, new int[]{FACTORY_1, FACTORY_2, FACTORY_3});

            if (nextToDrill && !firstDepotPlaced)
            {
                row_depot_pickup_1 = row;
                col_depot_pickup_1 = col;
                firstDepotPlaced = true;
            }
            else if (nextToFactory && !secondDepotPlaced)
            {
                row_depot_dropof_1 = row;
                col_depot_dropof_1 = col;
                secondDepotPlaced = true;
            }
        }
        else if (tile == DRILL || tile == DRILL_RESOURCE_1 || tile == DRILL_RESOURCE_2 || tile == DRILL_RESOURCE_3)
        {
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

            for (int[] dir : directions)
            {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                if (newRow >= 0 && newRow < map.length && newCol >= 0 && newCol < map[0].length)
                {
                    int adjacentTile = map[newRow][newCol];

                    if (adjacentTile >= 12 && adjacentTile <= 15)
                    {
                        checkAndSetDepot(newRow, newCol);
                    }
                }
            }

            return;
        }

        if (firstDepotPlaced && secondDepotPlaced && !vehicleSpawned)
        {
            spawnVehicle();
            vehicleSpawned = true;
        }
    }

    private boolean isAdjacentTo(int row, int col, int[] tileTypes)
    {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions)
        {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow >= 0 && newRow < map.length && newCol >= 0 && newCol < map[0].length)
            {
                int tile = map[newRow][newCol];
                for (int type : tileTypes)
                {
                    if (tile == type)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void handleRightClick()
    {
        if (effected_row < 0 || effected_row >= map.length || effected_col < 0 || effected_col >= map[0].length)
        {
            return;
        }
        
        int currentTile = map[effected_row][effected_col];

        switch (currentTile)
        {
            case GRASS:
                break;
            
            case RIVER_CROSSING_V:
                money += 3;
                map[effected_row][effected_col] = RIVER_H;
                break;
            
            case RIVER_CROSSING_H:
                money += 3;
                map[effected_row][effected_col] = RIVER_V;
                break;
            
            case HOUSE:
                money -= 4;
                map[effected_row][effected_col] = GRASS;
                break;
            
            case FOREST:
                money -= 1;
                map[effected_row][effected_col] = GRASS;
                break;
        
            case DRILL_RESOURCE_1:
                money += 5;
                map[effected_row][effected_col] = RESOURCE_1;
                break;
            
            case DRILL_RESOURCE_2:
                money += 5;
                map[effected_row][effected_col] = RESOURCE_2;
                break;
            
            case DRILL_RESOURCE_3:
                money += 5;
                map[effected_row][effected_col] = RESOURCE_3;
                break;

            case RIVER_V:
            case RIVER_H:
            case RIVER_CORNER_NE:
            case RIVER_CORNER_NW:
            case RIVER_CORNER_SW:
            case RIVER_CORNER_SE:
            case RESOURCE_1:
            case RESOURCE_2:
            case RESOURCE_3:
            case FACTORY_1:
            case FACTORY_2:
            case FACTORY_3:
                break;
            
            default:
                if (currentTile >= 12 && currentTile <= 15)
                {
                    firstDepotPlaced = false;
                    secondDepotPlaced = false;
                    vehicleSpawned = false;
                }
                money++;
                map[effected_row][effected_col] = GRASS;
        }
    }

    private void selectTile()
    {
        updateSelectedTile("1", ROAD_V);
        updateSelectedTile("2", CORNER_NW);
        updateSelectedTile("3", CROSS);
        updateSelectedTile("4", T_JUNCTION_UP);
        updateSelectedTile("5", DEPOT_PICKUP_RIGHT);
        updateSelectedTile("6", DRILL);

        if (Greenfoot.isKeyDown("r") && !rKeyWasDown)
        {
            rotate();
        }

        rKeyWasDown = Greenfoot.isKeyDown("r");
    }

    private void updateSelectedTile(String key, int tileId)
    {
        if (Greenfoot.isKeyDown(key))
        {
            selected_tile = tileId;
        }
    }

    private void highlightTile()
    {
        if (effected_row < 0 || effected_row >= map.length || effected_col < 0 || effected_col >= map[0].length)
        {
            return;
        }
        getBackground().setColor(new Color(0, 150, 255, 100));

        getBackground().fillRect(
            effected_col * 32,
            effected_row * 32,
            32,
            32
        );
    }

    private void rotate()
    {
        selected_tile = rotateThrough(
            selected_tile,
            new int[]{ROAD_V, ROAD_H},
            new int[]{T_JUNCTION_UP, T_JUNCTION_DOWN, T_JUNCTION_LEFT, T_JUNCTION_RIGHT},
            new int[]{CORNER_NW, CORNER_SW, CORNER_SE, CORNER_NE},
            new int[]{DEPOT_PICKUP_RIGHT, DEPOT_PICKUP_DOWN, DEPOT_PICKUP_LEFT, DEPOT_PICKUP_UP}
        );
    }

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
}

