package Map;

import org.newdawn.slick.opengl.Texture;

import static Map.Helpers.Artist.DrawQuadTex;
public class Fire {

    private int width, height;
    private float speed, x, y, water, fuel, startWater, startFuel;
    private Texture texture;
    private Tile startTile;
    private boolean first = true;

    public Fire(Texture texture, Tile startTile, int width, int height){
        this.texture = texture;
        this.x = startTile.getX();
        this.y = startTile.getY();
        this.width = width;
        this.height = height;
    }



    public void Update(Texture texture){
        if(first)
            first = false;
        else
          this.texture = texture;
    }

    public void Draw(){
        DrawQuadTex(texture,x,y,width,height);
    }
}
