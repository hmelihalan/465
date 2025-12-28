import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GamePanel extends JPanel {

    Grid grid = new Grid();
    Chaser ch = new Chaser(0, 0);
    Chaser ch2 = new Chaser(Grid.ROWS / 2, Grid.COLS / 2); // Pink tarzı
    Escaper es = new Escaper(Grid.ROWS - 1, Grid.COLS - 1);

    public GamePanel() {
        Scanner sc = new Scanner(System.in);
        JFrame frame = new JFrame("Chaser vs Escaper");
        frame.setSize(650, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setVisible(true);

        sc.nextLine();
        gameLoop();
    }

    private void gameLoop() {
        while (true) {

            //-----------------------------
            // CHASER 1 - A* normal
            //-----------------------------
            Node start = new Node(ch.r, ch.c);
            Node goal = new Node(es.r, es.c);
            List<Node> path = AStar.search(start, goal, grid);
            Node next = null;
            if (path != null && path.size() > 1) next = path.get(1);

            if (next != null && !(next.r == ch2.r && next.c == ch2.c)) {
                ch.move(next.r, next.c);
            }

            //-----------------------------
            // CHASER 2 - Pink tarzı A*
            //-----------------------------
            Node start2 = new Node(ch2.r, ch2.c);
            Node predictedTarget = AStar.predictEscaperTarget(es, grid, 2); // 2 adım ileri tahmin
            List<Node> path2 = AStar.search(start2, predictedTarget, grid);
            Node next2 = null;
            if (path2 != null && path2.size() > 1) next2 = path2.get(1);

            if (next2 != null && !(next2.r == ch.r && next2.c == ch.c)) {
                ch2.move(next2.r, next2.c);
            }

            //-----------------------------
            // COLLISION CHECK
            //-----------------------------
            if ((ch.r == es.r && ch.c == es.c) ||
                    (ch2.r == es.r && ch2.c == es.c))
            {
                JOptionPane.showMessageDialog(this, "Chasers Win!");
                System.exit(0);
            }

            //-----------------------------
            // ESCAPER - Minimax
            //-----------------------------
            List<Chaser> clist = new ArrayList<>();
            clist.add(ch);
            clist.add(ch2);

            int[] m = Minimax.bestMove(clist, es, grid, 3);
            es.move(m[0], m[1]);

            repaint();

            //-----------------------------
            // COLLISION AGAIN
            //-----------------------------
            if ((ch.r == es.r && ch.c == es.c) ||
                    (ch2.r == es.r && ch2.c == es.c))
            {
                JOptionPane.showMessageDialog(this, "Chasers Win!");
                System.exit(0);
            }

            try { Thread.sleep(300); }
            catch (Exception ignored) {}
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        int size = 40;

        // GRID ÇİZ
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

        // CHASER 1
        g.setColor(Color.RED);
        g.fillOval(ch.c * size + 5, ch.r * size + 5, size - 10, size - 10);

        // CHASER 2
        g.setColor(Color.BLACK);
        g.fillOval(ch2.c * size + 5, ch2.r * size + 5, size - 10, size - 10);

        // ESCAPER
        g.setColor(Color.BLUE);
        g.fillOval(es.c * size + 5, es.r * size + 5, size - 10, size - 10);
    }
}
