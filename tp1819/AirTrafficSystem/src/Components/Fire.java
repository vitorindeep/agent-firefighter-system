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

    @Override
    public String toString() {
        return "id: " + id + ", " +
                "x: " + coordX + ", " +
                "y: " + coordY + ", " +
                "intensidade: " + intensity + ", " +
                "ativo: " + active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCoordX() {
        return coordX;
    }

    public void setCoordX(int coordX) {
        this.coordX = coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public void setCoordY(int coordY) {
        this.coordY = coordY;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
