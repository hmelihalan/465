import java.util.ArrayList;
import java.util.List;

public class Escaper extends Agent {

    // MCTS settings (tweakable)
    private static final int MCTS_ITERS = 800;     // 300-1500 arası deneyebilirsin
    private static final int ROLLOUT_DEPTH = 18;   // 8-20 arası deneyebilirsin

    public Escaper(int r, int c) {
        super(r, c);
    }

    /**
     * Used by your older UI loop (where you had NN).
     * Now it runs MCTS and moves.
     */
    public void decideMove(Chaser c1, Chaser c2, Grid grid) {
        List<Chaser> clist = new ArrayList<>();
        clist.add(c1);
        clist.add(c2);

        int[] mv = MCTS.bestMove(clist, this, grid, MCTS_ITERS, ROLLOUT_DEPTH);
        move(mv[0], mv[1]);
    }
}
