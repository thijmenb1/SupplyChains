import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Level extends World
{
    private static final int Tile_Size = 32;

    public static GreenfootImage[] tiles;

    private int effected_col;
    private int effected_row;

    private boolean rKeyWasDown = false;

    // TILE IDS
    private static final int GRASS = 0;

    private static final int ROAD_V = 1;
    private static final int ROAD_H = 2;

    private static final int FOREST = 32;
    private static final int HOUSE = 33;

    private static final int RIVER_V = 24;
    private static final int RIVER_H = 25;

    private static final int RIVER_CROSSING_V = 26;
    private static final int RIVER_CROSSING_H = 27;

    private static final int RIVER_CORNER_NE = 28;
    private static final int RIVER_CORNER_NW = 29;
    private static final int RIVER_CORNER_SW = 30;
    private static final int RIVER_CORNER_SE = 31;

    private static final int RESOURCE_1 = 16;
    private static final int RESOURCE_2 = 17;
    private static final int RESOURCE_3 = 18;

    private static final int FACTORY_1 = 20;
    private static final int FACTORY_2 = 21;
    private static final int FACTORY_3 = 22;

    private static final int RESOURCE_FACTORY_PAIRS = 1;

    public static boolean game_started = false;

    public static int selected_tile = 1;

    public static int row_depot_pickup_1 = 5;
    public static int col_depot_pickup_1 = 5;

    public static int row_depot_dropof_1 = 18;
    public static int col_depot_dropof_1 = 18;

    private boolean firstDepotPlaced = false;
    private boolean secondDepotPlaced = false;

    private boolean building = false;
    private boolean removing = false;

    public static int money = 100;

    public static int[][] map = new int[32][32];

    public Level()
    {
        super(1024, 1024, 1);

        loadTiles();

        generateTerrain();

        generateRiver();

        spawnResourcesAndFactories();

        drawMap();

        addObject(new ui(), 56, 76);

    }
    public void act()
    {
        selectTile();

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
    private void loadTiles()
    {
        GreenfootImage sheet = new GreenfootImage("tilemap_concept.png");

        tiles = new GreenfootImage[36];

        for (int i = 0; i < 36; i++)
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
                bg.drawImage(
                    tiles[map[row][col]],
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
        if (map[effected_row][effected_col] == selected_tile)
        {
            return;
        }

        if (money == 0)
        {
            return;
        }

        int currentTile = map[effected_row][effected_col];

        switch (currentTile)
        {
            case GRASS:
                money--;
                map[effected_row][effected_col] = selected_tile;
                break;

            case FOREST:
                money -= 2;
                map[effected_row][effected_col] = selected_tile;
                break;

            case HOUSE:
                money -= 5;
                map[effected_row][effected_col] = selected_tile;
                break;

            case RIVER_CORNER_NE:
            case RIVER_CORNER_NW:
            case RIVER_CORNER_SW:
            case RIVER_CORNER_SE:
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

            case RIVER_CROSSING_V:
            case RIVER_CROSSING_H:
                break;

            default:
                map[effected_row][effected_col] = selected_tile;
        }
    }
    private void handleRightClick()
    {
        int currentTile = map[effected_row][effected_col];

        if (currentTile == GRASS)
        {
            return;
        }

        if (currentTile == RIVER_CROSSING_V)
        {
            money += 3;
            map[effected_row][effected_col] = RIVER_H;
            return;
        }

        if (currentTile == RIVER_CROSSING_H)
        {
            money += 3;
            map[effected_row][effected_col] = RIVER_V;
            return;
        }

        if (currentTile == HOUSE)
        {
            money -= 2;
            map[effected_row][effected_col] = GRASS;
            return;
        }

        if (currentTile == FOREST)
        {
            money -= 1;
            map[effected_row][effected_col] = GRASS;
            return;
        }

        if (currentTile == RIVER_V
            || currentTile == RIVER_H
            || currentTile == RIVER_CORNER_NE
            || currentTile == RIVER_CORNER_NW
            || currentTile == RIVER_CORNER_SW
            || currentTile == RIVER_CORNER_SE

            || currentTile == RESOURCE_1
            || currentTile == RESOURCE_2
            || currentTile == RESOURCE_3

            || currentTile == FACTORY_1
            || currentTile == FACTORY_2
            || currentTile == FACTORY_3)
        {
            return;
        }

        money++;
        map[effected_row][effected_col] = GRASS;
    }
    private void selectTile()
    {
        if (Greenfoot.isKeyDown("1"))
        {
            selected_tile = 1;
        }

        if (Greenfoot.isKeyDown("2"))
        {
            selected_tile = 11;
        }

        if (Greenfoot.isKeyDown("3"))
        {
            selected_tile = 3;
        }

        if (Greenfoot.isKeyDown("4"))
        {
            selected_tile = 4;
        }

        if (Greenfoot.isKeyDown("5"))
        {
            selected_tile = 14;
        }

        if (Greenfoot.isKeyDown("r") && !rKeyWasDown)
        {
            rotate();
        }

        rKeyWasDown = Greenfoot.isKeyDown("r");
    }
    private void highlightTile()
    {
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
        // roads
        if (selected_tile == 1)
        {
            selected_tile = 2;
        }

        else if (selected_tile == 2)
        {
            selected_tile = 1;
        }

        // t junctions
        else if (selected_tile == 4)
        {
            selected_tile = 6;
        }

        else if (selected_tile == 6)
        {
            selected_tile = 7;
        }

        else if (selected_tile == 7)
        {
            selected_tile = 5;
        }

        else if (selected_tile == 5)
        {
            selected_tile = 4;
        }

        // corners
        else if (selected_tile == 11)
        {
            selected_tile = 10;
        }

        else if (selected_tile == 10)
        {
            selected_tile = 9;
        }

        else if (selected_tile == 9)
        {
            selected_tile = 8;
        }

        else if (selected_tile == 8)
        {
            selected_tile = 11;
        }

        // depots
        else if (selected_tile == 14)
        {
            selected_tile = 13;
        }

        else if (selected_tile == 13)
        {
            selected_tile = 12;
        }

        else if (selected_tile == 12)
        {
            selected_tile = 15;
        }

        else if (selected_tile == 15)
        {
            selected_tile = 14;
        }
    }
}

