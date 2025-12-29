import java.util.*;

public class MCTS {

    private static final Random RNG = new Random();

    // --- AYARLAR ---
    private static final double C_PARAM = 1.41;

    // --- İSTATİSTİK ---
    private static long lastPlayouts = 0;

    public static int[] bestMove(List<Chaser> chasers, Escaper es, Grid grid, int iterations, int rolloutDepth) {
        lastPlayouts = 0;

        // Kök durum
        SimState rootState = SimState.from(chasers, es);

        // Zaten yakalandıysa olduğu yerde kalsın
        if (rootState.isCaught()) return new int[]{es.r, es.c};

        MctsNode root = new MctsNode(null, -1, rootState);

        // Kök düğümü genişletirken "güvenli filtreyi" kullanır
        root.ensureUntriedActions(grid);

        // Eğer kök düğümde hiç güvenli hamle yoksa (köşeye sıkıştıysa),
        // MCTS hata vermesin diye mecburen riskli hamleleri eklemesini sağlarız.
        if (root.untried.isEmpty() && root.children.isEmpty()) {
            root.forceAddRiskyActions(grid);
        }

        for (int i = 0; i < iterations; i++) {
            MctsNode node = root;
            SimState state = rootState.copy();

            // 1) SELECTION
            while (!node.isTerminal(grid) && node.isFullyExpanded() && !node.children.isEmpty()) {
                node = node.selectChildUCT();
                state.applyEscaperAction(node.action, grid);
                state.applyChasersStep(grid);
                if (state.isCaught()) break;
            }

            // 2) EXPANSION
            if (!state.isCaught() && !node.isTerminal(grid)) {
                int a = node.popOneUntried();
                if (a != -1) {
                    SimState next = state.copy();
                    next.applyEscaperAction(a, grid);
                    next.applyChasersStep(grid);

                    MctsNode child = new MctsNode(node, a, next);
                    // Çocuk düğümlerde de filtre uygula
                    child.ensureUntriedActions(grid);
                    node.children.add(child);
                    node = child;
                    state = next;
                }
            }

            // 3) SIMULATION (Rollout)
            double reward = rollout(state, grid, rolloutDepth);
            lastPlayouts++;

            // 4) BACKPROPAGATION
            while (node != null) {
                node.visits++;
                node.totalReward += reward;
                node = node.parent;
            }
        }

        // En çok ziyaret edilen çocuğu seç
        MctsNode best = null;
        for (MctsNode ch : root.children) {
            if (best == null || ch.visits > best.visits) {
                best = ch;
            }
        }

        if (best == null) {
            // Hiçbir yere gidemiyorsa olduğu yerde durur (ölümü bekler)
            return new int[]{es.r, es.c};
        }

        return actionToCell(es.r, es.c, best.action, grid);
    }

    // ----------------- ROLLOUT -----------------

    private static double rollout(SimState s0, Grid grid, int depth) {
        SimState s = s0.copy();

        if (s.isCaught()) return 0.0;

        for (int t = 0; t < depth; t++) {
            // Rollout sırasında da intihar etmesin, akıllı oynasın
            int action = heuristicRolloutMove(s, grid);

            s.applyEscaperAction(action, grid);
            s.applyChasersStep(grid);

            if (s.isCaught()) {
                // Ne kadar geç ölürse o kadar iyi
                return (double) t / (depth * 2.0);
            }
        }

        // Hayatta kalma ödülü
        int d1 = manhattan(s.c1r, s.c1c, s.er, s.ec);
        int d2 = manhattan(s.c2r, s.c2c, s.er, s.ec);
        int minDist = Math.min(d1, d2);

        // Açık alan bonusu
        int openSpaces = countOpenNeighbors(s.er, s.ec, grid);

        double distScore = Math.min(minDist, 8) / 8.0;
        double freedomScore = openSpaces / 4.0;

        return 0.6 + (0.3 * distScore) + (0.1 * freedomScore);
    }

    // Rollout için basit ama güvenli hamle seçici
    private static int heuristicRolloutMove(SimState s, Grid grid) {
        List<Integer> safeMoves = new ArrayList<>();
        List<Integer> riskyMoves = new ArrayList<>(); // Yedek plan

        for (int a = 0; a < 4; a++) {
            int[] cell = actionToCell(s.er, s.ec, a, grid);
            if (grid.isValid(cell[0], cell[1])) {
                // Hamle hedefi Chaser'a bitişik mi? (Mesafe <= 1 ise tehlikeli)
                int d1 = manhattan(cell[0], cell[1], s.c1r, s.c1c);
                int d2 = manhattan(cell[0], cell[1], s.c2r, s.c2c);

                if (d1 > 1 && d2 > 1) {
                    safeMoves.add(a);
                } else {
                    riskyMoves.add(a);
                }
            }
        }

        // Varsa güvenli hamlelerden birini seç
        if (!safeMoves.isEmpty()) {
            // Güvenliler arasında da en uzağa gideni seçmeye çalış (basitçe)
            return safeMoves.get(RNG.nextInt(safeMoves.size()));
        }

        // Güvenli yoksa mecburen riskli oyna
        if (!riskyMoves.isEmpty()) {
            return riskyMoves.get(RNG.nextInt(riskyMoves.size()));
        }

        return 4; // Hareket edemiyor
    }

    // ----------------- NODE & STATE -----------------

    private static class MctsNode {
        final MctsNode parent;
        final int action;
        final SimState state;
        long visits = 0;
        double totalReward = 0;
        final List<MctsNode> children = new ArrayList<>();
        final ArrayDeque<Integer> untried = new ArrayDeque<>();

