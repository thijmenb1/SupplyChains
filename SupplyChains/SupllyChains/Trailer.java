import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Trailer extends Actor
{
    private final Vehicle head;
    private final String vehicleType;
    private String currentCargo;

    private static final int hitch_distance = 22;

    public Trailer(Vehicle head, String vehicleType, String currentCargo)
    {
        this.head = head;
        this.vehicleType = vehicleType;
        this.currentCargo = currentCargo;
        updateTrailerSprite();
    }

    public void trackHead()
    {
        if (head == null || head.getWorld() == null)
        {
            if (getWorld() != null)
            {
                getWorld().removeObject(this);
            }
            return;
        }

        Level level = (Level) head.getWorld();

        double headWorld_X = head.getPosX();
        double headWorld_Y = head.getPosY();

        double trailerWorld_X = getX() + level.getCameraX();
        double trailerWorld_Y = getY() + level.getCameraY();

        int targetAngle = (int) Math.toDegrees(Math.atan2(headWorld_Y - trailerWorld_Y, headWorld_X - trailerWorld_X)) + 90;

        double angleRad = Math.toRadians(getRotation() - 90);
        double nextWorld_X = headWorld_X - (Math.cos(angleRad) * hitch_distance);
        double nextWorld_Y = headWorld_Y - (Math.sin(angleRad) * hitch_distance);
        
        int screen_X = (int) Math.round(nextWorld_X - level.getCameraX());
        int screen_Y = (int) Math.round(nextWorld_Y - level.getCameraY());
        setLocation(screen_X, screen_Y);
    }

    public void updateCargo(String cargo)
    {
        this.currentCargo = cargo;
        updateTrailerSprite();
    }

    private void updateTrailerSprite()
    {
        GreenfootImage tilemap = new GreenfootImage("trailers_topdown_spritesheet.png");
        int variantIndex = getVariantIndex();

        int row = variantIndex / 4;
        int col = variantIndex % 4;

        final int sprite_width = 11;
        final int sprite_height = 25;

        int x = col * sprite_width;
        int y = row * sprite_height;

        GreenfootImage trailerImg = new GreenfootImage(sprite_width, sprite_height);
        trailerImg.drawImage(tilemap, -x, -y);
        setImage(trailerImg);
    }

    private int getVariantIndex()
    {
        if (vehicleType.equals("Bulk"))
        {
            if ("Steel_Ore".equals(currentCargo)) return 1;
            if ("Copper_Ore".equals(currentCargo)) return 2;
            if ("Sulfur_Ore".equals(currentCargo)) return 3;
            return 0;
        }

        if (vehicleType.equals("Flatbed"))
        {
            if ("Steel_Beam".equals(currentCargo)) return 5;
            if ("Copper_Spool".equals(currentCargo)) return 6;
            if ("Sulfur_Create".equals(currentCargo)) return 7;
            if ("Pcb_Create".equals(currentCargo)) return 8;
            if ("HardenedSteel_Beam".equals(currentCargo)) return 9;
            if ("Cable_Spool".equals(currentCargo)) return 10;
            if ("CopperSulfate_IBC".equals(currentCargo)) return 11;
            return 4;
        }

        return 0;
    }
}
