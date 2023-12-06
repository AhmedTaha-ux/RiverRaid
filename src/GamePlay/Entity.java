package GamePlay;

public class Entity {
    public int x, y, idx, speed = 7, score = 1;

    Entity() {
        x = 450;
        y = 50;
        idx = 0;
    }

    Entity(int x) {
        this.x = x;
        y = 600;
        idx = 4;
    }

    Entity(int x, int y, int idx) {
        this.x = x;
        this.y = y;
        this.idx = idx;
    }
}
