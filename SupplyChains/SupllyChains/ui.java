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
    public static int activeTab = 0;        // 0 = building, 1 = route making
    public static int activeSubTab = 0;     // 0 = Roads, 1 = Factories, 2 = Drills
    private static final int NUM_TABS = 2;
    
    // UI Constants
    private static final int slot_size = 32;
    private static final int slot_padding = 12;
    private static final int cols = 2;
    private static final int row_spacing = slot_size + slot_padding + 10;
    private static final int keyLabel_size = 12;
    private static final int keyLabel_Y_offset = 14;
    
    // Tab Constants
    private static final int panel_width = 120;
    private static final int panel_height = 260;
    private static final int panel_start_X = 1280 - panel_width + 10;
    private static final int tab_width = 40;
    private static final int tab_height = 50;
    private static final int tab_spacing = 5;
    private static       int tab_start_X = panel_start_X - tab_width - 5;
    private static final int tab_start_Y = 50;

    // Factory stats
    public static boolean clickedFactory = false;
    public static int factoryRecoursesLeft;
    public static float craftTimeLeft;
    public static int processed;

    public static int factoryUIX;
    public static int factoryUIY;
    public static String activeFactoryKey = "";

    // Route building state
    public static int routeStep = 0;
    public static String uiMode = "home"; // "home", "adding", "viewing", "editing"
    public static int vehicleCount = 0;

    public static int selectedRouteIndex = -1;
    private static int vehicleShopScroll = 0;
    private static final int VEHICLE_SHOP_ROW_HEIGHT = 66;
    private static final int VEHICLE_SHOP_VISIBLE_ROWS = 3;
    private int keyDelayTimer = 0;

    private static final int [][][] categoryGroups = 
    {
        // Sub 0: Roads & Depots
        { {1, 2}, {11, 10, 9, 8}, {3}, {4, 5, 6, 7}, {14, 13, 12, 15}, {50, 51}, {16}, {32}},
        // Sub 1: Factories
        { {76}, {77}, {78}, {79}, {80}, {81}, {82}, {83} },
        // Sub 2: Drills
        { {75}, {67} }
    };

    private static final int[][] categoryDefaults =
    {
        {1, 11, 3, 4, 14, 50, 16, 32},      // Roads defaults
        {76, 77, 78, 79, 80, 81, 82, 83},   // Factories defaults
        {75, 67}                                // Drills defaults
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
            tab_start_X = panel_start_X - tab_width - 5;
        }
        else
        {
            tab_start_X = 1280 - tab_width - 5;
        }
    }

    private void handleInput()
    {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null)
        {
            return;
        }

        int mx = mouse.getX();
        int my = mouse.getY();

        if (Greenfoot.mouseClicked(null))
        {
            int contentStartX = 5;
            int contentStartY = 30;

            for (int i = 0; i < NUM_TABS; i++)
            {
                int tab_X = tab_start_X;
                int tab_Y = tab_start_Y + i * (tab_height + tab_spacing);
                
                if (mx > tab_X && mx < tab_X + tab_width &&
                    my > tab_Y && my < tab_Y + tab_height)
                {
                    if (i == activeTab && isOpen)
                    {
                        isOpen = false;
                    }
                    else
                    {
                        activeTab = i;
                        isOpen = true;
                    }
                    return;
                }
            }

            if ((isOpen && activeTab == 0) || (isOpen && activeTab == 1 && uiMode.equals("buying")))
            {
                if (my >= 5 && my <= 25)
                {
                    if (mx >= panel_start_X + 5 && mx < panel_start_X + 45)
                    {
                        activeSubTab = 0;
                        vehicleShopScroll = 0;
                        return;
                    }
                    else if (mx >= panel_start_X + 45 && mx < panel_start_X + 80)
                    {
                        activeSubTab = 1;
                        vehicleShopScroll = 0;
                        return;
                    }
                    else if (mx >= panel_start_X + 80 && mx <= panel_start_X + 120)
                    {
                        activeSubTab = 2;
                        vehicleShopScroll = 0;
                        return;
                    }
                }
            }

            if (isOpen && activeTab == 1)
            {
                if (uiMode.equals("home"))
                {
                    for (int i = 0; i < Level.garage.size(); i++)
                    {
                        int rowX = panel_start_X + contentStartX + 5;
                        int rowY = contentStartY + 32 + i * 35;
                        int rowW = panel_width - 30;
                        int rowH = 30;

                        if (mx > rowX && mx < rowX + rowW &&
                            my > rowY && my < rowY + rowH)
                        {
                            selectedRouteIndex = i;
                            uiMode = "viewing";
                            return;
                        }
                    }

                    int addButtonX = panel_start_X + contentStartX + 5;
                    int addButtonY = contentStartY + 32 + Level.garage.size() * 35;
                    int addButtonW = panel_width - 30;
                    int addButtonH = 30;

                    if (mx > addButtonX && mx < addButtonX + addButtonW &&
                        my > addButtonY && my < addButtonY + addButtonH)
                    {
                        uiMode = "buying";
                        vehicleShopScroll = 0;
                        return;
                    }
                }
                else if (uiMode.equals("viewing"))
                {
                    // Add your back/sell buttons here later!
                }
            }
        }

        if (isOpen && activeTab == 1 && uiMode.equals("buying"))
        {
            if (keyDelayTimer > 0)
            {
                keyDelayTimer--;
            }

            int scrollDirection = 0;

            if (keyDelayTimer == 0)
            {
                if (Greenfoot.isKeyDown("up"))
                {
                    scrollDirection = -1;
                    keyDelayTimer = 12;
                }
                else if (Greenfoot.isKeyDown("down"))
                {
                    scrollDirection = 1;
                    keyDelayTimer = 12;
                }
            }

            if (scrollDirection != 0)
            {
                int itemCount = getVehicleShopItemCount();
                int maxScroll = Math.max(0, itemCount - VEHICLE_SHOP_VISIBLE_ROWS);
                vehicleShopScroll = Math.max(0, Math.min(maxScroll, vehicleShopScroll + scrollDirection));
            }
        }
    }

    private void renderTabs()
    {
        GreenfootImage displayImg = new GreenfootImage(1280, 720);
        displayImg.setColor(new Color(0, 0, 0, 0)); 
        displayImg.fillRect(0, 0, 1280, 720);
        
        for (int i = 0; i < NUM_TABS; i++)
        {
            int tabX = tab_start_X;
            int tabY = tab_start_Y + i * (tab_height + tab_spacing);
            
            if (i == activeTab && isOpen)
                displayImg.setColor(Color.YELLOW);
            else
                displayImg.setColor(new Color(100, 100, 100));
            
            displayImg.fillRect(tabX, tabY, tab_width, tab_height);
            displayImg.setColor(Color.BLACK);
            displayImg.drawRect(tabX, tabY, tab_width, tab_height);
            
            displayImg.setFont(new Font("Arial", false, false, 10));
            displayImg.setColor(Color.BLACK);
            if (i == 0)
                drawRoadIcon(displayImg, tabX + tab_width / 2, tabY + tab_height / 2, (i == activeTab && isOpen));
            else if (i == 1)
                drawLocationPin(displayImg, tabX + tab_width / 2, tabY + tab_height / 2, (i == activeTab && isOpen));
        }
        
        if (isOpen)
        {
            GreenfootImage panelImg = new GreenfootImage(panel_width, panel_height);
            panelImg.setColor(new Color(0, 0, 0, 180));
            panelImg.fillRect(0, 0, panel_width, panel_height);

            int contentStartX = 5;
            int contentStartY = 35; 
            
            if (activeTab == 0)
            {
                panelImg.setFont(new Font("Arial", true, false, 10));
                panelImg.setColor(activeSubTab == 0 ? Color.YELLOW : Color.WHITE);
                panelImg.drawString("Road", 5, 15);
                panelImg.setColor(activeSubTab == 1 ? Color.YELLOW : Color.WHITE);
                panelImg.drawString("Fact", 45, 15);
                panelImg.setColor(activeSubTab == 2 ? Color.YELLOW : Color.WHITE);
                panelImg.drawString("Drill", 85, 15);

                drawTileSelectionTab(panelImg, contentStartX, contentStartY);
            }
            else if (activeTab == 1)
            {
                if (uiMode.equals("buying"))
                {
                    panelImg.setFont(new Font("Arial", true, false, 10));
                    panelImg.setColor(activeSubTab == 0 ? Color.YELLOW : Color.WHITE);
                    panelImg.drawString("Trans", 5, 15);
                    panelImg.setColor(activeSubTab == 1 ? Color.YELLOW : Color.WHITE);
                    panelImg.drawString("Trail", 45, 15);
                    panelImg.setColor(activeSubTab == 2 ? Color.YELLOW : Color.WHITE);
                    panelImg.drawString("Cons", 85, 15);
                }

                drawGarage(panelImg, contentStartX, contentStartY);
            }
            
            displayImg.drawImage(panelImg, panel_start_X, 0);
        }
        else if (clickedFactory)
        {
            drawFactoryUI(displayImg);
        }
        
        drawResources(displayImg);

        if (clickedFactory && Level.factories.containsKey(activeFactoryKey))
        {
            Level.Factory currentFactory = Level.factories.get(activeFactoryKey);
            factoryRecoursesLeft = currentFactory.getStoredResources();
            craftTimeLeft = currentFactory.getConstructionTime();
            processed = currentFactory.getProcessedResources();
        }

        setImage(displayImg);
    }

    private void drawTileSelectionTab(GreenfootImage img, int startX, int startY)
    {
        int y = startY;
        int numItems = categoryDefaults[activeSubTab].length;

        for (int i = 0; i < numItems; i++)
        {
            int displayTile = getDisplayTileIdForSlot(i);
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (slot_size + slot_padding);
            int slotY = y + row * row_spacing;

            drawSlot(img, i, x, slotY, displayTile);
        }
    }

    private boolean isSelectedQuickSlot(int slotIndex)
    {
        int current = Level.selectedTile;
        if (slotIndex >= categoryGroups[activeSubTab].length) return false;
        
        for (int option : categoryGroups[activeSubTab][slotIndex])
        {
            if (option == current) return true;
        }
        return false;
    }

    private int getDisplayTileIdForSlot(int slotIndex)
    {
        int current = Level.selectedTile;
        if (slotIndex >= categoryGroups[activeSubTab].length) return 0;

        for (int option : categoryGroups[activeSubTab][slotIndex])
        {
            if (option == current) return current;
        }
        return categoryDefaults[activeSubTab][slotIndex];
    }

    private void drawSlot(GreenfootImage img, int slotIndex, int x, int y, int displayTile)
    {
        if (isSelectedQuickSlot(slotIndex))
        {
            img.setColor(Color.YELLOW);
            img.drawRect(x - 1, y - 1, slot_size + 1, slot_size + 1);
        }

        GreenfootImage slotTile = getTileImage(displayTile, slot_size, slot_size);
        img.drawImage(slotTile, x, y);

        img.setFont(new Font("Arial", false, false, keyLabel_size));
        img.setColor(Color.WHITE);
        String keyLabel = String.valueOf(slotIndex + 1);
        int keyWidth = keyLabel.length() * 7;
        int keyX = x + (slot_size - keyWidth) / 2;
        img.drawString(keyLabel, keyX, y + slot_size + keyLabel_Y_offset);
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
            int tabY = tab_start_X + i * (tab_height + tab_spacing);
            if (mouseX > tab_start_X && mouseX < tab_start_X + tab_width &&
                mouseY > tabY && mouseY < tabY + tab_height)
            {
                return true;
            }
        }

        // Check panel area
        if (isOpen && mouseX > panel_start_X && mouseX < panel_start_X + panel_width &&
            mouseY > 0 && mouseY < panel_height)
        {
            return true;
        }

        return false;
    }

    private GreenfootImage getResourceIcon(int index)
    {
        final int icon_width = 6;
        final int icon_hight = 7;

        GreenfootImage iconSpriteSheet = new GreenfootImage("resources.png");
        GreenfootImage icon = new GreenfootImage(icon_width, icon_hight);

        icon.drawImage(iconSpriteSheet, -(index * icon_width), 0);
        icon.scale(12, 14);
        return icon;
    }


    private void drawResources(GreenfootImage mainScreenImg)
    {
        GreenfootImage resourcePanel = new GreenfootImage(525, 24);
        resourcePanel.setColor(new Color(0, 0, 0, 180));
        resourcePanel.fillRect(0, 0, 525, 24);


        resourcePanel.setFont(new Font("Arial", false, false, 10));
        resourcePanel.setColor(Color.WHITE);
        
        int start_X = 6;
        int start_Y = 2;

        int colSpacing = 52;

        for (int i = 0; i < Level.storedResources.length; i++)
        {

            int current_X = start_X + (i * colSpacing);

            GreenfootImage icon = getResourceIcon(i);
            
            icon.scale(18, 21);

            resourcePanel.drawImage(icon, current_X, start_Y);

            resourcePanel.drawString(": " + Level.storedResources[i], current_X + 20, start_Y + 14);
        }

        mainScreenImg.drawImage(resourcePanel, 377, 5);
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

    public static void drawFactoryUI(GreenfootImage img)
    {
        int panelX = factoryUIX;
        int panelY = factoryUIY;

        // Background panel box
        img.setColor(new Color(0, 0, 0, 180)); 
        img.fillRect(panelX, panelY, 120, 85);   

        img.setFont(new Font("Arial", false, false, 12));
        img.setColor(Color.WHITE);
        img.drawString("Factory:", panelX + 10, panelY + 15);
        img.drawString("Resources: " + factoryRecoursesLeft, panelX + 10, panelY + 35);
        
        // --- PROGRESS BAR LOGIC (BASED ON 180 FRAMES) ---
        int barX = panelX + 10;
        int barY = panelY + 44;
        int barWidth = 100;
        int barHeight = 10;

        // 1. Draw the empty background of the progress bar (Dark Gray)
        img.setColor(Color.DARK_GRAY);
        img.fillRect(barX, barY, barWidth, barHeight);

        if (factoryRecoursesLeft > 0)
        {
            // 2. Calculate progress (how many frames out of 180 have finished)
            // Starts at 180f (0% filled) and finishes at 0f (100% filled)
            float progressFraction = (180f - craftTimeLeft) / 180f;
            
            // Cap it between 0.0 and 1.0 just to be safe
            progressFraction = Math.max(0.0f, Math.min(1.0f, progressFraction));
            
            int fillWidth = (int)(barWidth * progressFraction);

            // 3. Draw the moving progress fill (factory color)
            Color fillHardwareColor = Color.GREEN;
        
            img.setColor(fillHardwareColor);
            img.fillRect(barX, barY, fillWidth, barHeight);
        }
        
        // Outline the progress bar for a cleaner look (Black outline)
        img.setColor(Color.BLACK);
        img.drawRect(barX, barY, barWidth, barHeight);
        // ------------------------------------------------

        img.setColor(Color.WHITE);
        img.drawString("Processed: " + processed, panelX + 10, panelY + 72);
    }

    private void drawGarage(GreenfootImage img, int startX, int startY)
    {
        if (uiMode.equals("home"))
        {
            img.setFont(new Font("Arial", false, false, 12));
            img.setColor(Color.WHITE);
            img.drawString("Vehicles: " + Level.garage.size(), startX + 5, startY + 10);

            for (int i = 0; i < Level.garage.size(); i++)
            {
                int rowY = startY + 32 + i * 35;

                img.setColor(new Color(255, 255, 255, 50));
                img.fillRect(startX + 5, rowY, panel_width - 30, 30);

                img.setFont(new Font("Arial", false, false, 12));
                img.setColor(Color.WHITE);
                img.drawString("Vehicle " + (i + 1) + ":", startX + 10, rowY + 20);
            }

            int addButtonY = startY + 32 + Level.garage.size() * 35;

            img.setColor(new Color(255, 255, 255, 50));
            img.fillRect(startX + 5, addButtonY, panel_width - 30, 30);

            img.setFont(new Font("Arial", false, false, 12));
            img.setColor(Color.GREEN);
            img.drawString("Open shop", startX + 10, addButtonY + 20);
        }
        else if (uiMode.equals("buying"))
        {
            int itemCount = getVehicleShopItemCount();
            int previewSize = 44;
            int previewX = startX + 8;
            int abilityBoxSize = 18;
            int maxVisibleRows = Math.max(1, VEHICLE_SHOP_VISIBLE_ROWS);
            int visibleRows = Math.min(itemCount - vehicleShopScroll, maxVisibleRows);

            for (int i = 0; i < visibleRows; i++)
            {
                int slotIndex = vehicleShopScroll + i;
                int rowY = startY + 6 + i * VEHICLE_SHOP_ROW_HEIGHT;

                img.setColor(new Color(255, 255, 255, 35));
                img.fillRect(startX + 3, rowY, panel_width - 20, VEHICLE_SHOP_ROW_HEIGHT - 6);
                img.setColor(Color.WHITE);
                img.drawRect(startX + 3, rowY, panel_width - 20, VEHICLE_SHOP_ROW_HEIGHT - 6);

                GreenfootImage vehicleSprite = getVehicleSprite(slotIndex);
                if (vehicleSprite != null)
                {
                    GreenfootImage previewCanvas = new GreenfootImage(previewSize, previewSize);
                    previewCanvas.setColor(new Color(0, 0, 0, 0));
                    previewCanvas.fillRect(0, 0, previewSize, previewSize);

                    int centerX = (previewSize - vehicleSprite.getWidth()) / 2;
                    int centerY = (previewSize - vehicleSprite.getHeight()) / 2;
                    previewCanvas.drawImage(vehicleSprite, centerX, centerY);
                    img.drawImage(previewCanvas, previewX, rowY + 7);
                }
                else
                {
                    img.setColor(Color.DARK_GRAY);
                    img.fillRect(previewX, rowY + 7, previewSize, previewSize);
                }

                int nameY = rowY + 5 + previewSize + 8;
                int textX = previewX + 1;
                img.setFont(new Font("Arial", true, false, 10));
                img.setColor(Color.WHITE);
                img.drawString(getVehicleName(slotIndex), textX, nameY);

                int abilityBoxX = startX + panel_width - 28 - abilityBoxSize;
                int abilityBoxY = rowY + 8;
                img.setColor(new Color(255, 255, 255, 60));
                img.fillRect(abilityBoxX, abilityBoxY, abilityBoxSize, abilityBoxSize);
                img.setColor(Color.LIGHT_GRAY);
                img.drawRect(abilityBoxX, abilityBoxY, abilityBoxSize, abilityBoxSize);

                int[] abilityIcons = getVehicleAbilityIcons(slotIndex);
                int boxCount = Math.min(abilityIcons.length, 2);

                for (int iconIndex = 0; iconIndex < boxCount; iconIndex++)
                {
                    int boxGap = 4;
                    int boxX = abilityBoxX + (iconIndex == 1 ? -(18 + boxGap) : 0);
                    int boxY = abilityBoxY;

                    img.setColor(new Color(255, 255, 255, 90));
                    img.fillRect(boxX, boxY, 18, 18);
                    img.setColor(Color.LIGHT_GRAY);
                    img.drawRect(boxX, boxY, 18, 18);

                    GreenfootImage abilityIcon = getAbilityIcon(abilityIcons[iconIndex]);
                    abilityIcon.scale(12, 12);
                    img.drawImage(abilityIcon, boxX + 3, boxY + 3);
                }

                int costX = abilityBoxX - 25;
                int costY = rowY + 40;
                img.setFont(new Font("Arial", false, false, 9));
                img.setColor(Color.YELLOW);
                img.drawString("Cost:", costX, costY);

                GreenfootImage resourceIcon = getResourceIcon(0);
                resourceIcon.scale(10, 12);
                img.drawImage(resourceIcon, costX + 26, costY - 10);
                img.drawString(String.valueOf(getVehicleCost(slotIndex)), costX + 36, costY);
            }
        }
    }

    private int getVehicleShopItemCount()
    {
        if (activeSubTab == 0)
        {
            return 4;
        }
        else if (activeSubTab == 1)
        {
            return 2;
        }
        else if (activeSubTab == 2)
        {
            return 3;
        }

        return 0;
    }

    private String getVehicleName(int slot)
    {
        if (activeSubTab == 0)
        {
            if (slot == 0) return "Dump Truck";
            if (slot == 1) return "Flatbed Truck";
            if (slot == 2) return "Tractor";
            if (slot == 3) return "Cement Truck";
        }
        else if (activeSubTab == 1)
        {
            if (slot == 0) return "Flatbed Trailer";
            if (slot == 1) return "Bulk Trailer";
        }
        else if (activeSubTab == 2)
        {
            if (slot == 0) return "Dozer";
            if (slot == 1) return "Asphalt paver";
            if (slot == 2) return "Excavator";
        }

        return "Vehicle";
    }

    private int getVehicleCost(int slot)
    {
        if (activeSubTab == 0)
        {
            if (slot == 0) return 80;
            if (slot == 1) return 120;
            if (slot == 2) return 140;
            if (slot == 3) return 160;
        }
        else if (activeSubTab == 1)
        {
            if (slot == 0) return 95;
            if (slot == 1) return 110;
        }
        else if (activeSubTab == 2)
        {
            if (slot == 0) return 90;
            if (slot == 1) return 130;
            if (slot == 2) return 150;
        }

        return 0;
    }

    private int[] getVehicleAbilityIcons(int slot)
    {
        if (activeSubTab == 0)
        {
            if (slot == 0) return new int[]{3};
            if (slot == 1) return new int[]{2, 0};
            if (slot == 2) return new int[]{1, 0};
            if (slot == 3) return new int[]{6};
        }
        else if (activeSubTab == 1)
        {
            if (slot == 0) return new int[]{2};
            if (slot == 1) return new int[]{3};
        }
        else if (activeSubTab == 2)
        {
            if (slot == 0) return new int[]{5};
            if (slot == 1) return new int[]{4};
            if (slot == 2) return new int[]{7};
        }

        return new int[]{};
    }

    private GreenfootImage getAbilityIcon(int iconIndex)
    {
        GreenfootImage iconSheet = new GreenfootImage("ability_icons.png");
        GreenfootImage icon = new GreenfootImage(16, 16);
        icon.drawImage(iconSheet, -(iconIndex * 16), 0);
        return icon;
    }

    private GreenfootImage getVehicleSprite(int slot)
    {
        if (activeSubTab == 0)
        {
            if (slot == 0)
            {
                GreenfootImage img = new GreenfootImage("dumptruck.png");
                return img;
            }
            else if (slot == 1 || slot == 2 || slot == 3)
            {
                GreenfootImage tilemap = new GreenfootImage("trucks_topdown_spritesheet.png");
                int variantIndex = (slot == 1) ? 4 : (slot == 2) ? 12 : 15;
                
                // Calculate row and column in the tilemap (4 rows, 4 columns)
                int row = variantIndex / 4;
                int col = variantIndex % 4;
                final int SPRITE_WIDTH = 11;
                final int SPRITE_HEIGHT = 27;
                int x = col * SPRITE_WIDTH;
                int y = row * SPRITE_HEIGHT;

                
                // Create a new image for this vehicle with the correct sprite
                GreenfootImage vehicleImage = new GreenfootImage(SPRITE_WIDTH, SPRITE_HEIGHT);
                vehicleImage.drawImage(tilemap, -x, -y);
                return vehicleImage;
            }
        }
        else if (activeSubTab == 1)
        {
            if (slot == 0 || slot ==1)
            {
                GreenfootImage tilemap = new GreenfootImage("trailers_topdown_spritesheet.png");
                int variantIndex = (slot == 0) ? 4 : 12;
                    
                // Calculate row and column in the tilemap (4 rows, 4 columns)
                int row = variantIndex / 4;
                int col = variantIndex % 4;
                final int SPRITE_WIDTH = 11;
                final int SPRITE_HEIGHT = 25;
                int x = col * SPRITE_WIDTH;
                int y = row * SPRITE_HEIGHT;

                    
                // Create a new image for this vehicle with the correct sprite
                GreenfootImage vehicleImage = new GreenfootImage(SPRITE_WIDTH, SPRITE_HEIGHT);
                vehicleImage.drawImage(tilemap, -x, -y);
                return vehicleImage;
            }
        }
        else if (activeSubTab == 2)
        {
            if (slot ==  0)
            {
                GreenfootImage img = new GreenfootImage("dozer.png");
                return img;
            }
            if (slot == 1)
            {
                GreenfootImage tilemap = new GreenfootImage("trucks_topdown_spritesheet.png");
                int variantIndex = 13;
                
                // Calculate row and column in the tilemap (4 rows, 4 columns)
                int row = variantIndex / 4;
                int col = variantIndex % 4;
                final int SPRITE_WIDTH = 11;
                final int SPRITE_HEIGHT = 27;
                int x = col * SPRITE_WIDTH;
                int y = row * SPRITE_HEIGHT;

                
                // Create a new image for this vehicle with the correct sprite
                GreenfootImage vehicleImage = new GreenfootImage(SPRITE_WIDTH, SPRITE_HEIGHT);
                vehicleImage.drawImage(tilemap, -x, -y);
                return vehicleImage;
            }
            if (slot == 2)
            {
                GreenfootImage tilemap = new GreenfootImage("excavator_topdown_spritesheet.png");
                int variantIndex = 0;
                
                // Calculate row and column in the tilemap (1 rows, 17 columns)
                int row = variantIndex / 17;
                int col = variantIndex % 17;
                final int SPRITE_WIDTH = 11;
                final int SPRITE_HEIGHT = 37;
                int x = col * SPRITE_WIDTH;
                int y = row * SPRITE_HEIGHT;

                
                // Create a new image for this vehicle with the correct sprite
                GreenfootImage vehicleImage = new GreenfootImage(SPRITE_WIDTH, SPRITE_HEIGHT);
                vehicleImage.drawImage(tilemap, -x, -y);
                return vehicleImage;
            }
        }

        return null;
    }

}