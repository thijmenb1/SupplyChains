import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

public class ui extends Actor
{
    public static ui instance;

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
    private static final int panel_start_X = 1280 - panel_width; // Shift slightly left to fix spacing layout boundaries
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

    public static int selectedGarageIndex = -1;
    public static int selectedShopIndex = -1;
    private static int vehicleShopScroll = 0;
    private static final int VEHICLE_SHOP_ROW_HEIGHT = 66;
    private static final int VEHICLE_SHOP_VISIBLE_ROWS = 3;

    private int popupTimer = 0;
    private String activePopupMessage = "";
    public static int messageTimer = 0;
    private String activeMessage = "";

    public static boolean clickedVehicle = false;
    private int vehicleCargoQuantity;
    private String vehicleId;
    private String vehicleCargo;
    private int vehicleUiX;
    private int vehicleUiY;
    public static Vehicle selectedVehicle = null;

    public static int location_1_row;
    public static int location_1_col;
    public static int location_2_row;
    public static int location_2_col;
    public static String workType = "transport_route";

    private static final int [][][] categoryGroups = 
    {
        // Sub 0: Roads & Depots
        { {1, 2}, {11, 10, 9, 8}, {3}, {4, 5, 6, 7}, {14, 13, 12, 15}, {60, 61}, {17}, {33}},
        // Sub 1: Factories
        { {96}, {97}, {98}, {99}, {100}, {101}, {102}, {103} },
        // Sub 2: Drills
        { {91}, {80} },
        // Sub 3: Base
        { {104}, {500, 506}, {501, 507}, {502, 508}, {503, 509} }
    };

    private static final int[][] categoryDefaults =
    {
        {1, 11, 3, 4, 14, 60, 17, 33},      // Roads defaults
        {96, 97, 98, 99, 100, 101, 102, 103},   // Factories defaults
        {91, 80},                            // Drills defaults
        {104, 500, 501, 502, 503}           // base
    };
    
    public ui()
    {
        instance = this;
    }

    protected void addedToWorld(World world)
    {
        setLocation(640, 360);
    }
   
