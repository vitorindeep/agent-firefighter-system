package Components;

public class Bombeiro {

    private String id;
    private int x, y;
    private boolean active, replenishment;

    public Bombeiro(String id, int coordX, int coordY, boolean active, boolean replenishment) {
        this.id = id;
        this.x = coordX;
        this.y = coordY;
        this.active = active;
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
        return (!active && !replenishment);
    }
}
