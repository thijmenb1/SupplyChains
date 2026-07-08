import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;

public class Vehicle extends Actor
{
    // Position tracking
    private int current_row;
    private int current_col;
    private int target_row;
    private int target_col;
    private int taskLocation_row_1;
    private int taskLocation_col_1;
    private int taskLocation_row_2;
    private int taskLocation_col_2;

    // Identification
    private String vehicleId; // vehicleType_vehicleNumber
    private Trailer myTrailer;

    // Movement
    private int lastWaitLocation_row = -1;
    private int lastWaitLocation_col = -1;
    private int targetX, targetY;
    private double posX, posY;
    private double exactRotation = 270.0;
    private double speed = 40.0; // pixels per second
    private double workSpeed = 24.0; //pps
    private boolean moving = false;
    private boolean parked = false;
    private double moveDirX = 0;
    private double moveDirY = -1;

    // Pathfinding
    private List<int[]> path;
    private final int RECALC_COOLDOWN = 30;
    private int recalcCooldown = 0;
    private int waitTimer;

    // Constants
    private final int WAIT_TIME = 180;  // 1 seconds at 180 FPS
    private final int TILE_SIZE = 32;

    private String workType; // "pickup", "dropoff", "hookTrailer", etc.
    private String pickupCargo;

    // Sprite variant selection
    private GreenfootImage tilemapSheet;
    private int spriteWidth;
    private int spriteHeight;
    private String cargoType = "none"; // "bulk", "flatbed", "none", "concrete", etc.
    private String vehicleType; // "dump Truck", "Tractor", "concreteMixer", etc.
    private String currentCargo = null;
    private int cargoQuantity = 0;
    private boolean trailerHooked = false;
    private int maxCargoQuantity; // Max cargo quantity based on cargo type and trailer status
    private MovementMode currentMovementMode = MovementMode.road;

    // Construction Vehicles
    private ArrayList<Task> workQueue;
    private int currentTaskIndex = 0;
    private int workStep = 0;
    private int totalCols;

    public static class Task
    {
        public int target_row;
        public int target_col;
        public String workType;
        public int location_row_1;
        public int location_col_1;
        public int location_row_2;
        public int location_col_2;
        public String pickupCargo;

        public Task(int target_row, int target_col, String workType)
        {
            this.target_row = target_row;
            this.target_col = target_col;
            this.workType = workType;
        }

        public Task(int target_row, int target_col, String workType, int r1, int c1, int r2, int c2, String cargo)
        {
            this.target_row = target_row;
            this.target_col = target_col;
            this.workType = workType;
            this.location_row_1 = r1;
            this.location_col_1 = c1;
            this.location_row_2 = r2;
            this.location_col_2 = c2;
            this.pickupCargo = cargo;
        }
    }

    public Vehicle(int vehicleNumber, String vehicleType, int base_col, int base_row)
    {
        this.vehicleId      = vehicleType + " " + vehicleNumber;
        this.vehicleType    = vehicleType;
        this.current_col    = base_col;
        this.current_row    = base_row;
        this.target_row     = base_row;
        this.target_col     = base_col;
        this.workQueue      = new ArrayList<>();

        this.maxCargoQuantity = ((cargoType.equals("Bulk")) ? 0 : (cargoType.equals("Flatbed")) ? 2 : (cargoType.equals("Concrete")) ? 2 : 0) + (trailerHooked ? 2 : 0);
        getSpritesheet();
    }