    public void act()
    {
        toggleOpen();
        handleInput();
        renderTabs();
        if (popupTimer > 0)
        {
            popupTimer--;
        }
        if (messageTimer > 0)
        {
            messageTimer--;
        }
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
        if (mouse == null) return;

        int mx = mouse.getX();
        int my = mouse.getY();

        if (Greenfoot.mouseClicked(null))
        {
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
                        activeSubTab = 0;
                        uiMode = "home";
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
                else if (my >= 25 && my <= 35 && activeTab == 0)
                {
                    if (mx >= panel_start_X + 5 && mx < panel_start_X + 45)
                    {
                        activeSubTab = 3;
                        vehicleShopScroll = 0;
                        return;
                    }
                }
            }

            if (isOpen && activeTab == 1)
            {
                List<Vehicle> flatVehicles = getFlatVehicleList();
                int maxVisibleRows = Math.max(1, VEHICLE_SHOP_VISIBLE_ROWS);
                
                if (uiMode.equals("home"))
                {
                    int visibleRows = Math.min(flatVehicles.size() - vehicleShopScroll, maxVisibleRows);
                    int nextY = contentStartY + 6;

                    for (int i = 0; i < visibleRows; i++)
                    {
                        int garageIndex = vehicleShopScroll + i;
                        int rowY = contentStartY + 6 + i * VEHICLE_SHOP_ROW_HEIGHT;
                        nextY = rowY + VEHICLE_SHOP_ROW_HEIGHT;

                        if (mx >= panel_start_X + 3 && mx <= panel_start_X + panel_width - 5 &&
                            my >= rowY && my <= rowY + VEHICLE_SHOP_ROW_HEIGHT - 6)
                        {
                            selectedGarageIndex = garageIndex;
                            selectedVehicle = flatVehicles.get(garageIndex); 
                            uiMode = "viewing";
                            return;
                        }
                    }

                    // Dynamic shop button location logic
                    int btnY = nextY + 5;
                    int btnW = panel_width - 10;
                    int btnH = 22;

                    if (mx >= panel_start_X + 5 && mx <= panel_start_X + 5 + btnW &&
                        my >= btnY && my <= btnY + btnH)
                    {
                        uiMode = "buying";
                        vehicleShopScroll = 0;
                        return;
                    }
                }
                else if (uiMode.equals("buying"))
                {

                    int actualContentStartY = 35;
                    if (mx >= panel_start_X && mx <= panel_start_X + 30 && my >= actualContentStartY && my <= actualContentStartY + 15)
                    {
                        uiMode = "home";
                        return;
                    }

                    int itemCount = getVehicleShopItemCount();
                    int visibleRows = Math.min(itemCount - vehicleShopScroll, maxVisibleRows);

                    for (int i = 0; i < visibleRows; i++)
                    {
                        int slotIndex = vehicleShopScroll + i;
                        int rowY = contentStartY + 6 + i * VEHICLE_SHOP_ROW_HEIGHT;
                        
                        if (mx >= panel_start_X + 3 && mx <= panel_start_X + panel_width - 5 &&
                            my >= rowY && my <= rowY + VEHICLE_SHOP_ROW_HEIGHT - 6)
                        {
                            selectedShopIndex = slotIndex;
                            uiMode = "buy-viewing";
                            return;
                        }
                    }
                }
                else if (uiMode.equals("buy-viewing"))
                {
                    Level level = (Level) getWorld();
                    if (mx >= panel_start_X + 5 && mx <= panel_start_X + 35 && my >= contentStartY + 5 && my <= contentStartY + 20)
                    {
                        uiMode = "buying";
                        return;
                    }
                    
                    int btnY = contentStartY + 185;
                    int btnW = (panel_width - 16) / 2;
                    int btnH = 22;
                    int[] cost = {getVehicleCost(selectedShopIndex), 0, 0, 0, 0, 0, 0, 0, 0, 0};

                    if (mx >= panel_start_X + 5 && mx <= panel_start_X + 5 + btnW &&
                        my >= btnY && my <= btnY + btnH)
                    {
                        if (Level.payResourceCost(cost))
                        {
                            message("Bought " + getVehicleName(selectedShopIndex), 360);
                            level.addVehicleToGarage(getVehicleName(selectedShopIndex));
                        }
                    }
                    else if (mx >= panel_start_X + 5 + btnW + 4 && mx <= panel_start_X + 5 + btnW + 4 + btnW &&
                             my >= btnY && my <= btnY + btnH)
                    {
                        if (Level.resourceDebt[0] + cost[0] >= Level.maxResourceDebt)
                        {
                            popUpMessage("You cant lend enough resources to finance this vehicle, you need to pay off some debt first.  ", 180);
                        }
                        else
                        {
                            Level.addDebt(cost);
                            level.addVehicleToGarage(getVehicleName(selectedShopIndex));
                            popUpMessage("This vehicle will now take half off your steel until it got 1.5 x the price ", 180);
                        }
                    }
                }
                else if (uiMode.equals("viewing"))
                {
                    if (mx >= panel_start_X + 5 && mx <= panel_start_X + 35 && my >= contentStartY + 5 && my <= contentStartY + 20)
                    {
                        uiMode = "home";
                        return;
                    }

                    // Click coordinates for "Set task" button
                    int actualContentStartY = 35; 
                    int cardX = 5 + 2; // contentStartX is 5, plus the padding of 2
                    int btnY = actualContentStartY + 185; 
                    int cardW = (panel_width - 10) - 4; 
                    int btnH = 22;

                    if (mx >= panel_start_X + cardX && mx <= panel_start_X + cardX + cardW &&
                        my >= btnY && my <= btnY + btnH)
                    {
                        uiMode = "task";
                        return;
                    }
                }
                else if (uiMode.equals("task"))
                {
                    // Back option from task menu back to viewing details
                    int actualContentStartY = 35;
                    if (mx >= panel_start_X + 5 && mx <= panel_start_X + 35 && my >= actualContentStartY + 5 && my <= actualContentStartY + 20)
                    {
                        uiMode = "viewing";
                        routeStep = 0;
                        return;
                    }

                    int cardX = 5 + 2; 
                    int cardW = (panel_width - 10) - 4;
                    int actionBtnW = (cardW - 4) / 2;
                    
                    String vehType = selectedVehicle.getVehicleType();
                    boolean isConstruction = vehType.equals("Dozer") || vehType.equals("Asphalt Paver") || vehType.equals("Excavator");

                    // Click Point 1 Button
                    if (mx >= panel_start_X + cardX && mx <= panel_start_X + cardX + cardW &&
                        my >= contentStartY + 45 && my <= contentStartY + 45 + 20)
                    {
                        routeStep = 1;
                        message("Click on the world map to set target", 180);
                        return;
                    }

                    // Click Point 2 Button (Only accessible if not a construction machine)
                    if (!isConstruction && mx >= panel_start_X + cardX && mx <= panel_start_X + cardX + cardW &&
                        my >= contentStartY + 75 && my <= contentStartY + 75 + 20)
                    {
                        routeStep = 2;
                        message("Click on the world map to set destination", 180);
                        return;
                    }

                    // Bottom Action Buttons
                    int actionBtnY = contentStartY + 185;
                    // Cancel Button Pressed
                    if (mx >= panel_start_X + cardX && mx <= panel_start_X + cardX + actionBtnW &&
                        my >= actionBtnY && my <= actionBtnY + 22)
                    {
                        uiMode = "viewing";
                        routeStep = 0;
                        return;
                    }
                    
                    // Send Task Button Pressed
                    if (mx >= panel_start_X + cardX + actionBtnW + 4 && mx <= panel_start_X + cardX + cardW &&
                        my >= actionBtnY && my <= actionBtnY + 22)
                    {
                        if (isConstruction)
                        {
                            selectedVehicle.addToWorkQueue(new Vehicle.Task(location_1_row, location_1_col, "Construction"));
                        }
                        else
                        {
                            selectedVehicle.addToWorkQueue(new Vehicle.Task(location_1_row, location_1_col, workType, location_1_row, location_1_col, location_2_row, location_2_col, ""));
                        }
                        message("Task dispatched to vehicle!", 200);
                        uiMode = "home";
                        routeStep = 0;
                        return;
                    }
                }
            }
            if (clickedVehicle && selectedVehicle != null) 
            {
                int btnX = vehicleUiX + 10;
                int btnY = vehicleUiY + 80;
                if (mx >= btnX && mx <= btnX + 100 && my >= btnY && my <= btnY + 18)
                {
                    activeTab = 1; 
                    isOpen = true;
                    uiMode = "viewing";
                    
                    String typeName = selectedVehicle.getVehicleType();
                    List<String> garageVehicles = new ArrayList<String>(Level.garage.keySet());
                    selectedGarageIndex = garageVehicles.indexOf(typeName); 
                    
                    clickedVehicle = false; 
                    return;
                }
            }
        }

