import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Grid {
    public static final int ROWS = 15;
    public static final int COLS = 15;

    public final int[][] map = new int[ROWS][COLS]; // 0 = empty, 1 = wall
    private final Random rng;

    public Grid(long seed, double wallDensity) {
        this.rng = new Random(seed);
        generateWalls(wallDensity);

        // Ensure start points are not walls
        map[0][0] = 0;
        map[ROWS / 2][COLS / 2] = 0;
        map[ROWS - 1][COLS - 1] = 0;
    }

    private void generateWalls(double wallDensity) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                map[r][c] = (rng.nextDouble() < wallDensity) ? 1 : 0;
            }
        }
    }

    public boolean isValid(int r, int c) {
        return r >= 0 && c >= 0 && r < ROWS && c < COLS && map[r][c] == 0;
    }

    public List<Node> getNeighbors(Node n) {
        List<Node> result = new ArrayList<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nr = n.r + d[0];
            int nc = n.c + d[1];
            if (isValid(nr, nc)) result.add(new Node(nr, nc));
        }
        return result;
    }
}
