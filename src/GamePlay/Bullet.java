package GamePlay;

public class Bullet {
    int x, y;
    boolean isFired;
    int initX, initY;

    public Bullet(int x, int y) {
        this.x = initX = x;
        this.y = initY = y;
        this.isFired = true;
    }
}
