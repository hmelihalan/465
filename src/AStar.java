import java.util.*;

public class AStar {
    public static List<Node> search(Node start, Node goal, Grid grid) {

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        HashSet<Node> closed = new HashSet<>();

        start.g = 0;
        start.h = manhattan(start, goal);
        start.f = start.g + start.h;
        open.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();

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
        return null; // no path
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
}
