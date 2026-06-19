import greenfoot.*;

/**
 * UI - the hud of the game
 * 
 * Functions:
 * - addedToWorld()             Makes sure it is centred
 * - act()                      Main loop
 * - toggleOpen()               Opens and closes ui
 * - handleInput()              Handles all clicking on the ui
 * - renderTabs()               draws the bare UI
 * - drawTileSelectionTab()     Draws the hotbar (Tab 0)
 * - drawRoute()                Draws the route management panel (Tab 1)
 * - drawMoney()                Draws the money at the bottom
 * - getTileImage()             Helper for hotbar returns the correct img
 * - isSelectedQuikeSlot()      Helper for hotbar returns if tile is on hotbar
 * - getDisplayTileIdForSlot()  Helper for hotbar returns the tile to display in hotbar
 * - drawRoaIcon()              draws icon for Tab 0
 * - drawLocationPin()          draws icon for tab 1
 * - isMouseOverUI()            Returns whether mouse is over ui
 * - routExists()               Checks whether a route already exists
 */

public class ui extends Actor
{
    // UI State
    public static boolean isOpen = true;
    public static int activeTab = 0;
    private static final int NUM_TABS = 2;
    
    // UI Constants
    private static final int SLOT_SIZE = 32;
    private static final int SLOT_PADDING = 12;
    private static final int COLS = 2;
    private static final int ROW_SPACING = SLOT_SIZE + SLOT_PADDING + 10;
    private static final int KEY_LABEL_SIZE = 12;
    private static final int KEY_LABEL_Y_OFFSET = 14;
    
    // Tab Constants
    private static final int PANEL_WIDTH = 120;
    private static final int PANEL_HEIGHT = 260;
    private static final int PANEL_START_X = 1280 - PANEL_WIDTH + 10;
    private static final int TAB_WIDTH = 40;
    private static final int TAB_HEIGHT = 50;
    private static final int TAB_SPACING = 5;
    private static       int TAB_START_X = PANEL_START_X - TAB_WIDTH - 5;
    private static final int TAB_START_Y = 50;
    private static final int cancelX = PANEL_START_X + 10;
    private static final int cancelY = 152;

    // Factory stats
    public static boolean clickedFactory = false;
    public static int factoryRecoursesLeft;
    public static int craftTimeLeft;

    // Route building state
    public static int routeStep = 0;
    public static String routeMode = "home"; // "home", "adding", "viewing", "editing"
    public static int vehicleCount = 0;

    public static int selectedRouteIndex = -1;

    private static final int[] QUICK_SELECT_TILES = {1, 11, 3, 4, 14, 39, 20};

    private static final int[][] QUICK_SELECT_GROUPS = {
        {1, 2},
        {11, 10, 9, 8},
        {3},
        {4, 5, 6, 7},
        {14, 13, 12, 15},
        {39},
        {20, 21, 22},
    };
   
    protected void addedToWorld(World world)
    {
        setLocation(640, 360);
    }
   
    public void act()
    {
        toggleOpen();
        handleInput();
        renderTabs();
    }
    
    public void toggleOpen()
    {
        if (isOpen)
        {
            TAB_START_X = PANEL_START_X - TAB_WIDTH - 5;
        }
        else
        {
            TAB_START_X = 1280 - TAB_WIDTH - 5;
        }
    }

    private void handleInput()
    {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null) return;
        
        int mx = mouse.getX();
        int my = mouse.getY();
        
