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
        "grass",    // 0
        "road v",   // 1
        "road h",   // 2
        "cross",    // 3
        "t-junct",  // 4
        "t-junct",  // 5
        "t-junct",  // 6
        "t-junct",  // 7
        "corner",   // 8
        "corner",   // 9
        "corner",   // 10
        "corner",   // 11
        "depot",    // 12
        "depot",    // 13
        "depot",    // 14
        "depot",    // 15
        "res blue", // 16
        "res red",  // 17
        "res yell", // 18
        "-",        // 19
        "fac blue", // 20
        "fac red",  // 21
        "fac yell", // 22
        "-"         // 23
    };
    /**
     * Act - do whatever the ui wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act()
    {
        selectedTile();
    }
    public void selectedTile(){
        GreenfootImage img = new GreenfootImage(96, 120);
        img.setColor(new Color(0, 0, 0, 150));
        img.fillRect(0, 0, 96, 120);
        
        // scale the tile to 64x64
        GreenfootImage tile = new GreenfootImage(Level.tiles[Level.selected_tile]);
        tile.scale(64, 64);
        img.drawImage(tile, 16, 8);
        
        // bigger font
        img.setFont(new Font("Arial", false, false, 16));
        img.setColor(Color.WHITE);
        int textWidth = tileNames[Level.selected_tile].length() * 9;
        int textX = (96 - textWidth) / 2;
        img.drawString(tileNames[Level.selected_tile], textX, 108);
        setImage(img);
    }
}