        if (isOpen && activeTab == 1 && (uiMode.equals("buying") || uiMode.equals("home")))
        {
            // Fetch the key that was clicked down this frame, if any
            String key = Greenfoot.getKey();
            int scrollDirection = 0;

            if (key != null)
            {
                if (key.equals("up"))
                {
                    scrollDirection = -1;
                }
                else if (key.equals("down"))
                {
                    scrollDirection = 1;
                }
            }

            if (scrollDirection != 0)
            {
                int itemCount = uiMode.equals("buying") ? getVehicleShopItemCount() : getFlatVehicleList().size();
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
                drawGarageIcon(displayImg, tabX + tab_width / 2, tabY + tab_height / 2);
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
                panelImg.setColor(activeSubTab == 3 ? Color.YELLOW : Color.WHITE);
                panelImg.drawString("Base", 5, 30);

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
        else if (clickedVehicle)
        {
            drawVehicleUI(displayImg);
        }
        
        drawResources(displayImg);

        if (clickedFactory && Level.factories.containsKey(activeFactoryKey))
        {
            Level.Factory currentFactory = Level.factories.get(activeFactoryKey);
            factoryRecoursesLeft = currentFactory.getStoredResources();
            craftTimeLeft = currentFactory.getConstructionTime();
            processed = currentFactory.getProcessedResources();
        }
        if (popupTimer > 0 && activePopupMessage != null)
        {
            displayImg.setFont(new Font("Arial", true, false, 14));
            int textWidth = activePopupMessage.length() * 7;
            int textX = (1280 - textWidth) / 2;
            int textY = 300;
            displayImg.setColor(new Color(0, 0, 0, 180));
            displayImg.fillRect(textX -20, textY - 27, textWidth + 40, 44);
            displayImg.setColor(Color.WHITE);
            displayImg.drawString(activePopupMessage, textX, textY);
            displayImg.drawRect(textX - 10, textY - 17, textWidth + 20, 24);
        }
        if (messageTimer > 0 && activeMessage != null)
        {
            displayImg.setFont(new Font("Arial", true, false, 14));
            int textWidth = activeMessage.length() * 7;
            int textX = 640 - (textWidth / 2);
            int textY = 60;
            displayImg.setColor(Color.WHITE);
            displayImg.drawString(activeMessage, textX, textY);
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

    private void drawGarageIcon(GreenfootImage img, int centerX, int centerY)
    {
        img.setColor(Color.WHITE);
        img.fillRect(centerX - 8, centerY - 4, 11, 8); 
        img.fillRect(centerX + 3, centerY - 1, 6, 5); 
        img.setColor(Color.BLACK);
        img.fillRect(centerX - 6, centerY - 3, 2, 3);
        img.setColor(Color.DARK_GRAY);
        img.fillOval(centerX - 5, centerY + 3, 4, 4);
        img.fillOval(centerX + 3, centerY + 3, 4, 4);
    }

    private void drawRoadIcon(GreenfootImage img, int cx, int cy, boolean isActive)
    {
        img.setColor(Color.BLACK);
        img.fillRect(cx - 10, cy - 14, 20, 28);
        img.setColor(Color.WHITE);
        img.fillRect(cx - 1, cy - 12, 2, 6);
        img.fillRect(cx - 1, cy - 2,  2, 6);
        img.fillRect(cx - 1, cy + 8,  2, 6);
    }

    public static boolean isMouseOverUI(int mouseX, int mouseY)
    {
        for (int i = 0; i < NUM_TABS; i++)
        {
            int tabY = tab_start_X + i * (tab_height + tab_spacing);
            if (mouseX > tab_start_X && mouseX < tab_start_X + tab_width &&
                mouseY > tabY && mouseY < tabY + tab_height)
            {
                return true;
            }
        }

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
        if (tileId >= 500)
        {
            try {
                GreenfootImage upgradeSheet = new GreenfootImage("upgrade_icons.png"); 
                int spriteW = 16; 
                int spriteH = 16;
                int index = tileId - 500;
                int offsetX = index * spriteW;
                tile = new GreenfootImage(spriteW, spriteH);
                tile.drawImage(upgradeSheet, -offsetX, 0);
            }
            catch (Exception e) {
                tile = new GreenfootImage(width, height);
                tile.setColor(Color.DARK_GRAY);
                tile.fillRect(0, 0, width, height);
                tile.setColor(Color.WHITE);
                tile.drawString("UPG " + tileId, 2, 15);
            }
        }
        else if (tileId >= 0 && tileId < Level.tiles.length && Level.tiles[tileId] != null)
        {
            tile = new GreenfootImage(Level.tiles[tileId]);
        }
        else
        {
            tile = new GreenfootImage(width, height);
        }
        tile.scale(width, height);
        return tile;
    }

    public static void drawFactoryUI(GreenfootImage img)
    {
        int panelX = factoryUIX;
        int panelY = factoryUIY;
        img.setColor(new Color(0, 0, 0, 180)); 
        img.fillRect(panelX, panelY, 120, 85);   
        img.setFont(new Font("Arial", false, false, 12));
        img.setColor(Color.WHITE);
        img.drawString("Factory:", panelX + 10, panelY + 15);
        img.drawString("Resources: " + factoryRecoursesLeft, panelX + 10, panelY + 35);
        
        int barX = panelX + 10;
        int barY = panelY + 44;
        int barWidth = 100;
        int barHeight = 10;

        img.setColor(Color.DARK_GRAY);
        img.fillRect(barX, barY, barWidth, barHeight);

        if (factoryRecoursesLeft > 0)
        {
            float progressFraction = (180f - craftTimeLeft) / 180f;
            progressFraction = Math.max(0.0f, Math.min(1.0f, progressFraction));
            int fillWidth = (int)(barWidth * progressFraction);
            img.setColor(Color.GREEN);
            img.fillRect(barX, barY, fillWidth, barHeight);
        }
        
        img.setColor(Color.BLACK);
        img.drawRect(barX, barY, barWidth, barHeight);
        img.setColor(Color.WHITE);
        img.drawString("Processed: " + processed, panelX + 10, panelY + 72);
    }

    private void drawGarage(GreenfootImage img, int startX, int startY)
    {
        int contentWidth = panel_width - 10; 

        if (uiMode.equals("home"))
        {
            List<Vehicle> flatVehicles = getFlatVehicleList();
            int itemCount = flatVehicles.size();

            img.setFont(new Font("Arial", true, false, 11));
            img.setColor(Color.WHITE);
            img.drawString("Vehicles (" + itemCount + ")", startX + 5, startY - 20);
            
            int previewSize = 44;
            int previewX = startX + 5;
            int maxVisibleRows = Math.max(1, VEHICLE_SHOP_VISIBLE_ROWS);
            int visibleRows = Math.min(itemCount - vehicleShopScroll, maxVisibleRows);
            int nextY = startY + 6;

            for (int i = 0; i < visibleRows; i++)
            {
                int garageIndex = vehicleShopScroll + i;
                int rowY = startY + 6 + i * VEHICLE_SHOP_ROW_HEIGHT;
                nextY = rowY + VEHICLE_SHOP_ROW_HEIGHT;

                img.setColor(new Color(255, 255, 255, 35));
                img.fillRect(startX + 2, rowY, contentWidth - 4, VEHICLE_SHOP_ROW_HEIGHT - 6);
                img.setColor(Color.WHITE);
                img.drawRect(startX + 2, rowY, contentWidth - 4, VEHICLE_SHOP_ROW_HEIGHT - 6);

                Vehicle targetVehicle = flatVehicles.get(garageIndex);
                String vehName = targetVehicle.getVehicleType();

                int typeSubTab = 0;
                int slotIndex = 0;

                if (vehName.equals("Dump Truck")) { typeSubTab = 0; slotIndex = 0; }
                else if (vehName.equals("Flatbed Truck")) { typeSubTab = 0; slotIndex = 1; }
                else if (vehName.equals("Tractor")) { typeSubTab = 0; slotIndex = 2; }
                else if (vehName.equals("Concrete Mixer")) { typeSubTab = 0; slotIndex = 3; }
                else if (vehName.equals("Flatbed Trailer")) { typeSubTab = 1; slotIndex = 0; }
                else if (vehName.equals("Bulk Trailer")) { typeSubTab = 1; slotIndex = 1; }
                else if (vehName.equals("Dozer")) { typeSubTab = 2; slotIndex = 0; }
                else if (vehName.equals("Asphalt Paver")) { typeSubTab = 2; slotIndex = 1; }
                else if (vehName.equals("Excavator")) { typeSubTab = 2; slotIndex = 2; }

                int oldSub = activeSubTab;
                activeSubTab = typeSubTab;
                GreenfootImage vehicleSprite = getVehicleSprite(slotIndex);
                activeSubTab = oldSub;

                if (vehicleSprite != null) {
                    GreenfootImage previewCanvas = new GreenfootImage(previewSize, previewSize);
                    previewCanvas.setColor(new Color(0, 0, 0, 0));
                    previewCanvas.fillRect(0, 0, previewSize, previewSize);
                    int centerX = (previewSize - vehicleSprite.getWidth()) / 2;
                    int centerY = (previewSize - vehicleSprite.getHeight()) / 2;
                    previewCanvas.drawImage(vehicleSprite, centerX, centerY);
                    img.drawImage(previewCanvas, previewX, rowY + 7);
                } else {
                    img.setColor(Color.DARK_GRAY);
                    img.fillRect(previewX, rowY + 7, previewSize, previewSize);
                }

                int nameY = rowY + 5 + previewSize + 8;
                img.setFont(new Font("Arial", true, false, 9));
                img.setColor(Color.WHITE);
                img.drawString(vehName, previewX + 1, nameY);

                img.setFont(new Font("Arial", false, false, 8));
                img.setColor(Color.LIGHT_GRAY);
                img.drawString("#" + (garageIndex + 1), contentWidth - 18, rowY + 14);
            }

            // Append shop button smoothly below the final visible item row
            int btnY = nextY + 5;
            int btnW = contentWidth - 4;
            int btnH = 22;

            img.setColor(new Color(0, 120, 220));
            img.fillRect(startX + 2, btnY, btnW, btnH);
            img.setColor(Color.WHITE);
            img.drawRect(startX + 2, btnY, btnW, btnH);
            img.setFont(new Font("Arial", true, false, 10));
            img.drawString("OPEN SHOP", startX + 2 + (btnW - 60) / 2, btnY + 15);
        }
        else if (uiMode.equals("buying"))
        {

            img.setFont(new Font("Arial", true, false, 10));
            img.setColor(Color.RED);
            img.drawString("< Back", startX, startY);

            int itemCount = getVehicleShopItemCount();
            int previewSize = 44;
            int previewX = startX + 5;
            int abilityBoxSize = 18;
            int maxVisibleRows = Math.max(1, VEHICLE_SHOP_VISIBLE_ROWS);
            int visibleRows = Math.min(itemCount - vehicleShopScroll, maxVisibleRows);

            for (int i = 0; i < visibleRows; i++)
            {
                int slotIndex = vehicleShopScroll + i;
                int rowY = startY + 6 + i * VEHICLE_SHOP_ROW_HEIGHT;

                img.setColor(new Color(255, 255, 255, 35));
                img.fillRect(startX + 2, rowY, contentWidth - 4, VEHICLE_SHOP_ROW_HEIGHT - 6);
                img.setColor(Color.WHITE);
                img.drawRect(startX + 2, rowY, contentWidth - 4, VEHICLE_SHOP_ROW_HEIGHT - 6);

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
                img.setFont(new Font("Arial", true, false, 9));
                img.setColor(Color.WHITE);
                img.drawString(getVehicleName(slotIndex), previewX + 1, nameY);

                int abilityBoxX = contentWidth - 22;
                int abilityBoxY = rowY + 8;

                int[] abilityIcons = getVehicleAbilityIcons(slotIndex);
                int boxCount = Math.min(abilityIcons.length, 2);

                for (int iconIndex = 0; iconIndex < boxCount; iconIndex++)
                {
                    int boxGap = 2;
                    int boxX = abilityBoxX + (iconIndex == 1 ? -(abilityBoxSize + boxGap) : 0);
                    int boxY = abilityBoxY;

                    img.setColor(new Color(255, 255, 255, 90));
                    img.fillRect(boxX, boxY, abilityBoxSize, abilityBoxSize);
                    img.setColor(Color.LIGHT_GRAY);
                    img.drawRect(boxX, boxY, abilityBoxSize, abilityBoxSize);

                    GreenfootImage abilityIcon = getAbilityIcon(abilityIcons[iconIndex]);
                    abilityIcon.scale(12, 12);
                    img.drawImage(abilityIcon, boxX + 3, boxY + 3);
                }

                int costX = contentWidth - 48;
                int costY = rowY + 40;
                img.setFont(new Font("Arial", false, false, 9));
                img.setColor(Color.YELLOW);
                img.drawString("Cost:", costX, costY);

                GreenfootImage resourceIcon = getResourceIcon(0);
                resourceIcon.scale(10, 12);
                img.drawImage(resourceIcon, costX + 24, costY - 10);
                img.drawString(String.valueOf(getVehicleCost(slotIndex)), costX + 34, costY);
            }
        }
        else if (uiMode.equals("buy-viewing") || uiMode.equals("viewing"))
        {
            int slotIndex = uiMode.equals("buy-viewing") ? selectedShopIndex : selectedGarageIndex;
            if (slotIndex >= 0)
            {
                img.setFont(new Font("Arial", true, false, 10));
                img.setColor(Color.RED);
                img.drawString("< Back", startX, startY + 5);

                int cardX = startX + 2;
                int cardY = startY + 15;
                int cardW = contentWidth - 4;
                int cardH = 75;

                img.setColor(new Color(255, 255, 255, 45));
                img.fillRect(cardX, cardY, cardW, cardH);
                img.setColor(Color.WHITE);
                img.drawRect(cardX, cardY, cardW, cardH);

                String name = "Vehicle";
                int typeSubTab = activeSubTab;
                int targetSlot = slotIndex;

                if (uiMode.equals("viewing"))
                {
                    name = selectedVehicle.getVehicleType() + " #" + (selectedGarageIndex + 1);

                    String typeName = selectedVehicle.getVehicleType();
                    if (typeName.equals("Dump Truck")) { typeSubTab = 0; targetSlot = 0; }
                    else if (typeName.equals("Flatbed Truck")) { typeSubTab = 0; targetSlot = 1; }
                    else if (typeName.equals("Tractor")) { typeSubTab = 0; targetSlot = 2; }
                    else if (typeName.equals("Concrete Mixer")) { typeSubTab = 0; targetSlot = 3; }
                    else if (typeName.equals("Flatbed Trailer")) { typeSubTab = 1; targetSlot = 0; }
                    else if (typeName.equals("Bulk Trailer")) { typeSubTab = 1; targetSlot = 1; }
                    else if (typeName.equals("Dozer")) { typeSubTab = 2; targetSlot = 0; }
                    else if (typeName.equals("Asphalt Paver")) { typeSubTab = 2; targetSlot = 1; }
                    else if (typeName.equals("Excavator")) { typeSubTab = 2; targetSlot = 2; }
                } 
                else {
                    name = getVehicleName(slotIndex);
                }

                int oldSub = activeSubTab;
                activeSubTab = typeSubTab;
                GreenfootImage vehicleSprite = getVehicleSprite(targetSlot);
                activeSubTab = oldSub;

                int previewSize = 44;
                int pX = cardX + (cardW - previewSize) / 2;
                int pY = cardY + 6;

                if (vehicleSprite != null)
                {
                    GreenfootImage previewCanvas = new GreenfootImage(previewSize, previewSize);
                    previewCanvas.setColor(new Color(0, 0, 0, 0));
                    previewCanvas.fillRect(0, 0, previewSize, previewSize);
                    int centerX = (previewSize - vehicleSprite.getWidth()) / 2;
                    int centerY = (previewSize - vehicleSprite.getHeight()) / 2;
                    previewCanvas.drawImage(vehicleSprite, centerX, centerY);
                    img.drawImage(previewCanvas, pX, pY);
                }
                else
                {
                    img.setColor(Color.DARK_GRAY);
                    img.fillRect(pX, pY, previewSize, previewSize);
                }

                img.setFont(new Font("Arial", true, false, 9));
                img.setColor(Color.YELLOW);
                int nameWidth = name.length() * 5; 
                img.drawString(name, cardX + (cardW - nameWidth) / 2, cardY + 64);

                int labelY = cardY + cardH + 15;
                img.setFont(new Font("Arial", false, false, 10));
                img.setColor(Color.WHITE);
                img.drawString("Abilities:", startX + 5, labelY);

                int[] abilityIcons = getVehicleAbilityIcons(targetSlot);
                int iconY = labelY + 4;
                int iconSize = 18;

                if (abilityIcons.length == 0)
                {
                    img.setFont(new Font("Arial", false, true, 9));
                    img.setColor(Color.LIGHT_GRAY);
                    img.drawString("None", startX + 5, iconY + 10);
                }
                else
                {
                    for (int i = 0; i < abilityIcons.length; i++)
                    {
                        int iconX = startX + 5 + i * (iconSize + 4);
                        img.setColor(new Color(255, 255, 255, 90));
                        img.fillRect(iconX, iconY, iconSize, iconSize);
                        img.setColor(Color.LIGHT_GRAY);
                        img.drawRect(iconX, iconY, iconSize, iconSize);

                        GreenfootImage abilityIcon = getAbilityIcon(abilityIcons[i]);
                        abilityIcon.scale(12, 12);
                        img.drawImage(abilityIcon, iconX + 3, iconY + 3); 
                    }
                }

                int btnY = startY + 185;
                int btnH = 22;

                if (uiMode.equals("buy-viewing")) {
                    int priceY = iconY + iconSize + 15;
                    img.setFont(new Font("Arial", true, false, 10));
                    img.setColor(Color.WHITE);
                    img.drawString("Price:", startX + 5, priceY);

                    GreenfootImage resourceIcon = getResourceIcon(0);
                    resourceIcon.scale(10, 12);
                    img.drawImage(resourceIcon, startX + 40, priceY - 10);
                    img.setColor(Color.YELLOW);
                    img.drawString(String.valueOf(getVehicleCost(slotIndex)), startX + 52, priceY);

                    int btnW = (cardW - 4) / 2;
                    img.setColor(new Color(0, 150, 0));
                    img.fillRect(cardX, btnY, btnW, btnH);
                    img.setColor(Color.WHITE);
                    img.drawRect(cardX, btnY, btnW, btnH);
                    img.setFont(new Font("Arial", true, false, 9));
                    img.drawString("BUY", cardX + (btnW - 20) / 2, btnY + 15);

                    img.setColor(new Color(0, 100, 200));
                    img.fillRect(cardX + btnW + 4, btnY, btnW - 2, btnH);
                    img.setColor(Color.WHITE);
                    img.drawRect(cardX + btnW + 4, btnY, btnW - 2, btnH);
                    img.drawString("FIN", cardX + btnW + 4 + ((btnW - 2) - 16) / 2, btnY + 15);
                } else {
                    img.setColor(new Color(0, 150, 0));
                    img.fillRect(cardX, btnY, cardW, btnH);
                    img.setColor(Color.WHITE);
                    img.drawRect(cardX, btnY, cardW, btnH);
                    img.setFont(new Font("Arial", true, false, 10));
                    img.drawString("Set task", cardX + (cardW - 40) / 2, btnY + 15);
                }
            }
        }
        else if (uiMode.equals("task"))
        {
            if (selectedVehicle != null)
            {
                // Title & Back Button
                img.setFont(new Font("Arial", true, false, 10));
                img.setColor(Color.RED);
                img.drawString("< Back", startX, startY + 5);

                img.setFont(new Font("Arial", true, false, 11));
                img.setColor(Color.WHITE);
                img.drawString("Configure Task", startX + 5, startY + 22);

                int cardX = startX + 2;
                int cardW = contentWidth - 4;
                int btnH = 20;
                
                // Determine vehicle type capability
                String vehType = selectedVehicle.getVehicleType();
                boolean isConstruction = vehType.equals("Dozer") || 
                                         vehType.equals("Asphalt Paver") || 
                                         vehType.equals("Excavator");

                // --- POINT 1 BUTTON (Pickup or Deploy) ---
                int btn1Y = startY + 45;
                if (routeStep == 1) img.setColor(new Color(230, 140, 0)); // Highlight if actively choosing
                else img.setColor(new Color(60, 60, 60));
                
                img.fillRect(cardX, btn1Y, cardW, btnH);
                img.setColor(Color.WHITE);
                img.drawRect(cardX, btn1Y, cardW, btnH);
                img.setFont(new Font("Arial", false, false, 9));
                
                String p1Label = isConstruction ? "Set Target Site" : "1. Set Pickup Point";
                p1Label = (location_1_row >= 0 && location_1_col >= 0)? "(" + location_1_row + "," + location_1_col + ")" : p1Label;
                img.drawString(p1Label, cardX + 6, btn1Y + 13);

                // --- POINT 2 BUTTON (Dropoff - Transport only) ---
                int btn2Y = startY + 75;
                if (!isConstruction)
                {
                    if (routeStep == 2) img.setColor(new Color(230, 140, 0));
                    else img.setColor(new Color(60, 60, 60));
                    
                    img.fillRect(cardX, btn2Y, cardW, btnH);
                    img.setColor(Color.WHITE);
                    img.drawRect(cardX, btn2Y, cardW, btnH);
                    String p2Label = (location_2_row != 0 && location_2_col != 0)? "(" + location_2_row + "," + location_2_col + ")" : "2. Set Dropoff Point";
                    img.drawString(p2Label, cardX + 6, btn2Y + 13);
                }
                else
                {
                    // Visual placeholder for construction units
                    img.setFont(new Font("Arial", false, true, 9));
                    img.setColor(Color.LIGHT_GRAY);
                    img.drawString("Direct deployment", cardX + 6, btn2Y + 13);
                }

                // --- ACTION BUTTONS (Cancel & Confirm/Send) ---
                int actionBtnY = startY + 185;
                int actionBtnH = 22;
                int actionBtnW = (cardW - 4) / 2;

                // Cancel Button
                img.setColor(new Color(180, 40, 40));
                img.fillRect(cardX, actionBtnY, actionBtnW, actionBtnH);
                img.setColor(Color.WHITE);
                img.drawRect(cardX, actionBtnY, actionBtnW, actionBtnH);
                img.setFont(new Font("Arial", true, false, 10));
                img.drawString("CANCEL", cardX + (actionBtnW - 40) / 2, actionBtnY + 14);

                // Send/Confirm Button
                img.setColor(new Color(0, 140, 60));
                img.fillRect(cardX + actionBtnW + 4, actionBtnY, actionBtnW, actionBtnH);
                img.setColor(Color.WHITE);
                img.drawRect(cardX + actionBtnW + 4, actionBtnY, actionBtnW, actionBtnH);
                img.drawString("SEND", cardX + actionBtnW + 4 + (actionBtnW - 30) / 2, actionBtnY + 14);
            }
        }
    }

    private int getVehicleShopItemCount()
    {
        if (activeSubTab == 0) return 4;
        else if (activeSubTab == 1) return 2;
        else if (activeSubTab == 2) return 3;
        return 0;
    }

    private String getVehicleName(int slot)
    {
        if (activeSubTab == 0)
        {
            if (slot == 0) return "Dump Truck";
            if (slot == 1) return "Flatbed Truck";
            if (slot == 2) return "Tractor";
            if (slot == 3) return "Concrete Mixer";
        }
        else if (activeSubTab == 1)
        {
            if (slot == 0) return "Flatbed Trailer";
            if (slot == 1) return "Bulk Trailer";
        }
        else if (activeSubTab == 2)
        {
            if (slot == 0) return "Dozer";
            if (slot == 1) return "Asphalt Paver";
            if (slot == 2) return "Excavator";
        }
        return "Vehicle";
    }

    private int getVehicleCost(int slot)
    {
        if (activeSubTab == 0)
        {
            if (slot == 0) return 60;
            if (slot == 1) return 60;
            if (slot == 2) return 60;
            if (slot == 3) return 80;
        }
        else if (activeSubTab == 1)
        {
            if (slot == 0) return 45;
            if (slot == 1) return 45;
        }
        else if (activeSubTab == 2)
        {
            if (slot == 0) return 60;
            if (slot == 1) return 100;
            if (slot == 2) return 60;
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
                return new GreenfootImage("dumptruck.png");
            }
            else if (slot == 1 || slot == 2 || slot == 3)
            {
                GreenfootImage tilemap = new GreenfootImage("trucks_topdown_spritesheet.png");
                int variantIndex = (slot == 1) ? 4 : (slot == 2) ? 12 : 15;
                int row = variantIndex / 4;
                int col = variantIndex % 4;
                final int SPRITE_WIDTH = 11;
                final int SPRITE_HEIGHT = 27;
                GreenfootImage vehicleImage = new GreenfootImage(SPRITE_WIDTH, SPRITE_HEIGHT);
                vehicleImage.drawImage(tilemap, -(col * SPRITE_WIDTH), -(row * SPRITE_HEIGHT));
                return vehicleImage;
            }
        }
        else if (activeSubTab == 1)
        {
            if (slot == 0 || slot == 1)
            {
                GreenfootImage tilemap = new GreenfootImage("trailers_topdown_spritesheet.png");
                int variantIndex = (slot == 0) ? 4 : 12;
                int row = variantIndex / 4;
                int col = variantIndex % 4;
                final int SPRITE_WIDTH = 11;
                final int SPRITE_HEIGHT = 25;
                GreenfootImage vehicleImage = new GreenfootImage(SPRITE_WIDTH, SPRITE_HEIGHT);
                vehicleImage.drawImage(tilemap, -(col * SPRITE_WIDTH), -(row * SPRITE_HEIGHT));
                return vehicleImage;
            }
        }
        else if (activeSubTab == 2)
        {
            if (slot == 0)
            {
                return new GreenfootImage("dozer.png");
            }
            if (slot == 1)
            {
                GreenfootImage tilemap = new GreenfootImage("trucks_topdown_spritesheet.png");
                int variantIndex = 13;
                int row = variantIndex / 4;
                int col = variantIndex % 4;
                final int SPRITE_WIDTH = 11;
                final int SPRITE_HEIGHT = 27;
                GreenfootImage vehicleImage = new GreenfootImage(SPRITE_WIDTH, SPRITE_HEIGHT);
                vehicleImage.drawImage(tilemap, -(col * SPRITE_WIDTH), -(row * SPRITE_HEIGHT));
                return vehicleImage;
            }
            if (slot == 2)
            {
                GreenfootImage tilemap = new GreenfootImage("excavator_topdown_spritesheet.png");
                int row = 0;
                int col = 0;
                final int SPRITE_WIDTH = 11;
                final int SPRITE_HEIGHT = 37;
                GreenfootImage vehicleImage = new GreenfootImage(SPRITE_WIDTH, SPRITE_HEIGHT);
                vehicleImage.drawImage(tilemap, -(col * SPRITE_WIDTH), -(row * SPRITE_HEIGHT));
                return vehicleImage;
            }
        }
        return null;
    }

    public void popUpMessage(String message, int duration)
    {
        popupTimer = duration;
        this.activePopupMessage = message;
    }

    public void message(String message, int duration)
    {
        messageTimer = duration;
        this.activeMessage = message;
    }

    public void vehicleUi(GreenfootImage img, double x, double y, String vehicleId, String cargo, int cargoQuantity, Vehicle instance)
    {
        clickedVehicle = true;
        clickedFactory = false;
        this.vehicleCargo = cargo;
        this.vehicleCargoQuantity = cargoQuantity;
        this.vehicleId = vehicleId;
        ui.selectedVehicle = instance;
        vehicleUiX = (int) x;
        vehicleUiY = (int) y;
    }

    private void drawVehicleUI(GreenfootImage img)
    {
        int panelX = vehicleUiX;
        int panelY = vehicleUiY;
        img.setColor(new Color(0, 0, 0, 180)); 
        img.fillRect(panelX, panelY, 120, 120);   
        img.setFont(new Font("Arial", false, false, 12));
        img.setColor(Color.WHITE);
        img.drawString("Vehicle:", panelX + 10, panelY + 20);
        img.drawString(vehicleId, panelX + 10, panelY + 35);
        img.drawString("Cargo: " + vehicleCargo, panelX + 10, panelY + 55);
        img.drawString("Qty: " + vehicleCargoQuantity, panelX + 10, panelY + 70);

        int btnX = panelX + 10;
        int btnY = panelY + 80;
        int btnW = 100;
        int btnH = 18;

        img.setColor(new Color(0, 120, 220)); 
        img.fillRect(btnX, btnY, btnW, btnH);
        img.setColor(Color.WHITE);
        img.drawRect(btnX, btnY, btnW, btnH);
        img.setFont(new Font("Arial", true, false, 10));
        img.drawString("VIEW IN UI", btnX + 22, btnY + 13);
    }

    private List<Vehicle> getFlatVehicleList()
    {
        List<Vehicle> flatList = new ArrayList<>();
        if (Level.garage != null)
        {
            for (ArrayList<Vehicle> list : Level.garage.values())
            {
                if (list != null) flatList.addAll(list);
            }
        }
        return flatList;
    }
}