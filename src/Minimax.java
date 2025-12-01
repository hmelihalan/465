import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Minimax {

    private static Random rand = new Random();

    // chasers listesi eklendi
    public static int[] bestMove(List<Chaser> chasers, Escaper es, Grid grid, int depth) {
        int bestScore = Integer.MIN_VALUE;

        List<int[]> bestMoves = new ArrayList<>();

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        for (int[] d : dirs) {
            int nr = es.r + d[0];
            int nc = es.c + d[1];

            if (!grid.isValid(nr, nc)) continue;

            int score = minimax(chasers, nr, nc, grid, depth - 1, false);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(new int[]{nr, nc});
            } else if (score == bestScore) {
                bestMoves.add(new int[]{nr, nc});
            }
        }

        return bestMoves.get(rand.nextInt(bestMoves.size()));
    }

    private static int minimax(List<Chaser> chasers, int er, int ec, Grid grid, int depth, boolean maximizing) {

        // Eğer escaper herhangi bir chaser ile aynı karedeyse büyük ceza
        for (Chaser ch : chasers) {
            if (ch.r == er && ch.c == ec) return -1000;
        }

        if (depth == 0) {
            // En yakın chaser ile mesafe
            int minDist = Integer.MAX_VALUE;
            for (Chaser ch : chasers) {
                int dist = Math.abs(ch.r - er) + Math.abs(ch.c - ec);
                if (dist < minDist) minDist = dist;
            }
            return minDist;
        }

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        if (maximizing) { // ESCAPER
            int maxEval = Integer.MIN_VALUE;
            for (int[] d : dirs) {
                int nr = er + d[0];
                int nc = ec + d[1];

                if (!grid.isValid(nr, nc)) continue;

                int eval = minimax(chasers, nr, nc, grid, depth - 1, false);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else { // CHASERS simülasyonu
            int minEval = Integer.MAX_VALUE;
            for (Chaser ch : chasers) {
                for (int[] d : dirs) {
                    int nr = ch.r + d[0];
                    int nc = ch.c + d[1];

                    if (!grid.isValid(nr, nc)) continue;

                    // Diğer chaser'ları olduğu gibi bırakıyoruz
                    List<Chaser> newChasers = new ArrayList<>();
                    for (Chaser c : chasers) {
                        if (c == ch) {
                            newChasers.add(new Chaser(nr, nc)); // bu chaser'ı hareket ettir
                        } else {
                            newChasers.add(new Chaser(c.r, c.c)); // diğerleri aynı
                        }
                    }

                    int eval = minimax(newChasers, er, ec, grid, depth - 1, true);
                    minEval = Math.min(minEval, eval);
                }
            }
            return minEval;
        }
    }
}