    // Runs on creation prepares for route
    public void addedToWorld(World world) {
        posX = current_col * TILE_SIZE + TILE_SIZE / 2;
        posY = current_row * TILE_SIZE + TILE_SIZE / 2;
        Level level = (Level) world;
        setLocation((int)Math.round(posX - level.getCameraX()), (int)Math.round(posY - level.getCameraY()));

        setImageVariant();

        if ("Dump Truck".equals(vehicleType))
        {
            myTrailer = new Trailer(this, "DumpTruck", null, 0);
            world.addObject(myTrailer, getX(), getY());
            currentMovementMode = MovementMode.articulated;
        }
        if ("Flatbed Truck".equals(vehicleType))
        {
            myTrailer = new Trailer(this, "Flatbed", null, 0);
            world.addObject(myTrailer, getX(), getY());
            currentMovementMode = MovementMode.trailer;
        }
        if ("Tractor".equals(vehicleType))
        {
            myTrailer = new Trailer(this, "Bulk", null, 0);
            world.addObject(myTrailer, getX(), getY());
        }
        if ("Dozer".equals(vehicleType))
        {
            currentMovementMode = MovementMode.tracked;
        }
    }

    // Main loop
    public void act()
    {
        if (((workType == null) || "completed".equals(workType)) && workQueue != null && currentTaskIndex < workQueue.size())
        {
            loadNextTaskFromQueue();
        }

        if (workQueue != null && currentTaskIndex >= workQueue.size() && !moving && waitTimer <= 0 && parked == false)
        {
            //park add base
        }

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

        if (!moving && (path == null || path.isEmpty()) && "Construction".equals(workType)) {
            constructionWork(); 
        }

        // Smooth movement
        if (moving) {
            updateMovement();
        }

        if (Greenfoot.mouseClicked(this))
        {
            ui.instance.vehicleUi(new GreenfootImage(getImage()), posX, posY, vehicleId, currentCargo, cargoQuantity, this);
        }

        if (myTrailer != null && myTrailer.getWorld() != null)
        {
            myTrailer.trackHead();
        }
    }

    private void loadNextTaskFromQueue()
    {
        Task task = workQueue.get(currentTaskIndex);
        this.target_row = task.target_row;
        this.target_col = task.target_col;
        this.workType = task.workType;
        this.pickupCargo = task.pickupCargo;
        this.taskLocation_col_1 = task.location_col_1;
        this.taskLocation_row_1 = task.location_row_1;
        this.taskLocation_col_2 = task.location_col_2;
        this.taskLocation_row_2 = task.location_row_2;

        path = findPath(current_row, current_col, target_row, target_col);
    }


    private boolean canVehicleCarry(String resourceName)
    {
        if (resourceName == null) return false;

        if (cargoType.equals("Bulk"))
        {
            return resourceName.endsWith("_Ore");
        }
        else if (cargoType.equals("Flatbed"))
        {
            return resourceName.endsWith("_Pallet") || resourceName.endsWith("_IBC") || resourceName.endsWith("_Beam") || resourceName.endsWith("_Spool"); 
        }
        return false;
    }

    private void alterWorldTileMap(int row, int col)
    {
        int currentTile = Level.map[row][col]; // Fixed array indexing order
        if (vehicleType.equals("Dozer"))
        {
            switch (currentTile) {
                case 64: case 68: case 72: Level.map[row][col] = 60; break;
                case 65: case 69: case 73: Level.map[row][col] = 61; break;
                case 66: case 70: case 74: Level.map[row][col] = 62; break;

                default:
                    Level.map[row][col] = 0;
            }
        }
        else if (vehicleType.equals("Asphalt Paver"))
        {
            Level.map[row][col] = Level.getUpgradedTileId(currentTile, 32);
        }
    }


