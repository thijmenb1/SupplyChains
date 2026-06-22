import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Vehicle - a truck that moves resources between depots
 * 
 * Functions:
 * - Vehicle()                  Receives all variables when created
 * - addedToWorld()             Runs on creation
 * - act()                      Main loop
 * - handleDestinationReached() Updates the truck when arrives and prepares for return
 * - checkDestinationArrival()  Checks whether the vehicle has arrived
 * - moveToNextTile()           Sets next waypoint
 * - updateMovement()           Moves the vehicle toward target
 * - updateRotation()           Rotates sprite smoothly toward targetAngle
 * - findPath()                 Pathfinding BFS over the road
 * - tracePath()                Reconstructing the path for the return
 * - canMove()                  Returns in what direction the vehicle can travel
 * - setImageVariant            Sets the correct sprite from the spritesheet
 * - getVariantIndex            Returns what img should be set
 * - checkForDeletion()         Removes vehicle when removed from route
 * 
 * Know bugs:
 * - the checkForDeletion is currently broken
 */
public class Vehicle extends Actor
{
    // Position tracking
    private int current_row;
    private int current_col;
    private int target_row;
    private int target_col;
    private int pickup_row;
    private int pickup_col;
    private int factory_row;
    private int factory_col;
    private int routeNumber;

    // Identification
    private int vehicleNumber;
    private boolean Sell;

    // Movement
    private int lastWaitLocation_row = -1;
    private int lastWaitLocation_col = -1;
    private int targetX, targetY;
    private double posX, posY;
    private double speed = 64.0; // pixels per second
    private long lastActTime = 0;
    private boolean moving = false;

    // Pathfinding
    private List<int[]> path;
    private static final int RECALC_COOLDOWN = 30;
    private int recalcCooldown = 0;
    private int waitTimer = 0;

    // Constants
    private static final int WAIT_TIME = 180;  // 2 seconds at 60 FPS
    private static final int TILE_SIZE = 32;


    // Sprite variant selection
    private String color; // "red", "blue", "yellow"
    private boolean isFull;

    public Vehicle(int start_row, int start_col, int target_row, int target_col, String color, boolean isFull, int routeNumber, int vehicleNumber, boolean Sell)
    {
        this.current_row    = start_row;
        this.current_col    = start_col;
        this.target_row     = target_row;
        this.target_col     = target_col;
        this.pickup_row     = start_row;
        this.pickup_col     = start_col;
        this.factory_row    = target_row;
        this.factory_col    = target_col;
        this.color          = color;
        this.isFull         = isFull;
        this.routeNumber    = routeNumber;
        this.vehicleNumber  = vehicleNumber;
        this.Sell           = Sell;
    }

    // Runs on creation prepares for route
    public void addedToWorld(World world) {
        posX = current_col * TILE_SIZE + TILE_SIZE / 2;
        posY = current_row * TILE_SIZE + TILE_SIZE / 2;
        Level level = (Level) world;
        setLocation((int)Math.round(posX - level.getCameraX()), (int)Math.round(posY - level.getCameraY()));
        setImageVariant();
        path = findPath(current_row, current_col, target_row, target_col);
    }

    // Main loop
    public void act()
    {
        //checkForDeletion();
       
        // Waiting at destinations
        if (waitTimer > 0) {
            waitTimer--;
            if (waitTimer == 0) {
                handleDestinationReached();
            }
            return;
        }
        
        // Cooldown between path calculations
        if (recalcCooldown > 0) recalcCooldown--;

        if ((path == null || path.isEmpty()) && recalcCooldown == 0) {
            path = findPath(current_row, current_col, target_row, target_col);
            recalcCooldown = RECALC_COOLDOWN;
            return;
        }

        // Get next tile if not moving
        if (!moving && path != null && !path.isEmpty()) {
            moveToNextTile();
        }

        // Smooth movement
        if (moving) {
            updateMovement();
        }
    }

