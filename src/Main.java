import java.util.ArrayList;
import java.util.List;

public class Main {

    // ---- DENEY AYARLARI ----
    static final int RUNS = 100;           // Toplam kaç oyun oynanacak
    static final int MAX_TURNS = 70;       // Her oyun max kaç tur sürer
    static final double WALL_DENSITY = 0.15;

    // ---- MCTS AYARLARI ----
    static final int MCTS_ITERS = 600;     // MCTS iterasyon sayısı
    static final int MCTS_ROLLOUT = 12;    // Simülasyon derinliği

    public static void main(String[] args) {

        int chaserWins = 0;
        int escaperSurvives = 0;
        long sumTurns = 0;

        // Zaman ve Performans İstatistikleri
        long sumMctsTimeNs = 0;
        long maxMctsTimeNs = 0;
        long totalPlayouts = 0;

        System.out.println("Deney baslatiliyor: " + RUNS + " oyun...");

        for (int run = 0; run < RUNS; run++) {
            // Her seferinde farklı ama takip edilebilir bir seed
            long seed = 1000L + run;
            Grid grid = new Grid(seed, WALL_DENSITY);

            Chaser ch1 = new Chaser(0, 0);
            Chaser ch2 = new Chaser(Grid.ROWS / 2, Grid.COLS / 2);
            Escaper es = new Escaper(Grid.ROWS - 1, Grid.COLS - 1);

            int turns = 0;
            boolean caught = false;

            for (; turns < MAX_TURNS; turns++) {

                // --- CHASER 1 (A*) ---
                List<Node> path1 = AStar.search(new Node(ch1.r, ch1.c), new Node(es.r, es.c), grid);
                if (path1 != null && path1.size() > 1) {
                    Node n = path1.get(1);
                    if (!(n.r == ch2.r && n.c == ch2.c)) {
                        ch1.move(n.r, n.c);
                    }
                }

                // --- CHASER 2 (Tahminli A*) ---
                Node predicted = AStar.predictEscaperTarget(es, grid, 2);
                List<Node> path2 = AStar.search(new Node(ch2.r, ch2.c), predicted, grid);
                if (path2 != null && path2.size() > 1) {
                    Node n2 = path2.get(1);
                    if (!(n2.r == ch1.r && n2.c == ch1.c)) {
                        ch2.move(n2.r, n2.c);
                    }
                }

                // Yakalama Kontrolü 1
                if (sameCell(ch1, es) || sameCell(ch2, es)) {
                    caught = true;
                    break;
                }

                // --- ESCAPER (MCTS) ---
                List<Chaser> clist = new ArrayList<>();
                clist.add(ch1);
                clist.add(ch2);

                long tStart = System.nanoTime();
                int[] mv = MCTS.bestMove(clist, es, grid, MCTS_ITERS, MCTS_ROLLOUT);
                long tEnd = System.nanoTime();

                long dt = (tEnd - tStart);
                sumMctsTimeNs += dt;
                maxMctsTimeNs = Math.max(maxMctsTimeNs, dt);

                es.move(mv[0], mv[1]);

                // Yakalama Kontrolü 2
                if (sameCell(ch1, es) || sameCell(ch2, es)) {
                    caught = true;
                    break;
                }
            }

            sumTurns += (turns + 1);
            if (caught) chaserWins++;
            else escaperSurvives++;

            if ((run + 1) % 10 == 0) {
                System.out.println("Tamamlanan oyun: " + (run + 1));
            }
        }

        // ---- RAPORLAMA ----
        System.out.println("\n===== MCTS DENEY SONUCLARI =====");
        System.out.println("Toplam Oyun: " + RUNS);
        System.out.println("Max Tur Limiti: " + MAX_TURNS);
        System.out.println("MCTS Iters: " + MCTS_ITERS + " | Rollout: " + MCTS_ROLLOUT);
        System.out.println("--------------------------------");
        System.out.println("Chaser Galibiyeti: " + chaserWins + " (%" + pct(chaserWins, RUNS) + ")");
        System.out.println("Escaper Kurtulusu: " + escaperSurvives + " (%" + pct(escaperSurvives, RUNS) + ")");
        System.out.println("Ortalama Hayatta Kalma Süresi: " + (sumTurns / (double) RUNS) + " tur");
        System.out.println("--------------------------------");
        System.out.println("Ortalama MCTS Karar Süresi: " + String.format("%.2f", (sumMctsTimeNs / 1e6 / sumTurns)) + " ms");
        System.out.println("En Yavaş MCTS Kararı: " + (maxMctsTimeNs / 1e6) + " ms");
        System.out.println("================================");
    }

    private static boolean sameCell(Agent a, Agent b) {
        return a.r == b.r && a.c == b.c;
    }

    private static String pct(int x, int total) {
        return String.format("%.1f", (100.0 * x / total));
    }
}