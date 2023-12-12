package GamePlay;

public class Entity {
    public int x, y,x1, y1,x2, y2, idx,idx2, speed = 7, score = 10;

    Entity() {
        x = 450;
        y = 50;
        x1 = 650;
        y1 = 50;
        x2 = 250;
        y2 = 50;
        idx = 0;
        idx2 = 19;
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
