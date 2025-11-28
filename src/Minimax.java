import java.util.List;

public class Minimax {

    public static int[] bestMove(Chaser ch, Escaper es, Grid grid, int depth) {
        int bestScore = Integer.MIN_VALUE;
        int[] best = {es.r, es.c};

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        for (int[] d : dirs) {
            int nr = es.r + d[0];
            int nc = es.c + d[1];

            if (!grid.isValid(nr, nc)) continue;

            int score = minimax(ch.r, ch.c, nr, nc, grid, depth - 1, false);

            if (score > bestScore) {
                bestScore = score;
                best = new int[]{nr, nc};
            }
        }
        return best;
    }

    private static int minimax(int cr, int cc, int er, int ec, Grid grid, int depth, boolean maximizing) {

        if (cr == er && cc == ec) return -1000;
        if (depth == 0) return Math.abs(cr - er) + Math.abs(cc - ec);

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        if (maximizing) { // ESCAPER
            int maxEval = Integer.MIN_VALUE;

            for (int[] d : dirs) {
                int nr = er + d[0];
                int nc = ec + d[1];

                if (!grid.isValid(nr, nc)) continue;

                int eval = minimax(cr, cc, nr, nc, grid, depth - 1, false);
                maxEval = Math.max(maxEval, eval);
            }

            return maxEval;
        } else { // CHASER
            int minEval = Integer.MAX_VALUE;

            for (int[] d : dirs) {
                int nr = cr + d[0];
                int nc = cc + d[1];

                if (!grid.isValid(nr, nc)) continue;

                int eval = minimax(nr, nc, er, ec, grid, depth - 1, true);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }
}
