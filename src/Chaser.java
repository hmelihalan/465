public class Chaser {

    public int r, c;

    // 0 = RIGHT, 1 = DOWN, 2 = LEFT, 3 = UP
    public int dir = 0;

    public Chaser(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public void move(int nr, int nc) {

        if (nr < r) dir = 3;        // UP
        else if (nr > r) dir = 1;  // DOWN
        else if (nc > c) dir = 0;  // RIGHT
        else if (nc < c) dir = 2;  // LEFT

        r = nr;
        c = nc;
    }
}
