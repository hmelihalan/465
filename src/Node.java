public class Node {
    public int r, c;
    public double g, h, f;

    public Node parent;

    public Node(int r, int c) {
        this.r = r;
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node)) return false;
        Node n = (Node) o;
        return n.r == r && n.c == c;
    }

    @Override
    public int hashCode() {
        return r * 31 + c;
    }
}
