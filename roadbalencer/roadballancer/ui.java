import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class ui here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ui extends Actor
{
    private String[] tileNames = {
        "grass",             // 0
        "road v",            // 1
        "road h",            // 2
        "cross",             // 3
        "t-junct up",        // 4
        "t-junct right",     // 5
        "t-junct down",      // 6
        "t-junct left",      // 7
        "corner ne",         // 8
        "corner se",         // 9
        "corner sw",         // 10
        "corner nw",         // 11
        "depot left",        // 12
        "depot down",        // 13
        "depot right",       // 14
        "depot up",          // 15
        "res blue",          // 16
        "res red",           // 17
        "res yellow",        // 18
        "unused",            // 19
        "fac blue",          // 20
        "fac red",           // 21
        "fac yellow",        // 22
        "unused",            // 23
        "river v",           // 24
        "river h",           // 25
        "river cross v",     // 26
        "river cross h",     // 27
        "river corner ne",   // 28
        "river corner nw",   // 29
        "river corner sw",   // 30
        "river corner se",   // 31
        "forest",            // 32
        "house",             // 33
        "unused",            // 34
        "unused",            // 35
        "drill res blue 2",  // 36
        "drill res red 2",   // 37
        "drill res yellow 2",// 38
        "drill",             // 39
        "drill res blue",    // 40
        "drill res red",     // 41
        "drill res yellow",  // 42
        "unused"             // 43
    };

    private static final int[] QUICK_SELECT_TILES = {1, 11, 3, 4, 14, 39};

    private static final int[][] QUICK_SELECT_GROUPS = {
        {1, 2},
        {11, 10, 9, 8},
        {3},
        {4, 5, 6, 7},
        {14, 13, 12, 15},
        {39}
    };
   
    public void act()
    {
        selectedTile();
    }
    public void selectedTile(){
        int slotSize = 32;
        int slotPadding = 12;
        int cols = 2;
        int rows = 3;
        int rowSpacing = slotSize + slotPadding + 10;
        int gridWidth = cols * slotSize + (cols - 1) * slotPadding;
        int gridHeight = slotSize * rows + (rows - 1) * rowSpacing;
        int panelWidth = gridWidth + 20;
        int panelHeight = gridHeight;

        GreenfootImage img = new GreenfootImage(panelWidth, panelHeight);
        img.setColor(new Color(0, 0, 0, 180));
        img.fillRect(0, 0, panelWidth, panelHeight);

        int startX = 10;
        int startY = 10;

        for (int i = 0; i < QUICK_SELECT_TILES.length; i++)
        {
            int tileId = QUICK_SELECT_TILES[i];
            int displayTile = getDisplayTileIdForSlot(i);
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (slotSize + slotPadding);
            int y = startY + row * rowSpacing;

            if (isSelectedQuickSlot(i))
            {
                img.setColor(Color.YELLOW);
                img.drawRect(x - 1, y - 1, slotSize + 1, slotSize + 1);
            }

            GreenfootImage slotTile = getTileImage(displayTile, slotSize, slotSize);
            img.drawImage(slotTile, x, y);

            img.setFont(new Font("Arial", false, false, 12));
            img.setColor(Color.WHITE);
            String keyLabel = String.valueOf(i + 1);
            int keyWidth = keyLabel.length() * 7;
            int keyX = x + (slotSize - keyWidth) / 2;
            img.drawString(keyLabel, keyX, y + slotSize + 14);
        }

        img.setFont(new Font("Arial", false, false, 12));
        img.setColor(Color.WHITE);

        // Selected tile text
        String tileLabel = (Level.selected_tile >= 0 && Level.selected_tile < tileNames.length)
            ? tileNames[Level.selected_tile]
            : "tile " + Level.selected_tile;

        int textWidth = tileLabel.length() * 7;
        int textX = (panelWidth - textWidth) / 2;

        img.drawString(tileLabel, textX, panelHeight - 28);

        // Money text
        String moneyText = "$" + Level.money;

        int moneyWidth = moneyText.length() * 7;
        int moneyX = (panelWidth - moneyWidth) / 2;

        img.drawString(moneyText, moneyX, panelHeight - 10);

        setImage(img);
            }

            private GreenfootImage getTileImage(int tileId, int width, int height)
            {
                GreenfootImage tile;
                if (tileId >= 0 && tileId < Level.tiles.length)
                {
                    tile = new GreenfootImage(Level.tiles[tileId]);
                }
                else
                {
                    tile = new GreenfootImage(width, height);
                    tile.setColor(Color.DARK_GRAY);
                    tile.fillRect(0, 0, width, height);
                }

                tile.scale(width, height);
                return tile;
            }

    private boolean isSelectedQuickSlot(int slotIndex)
    {
        int current = Level.selected_tile;
        for (int option : QUICK_SELECT_GROUPS[slotIndex])
        {
            if (option == current)
            {
                return true;
            }
        }
        return false;
    }

    private int getDisplayTileIdForSlot(int slotIndex)
    {
        int current = Level.selected_tile;
        for (int option : QUICK_SELECT_GROUPS[slotIndex])
        {
            if (option == current)
            {
                return current;
            }
        }
        return QUICK_SELECT_TILES[slotIndex];
    }
}
