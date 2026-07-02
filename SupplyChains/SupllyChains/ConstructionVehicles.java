import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;

public class ConstructionVehicles extends Actor
{
    
    private String vehicleType;
    private ArrayList<int[]> workQueue;
    private int currentTaskIndex = 0;

    private double world_X;
    private double world_Y;
    private boolean arrivedAtCurrentTile = false;

    private int workStep = 0;
    private int animationFrames = 360;
    private int currentFrame = 0;
    int targetRow;
    int targetCol;
    double moveSpeed = 0.2;
    
    private GreenfootImage tilemap;
    private int spriteWidth;
    private int spriteHeight;
    private int variantIndex;
    private int totalCols;


    public ConstructionVehicles(String vehicleType, int startRow, int startCol, ArrayList<int[]> tasks)
    {
        this.vehicleType = vehicleType;
        this.workQueue = tasks;
        this.currentTaskIndex = 0;

        int tileSize = 32;
        this.world_X = (startCol * tileSize) + (tileSize / 2);
        this.world_Y = (startRow * tileSize) + (tileSize / 2);

        initializeVehicleSpecs();
    }

   private void initializeVehicleSpecs()
    {
        switch (vehicleType) {
            case "excavator":
                tilemap = new GreenfootImage("excavator_topdown_spritesheet.png");
                spriteWidth = 11; spriteHeight = 37; variantIndex = 0; totalCols = 17;
                break;
            
            case "dozer":
                tilemap = new GreenfootImage("dozer.png");
                spriteWidth = 16; spriteHeight = 25; variantIndex = 0; totalCols = 1;
                break;

            case "asphaltLayer":
                tilemap = new GreenfootImage("trucks_topdown_spritesheet.png");
                spriteWidth = 11; spriteHeight = 27; variantIndex = 13; totalCols = 4;
                break;

            default:
                tilemap = new GreenfootImage(32, 32);
                break;
        }
        updateVehicleSprite(0);
    }


    public void act()
    {
        if (getWorld() == null) return;
        Level level = (Level) getWorld();
        int tileSize = 32;

        if (workQueue == null || currentTaskIndex >= workQueue.size()) {
            // Task list completed! Remove vehicle or drive back to depot
            getWorld().removeObject(this);
            return;
        }

        int[] currentTile = workQueue.get(currentTaskIndex);
        targetRow = currentTile[0];
        targetCol = currentTile[1];

        if (!arrivedAtCurrentTile) {
            driveToCoordinate(targetRow, targetCol, tileSize);
        } else {
            processTileWork(level, targetRow, targetCol);
        }

        int screen_X = (int) world_X - level.getCameraX();
        int screen_Y = (int) world_Y - level.getCameraY();
        setLocation(screen_X, screen_Y);
    }

    private void driveToCoordinate(int targetRow, int targetCol, int tileSize)
    {
        double targetWorld_X = 0;
        double targetWorld_Y = 0;

        if (vehicleType.equals("excavator"))
        {
            targetWorld_X = (targetCol * tileSize) + (tileSize - 6);
            targetWorld_Y = (targetRow * tileSize) + (tileSize / 2);
        }
        else if (vehicleType.equals("asphaltLayer"))
        {
            targetWorld_X = (targetCol * tileSize) + tileSize ;
            targetWorld_Y = (targetRow * tileSize) + (tileSize / 2);
        }
        else if (vehicleType.equals("dozer"))
        {
            switch (workStep) {
                case 0:
                    targetWorld_X = (targetCol * tileSize) + tileSize;
                    targetWorld_Y = (targetRow * tileSize) + 6;
                    break;
            
                case 1:
                    targetWorld_X = (targetCol * tileSize) - (spriteHeight / 2);
                    targetWorld_Y = (targetRow * tileSize) + 6;
                    break;
                
                case 2:
                    targetWorld_X = (targetCol * tileSize) - (spriteHeight / 2);
                    targetWorld_Y = (targetRow * tileSize) + spriteWidth + 6;
                    break;

                case 3:
                case 4: 
                    targetWorld_X = (targetCol * tileSize) + tileSize;
                    targetWorld_Y = (targetRow * tileSize) + spriteWidth + 6;
                    
                default:
                    break;
            }
            
        }

        double diff_X = targetWorld_X - world_X;
        double diff_Y = targetWorld_Y - world_Y;
        double distance = Math.hypot(diff_X, diff_Y);

        if (distance <= moveSpeed)
        {
            world_X = targetWorld_X;
            world_Y = targetWorld_Y;
            arrivedAtCurrentTile = true;
        }
        else
        {
            world_X += (diff_X / distance) * (moveSpeed);
            world_Y += (diff_Y / distance) * (moveSpeed);

            int rotationAngle = (int) Math.toDegrees(Math.atan2(diff_Y, diff_X)) + 90;
            setRotation(rotationAngle);
        }
    }

    private void processTileWork(Level level, int row, int col)
    {
        if (vehicleType.equals("excavator"))
        {
            return;
        }
        else if (vehicleType.equals("asphaltLayer"))
        {
            return;
        }
        else if (vehicleType.equals("dozer"))
        {   if (arrivedAtCurrentTile){
                switch (workStep) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        if (workStep != 3)
                        {
                            moveSpeed = 0.05;
                        }
                        else
                        {
                            moveSpeed = 0.2;
                        }
                        workStep++;
                        arrivedAtCurrentTile = false;
                        break;
                    
                    case 4:
                        alterWorldTileMap(level, row, col);
                        currentTaskIndex++;
                        arrivedAtCurrentTile = false;
                        workStep = 0;
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private void alterWorldTileMap(Level level, int row, int col)
    {
        int currentTile = level.map[row][col];

        if (vehicleType.equals("dozer")) {
            switch (currentTile) {
                case 64:
                case 68:
                case 72:
                    level.map[row][col] = 60;                    
                    break;
                
                case 65:
                case 69:
                case 73:
                    level.map[row][col] = 61;
                    break;

                case 66:
                case 70:
                case 74:
                    level.map[row][col] = 62;
                    break;
            
                default:
                    level.map[row][col] = 0;
            }
        } 
        else if (vehicleType.equals("asphaltLayer")) {
            level.map[row][col] = Level.getUpgradedTileId(currentTile, 32);
        }
        
    }

    private void updateVehicleSprite(int frameIndex)
    {
        if (tilemap == null) return;
        int frameRow = variantIndex + (frameIndex / totalCols);
        int frameCol = frameIndex % totalCols;

        int srcX = frameCol * spriteWidth;
        int srcY = frameRow * spriteHeight;

        if (srcX + spriteWidth <= tilemap.getWidth() && srcY + spriteHeight <= tilemap.getHeight()) {
            GreenfootImage frameImg = new GreenfootImage(spriteWidth, spriteHeight);
            frameImg.drawImage(tilemap, -srcX, -srcY);
            setImage(frameImg);
        }
    }

    public void addToWorkQueue(int row, int col)
    {
        if (this.workQueue == null)
        {
            this.workQueue = new ArrayList<int[]>();
        }
        this.workQueue.add(new int[]{row, col});
    }

    public String getVehicleType()
    {
        return this.vehicleType;
    }
}
