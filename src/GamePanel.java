import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {

    Grid grid = new Grid();
    Chaser ch = new Chaser(0, 0);
    Escaper es = new Escaper(Grid.ROWS - 1, Grid.COLS - 1);

    public GamePanel() {
        JFrame frame = new JFrame("Chaser vs Escaper");
        frame.setSize(650, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setVisible(true);

        gameLoop();
    }

    private void gameLoop() {
        while (true) {

            // Chaser uses A*
            Node start = new Node(ch.r, ch.c);
            Node goal = new Node(es.r, es.c);
            List<Node> path = AStar.search(start, goal, grid);

            if (path != null && path.size() > 1) {
                Node next = path.get(1);
                ch.move(next.r, next.c);
            }

            // Collision?
            if (ch.r == es.r && ch.c == es.c) {
                JOptionPane.showMessageDialog(this, "Chaser Wins!");
                System.exit(0);
            }

            // Escaper uses Minimax
            int[] m = Minimax.bestMove(ch, es, grid, 3);
            es.move(m[0], m[1]);

            repaint();

            if (ch.r == es.r && ch.c == es.c) {
                JOptionPane.showMessageDialog(this, "Chaser Wins!");
                System.exit(0);
            }

            try { Thread.sleep(300); } catch (Exception ignored) {}
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        int size = 40;

        // grid
        for (int r = 0; r < Grid.ROWS; r++) {
            for (int c = 0; c < Grid.COLS; c++) {
                if (Grid.map[r][c] == 1)
                    g.setColor(Color.BLACK);
                else
                    g.setColor(Color.WHITE);

                g.fillRect(c * size, r * size, size, size);
                g.setColor(Color.GRAY);
                g.drawRect(c * size, r * size, size, size);
            }
        }

        // Chaser
        g.setColor(Color.RED);
        g.fillOval(ch.c * size + 5, ch.r * size + 5, size - 10, size - 10);

        // Escaper
        g.setColor(Color.BLUE);
        g.fillOval(es.c * size + 5, es.r * size + 5, size - 10, size - 10);
    }
}