    // Called when arriving at depot updating the vehicle state
    private void handleDestinationReached()
    {   
        String adjacentFactoryKey = getAdjacentFactoryKey();
        Level.Factory adjacentFactory = Level.factories.get(adjacentFactoryKey);

        if ("Construction".equals(workType))
        {
            constructionWork();
        }


        if ("transport_route".equals(workType) || "materialDropoff".equals(workType))
        {
            // Occurs when the vehicle arrives at the Factory/Dropoff Depot
            if (current_row == target_row && current_col == target_col)
            {
                if (adjacentFactory != null && currentCargo != null && current_row == taskLocation_row_2 && current_col == taskLocation_col_2)
                {
                    if (adjacentFactory.acceptsResource(currentCargo))
                    {
                        adjacentFactory.addResource(currentCargo);
                        cargoQuantity--;
                        if (cargoQuantity == 0) {
                            currentCargo = null;
                        }
                    }
                    else
                    {
                        ui.instance.message("Factory rejects cargo: " + currentCargo, 180);
                    }
                }
                
                if (adjacentFactory != null && adjacentFactory.getProcessedResources() > 0 && cargoQuantity < maxCargoQuantity && current_row == taskLocation_row_1 && current_col == taskLocation_col_1)
                {
                    String outputGood = adjacentFactory.getOutputResource();
                    if (canVehicleCarry(outputGood))
                    {
                        if (currentCargo == outputGood)
                        {
                            cargoQuantity++;
                            currentCargo = adjacentFactory.pickupResource(pickupCargo);
                        }
                        else if (currentCargo == null)
                        {
                            cargoQuantity++;
                            currentCargo = adjacentFactory.pickupResource(pickupCargo);
                        }
                        else
                        {
                            ui.instance.message("Vehicle cannot carry different cargo types.", 180);
                        }
                    }
                }

                setImageVariant();
                target_row = (workType.equals("transport_route"))? ((current_row == taskLocation_row_1 && current_col == taskLocation_col_1) ? taskLocation_row_2 : taskLocation_row_1) : ((current_row == taskLocation_row_1 && current_col == taskLocation_col_1) ? taskLocation_row_2 : Level.base_row) ;
                target_col = (workType.equals("transport_route"))? ((current_row == taskLocation_row_1 && current_col == taskLocation_col_1) ? taskLocation_col_2 : taskLocation_col_1) : ((current_row == taskLocation_row_1 && current_col == taskLocation_col_1) ? taskLocation_col_2 : Level.base_col) ;
                path = findPath(current_row, current_col, target_row, target_col);
                return;
            }
        }
        else if ("hookTrailer".equals(workType))
        {
            trailerHooked = true;
        }
    }

    public enum MovementMode{
        road,           // all normal vehicles
        tracked,        // more precise turning and movement (dozers, asphaltPaver)
        articulated,     // snapy movement like it is getting pushed by hydraulics
        trailer         // vehicles with large trailers take the corners wider
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
        double moveStepPerFrame = speed / 180.0; 
        double dx = targetX - posX;
        double dy = targetY - posY;
        double distanceToTarget = Math.hypot(dx, dy);

        if (distanceToTarget <= moveStepPerFrame || (currentMovementMode == MovementMode.tracked && distanceToTarget < 1.5))
        {
            posX = targetX;
            posY = targetY;
            current_col = (int)(posX / 32);
            current_row = (int)(posY / 32);
            moving = false;

            if (currentMovementMode == MovementMode.tracked && "Construction".equals(workType))
            {
                workStep++;
                constructionWork();
            }
            else{
                checkDestinationArrival();
            }
            return;
        } 
        
        switch (currentMovementMode) {
            case road:
                handleRoadMovement(dx, dy, distanceToTarget, moveStepPerFrame);
                break;
            
            case tracked:
                handleTrackedMovement(dx, dy, distanceToTarget, moveStepPerFrame);
                break;

            case articulated:
                handleArticulatedMovement(dx, dy, distanceToTarget, moveStepPerFrame);
                break;

            case trailer:
                handleTrailerMovement(dx, dy, distanceToTarget, moveStepPerFrame);
                break;

            default:
                System.err.println("current moveMode not expected" + currentMovementMode);
        }
        Level level = (Level) getWorld();
        if (null != level)
        {
            setLocation((int)Math.round(posX - level.getCameraX()),(int)Math.round(posY - level.getCameraY()));
        }
    }

