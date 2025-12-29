import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Escaper extends Agent {

    private NeuralNetwork nn = new NeuralNetwork();
    private Random rnd = new Random();

    public Escaper(int r, int c) {
        super(r, c);
    }

    public void decideMove(Chaser c1, Chaser c2, Grid grid) {

        double[] input = buildInput(c1, c2);
        int action = nn.predict(input);

        // 1️⃣ NN'nin istediği hareket
        if (tryMove(action, grid)) {
            return;
        }

        // 2️⃣ NN başarısızsa → RANDOM GEÇERLİ HAREKET
        List<Integer> validMoves = new ArrayList<>();

        for (int a = 0; a < 4; a++) {
            if (canMove(a, grid)) {
                validMoves.add(a);
            }
        }

        if (!validMoves.isEmpty()) {
            int fallback = validMoves.get(rnd.nextInt(validMoves.size()));
            tryMove(fallback, grid);
        }
    }

    // -----------------------------

    private boolean tryMove(int action, Grid grid) {
        int nr = r;
        int nc = c;

        switch (action) {
            case 0 -> nr--; // UP
            case 1 -> nr++; // DOWN
            case 2 -> nc--; // LEFT
            case 3 -> nc++; // RIGHT
        }

        if (grid.isValid(nr, nc)) {
            move(nr, nc);
            return true;
        }
        return false;
    }

    private boolean canMove(int action, Grid grid) {
        int nr = r;
        int nc = c;

        switch (action) {
            case 0 -> nr--;
            case 1 -> nr++;
            case 2 -> nc--;
            case 3 -> nc++;
        }

        return grid.isValid(nr, nc);
    }

    // -----------------------------

    private double[] buildInput(Chaser c1, Chaser c2) {
        double[] in = new double[8];

        in[0] = norm(c1.r - r);
        in[1] = norm(c1.c - c);

        in[2] = norm(c2.r - r);
        in[3] = norm(c2.c - c);

        in[4] = r / (double) Grid.ROWS;
        in[5] = c / (double) Grid.COLS;

        in[6] = distance(c1);
        in[7] = distance(c2);

        return in;
    }

    private double norm(int v) {
        return Math.max(-1, Math.min(1, v / 20.0));
    }

    private double distance(Chaser ch) {
        return Math.sqrt(
                (ch.r - r) * (ch.r - r) +
                        (ch.c - c) * (ch.c - c)
        ) / 30.0;
    }
}
