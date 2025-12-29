import java.util.ArrayList;
import java.util.List;
//deneme helooooo dfdfdfdfd NEW NEW NEW
public class Main {
//hello
    // ---- EXPERIMENT SETTINGS ----
    static final int RUNS = 1000;
    static final int MAX_TURNS = 500;

    static final double WALL_DENSITY = 0.15;

    static final int MINIMAX_DEPTH = 3;
    static final int PINK_LOOKAHEAD = 2;

    static final boolean SHOW_UI_AFTER = false; // true yaparsan testten sonra UI açar
    // --------------------------------

    public static void main(String[] args) {

        int chaserWins = 0;
        int escaperSurvives = 0;

        long sumTurns = 0;

        long sumAStarTimeNs = 0;
        long sumMinimaxTimeNs = 0;

        long sumAStarExpanded = 0;
        long sumMinimaxEvaluated = 0;

        long sumDistanceDelta = 0; // d_before - d_after (positive => chasers closer)
        long sumDistance = 0;

        long maxAStarTimeNs = 0;
        long maxMinimaxTimeNs = 0;

        int aStarNullCount = 0;

        for (int run = 0; run < RUNS; run++) {
            long seed = 1000L + run;

            Grid grid = new Grid(seed, WALL_DENSITY);

            Chaser ch1 = new Chaser(0, 0);
            Chaser ch2 = new Chaser(Grid.ROWS / 2, Grid.COLS / 2);
            Escaper es = new Escaper(Grid.ROWS - 1, Grid.COLS - 1);

            int turns = 0;
            boolean caught = false;

            for (; turns < MAX_TURNS; turns++) {

                int dBefore = minDistToEscaper(ch1, ch2, es);
                sumDistance += dBefore;

                // --- CHASER 1 A* ---
                long t0 = System.nanoTime();
                List<Node> path1 = AStar.search(new Node(ch1.r, ch1.c), new Node(es.r, es.c), grid);
                long t1 = System.nanoTime();

                long dtA1 = (t1 - t0);
                sumAStarTimeNs += dtA1;
                maxAStarTimeNs = Math.max(maxAStarTimeNs, dtA1);
                sumAStarExpanded += AStar.getLastExpandedNodes();

                Node next1 = (path1 != null && path1.size() > 1) ? path1.get(1) : null;
                if (path1 == null) aStarNullCount++;

                if (next1 != null && !(next1.r == ch2.r && next1.c == ch2.c)) {
                    ch1.move(next1.r, next1.c);
                }

                // --- CHASER 2 A* (predicted target) ---
                Node predicted = AStar.predictEscaperTarget(es, grid, PINK_LOOKAHEAD);

                long t2 = System.nanoTime();
                List<Node> path2 = AStar.search(new Node(ch2.r, ch2.c), predicted, grid);
                long t3 = System.nanoTime();

                long dtA2 = (t3 - t2);
                sumAStarTimeNs += dtA2;
                maxAStarTimeNs = Math.max(maxAStarTimeNs, dtA2);
                sumAStarExpanded += AStar.getLastExpandedNodes();

                Node next2 = (path2 != null && path2.size() > 1) ? path2.get(1) : null;
                if (path2 == null) aStarNullCount++;

                if (next2 != null && !(next2.r == ch1.r && next2.c == ch1.c)) {
                    ch2.move(next2.r, next2.c);
                }

                // capture check
                if (sameCell(ch1, es) || sameCell(ch2, es)) {
                    caught = true;
                    break;
                }

                // --- ESCAPER Minimax ---
                List<Chaser> clist = new ArrayList<>();
                clist.add(ch1);
                clist.add(ch2);

                long t4 = System.nanoTime();
                int[] mv = Minimax.bestMove(clist, es, grid, MINIMAX_DEPTH);
                long t5 = System.nanoTime();

                long dtM = (t5 - t4);
                sumMinimaxTimeNs += dtM;
                maxMinimaxTimeNs = Math.max(maxMinimaxTimeNs, dtM);
                sumMinimaxEvaluated += Minimax.getLastEvaluatedStates();

                es.move(mv[0], mv[1]);

                // capture check again
                if (sameCell(ch1, es) || sameCell(ch2, es)) {
                    caught = true;
                    break;
                }

                int dAfter = minDistToEscaper(ch1, ch2, es);
                sumDistanceDelta += (dBefore - dAfter);
            }

            sumTurns += (turns + 1);

            if (caught) chaserWins++;
            else escaperSurvives++;
        }

        // ---- REPORT ----
        System.out.println("===== EXPERIMENT RESULTS =====");
        System.out.println("Runs: " + RUNS);
        System.out.println("Grid: " + Grid.ROWS + "x" + Grid.COLS + " | wallDensity=" + WALL_DENSITY);
        System.out.println("Max turns per run: " + MAX_TURNS);
        System.out.println("Minimax depth: " + MINIMAX_DEPTH + " | Pink lookahead: " + PINK_LOOKAHEAD);
        System.out.println();

        System.out.println("Chaser wins: " + chaserWins + " (" + pct(chaserWins, RUNS) + "%)");
        System.out.println("Escaper survives: " + escaperSurvives + " (" + pct(escaperSurvives, RUNS) + "%)");
        System.out.println("Avg survival/capture time (turns): " + (sumTurns / (double) RUNS));
        System.out.println();

        System.out.println("A* null path count (total calls): " + aStarNullCount);
        System.out.println();

        long totalAStarCalls = RUNS * (long) MAX_TURNS * 2; // upper bound; some runs end early
        // better: approximate average per-turn calls used (still fine for report)
        System.out.println("Avg A* decision time (ms): " + (sumAStarTimeNs / 1e6));
        System.out.println("Max A* decision time (ms): " + (maxAStarTimeNs / 1e6));
        System.out.println("Total A* expanded nodes: " + sumAStarExpanded);
        System.out.println();

        System.out.println("Avg Minimax decision time (ms): " + (sumMinimaxTimeNs / 1e6));
        System.out.println("Max Minimax decision time (ms): " + (maxMinimaxTimeNs / 1e6));
        System.out.println("Total Minimax evaluated states: " + sumMinimaxEvaluated);
        System.out.println();

        System.out.println("Avg min-distance to escaper (Manhattan): " + (sumDistance / (double) sumTurns));
        System.out.println("Avg Δdistance per turn (d_before - d_after): " + (sumDistanceDelta / (double) sumTurns));
        System.out.println("================================");

        if (SHOW_UI_AFTER) {
            new GamePanel(9999L, WALL_DENSITY);
        }
    }

    private static boolean sameCell(Agent a, Agent b) {
        return a.r == b.r && a.c == b.c;
    }

    private static int minDistToEscaper(Chaser c1, Chaser c2, Escaper e) {
        int d1 = Math.abs(c1.r - e.r) + Math.abs(c1.c - e.c);
        int d2 = Math.abs(c2.r - e.r) + Math.abs(c2.c - e.c);
        return Math.min(d1, d2);
    }

    private static String pct(int x, int total) {
        double p = (total == 0) ? 0 : (100.0 * x / total);
        return String.format("%.1f", p);
    }
}
