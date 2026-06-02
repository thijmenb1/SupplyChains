import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
/**
 * Write a description of class Vehicle here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Vehicle extends Actor
{
    private int current_row;
    private int current_col;
    private int target_row;
    private int target_col;
    private int recalcCooldown = 0;
    private int targetX, targetY;
    private double posX, posY;
    private double speed = 64.0; // pixels per second
    private long lastActTime = 0;
    private boolean moving = false;
    private List<int[]> path;
    
    // Sprite variant selection (2x3 tilemap)
    private String color; // "red", "blue", "yellow"
    private boolean isFull; // true for full tank, false for empty
    /**
     * Act - do whatever the Vehicle wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
        public void act()
    {
        if (Level.game_started == true) {
              return;
        }
        
        if (recalcCooldown > 0) recalcCooldown--;

        if ((path == null || path.isEmpty()) && recalcCooldown == 0) {
            path = findPath(current_row, current_col, target_row, target_col);
            recalcCooldown = 30;
            return;
        }

        // Get next tile if not moving
        if (!moving && path != null && !path.isEmpty()) {
            int[] next = path.remove(0);

            current_row = next[0];
            current_col = next[1];

            targetX = current_col * 32 + 16;
            targetY = current_row * 32 + 16;

            moving = true;
        }

            // Smooth movement
        if (moving) {
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

            // Smooth rotation
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

            double distanceToTarget = Math.hypot(dx, dy);
            if (distanceToTarget <= distance) {
                posX = targetX;
                posY = targetY;
                setLocation(targetX, targetY);
                moving = false;

                // Check if vehicle arrived at final destination
                if (current_row == target_row && current_col == target_col) {
                    isFull = false;
                    setImageVariant();
                }
            }
            else {
                double moveX = Math.cos(Math.atan2(dy, dx)) * distance;
                double moveY = Math.sin(Math.atan2(dy, dx)) * distance;
                posX += moveX;
                posY += moveY;
                setLocation((int)Math.round(posX), (int)Math.round(posY));
            }
        }
    }
    public void addedToWorld(World world) {
        posX = current_col * 32 + 16;
        posY = current_row * 32 + 16;
        setLocation((int)Math.round(posX), (int)Math.round(posY));
        setImageVariant();
        path = findPath(current_row, current_col, target_row, target_col);
    }       
    public Vehicle(int start_row, int start_col, int target_row, int target_col, String color, boolean isFull){
        this.current_row = start_row;
        this.current_col = start_col;
        this.target_row = target_row;
        this.target_col = target_col;
        this.color = color;
        this.isFull = isFull;
    }
    
    private void setImageVariant() {
        // Load the tilemap (2x3 layout: blue_empty, red_empty, yellow_empty / blue_full, red_full, yellow_full)
        GreenfootImage tilemap = new GreenfootImage("trucks_topdown_spritesheet.png");
        
        // Determine the variant index based on color and fuel state
        int variantIndex = 0;
        if ("red".equalsIgnoreCase(color)) {
            variantIndex = isFull ? 3 : 0;
        } else if ("blue".equalsIgnoreCase(color)) {
            variantIndex = isFull ? 4 : 1;
        } else if ("yellow".equalsIgnoreCase(color)) {
            variantIndex = isFull ? 5 : 2;
        }
        
        // Calculate row and column in the tilemap (2 rows, 3 columns)
        int row = variantIndex / 3;
        int col = variantIndex % 3;
        
        // Each sprite is 11 pixels wide (33/3) and 32 pixels tall (64/2)
        int spriteWidth = 11;
        int spriteHeight = 32;
        int x = col * spriteWidth;
        int y = row * spriteHeight;
        
        // Create a new image for this vehicle with the correct sprite
        GreenfootImage vehicleImage = new GreenfootImage(spriteWidth, spriteHeight);
        vehicleImage.drawImage(tilemap, -x, -y);
        setImage(vehicleImage);
    }
    
    public void changeVariant(String newColor, boolean newIsFull) {
        this.color = newColor;
        this.isFull = newIsFull;
        setImageVariant();
    }
    
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
}

