package Map;

import static Map.Helpers.Artist.DrawQuadTex;

public class TileGrid {

    public Tile[][] map;

    public TileGrid(){
        map = new Tile[40][30];
        for(int i = 0;i < map.length;i++){
            for (int j = 0; j < map[i].length;j++){
                map[i][j] = new Tile(i * 32, j * 32, 32, 32, TileType.Grass);
            }
        }
    }
    public TileGrid(int[][] newMap){
        map = new Tile[40][30];
        for(int i = 0;i < map.length;i++){
            for (int j = 0; j < map[i].length;j++){

                switch (newMap[j][i]){
                    case 0:
                        map[i][j] = new Tile(i * 32, j * 32, 32, 32, TileType.Grass);
                        break;
                    case 1:
                        map[i][j] = new Tile(i * 32, j * 32, 32, 32, TileType.Dirt);
                        break;
                    case 2:
                        map[i][j] = new Tile(i * 32, j * 32, 32, 32, TileType.Water);
                        break;
                }

            }
        }
    }

    public void SetTile(int xCoord, int yCoord, TileType type){
        map[xCoord][yCoord] = new Tile(xCoord * 32, yCoord * 32,32, 32, type);
    }

    public Tile GetTile(int xCoord, int yCoord){
        return map[xCoord][yCoord];
    }

    public void Draw(){
        for (Tile[] tiles : map) {
            for (Tile t : tiles) {
                t.Draw();
                //DrawQuadTex(t.getTexture(),t.getX(),t.getY(),t.getWidth(),t.getHeight());
            }
        }
    }
}