    // Called when arriving at depot updating the vehicle state
    private void handleDestinationReached()
    {
        // Occurs when the vehicle arrives at the Factory/Dropoff Depot
        if (current_row == factory_row && current_col == factory_col)
        {
            if (Sell)
            {
                Level.money++; // Instantly make money at a selling depot
            }
            else
            {
                // Call your helper method to search for adjacent factories
                String adjacentFactoryKey = getAdjacentFactoryKey();
                
                if (adjacentFactoryKey != null && Level.factories.get(adjacentFactoryKey).getFactoryColor() == color)
                {
                    // A factory is adjacent to this depot. Drop off the resource.
                    Level.factories.get(adjacentFactoryKey).addResource();
                }
                else
                {
                    System.out.println("Wrong color. Should be: " + color);
                    target_row = pickup_row;
                    target_col = pickup_col;
                    path = findPath(current_row, current_col, target_row, target_col);
                    return;
                }
            }
            
            isFull = false;
            setImageVariant();
            
            // Set up variables for the trip back to the pickup depot
            target_row = pickup_row;
            target_col = pickup_col;
            
            // Rebuild path back to the drill 
            path = findPath(current_row, current_col, target_row, target_col);
            return;
        }

        // Occurs when the vehicle arrives back at the Drill/Pickup Depot
        if (current_row == pickup_row && current_col == pickup_col)
        {   
            if (Sell)
            {
                String adjacentFactoryKey = getAdjacentFactoryKey();
                if (adjacentFactoryKey != null && Level.factories.get(adjacentFactoryKey).getProcessedResources() > 0)
                {
                    Level.factories.get(adjacentFactoryKey).pickupResource();
                    
                    isFull = true;
                    setImageVariant();
                    
                    target_row = factory_row;
                    target_col = factory_col;
                    
                    //  Rebuild path out to the factory
                    path = findPath(current_row, current_col, target_row, target_col);
                    return;
                }
                else
                {
                    lastWaitLocation_row = -1;
                    lastWaitLocation_col = -1;
                    waitTimer = WAIT_TIME;
                    return;
                }
            }
            else
            {
                isFull = true;
                setImageVariant();
                
                target_row = factory_row;
                target_col = factory_col;
                
                //  Rebuild path out to the factory
                path = findPath(current_row, current_col, target_row, target_col);
                return;
            }
        }
    }

    // Adds the next pathfinding waypoint
    private void moveToNextTile()
    {
        int[] next = path.remove(0);
        current_row = next[0];
        current_col = next[1];
        targetX = current_col * TILE_SIZE + TILE_SIZE / 2;
        targetY = current_row * TILE_SIZE + TILE_SIZE / 2;
        moving = true;
    }

    // Updated vehicle position
    private void updateMovement()
    {
        if (lastActTime == 0) {
            lastActTime = System.nanoTime();
        }
        long now = System.nanoTime();
        double deltaSeconds = (now - lastActTime) / 1_000_000_000.0;
        lastActTime = now;
        if (deltaSeconds <= 0) {
            return;
        }

        double dx = targetX - posX;
        double dy = targetY - posY;
        double distance = speed * deltaSeconds;

        // Calculate angle (with offset because sprite faces UP)
        int targetAngle = (int)Math.toDegrees(Math.atan2(dy, dx)) + 90;
        updateRotation(targetAngle);

        double distanceToTarget = Math.hypot(dx, dy);
        Level level = (Level) getWorld();
        if (distanceToTarget <= distance) {
            posX = targetX;
            posY = targetY;
            setLocation((int)(targetX - level.getCameraX()), (int)(targetY - level.getCameraY()));
            moving = false;
            checkDestinationArrival();
        }
        else {
            double moveX = Math.cos(Math.atan2(dy, dx)) * distance;
            double moveY = Math.sin(Math.atan2(dy, dx)) * distance;
            posX += moveX;
            posY += moveY;
            setLocation((int)Math.round(posX - level.getCameraX()), (int)Math.round(posY - level.getCameraY()));
        }
    }

    // Rotates sprite smoothly toward targetAngle
    private void updateRotation(int targetAngle)
    {
        int current = getRotation();
        int diff = targetAngle - current;

        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;

        int turnSpeed = 5;

        if (Math.abs(diff) < turnSpeed) {
            setRotation(targetAngle);
        } else {
            setRotation(current + (diff > 0 ? turnSpeed : -turnSpeed));
        }
    }

    // Checks whether the vehicle has arrived at a depot
    private void checkDestinationArrival()
    {
        boolean atActiveTarget = (current_row == target_row && current_col == target_col);
        boolean justWaitedHere = (current_row == lastWaitLocation_row && current_col == lastWaitLocation_col);
        
        if (atActiveTarget && !justWaitedHere) {
            waitTimer = WAIT_TIME;
            lastWaitLocation_row = current_row;
            lastWaitLocation_col = current_col;
        }
    }
    

    // Applies the correct sprite form the spritesheet
    private void setImageVariant() {
        GreenfootImage tilemap = new GreenfootImage("trucks_topdown_spritesheet.png");
        int variantIndex = getVariantIndex();
        
        // Calculate row and column in the tilemap (2 rows, 4 columns)
        int row = variantIndex / 4;
        int col = variantIndex % 4;
        final int SPRITE_WIDTH = 11;
        final int SPRITE_HEIGHT = 36;
        int x = col * SPRITE_WIDTH;
        int y = row * SPRITE_HEIGHT;

        
        // Create a new image for this vehicle with the correct sprite
        GreenfootImage vehicleImage = new GreenfootImage(SPRITE_WIDTH, SPRITE_HEIGHT);
        vehicleImage.drawImage(tilemap, -x, -y);
        setImage(vehicleImage);
    }

