import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GamePanel extends JPanel {

    Grid grid = new Grid();
    Chaser ch = new Chaser(0, 0);
    Chaser ch2 = new Chaser(Grid.ROWS / 2, Grid.COLS / 2);
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
            // CHASER 1 - A*
            //-----------------------------
            Node start = new Node(ch.r, ch.c);
            Node goal = new Node(es.r, es.c);
            List<Node> path = AStar.search(start, goal, grid);

            if (path != null && path.size() > 1) {
                Node next = path.get(1);
                if (!(next.r == ch2.r && next.c == ch2.c)) {
                    ch.move(next.r, next.c);
                }
            }

            //-----------------------------
            // CHASER 2 - Pink tarzı A*
            //-----------------------------
            Node start2 = new Node(ch2.r, ch2.c);
            Node predicted = AStar.predictEscaperTarget(es, grid, 2);
            List<Node> path2 = AStar.search(start2, predicted, grid);

            if (path2 != null && path2.size() > 1) {
                Node next2 = path2.get(1);
                if (!(next2.r == ch.r && next2.c == ch.c)) {
                    ch2.move(next2.r, next2.c);
                }
            }

            //-----------------------------
            // COLLISION
            //-----------------------------
            if ((ch.r == es.r && ch.c == es.c) ||
                    (ch2.r == es.r && ch2.c == es.c)) {

                JOptionPane.showMessageDialog(this, "Chasers Win!");
                System.exit(0);
            }

            //-----------------------------
            // ESCAPER - NEURAL NETWORK
            // (MINIMAX KALDIRILDI, SADECE BU SATIR EKLENDİ)
            //-----------------------------
            es.decideMove(ch, ch2, grid);

            repaint();

            //-----------------------------
            // COLLISION AGAIN
            //-----------------------------
            if ((ch.r == es.r && ch.c == es.c) ||
                    (ch2.r == es.r && ch2.c == es.c)) {

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

        //-----------------------------
        // GRID
        //-----------------------------
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

        //-----------------------------
        // PAC-MAN SETTINGS
        //-----------------------------
        int mouth = 60;

        //-----------------------------
        // CHASER 1
        //-----------------------------
        g.setColor(Color.green);
        g.fillArc(
                ch.c * size + 5,
                ch.r * size + 5,
                size - 10,
                size - 10,
                pacmanAngle(ch.dir, mouth),
                360 - mouth
        );

        //-----------------------------
        // CHASER 2
        //-----------------------------
        g.setColor(Color.cyan);
        g.fillArc(
                ch2.c * size + 5,
                ch2.r * size + 5,
                size - 10,
                size - 10,
                pacmanAngle(ch2.dir, mouth),
                360 - mouth
        );

        //-----------------------------
        // ESCAPER
        //-----------------------------
        g.setColor(Color.MAGENTA);
        g.fillOval(
                es.c * size + 5,
                es.r * size + 5,
                size - 10,
                size - 10
        );
    }

    //-----------------------------
    // PAC-MAN DIRECTION ANGLE
    //-----------------------------
    private int pacmanAngle(int dir, int mouth) {
        switch (dir) {
            case 0: return mouth / 2;            // RIGHT
            case 1: return 270 + mouth / 2;      // DOWN
            case 2: return 180 + mouth / 2;      // LEFT
            case 3: return 90 + mouth / 2;       // UP
        }
        return 0;
    }
}