        MctsNode(MctsNode parent, int action, SimState state) {
            this.parent = parent;
            this.action = action;
            this.state = state;
        }

        // --- KRİTİK DEĞİŞİKLİK: Sadece güvenli hamleleri ekle ---
        void ensureUntriedActions(Grid grid) {
            if (!untried.isEmpty() || !children.isEmpty()) return;

            List<Integer> safeActions = new ArrayList<>();
            List<Integer> riskyActions = new ArrayList<>();

            for (int a = 0; a < 4; a++) { // 4 yön
                int[] cell = actionToCell(state.er, state.ec, a, grid);

                if (grid.isValid(cell[0], cell[1])) {
                    // Chaser kontrolü: Gideceğim yer, şu anki Chaser konumuna 1 birim veya daha yakın mı?
                    // Eğer öyleyse, Chaser hamle yapınca beni yer.
                    int d1 = manhattan(cell[0], cell[1], state.c1r, state.c1c);
                    int d2 = manhattan(cell[0], cell[1], state.c2r, state.c2c);

                    if (d1 > 1 && d2 > 1) {
                        safeActions.add(a);
                    } else {
                        riskyActions.add(a);
                    }
                }
            }

            // EĞER GÜVENLİ HAMLE VARSA, SADECE ONLARI EKLE.
            // Böylece ağaç, intihar hamlelerini düşünmekle vakit kaybetmez.
            if (!safeActions.isEmpty()) {
                Collections.shuffle(safeActions, RNG);
                untried.addAll(safeActions);
            } else {
                // Eğer hiç güvenli yer yoksa (sıkıştıysak), mecburen risklileri ekle
                Collections.shuffle(riskyActions, RNG);
                untried.addAll(riskyActions);
            }
        }

        // Kök düğümde hiç hamle kalmazsa çağırmak için
        void forceAddRiskyActions(Grid grid) {
            for (int a = 0; a < 4; a++) {
                int[] cell = actionToCell(state.er, state.ec, a, grid);
                if (grid.isValid(cell[0], cell[1])) {
                    if (!untried.contains(a)) untried.add(a);
                }
            }
        }

        boolean isTerminal(Grid grid) {
            return state.isCaught();
        }

        boolean isFullyExpanded() {
            return untried.isEmpty();
        }

        int popOneUntried() {
            return untried.isEmpty() ? -1 : untried.pollFirst();
        }

        MctsNode selectChildUCT() {
            MctsNode best = null;
            double bestVal = -Double.MAX_VALUE;
            for (MctsNode ch : children) {
                double q = ch.totalReward;
                double n = ch.visits;
                double uct = (n == 0) ? 1e6 : (q / n) + C_PARAM * Math.sqrt(Math.log(this.visits + 1) / n);
                if (uct > bestVal) {
                    bestVal = uct;
                    best = ch;
                }
            }
            return best;
        }
    }

    private static class SimState {
        int er, ec, c1r, c1c, c2r, c2c;

        static SimState from(List<Chaser> chasers, Escaper es) {
            SimState s = new SimState();
            s.er = es.r; s.ec = es.c;
            s.c1r = chasers.get(0).r; s.c1c = chasers.get(0).c;
            s.c2r = chasers.get(1).r; s.c2c = chasers.get(1).c;
            return s;
        }

        SimState copy() {
            SimState s = new SimState();
            s.er = er; s.ec = ec; s.c1r = c1r; s.c1c = c1c; s.c2r = c2r; s.c2c = c2c;
            return s;
        }

        boolean isCaught() {
            return (er == c1r && ec == c1c) || (er == c2r && ec == c2c);
        }

        void applyEscaperAction(int action, Grid grid) {
            int[] cell = actionToCell(er, ec, action, grid);
            if (grid.isValid(cell[0], cell[1])) {
                er = cell[0]; ec = cell[1];
            }
        }

        void applyChasersStep(Grid grid) {
            int[] n1 = greedyStep(c1r, c1c, er, ec, grid);
            int[] n2 = greedyStep(c2r, c2c, er, ec, grid);

            if (n1[0] == n2[0] && n1[1] == n2[1]) {
                c1r = n1[0]; c1c = n1[1];
            } else {
                c1r = n1[0]; c1c = n1[1];
                c2r = n2[0]; c2c = n2[1];
            }
        }
    }

    // ----------------- YARDIMCILAR -----------------

    private static int[] actionToCell(int r, int c, int action, Grid grid) {
        int nr = r, nc = c;
        switch (action) {
            case 0 -> nr--; // UP
            case 1 -> nr++; // DOWN
            case 2 -> nc--; // LEFT
            case 3 -> nc++; // RIGHT
        }
        return new int[]{nr, nc};
    }

    private static int[] greedyStep(int cr, int cc, int tr, int tc, Grid grid) {
        int bestD = Integer.MAX_VALUE;
        int[] bestMove = new int[]{cr, cc};
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};

        for (int[] d : dirs) {
            int nr = cr + d[0];
            int nc = cc + d[1];
            // Chaserlar kendi üzerlerine basamaz diye bir kural yok burada
            // Ama duvara basamazlar
            if (grid.isValid(nr, nc)) {
                int dist = manhattan(nr, nc, tr, tc);
                if (dist < bestD) {
                    bestD = dist;
                    bestMove = new int[]{nr, nc};
                }
            }
        }
        return bestMove;
    }

    private static int manhattan(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }

    private static int countOpenNeighbors(int r, int c, Grid grid) {
        int count = 0;
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            if (grid.isValid(r + d[0], c + d[1])) count++;
        }
        return count;
    }
}