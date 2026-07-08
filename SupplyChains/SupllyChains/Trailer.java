import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Trailer extends Actor
{
    private final Vehicle head;
    private final String vehicleType;
    private String currentCargo;
    private int cargoQuantity;

    private double hitch_distance = 13.5;
    private double posX;
    private double posY;
    private boolean firstFrame = true;

    public Trailer(Vehicle head, String vehicleType, String currentCargo, int cargoQuantity)
    {
        this.head = head;
        this.vehicleType = vehicleType;
        this.currentCargo = currentCargo;
        this.cargoQuantity = cargoQuantity;
        if (vehicleType.equals("DumpTruck"))
        {
            hitch_distance = 5.5;
        }
        updateTrailerSprite();
    }

public void trackHead()
    {
        if (head == null || head.getWorld() != null && head.getWorld() == null) // Safe check matching your structure
        {
            if (getWorld() != null)
                getWorld().removeObject(this);
            return;
        }

        Level level = (Level)head.getWorld();
        double headAngleRad = Math.toRadians(head.getVisualRotation() - 90);

        // 1. Determine where the cab hooks up/pivots
        double hitchX = head.getPosX() - Math.cos(headAngleRad) * hitch_distance;
        double hitchY = head.getPosY() - Math.sin(headAngleRad) * hitch_distance;

        if (firstFrame)
        {
            posX = hitchX;
            posY = hitchY;
            firstFrame = false;
        }

        // 2. Calculate distance vector to hitch point
        double dx = hitchX - posX;
        double dy = hitchY - posY;
        double distance = Math.hypot(dx, dy);

        if (distance < 0.001) distance = 0.001;

        // 3. Delegate to specific physics engine methods based on trailer type
        if (vehicleType.equals("DumpTruck"))
        {
            calculateArticulatedPhysics(hitchX, hitchY, dx, dy, distance);
        }
        else
        {
            calculateDrawbarPhysics(hitchX, hitchY, dx, dy, distance);
        }

        // 4. Update Greenfoot screen positioning relative to camera
        setLocation(
            (int)Math.round(posX - level.getCameraX()),
            (int)Math.round(posY - level.getCameraY())
        );
    }

    private void calculateArticulatedPhysics(double hitchX, double hitchY, double dx, double dy, double distance)
    {
        double trailerLength = 14.0;

        double targetAngle = Math.toDegrees(Math.atan2(hitchY - posY, hitchX - posX)) + 90;

        targetAngle = (targetAngle + 360) % 360;

        double articulation = targetAngle - head.getVisualRotation();

        while (articulation > 180) articulation -= 360;
        while (articulation < -180) articulation += 360;

        articulation = Math.max(-35, Math.min(35, articulation));

        double finalAngle = head.getVisualRotation() + articulation;

        setRotation((int)Math.round(finalAngle));

        double rad = Math.toRadians(finalAngle - 90);

        posX = hitchX - Math.cos(rad) * trailerLength;
        posY = hitchY - Math.sin(rad) * trailerLength;
    }

    private void calculateDrawbarPhysics(double hitchX, double hitchY, double dx, double dy, double distance)
    {
        double trailerLength = (vehicleType.equals("Bulk")) ? 3.0 : 12.0;

        posX = hitchX - (dx / distance) * trailerLength;
        posY = hitchY - (dy / distance) * trailerLength;

        int targetAngle = (int)Math.toDegrees(Math.atan2(dy, dx)) + 90;
        
        // Uses progressive dampening rotation logic for traditional wide-turn trailing
        updateRotation((targetAngle + 360) % 360);
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
        if (cargoQuantity > 2)
        {
            if (vehicleType.equals("DumpTruck"))
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
        }
        if (vehicleType.equals("DumpTruck")) return 0;
        if (vehicleType.equals("Flatbed")) return 4;
        if (vehicleType.equals("Bulk")) return 12;
        else return 4;
    }
    private void updateRotation(int targetAngle)
    {
        int current = getRotation();
        int diff = targetAngle - current;

        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;

        int turnSpeed = 5;

        if (Math.abs(diff) < turnSpeed)
        {
            setRotation(targetAngle);
        }
        else
        {
            setRotation(current + (diff > 0 ? turnSpeed : -turnSpeed));
        }
    }
}
