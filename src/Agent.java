public class Agent {
    public int r, c;

    public Agent(int r, int c) {
        this.r = r; this.c = c;
    }

    public void move(int nr, int nc) {
        r = nr;
        c = nc;
    }
}