    private void handleTrackedMovement(double dx, double dy, double distance, double step)
    {
        posX += (dx / distance) * step;
        posY += (dy / distance) * step;

        int targetAngel = (int) Math.toDegrees(Math.atan2(dy, dx)) + 90;
        exactRotation = (targetAngel + 360) % 360;
        setRotation((int)exactRotation);
    }

    private void handleRoadMovement(double dx, double dy, double distance, double step)
    {
        // standard physics for rigid vehicles
        int targetAngle = (int)Math.toDegrees(Math.atan2(dy, dx)) + 90;
        targetAngle = (targetAngle + 360) % 360;

        // Fast rotation adjustment for rigid vehicles
        updateRotationAdjusted(targetAngle, 2.5); 

        double angleError = Math.abs(targetAngle - exactRotation);
        if (angleError > 180) angleError = 360 - angleError;

        // Moderate slowing if heavily misaligned
        double speedFactor = Math.cos(Math.toRadians(angleError));
        if (speedFactor < 0.70) speedFactor = 0.70; 

        // Rigid trucks follow the path strictly (90% path pull, 10% forward momentum momentum)
        double pathX = dx / distance;
        double pathY = dy / distance;

        posX += pathX * (step * speedFactor);
        posY += pathY * (step * speedFactor);
    }

    private void handleArticulatedMovement(double dx, double dy, double distance, double step)
    {
        int targetAngle = (int)Math.toDegrees(Math.atan2(dy, dx)) + 90;
        targetAngle = (targetAngle + 360) % 360;

        updateRotationAdjusted(targetAngle, 1.0);

        double angleRad = Math.toRadians(exactRotation - 90);
        double forwardX = Math.cos(angleRad);
        double forwardY = Math.sin(angleRad);

        double angleError = targetAngle - exactRotation;
        while (angleError > 180) angleError -= 360;
        while (angleError < -180) angleError += 360;

        double speedFactor = 0.82 + (0.18 * Math.cos(Math.toRadians(Math.abs(angleError))));
        speedFactor = Math.min(1.0, Math.max(0.72, speedFactor));

        posX += forwardX * step * speedFactor;
        posY += forwardY * step * speedFactor;
    }

    private void handleTrailerMovement(double dx, double dy, double distance, double step)
    {
        // wide sweeping physics like a large semi truck
        int targetAngle = (int)Math.toDegrees(Math.atan2(dy, dx)) + 90;
        targetAngle = (targetAngle + 360) % 360;

        // Slow cab steering update creating progressive trailer track drift
        updateRotationAdjusted(targetAngle, 0.5); 

        double angleError = Math.abs(targetAngle - exactRotation);
        if (angleError > 180) angleError = 360 - angleError;

        double speedFactor = Math.cos(Math.toRadians(angleError));
        if (speedFactor < 0.55) speedFactor = 0.55; // Brake significantly on sharp corners

        double adjustedDistance = step * speedFactor;

        double forwardAngleRad = Math.toRadians(exactRotation - 90);
        double forwardX = Math.cos(forwardAngleRad);
        double forwardY = Math.sin(forwardAngleRad);
        
        double pathX = dx / distance;
        double pathY = dy / distance;

        // Sweeping curve blend weights
        double blendX = (forwardX * 0.45) + (pathX * 0.55);
        double blendY = (forwardY * 0.45) + (pathY * 0.55);

        double blendLength = Math.hypot(blendX, blendY);
        if (blendLength > 0) {
            blendX /= blendLength;
            blendY /= blendLength;
    }

    posX += blendX * adjustedDistance;
    posY += blendY * adjustedDistance;
    }

