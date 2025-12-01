import java.util.*;

public class AStar {

    private static Random rand = new Random();

    public static List<Node> search(Node start, Node goal, Grid grid) {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        HashSet<Node> closed = new HashSet<>();

        start.g = 0;
        start.h = manhattan(start, goal);
        start.f = start.g + start.h;
        open.add(start);

        while (!open.isEmpty()) {

            Node current = pollWithRandomTieBreak(open);

            if (current.equals(goal)) return reconstruct(current);

            closed.add(current);

            for (Node nb : grid.getNeighbors(current)) {
                if (closed.contains(nb)) continue;

                double g_new = current.g + 1;

                boolean better = false;

                if (!open.contains(nb)) {
                    better = true;
                } else if (g_new < nb.g) {
                    better = true;
                }

                if (better) {
                    nb.parent = current;
                    nb.g = g_new;
                    nb.h = manhattan(nb, goal);
                    nb.f = nb.g + nb.h;

                    open.add(nb);
                }
            }
        }
        return null;
    }

    // Stochastic tie-break
    private static Node pollWithRandomTieBreak(PriorityQueue<Node> open) {
        List<Node> list = new ArrayList<>(open);
        double bestF = Double.POSITIVE_INFINITY;
        for (Node n : list) if (n.f < bestF) bestF = n.f;

        List<Node> bestNodes = new ArrayList<>();
        for (Node n : list) if (n.f == bestF) bestNodes.add(n);

        Node chosen = bestNodes.get(rand.nextInt(bestNodes.size()));
        open.remove(chosen);
        return chosen;
    }

    private static List<Node> reconstruct(Node node) {
        List<Node> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public static int manhattan(Node a, Node b) {
        return Math.abs(a.r - b.r) + Math.abs(a.c - b.c);
    }

    // -----------------------------
    // PINK TAHMİN HEDEFİ
    // -----------------------------
    public static Node predictEscaperTarget(Escaper es, Grid grid, int lookahead) {
        int r = es.r;
        int c = es.c;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        for (int i = 0; i < lookahead; i++) {
            List<int[]> validMoves = new ArrayList<>();
            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (grid.isValid(nr, nc)) validMoves.add(new int[]{nr, nc});
            }

            if (validMoves.isEmpty()) break;

            int[] move = validMoves.get(rand.nextInt(validMoves.size()));
            r = move[0];
            c = move[1];
        }

        return new Node(r, c);
    }
}
