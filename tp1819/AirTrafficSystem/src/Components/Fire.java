package Components;

public class Fire {

    private int id;
    private int coordX, coordY;
    private int intensity;
    private boolean active;

    public Fire(int id, int coordX, int coordY) {
        this.id = id;
        this.coordX = coordX;
        this.coordY = coordY;
        this.intensity = 1;
        this.active = true;
    }

    public void increaseFireIntensity() {
        this.intensity++;
    }

    public void decreaseFireIntensity() {
        this.intensity--;
        if (this.intensity == 0) {
            this.active = false;
        }
    }

    public boolean isFireExtinguished() {
        return !active;
    }
}
