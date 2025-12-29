public class Chaser extends Agent {

    // 0 = RIGHT, 1 = DOWN, 2 = LEFT, 3 = UP
    public int dir = 0;

    public Chaser(int r, int c) {
        super(r, c);
    }

    @Override
    public void move(int nr, int nc) {
        // update direction for drawing
        if (nr < r) dir = 3;        // UP
        else if (nr > r) dir = 1;   // DOWN
        else if (nc > c) dir = 0;   // RIGHT
        else if (nc < c) dir = 2;   // LEFT

        super.move(nr, nc);
    }
}
