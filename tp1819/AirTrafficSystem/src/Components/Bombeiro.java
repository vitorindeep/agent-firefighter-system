package Components;

public class Bombeiro {

    private String id;
    private int x, y;
    private boolean moving, fighting, replenishment;

    public Bombeiro(String id, int coordX, int coordY, boolean moving, boolean fighting, boolean replenishment) {
        this.id = id;
        this.x = coordX;
        this.y = coordY;
        this.moving = moving;
        this.fighting = fighting;
        this.replenishment = replenishment;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isAvailable() {
        // só disponível quando não está ativamente a combater fogo
        // e quando não está em reabastecimento
        return (!moving && !fighting && !replenishment);
    }
}
