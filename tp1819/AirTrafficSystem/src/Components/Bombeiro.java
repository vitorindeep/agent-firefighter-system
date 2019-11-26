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
}