    // Rotates sprite smoothly toward targetAngle
    private void updateRotationAdjusted(int targetAngle, double turnSpeed)
    {
        double current = exactRotation;
        double diff = targetAngle - current;

        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;

        if (Math.abs(diff) < turnSpeed) {
            exactRotation = targetAngle;
        } else {
            exactRotation = current + (diff > 0 ? turnSpeed : -turnSpeed);
        }

        exactRotation = (exactRotation + 360) % 360;
        setRotation((int)Math.round(exactRotation));
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
    

    private void getSpritesheet()
    {
        switch (vehicleType) {
            case "Excavator":
                tilemapSheet = new GreenfootImage("excavator_topdown_spritesheet.png");
                spriteWidth = 11; spriteHeight = 37; totalCols = 17;
                break;
            
            case "Dozer":
                tilemapSheet = new GreenfootImage("dozer.png");
                spriteWidth = 16; spriteHeight = 25; totalCols = 1;
                break;

            default:
                tilemapSheet = new GreenfootImage("trucks_topdown_spritesheet.png");
                spriteWidth = 11; spriteHeight = 27; totalCols = 4;
                break;
        }
    }

    // Applies the correct sprite form the spritesheet
    private void setImageVariant() {
        int variantIndex = getVariantIndex();
        
        // Calculate row and column in the tilemap (4 rows, 4 columns)
        int row = variantIndex / totalCols;
        int col = variantIndex % totalCols;
        int x = col * spriteWidth;
        int y = row * spriteHeight;

        
        // Create a new image for this vehicle with the correct sprite
        GreenfootImage vehicleImage = new GreenfootImage(spriteWidth, spriteHeight);
        vehicleImage.drawImage(tilemapSheet, -x, -y);
        setImage(vehicleImage);
    }

    // Returns the correct sprite
    private int getVariantIndex()
    {

        if ("Dump Truck".equals(vehicleType))
        {
            return 0;
        }
        else if ("Flatbed Truck".equals(vehicleType))
        {
            if (null == currentCargo) return 4;
            switch (currentCargo) {
                case "Steel_Beam":          return 5;
                case "Copper_Spool":        return 6;
                case "Sulfur_crate":        return 7;
                case "Pcb_Create":          return 8;
                case "HardenedSteel_Beam":  return 9;
                case "Cable_Spool":         return 10;
                case "CopperSulfate_IBC":   return 11;
                default:                    return 4;
            }
        }
        else if ("Tractor".equals(vehicleType))
        {
            return 12;
        }
        else if ("Concrete Mixer".equals(vehicleType))
        {
            return 15;
        }
        else if ("Excavator".equals(vehicleType))
        {
            return 0;
        }
        else if ("Dozer".equals(vehicleType))
        {
            return 0;
        }
        else if ("Asphalt Paver".equals(vehicleType))
        {
            return 13;
        }
        else
        {
            System.out.println("Unknown vehicle type: " + vehicleType);
            return 4; //fillsave for unknown vehicle types
        }
    }

    private boolean isOffRoadVehicle()
    {
        return "Tractor".equals(vehicleType) || "Excavator".equals(vehicleType) || "Dozer".equals(vehicleType) || "Asphalt Paver".equals(vehicleType);
    }

    private boolean isTilePassableOffroad(int tileId)
    {
        boolean isRiver = (tileId == 48 || tileId == 49 || 52 <= tileId && tileId <= 55);
        boolean isBuilding = (64 <= tileId && tileId <= 83);

        return !(isRiver || isBuilding);
    }

    private boolean hasLineOfSight(int r1, int c1, int r2, int c2)
    {
        int[][] map = Level.map;
        int numRows = map.length;
        int numCols = map[0].length;

        int dr = Math.abs(r2 - r1);
        int dc = Math.abs(c2 - c1);
        int r = r1;
        int c = c1;

        int r_inc = (r2 > r1)? 1 : -1;
        int c_inc = (c2 > c1)? 1 : -1;

        int error = dc -dr;
        dr *= 2;
        dc *= 2;

        int steps = 1 + dr + dc;
        for (int i = 0; i < steps; i++)
        {
            if (r < 0 || r >= numRows || c < 0 || c >= numCols) return false;
            if (!isTilePassableOffroad(map[r][c])) return false;
            if (r == r2 && c == c2) break;
            if (error > 0)
            {
                c += c_inc;
                error -= dr;
            }
            else
            {
                r += r_inc;
                error += dc;
            }
        }
        return true;
    }

    private List<int[]> smoothPath(List<int[]> originalPath)
    {
        if (originalPath == null || originalPath.size() <= 2) return originalPath;
        List<int[]> smoothed = new ArrayList<>();
        smoothed.add(originalPath.get(0));

        int currentIdx = 0;
        while (currentIdx < originalPath.size() -1)
        {
            int nextIdx = currentIdx + 1;

            for (int i = originalPath.size() -1; i > currentIdx; i--)
            {
                int[] start = originalPath.get(currentIdx);
                int[] end = originalPath.get(i);
                if (hasLineOfSight(start[0], start[1], end[0], end[1]))
                {
                    nextIdx = i;
                    break;
                }
            }
            smoothed.add(originalPath.get(nextIdx));
            currentIdx = nextIdx;
        }
        return smoothed;
    }

    
    // Pathfinding with BFS finds the shortest valid route between two tiles
    private List<int[]> findPath(int start_row, int start_col, int target_row, int target_col) {
        int[][] map = Level.map;
        int numRows = map.length;
        int numCols = map[0].length;
        boolean[][] visited = new boolean[numRows][numCols];
        int[][] came_from_row = new int[numRows][numCols];
        int[][] came_from_col = new int[numRows][numCols];
        
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{start_row, start_col});
        visited[start_row][start_col] = true;
        
        int[] dr = {-1, 1, 0, 0};  
        int[] dc = {0, 0, -1, 1};
        
        boolean offroadCapable = isOffRoadVehicle();
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];
            
