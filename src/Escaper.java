import java.util.*;

public class Escaper extends Agent {

    private NeuralNetwork nn = new NeuralNetwork();
    private Random rnd = new Random();

    public Escaper(int r, int c) {
        super(r, c);
    }

    public void decideMove(Chaser c1, Chaser c2, Grid grid) {

        double bestScore = -Double.MAX_VALUE;
        int bestAction = -1;

        for (int action = 0; action < 4; action++) {

            int nr = r;
            int nc = c;

            switch (action) {
                case 0 -> nr--;
                case 1 -> nr++;
                case 2 -> nc--;
                case 3 -> nc++;
            }

            if (!grid.isValid(nr, nc))
                continue;

            double[] input = buildInput(nr, nc, c1, c2, grid);
            double score = nn.score(input);

            // 🔹 tiny tie-break noise
            score += rnd.nextDouble() * 0.0005;

            if (score > bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }

        if (bestAction != -1)
            moveByAction(bestAction);
    }

    // ------------------------------------------------

    private double[] buildInput(int nr, int nc,
                                Chaser c1, Chaser c2,
                                Grid grid) {

        double d1 = manhattan(nr, nc, c1);
        double d2 = manhattan(nr, nc, c2);

        double nearest = Math.min(d1, d2);
        double other   = Math.max(d1, d2);

        double wallPenalty = wallRisk(nr, nc, grid);
        double deadEnd = deadEndRisk(nr, nc, grid);

        return new double[] {
                nearest,
                other,
                wallPenalty,
                deadEnd
        };
    }

    // 🔧 KRİTİK DEĞİŞİKLİK
    private double manhattan(int r, int c, Chaser ch) {
        return (Math.abs(ch.r - r) + Math.abs(ch.c - c)) / 30.0;
    }

    private double wallRisk(int r, int c, Grid g) {
        int walls = 0;
        if (!g.isValid(r - 1, c)) walls++;
        if (!g.isValid(r + 1, c)) walls++;
        if (!g.isValid(r, c - 1)) walls++;
        if (!g.isValid(r, c + 1)) walls++;
        return walls / 4.0;
    }

    // 🔧 YUMUŞATILDI
    private double deadEndRisk(int r, int c, Grid g) {
        int free = 0;
        if (g.isValid(r - 1, c)) free++;
        if (g.isValid(r + 1, c)) free++;
        if (g.isValid(r, c - 1)) free++;
        if (g.isValid(r, c + 1)) free++;
        return (4 - free) / 4.0;
    }

    private void moveByAction(int a) {
        switch (a) {
            case 0 -> move(r - 1, c);
            case 1 -> move(r + 1, c);
            case 2 -> move(r, c - 1);
            case 3 -> move(r, c + 1);
        }
    }
}
