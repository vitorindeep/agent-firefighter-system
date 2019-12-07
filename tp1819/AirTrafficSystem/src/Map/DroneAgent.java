package Map;

import org.newdawn.slick.opengl.Texture;

import static Map.Helpers.Artist.DrawQuadTex;
import static Map.Helpers.Artist.QuickLoad;
import static Map.Helpers.Clock.*;

public class DroneAgent {

    private int width, height;
    private float speed, x, y, water, fuel, startWater, startFuel;
    private Texture texture, waterBackground, fuelBackground, waterForeground, fuelForeground, waterBorder, fuelBorder;
    private Tile startTile;
    private boolean first = true;

    public DroneAgent(Texture texture, Tile startTile, int width, int height, float speed, float water, float fuel){
        this.texture = texture;
        this.waterBackground = QuickLoad("barBackground");
        this.fuelBackground = QuickLoad("barBackground");
        this.waterForeground = QuickLoad("barForeground2");
        this.fuelForeground = QuickLoad("barForeground");
        this.waterBorder = QuickLoad("barBorder");
        this.fuelBorder = QuickLoad("barBorder");

        this.x = startTile.getX();
        this.y = startTile.getY();
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.water = water;
        this.startWater = water;
        this.fuel = fuel;
        this.startFuel = fuel;

    }

    public void Update(float x, float y, int water, int fuel){
        if(first)
            first = false;
        else
            this.x = x * 32;
            this.y = y * 32;
        if (water>0)
            this.water = water;
        if (fuel>0)
            this.fuel = fuel;
    }

    public void Update(){
        if(first)
            first = false;
        else
            x += Delta() * speed;
        if (water>0)
                water--;
        if (fuel>0)
                fuel--;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public void Draw(){
        float waterPercentage = water / startWater;
        float fuelPercentage = fuel / startFuel;
        DrawQuadTex(texture,x,y,width,height);
        DrawQuadTex(waterBackground,x,y - 7,width,height/5);
        DrawQuadTex(fuelBackground,x,y - 16,width,height/5);
        DrawQuadTex(waterForeground,x,y - 7,width * waterPercentage,height/5);
        DrawQuadTex(fuelForeground,x,y - 16, width * fuelPercentage,height/5);
        DrawQuadTex(waterBorder,x,y - 7,width,height/5);
        DrawQuadTex(fuelBorder,x,y - 16,width,height/5);

    }
}