        if (Greenfoot.mouseClicked(null))
        {
            int contentStartX = 5;
            int contentStartY = 30;
            
            if (routeMode.equals("home"))
            {
                // Check if clicking on a tab
                for (int i = 0; i < NUM_TABS; i++)
                {
                    int tabX = TAB_START_X;
                    int tabY = TAB_START_Y + i * (TAB_HEIGHT + TAB_SPACING);
                    
                    if (mx > tabX && mx < tabX + TAB_WIDTH &&
                        my > tabY && my < tabY + TAB_HEIGHT)
                    {
                        if (i == activeTab && isOpen)
                            isOpen = false;
                        else { activeTab = i; isOpen = true; }
                        return;
                    }
                }
                
                if (isOpen && activeTab == 1)
                {
                    

                    // Check route list entries
                    for (int i = 0; i < Level.routes.size(); i++)
                    {
                        int rowX = PANEL_START_X + contentStartX + 5;
                        int rowY = contentStartY + 32 + i * 35;
                        int rowW = PANEL_WIDTH - 30;
                        int rowH = 30;

                        if (mx > rowX && mx < rowX + rowW &&
                            my > rowY && my < rowY + rowH)
                        {
                            selectedRouteIndex = i;
                            routeMode = "viewing";
                            return;
                        }
                    }
                }
            

                // Check "+ add route" button
                int addButtonX = PANEL_START_X + contentStartX + 5;
                int addButtonY = contentStartY + 32 + Level.routes.size() * 35;
                int addButtonW = PANEL_WIDTH - 30;
                int addButtonH = 30;

                if (mx > addButtonX && mx < addButtonX + addButtonW &&
                    my > addButtonY && my < addButtonY + addButtonH)
                {
                    Level.routes.add(new Level.Route(-1, -1, -1, -1, 0, null, false));
                    selectedRouteIndex = Level.routes.size() -1;
                    routeStep = 0;
                    vehicleCount = 0;
                    routeMode = "adding";
                    return;
                }
            }
                // Check cancel button (shown during route building)
                if (routeStep == 0 || routeStep == 1 ||(routeStep == 2 && Level.routes.get(selectedRouteIndex).getVehicleCount() == 0) && routeMode.equals("adding"))
                {
                    if (mx > cancelX && mx < cancelX + 40 &&
                        my > cancelY - 12 && my < cancelY + 5)
                    {
                        Level.routes.remove(selectedRouteIndex);
                        selectedRouteIndex = -1;
                        routeMode = "home";
                        routeStep = 0;
                        return;
                    }
                }
                if (routeStep == 2)
                {
                    int minusX = PANEL_START_X + 60;
                    int minusY = 115;

                    if (mx >= minusX && mx <= minusX + 15 &&
                        my >= minusY && my <= minusY + 15)
                    {
                        if (Level.routes.get(selectedRouteIndex).getVehicleCount() > 0)
                        {
                            Level.money += 10;
                            Level.routes.get(selectedRouteIndex).setVehicleCount(Level.routes.get(selectedRouteIndex).getVehicleCount() - 1);
                            vehicleCount = Level.routes.get(selectedRouteIndex).getVehicleCount();
                        }
                        else
                        {
                            System.out.println("Vehicle count cannot be negative!");
                        }
                        return;
                    }

                    int plusX = PANEL_START_X + 75;
                    int plusY = 115;

                    if (mx >= plusX && mx <= plusX + 15 &&
                        my >= plusY && my <= plusY + 15)
                    {
                        if (Level.money >= 10)
                        {   
                            Level.money -= 10;
                            Level.routes.get(selectedRouteIndex).setVehicleCount(Level.routes.get(selectedRouteIndex).getVehicleCount() + 1);
                            vehicleCount = Level.routes.get(selectedRouteIndex).getVehicleCount();
                            Level level = (Level)getWorld();
                            level.spawnVehicle(Level.routes.get(selectedRouteIndex).getStartRow(), Level.routes.get(selectedRouteIndex).getStartCol(), Level.routes.get(selectedRouteIndex).getEndRow(), Level.routes.get(selectedRouteIndex).getEndCol(), selectedRouteIndex, Level.routes.get(selectedRouteIndex).getVehicleCount());
                            return;
                        }
                    }
                    
                    if (mx > cancelX && mx < cancelX + 40 && my > cancelY - 12 && my < cancelY + 5 && Level.routes.get(selectedRouteIndex).getVehicleCount() > 0)
                    {   
                        routeMode = "home";
                        routeStep = 0;
                        return;
                    }
                }
            
        }
    }    
    private void renderTabs()
    {
        GreenfootImage displayImg = new GreenfootImage(1280, 720);
        displayImg.setColor(new Color(0, 0, 0, 0)); // Transparent background
        displayImg.fillRect(0, 0, 1280, 720);
        
        // Draw tabs on the right side
        for (int i = 0; i < NUM_TABS; i++)
        {
            int tabX = TAB_START_X;
            int tabY = TAB_START_Y + i * (TAB_HEIGHT + TAB_SPACING);
            
            if (i == activeTab && isOpen)
            {
                displayImg.setColor(Color.YELLOW);
            }
            else
            {
                displayImg.setColor(new Color(100, 100, 100));
            }
            
            displayImg.fillRect(tabX, tabY, TAB_WIDTH, TAB_HEIGHT);
            displayImg.setColor(Color.BLACK);
            displayImg.drawRect(tabX, tabY, TAB_WIDTH, TAB_HEIGHT);
            
            // Draw tab label
            displayImg.setFont(new Font("Arial", false, false, 10));
            displayImg.setColor(Color.BLACK);
            if (i == 0)
            {
                boolean isActive = (i == activeTab && isOpen);
                drawRoadIcon(displayImg, tabX + TAB_WIDTH / 2, tabY + TAB_HEIGHT / 2, isActive);
            }
            else if (i == 1)
            {
                boolean isActive = (i == activeTab && isOpen);
                drawLocationPin(displayImg, tabX + TAB_WIDTH / 2, tabY + TAB_HEIGHT / 2, isActive);
            }
        }
        
        // Draw panel if open
        if (isOpen)
        {
            GreenfootImage panelImg = new GreenfootImage(PANEL_WIDTH, PANEL_HEIGHT);
            panelImg.setColor(new Color(0, 0, 0, 180));
            panelImg.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            // Draw content based on active tab
            int contentStartX = 5;
            int contentStartY = 30;
            
            if (activeTab == 0)
            {
                drawTileSelectionTab(panelImg, contentStartX, contentStartY);
            }
            else if (activeTab == 1)
            {
                drawRoute(panelImg, contentStartX, contentStartY);
            }


            
            // Always draw money at bottom
            drawMoney(panelImg);
            
            // Draw panel onto display image
            displayImg.drawImage(panelImg, PANEL_START_X, 0);
        }
        else if (clickedFactory)
        {
            drawFactoryUI(displayImg);
        }
        
        setImage(displayImg);
    }

    private void drawLocationPin(GreenfootImage img, int cx, int cy, boolean isActive)
    {
        int r = 8;
        
        // Circle top
        img.setColor(Color.BLACK);
        img.fillOval(cx - r, cy - r - 4, r * 2, r * 2);
        
        // Hole in circle
        img.setColor(isActive ? Color.YELLOW : new Color(100, 100, 100));
        img.fillOval(cx - r/2, cy - r/2 - 4, r, r);
        
        // Triangle point below circle
        img.setColor(Color.BLACK);
        int[] xPoints = {cx - r, cx + r, cx};
        int[] yPoints = {cy - 4, cy - 4, cy + r + 2};
        img.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawRoadIcon(GreenfootImage img, int cx, int cy, boolean isActive)
    {
        // Road surface
        img.setColor(Color.BLACK);
        img.fillRect(cx - 10, cy - 14, 20, 28);
        
        // Lane markings (dashed center line)
        img.setColor(Color.WHITE);
        img.fillRect(cx - 1, cy - 12, 2, 6);
        img.fillRect(cx - 1, cy - 2,  2, 6);
        img.fillRect(cx - 1, cy + 8,  2, 6);
    }
    public static boolean isMouseOverUI(int mouseX, int mouseY)
    {
        // Always check tabs regardless of open/closed
        for (int i = 0; i < NUM_TABS; i++)
        {
            int tabY = TAB_START_Y + i * (TAB_HEIGHT + TAB_SPACING);
            if (mouseX > TAB_START_X && mouseX < TAB_START_X + TAB_WIDTH &&
                mouseY > tabY && mouseY < tabY + TAB_HEIGHT)
            {
                return true;
            }
        }

        // Check panel area
        if (isOpen && mouseX > PANEL_START_X && mouseX < PANEL_START_X + PANEL_WIDTH &&
            mouseY > 0 && mouseY < PANEL_HEIGHT)
        {
            return true;
        }

        return false;
    }
    
    private void drawTileSelectionTab(GreenfootImage img, int startX, int startY)
    {
        int y = startY;

        for (int i = 0; i < QUICK_SELECT_TILES.length; i++)
        {
            int displayTile = getDisplayTileIdForSlot(i);
            int col = i % COLS;
            int row = i / COLS;
            int x = startX + col * (SLOT_SIZE + SLOT_PADDING);
            int slotY = y + row * ROW_SPACING;

            drawSlot(img, i, x, slotY, displayTile);
        }
    }
    
    private void drawRoute(GreenfootImage img, int startX, int startY)
    {   

        if (routeMode.equals("home"))
        {
            img.setFont(new Font("Arial", false, false, 12));
            img.setColor(Color.WHITE);
            img.drawString("Routes: " + Level.routes.size(), startX + 5, startY + 10);

            for (int i = 0; i < Level.routes.size(); i++)
            {
                int rowY = startY + 32 + i * 35;

                img.setColor(new Color(255, 255, 255, 50));
                img.fillRect(startX + 5, rowY, PANEL_WIDTH - 30, 30);

                img.setFont(new Font("Arial", false, false, 12));
                img.setColor(Color.WHITE);
                img.drawString("Route " + (i + 1) + ":", startX + 10, rowY + 20);
            }

            int addButtonY = startY + 32 + Level.routes.size() * 35;

            img.setColor(new Color(255, 255, 255, 50));
            img.fillRect(startX + 5, addButtonY, PANEL_WIDTH - 30, 30);

            img.setFont(new Font("Arial", false, false, 12));
            img.setColor(Color.GREEN);
            img.drawString("+ add route", startX + 10, addButtonY + 20);
        }

        if (selectedRouteIndex != -1 && routeMode.equals("viewing"))
        {
            Level.Route route = Level.routes.get(selectedRouteIndex);
            img.setFont(new Font("Arial", false, false, 12));
            img.setColor(Color.WHITE);
            img.drawString("Route " + (selectedRouteIndex + 1) + ":", startX + 5, startY + 10);
            img.drawString("Drill: (" + route.getStartRow() + "," + route.getStartCol() + ")", startX + 5, startY + 32);
            img.drawString("Factory: (" + route.getEndRow() + "," + route.getEndCol() + ")", startX + 5, startY + 66);
            img.drawString("Vehicles: " + route.getVehicleCount(), startX + 5, startY + 100);
            img.setColor(Color.YELLOW);
            img.drawString("Edit Route", startX + 5, startY + 152);
        }

        if (selectedRouteIndex != -1 && routeMode.equals("adding"))
        {
            img.setFont(new Font("Arial", false, false, 12));
            img.setColor(Color.WHITE);
            img.drawString("Drill:", startX + 5, startY + 32);
            img.drawString("Factory:", startX + 5, startY + 66);
            if (routeStep == 0)
            {
                img.drawString("Click the drill depot", startX + 5, startY + 44);
                img.setColor(Color.RED);
                img.drawString("Cancel", startX + 5, startY + 122);
            }
            else if (routeStep == 1)
            {
                img.drawString("Selected: (" + Level.routes.get(selectedRouteIndex).getStartRow() + "," + Level.routes.get(selectedRouteIndex).getStartCol() + ")", startX + 5, startY + 44);
                img.drawString("Click the factory depot", startX + 5, startY + 78);
                img.setColor(Color.RED);
                img.drawString("Cancel", startX + 5, startY + 122);
            }
            else if (routeStep == 2)
            {   
                if (selectedRouteIndex < Level.routes.size() && Level.routes.get(selectedRouteIndex) != null)
                {
                    vehicleCount = Level.routes.get(selectedRouteIndex).getVehicleCount();
                }
                else
                {
                    vehicleCount = 0;
                }
                img.drawString("Selected: (" + Level.routes.get(selectedRouteIndex).getStartRow() + "," + Level.routes.get(selectedRouteIndex).getStartCol() + ")", startX + 5, startY + 44);
                img.drawString("Selected: (" + Level.routes.get(selectedRouteIndex).getEndRow() + "," + Level.routes.get(selectedRouteIndex).getEndCol() + ")", startX + 5, startY + 78);
                img.drawString("Vehicles:    " + vehicleCount, startX + 5, startY + 100);
                img.drawString("-", startX + 60, startY + 100);
                img.drawString("+", startX + 75, startY + 100);
                if (vehicleCount == 0)
                {
                    img.setColor(Color.RED);
                    img.drawString("Cancel Route", startX + 5, startY + 122);
                }
                else
                {
                    img.setColor(Color.YELLOW);
                    img.drawString("confirm route", startX + 5, startY + 122);
                }
            }
        }
    }
    
    public static boolean routeExists(int startRow, int startCol, int endRow, int endCol) {
        for (Level.Route route : Level.routes) {
            if (route.getStartRow() == startRow && 
                route.getStartCol() == startCol &&
                route.getEndRow() == endRow && 
                route.getEndCol() == endCol) {
                return true;
            }
        }
        return false;
    }

    private void drawMoney(GreenfootImage img)
    {
        img.setFont(new Font("Arial", false, false, 10));
        img.setColor(Color.WHITE);
        
        img.drawString("Money: $" + Level.money, 5, PANEL_HEIGHT - 5);
    }

    private void drawSlot(GreenfootImage img, int slotIndex, int x, int y, int displayTile)
    {
        if (isSelectedQuickSlot(slotIndex))
        {
            img.setColor(Color.YELLOW);
            img.drawRect(x - 1, y - 1, SLOT_SIZE + 1, SLOT_SIZE + 1);
        }

        GreenfootImage slotTile = getTileImage(displayTile, SLOT_SIZE, SLOT_SIZE);
        img.drawImage(slotTile, x, y);

        img.setFont(new Font("Arial", false, false, KEY_LABEL_SIZE));
        img.setColor(Color.WHITE);
        String keyLabel = String.valueOf(slotIndex + 1);
        int keyWidth = keyLabel.length() * 7;
        int keyX = x + (SLOT_SIZE - keyWidth) / 2;
        img.drawString(keyLabel, keyX, y + SLOT_SIZE + KEY_LABEL_Y_OFFSET);
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

    public static void drawFactoryUI(GreenfootImage img)
    {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        int mx = mouse.getX();
        int my = mouse.getY();

        int panelX = mx;
        int panelY = my - 40;

        img.setColor(new Color(0, 0, 0, 50));
        img.fillRect(panelX, panelY, 100, 70);

        img.setFont(new Font("Arial", false, false, 12));
        img.setColor(Color.WHITE);
        img.drawString("factory:", panelX + 10, panelY + 15);
        img.drawString("Resources: " + factoryRecoursesLeft, panelX + 10, panelY + 35);
        img.drawString("TimeLeft: " + craftTimeLeft/60 + "s" , panelX + 10, panelY + 52);
    }
}