    // Returns the correct sprite
    private int getVariantIndex()
    {
        if (!isFull){  
            return Sell ? 4 : 0;
        
        } else if ("blue".equalsIgnoreCase(color)) {
            return Sell ? 6 : 2;
        } else if ("red".equalsIgnoreCase(color)) {
            return Sell ? 5 : 1;
        } else if ("yellow".equalsIgnoreCase(color)) {
            return Sell ? 7 : 3;
        }
        return 0;
    }
    
    // Changes the color and load state
    public void changeVariant(String newColor, boolean newIsFull) {
        this.color = newColor;
        this.isFull = newIsFull;
        setImageVariant();
    }
    
    // Pathfinding with BFS finds the shortest valid route between two tiles
    private List<int[]> findPath(int start_row, int start_col, int target_row, int target_col){
        int[][] map = Level.map;
        int numRows = map.length;
        int numCols = map[0].length;
        boolean[][] visited = new boolean[numRows][numCols];
        int[][] came_from_row = new int[numRows][numCols];
        int[][] came_from_col = new int[numRows][numCols];
        
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{start_row, start_col});
        visited[start_row][start_col] = true;
        
        int[] dr = {-1, 1, 0, 0};  // up, down, left, right
        int[] dc = {0, 0, -1, 1};
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];
            
            if (r == target_row && c == target_col) {
                return tracePath(came_from_row, came_from_col, start_row, start_col, target_row, target_col);
            }
            
            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (nr >= 0 && nr < numRows && nc >= 0 && nc < numCols && !visited[nr][nc]) {
                    int currentTile = map[r][c];
                    int nextTile = map[nr][nc];

                    if (nextTile != 0 &&
                        canMove(currentTile, dr[i], dc[i]) &&
                        canMove(nextTile, -dr[i], -dc[i])) {

                        visited[nr][nc] = true;
                        came_from_row[nr][nc] = r;
                        came_from_col[nr][nc] = c;
                        queue.add(new int[]{nr, nc});
                    }
                }
            }
        }
        return null; // no path found
    }

    // Reconstructing the path for the return
    private List<int[]> tracePath(int[][] came_from_row, int[][] came_from_col, int start_row, int start_col, int target_row, int target_col){
        List<int[]> path = new ArrayList<>();
        int r = target_row;
        int c = target_col;
        while (r != start_row || c != start_col) {

            path.add(new int[]{r, c});
            int prev_r = came_from_row[r][c];
            int prev_c = came_from_col[r][c];
            r = prev_r;
            c = prev_c;
        }
        path.add(new int[]{start_row, start_col});
        Collections.reverse(path);
        return path;
    }

    // Returns in what direction the vehicle can travel
    private boolean canMove(int tile, int dr, int dc) {
        // dr = change in row, dc = change in col
    
        //roads
        if (tile == 1) return (dr == -1 || dr == 1);
        if (tile == 2) return (dc == -1 || dc == 1);
    
        // Crossroad
        if (tile == 3) return true;

        // river crossings (bridges)
        if (tile == 26) return (dr == -1 || dr == 1); // RIVER_CROSSING_V
        if (tile == 27) return (dc == -1 || dc == 1); // RIVER_CROSSING_H
    
        // T junctions
        if (tile == 4) return (dr == -1 || dr == 1 || dc == 1); // right
        if (tile == 5) return (dc == -1 || dc == 1 || dr == -1); // up
        if (tile == 6) return (dc == -1 || dc == 1 || dr == 1); // down
        if (tile == 7) return (dr == -1 || dr == 1 || dc == -1); // left
    
        // Corners
        if (tile == 8) return (dr == -1 || dc == 1);  // NE
        if (tile == 9) return (dr == -1 || dc == -1); // NW
        if (tile == 10) return (dr == 1 || dc == -1); // SW
        if (tile == 11) return (dr == 1 || dc == 1);  // SE
    
        // Depots
        if (tile >= 12 && tile <= 15) return true;
    
        return false;
    }


    public void checkForDeletion()
    {
        if (routeNumber >= Level.routes.size() || Level.routes.get(routeNumber) == null)
        {
            getWorld().removeObject(this);
            return;
        }
        if (Level.routes.get(routeNumber).getVehicleCount() <= vehicleNumber)
        {
            getWorld().removeObject(this);
        }
    }
    private String getAdjacentFactoryKey()
    {
        // Check all directions
        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
        
        for (int[] dir : directions)
        {
            int neighborRow = current_row + dir[0];
            int neighborCol = current_col + dir[1];
            
            String checkKey = neighborRow + "," + neighborCol;
            
            // If it is in the hashmap return Key
            if (Level.factories.containsKey(checkKey))
            {
                return checkKey;
            }
        }
        return null; // No factory found
    }
}