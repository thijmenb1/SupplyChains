import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class MyWorld here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Level extends World
{
    private static final int Tile_Size = 32;
    public static GreenfootImage[] tiles;
    private int effected_col;
    private int effected_row;
    private boolean rKeyWasDown = false;
    private boolean game_started = false;
    
    public static int selected_tile = 1;
    public static int row_depot_pickup_1 = 5;
    public static int col_depot_pickup_1 = 5;
    public static int row_depot_dropof_1 = 18;
    public static int col_depot_dropof_1 = 18;
    
    public static int[][] map = {
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };
    /*   
     0  = grass
     1  = road_v
     2  = road_h
     3  = crossroad
     4  = t_junction (right)
     5  = t_junction (up)
     6  = t_junction (down)
     7  = t_junction (left)
     8  = corner_NE
     9  = corner_NW
     10 = corner_SW
     11 = corner_SE
     12 = depot (left)
     13 = depot (down)
     14 = depot (right)
     15 = depot (up)
     16 = resource_b
     17 = resource_r
     18 = resource_yp   A 
     19 = -
     20 = factory_b
     21 = factory_r
     22 = factory_y
     23 = -
    */
    /**
     * Constructor for objects of class MyWorld.
     * 
     */
     public Level()
    {    
        // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
        super(640, 640, 1);
        loadTiles();
        drawMap();
        addObject(new ui(), 56, 76);
        addObject(new Vehicle( col_depot_pickup_1, row_depot_pickup_1, col_depot_dropof_1, row_depot_dropof_1), col_depot_pickup_1 * 32-16, row_depot_pickup_1 * 32-16);
    }
    public void act(){
        selectTile();
        drawMap();
        drawRoads();
        highlightTile();
        if (Greenfoot.isKeyDown("space")){
            game_started = true;
        }
    }
    private void loadTiles(){
        GreenfootImage sheet = new  GreenfootImage("tilemap_concept.png");
        tiles = new GreenfootImage[24];
        for (int i = 0; i < 24; i++){
            int col = i % 4;
            int row = i / 4;
            GreenfootImage tile = new GreenfootImage(Tile_Size, Tile_Size);
            tile.drawImage(sheet, -(col * Tile_Size), -(row * Tile_Size));
            tiles[i] = tile;
        }
    }
    private void drawMap() {
        GreenfootImage bg = getBackground();
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                bg.drawImage(tiles[map[row][col]], col * Tile_Size, row * Tile_Size);
            }
        }
    }
    private void drawRoads() {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        int mouse_x;
        int mouse_y;
        if (mouse != null){
            mouse_x = mouse.getX();
            mouse_y = mouse.getY();
            effected_col = mouse_x / 32;
            effected_row = mouse_y / 32;
            if (Greenfoot.mouseClicked(null)){
                if (game_started == false){
                    if (mouse.getButton() == 1){
                        map[effected_row][effected_col] = selected_tile;
                    }
                    if (mouse.getButton() == 3){
                        map[effected_row][effected_col] = 0;
                    }
                }
            }
        }
    }
    private void selectTile() {
        if (Greenfoot.isKeyDown("1")) { selected_tile = 1; }    //straight
        if (Greenfoot.isKeyDown("2")) { selected_tile = 11; }   //corner
        if (Greenfoot.isKeyDown("3")) { selected_tile = 3; }    //cross
        if (Greenfoot.isKeyDown("4")) { selected_tile = 4; }    //t_junction
        if (Greenfoot.isKeyDown("5")) { selected_tile = 14; }    //depot
        if (Greenfoot.isKeyDown("r") && !rKeyWasDown) { rotate(); } rKeyWasDown = Greenfoot.isKeyDown("r");
    }
    private void highlightTile(){
        getBackground().setColor(new Color(0, 150, 255, 100));
        getBackground().fillRect(effected_col*32,effected_row*32,32,32);
    }
    private void rotate(){
        //roads
        if (selected_tile == 1){selected_tile = 2;}
        else if (selected_tile == 2){selected_tile = 1;}
        //t_junction
        else if (selected_tile == 4){selected_tile = 6;}
        else if (selected_tile == 6){selected_tile = 7;}
        else if (selected_tile == 7){selected_tile = 5;}
        else if (selected_tile == 5){selected_tile = 4;}
        //corners
        else if (selected_tile == 11){selected_tile = 10;}
        else if (selected_tile == 10){selected_tile = 9; }
        else if (selected_tile == 9 ){selected_tile = 8; }
        else if (selected_tile == 8 ){selected_tile = 11;}
        //depots
        else if (selected_tile == 14){selected_tile = 13;}
        else if (selected_tile == 13){selected_tile = 12;}
        else if (selected_tile == 12){selected_tile = 15;}
        else if (selected_tile == 15){selected_tile = 14;}
    }
}