            if (r == target_row && c == target_col) {
                List<int[]> calculatedPath = tracePath(came_from_row, came_from_col, start_row, start_col, target_row, target_col);

                if (offroadCapable)
                {
                    return smoothPath(calculatedPath);
                }
                return calculatedPath;
            }
            
            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (nr >= 0 && nr < numRows && nc >= 0 && nc < numCols && !visited[nr][nc]) {
                    int nextTile = map[nr][nc];

                    if (offroadCapable) 
                    {
                        // OffRoad pathfinding check: bypass road connection rules
                        if (isTilePassableOffroad(nextTile)) {
                            visited[nr][nc] = true;
                            came_from_row[nr][nc] = r;
                            came_from_col[nr][nc] = c;
                            queue.add(new int[]{nr, nc});
                        }
                    } 
                    else {
                        // Standard rigid on-road connection rule checks
                        int currentTile = map[r][c];
                        if (nextTile != 0 && canMove(currentTile, dr[i], dc[i]) && canMove(nextTile, -dr[i], -dc[i])) {
                            visited[nr][nc] = true;
                            came_from_row[nr][nc] = r;
                            came_from_col[nr][nc] = c;
                            queue.add(new int[]{nr, nc});
                        }
                    }
                }
            }
        }
        return null; 
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
        if (tile == 1 || tile == 16 || tile == 32) return (dr == -1 || dr == 1);
        if (tile == 2 || tile == 17 || tile == 33) return (dc == -1 || dc == 1);
    
        // Crossroad
        if (tile == 3 || tile == 18 || tile == 34) return true;

        // river crossings (bridges)
        if (tile == 50 || tile == 46) return (dr == -1 || dr == 1); // RIVER_CROSSING_V
        if (tile == 51 || tile == 47) return (dc == -1 || dc == 1); // RIVER_CROSSING_H
    
        // T junctions
        if (tile == 4 || tile == 19 || tile == 35) return (dr == -1 || dr == 1 || dc == 1); // right
        if (tile == 5 || tile == 20 || tile == 36) return (dc == -1 || dc == 1 || dr == -1); // up
        if (tile == 6 || tile == 21 || tile == 37) return (dc == -1 || dc == 1 || dr == 1); // down
        if (tile == 7 || tile == 22 || tile == 38) return (dr == -1 || dr == 1 || dc == -1); // left
    
        // Corners
        if (tile == 8 || tile == 23 || tile == 39) return (dr == -1 || dc == 1);  // NE
        if (tile == 9 || tile == 24 || tile == 40) return (dr == -1 || dc == -1); // NW
        if (tile == 10 || tile == 25 || tile == 41) return (dr == 1 || dc == -1); // SW
        if (tile == 11 || tile == 26 || tile == 42) return (dr == 1 || dc == 1);  // SE
    
        // Depots
        if (tile >= 12 && tile <= 15 || tile >= 27 && tile <= 31 || tile >= 43 && tile <= 45 || tile >= 104 && tile <= 119) return true;
    
        return false;
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

    double getPosX() {return posX; }
    double getPosY() {return posY; }
    public double getMoveDirX() {return moveDirX;}
    public double getMoveDirY() {return moveDirY;}
    public double getVisualRotation() { return (int)Math.round(exactRotation);}

    public void addToWorkQueue(Task task)
    {
        if (this.workQueue == null) this.workQueue = new ArrayList<>();
        this.workQueue.add(task);
    }

    public boolean containsTask(int row, int col)
    {
        if (this.workQueue == null) return false;
        for (Task task : this.workQueue)
        {
            if (task.target_row == row && task.target_col == col) return true;
        }
        return false;
    }

    public void constructionWork()
    {
        if (vehicleType.equals("Excavator"))
        {
            return;
        }
        else if (vehicleType.equals("Asphalt Paver"))
        {
            alterWorldTileMap(target_row, target_col);
            currentTaskIndex++;
            workStep = 0;
            return;
        }
        else if (vehicleType.equals("Dozer"))
        {
            // Clear any path since we are overriding coordinates manually
            if (path != null) path.clear(); 
            moving = true; // Tell act loop to run updateMovement()

            // Base calculations for the top-left pixel anchor of the target tile
            int tileStartX = target_col * TILE_SIZE;
            int tileStartY = target_row * TILE_SIZE;

            switch (workStep) {
                case 0:
                    // Step 0: Move to initial sweep start position (offset right, slightly down)
                    targetX = tileStartX + TILE_SIZE;
                    targetY = tileStartY + 6;
                    workStep++;
                    break;
                    
                case 1:
                    // Step 1: Sweep straight left across the tile
                    speed = workSpeed; // Engages slower blade-down work speed
                    targetX = tileStartX - (spriteHeight / 2);
                    targetY = tileStartY + 6;
                    workStep++;
                    break;
                    
                case 2:
                    // Step 2: Shift downward on the left side to prepare for next lane
                    speed = speed; // standard repositioning speed
                    targetX = tileStartX - (spriteHeight / 2);
                    targetY = tileStartY + spriteWidth + 6;
                    workStep++;
                    break;

                case 3:
                    // Step 3: Sweep straight back right across the tile
                    speed = workSpeed;
                    targetX = tileStartX + TILE_SIZE;
                    targetY = tileStartY + spriteWidth + 6;
                    workStep++;
                    break;

                case 4:
                    // Step 4: Finalize layout modifications on the tile map
                    alterWorldTileMap(target_row, target_col);
                    speed = 64.0; // Return speed
                    
                    if (workQueue == null || currentTaskIndex + 1 >= workQueue.size()) 
                    {
                        // No more tasks: Re-engage standard pathfinding to go home
                        target_row = Level.base_row;
                        target_col = Level.base_col;
                        path = findPath(current_row, current_col, target_row, target_col);
                        workStep = 5;
                    }
                    else
                    {
                        currentTaskIndex++;
                        workStep = 0;
                        loadNextTaskFromQueue();
                    }
                    break;
                    
                case 5:
                    // Finished returning home
                    currentTaskIndex++;
                    workStep = 0;
                    break;
            }
        }
    }
    public String getVehicleType()
    {
        return vehicleType;
    }
}