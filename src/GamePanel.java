import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private final Grid grid;
    private final Chaser ch1;
    private final Chaser ch2;
    private final Escaper es;
    private final Timer timer;

    private int turn = 0;

    // ---- AYARLAR ----
    private static final int TILE = 40;
    private static final int TIMER_MS = 350; // Hız yavaşlatıldı (izlenebilir hız)
    private static final int MAX_TURNS = 70; // Escaper bu kadar dayanırsa kazanır

    private static final int MCTS_ITERS = 600;
    private static final int MCTS_ROLLOUT = 12;
    // ------------------

    public GamePanel(long seed, double wallDensity) {

        this.grid = new Grid(seed, wallDensity);

        this.ch1 = new Chaser(0, 0);
        this.ch2 = new Chaser(Grid.ROWS / 2, Grid.COLS / 2);
        this.es  = new Escaper(Grid.ROWS - 1, Grid.COLS - 1);

        JFrame frame = new JFrame("Chaser vs Escaper (Tek Oyun)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(Grid.COLS * TILE + 20, Grid.ROWS * TILE + 40);
        frame.add(this);
        frame.setVisible(true);

        // Timer'ı değişkene atadık ki oyun bitince durdurabilelim
        this.timer = new Timer(TIMER_MS, e -> step());
        timer.start();
    }

    // ---------------- STEP ----------------
    private void step() {
        turn++;

        System.out.printf(
                "Turn %d | E=(%d,%d) C1=(%d,%d) C2=(%d,%d)%n",
                turn, es.r, es.c, ch1.r, ch1.c, ch2.r, ch2.c
        );

        // -------- CHASER 1 --------
        List<Node> path1 = AStar.search(
                new Node(ch1.r, ch1.c),
                new Node(es.r, es.c),
                grid
        );

        if (path1 != null && path1.size() > 1) {
            Node n = path1.get(1);
            if (!(n.r == ch2.r && n.c == ch2.c)) {
                ch1.move(n.r, n.c);
            }
        }

        // -------- CHASER 2 (Tahminli) --------
        Node predicted = AStar.predictEscaperTarget(es, grid, 2);
        List<Node> path2 = AStar.search(
                new Node(ch2.r, ch2.c),
                predicted,
                grid
        );

        if (path2 != null && path2.size() > 1) {
            Node n2 = path2.get(1);
            if (!(n2.r == ch1.r && n2.c == ch1.c)) {
                ch2.move(n2.r, n2.c);
            }
        }

        // -------- YAKALAMA KONTROLÜ (Escaper hamlesinden önce) --------
        if (sameCell(ch1, es) || sameCell(ch2, es)) {
            gameOver("Chasers kazandı! Tur: " + turn);
            return;
        }

        // -------- ESCAPER (MCTS) --------
        List<Chaser> clist = new ArrayList<>();
        clist.add(ch1);
        clist.add(ch2);

        int[] mv = MCTS.bestMove(
                clist, es, grid,
                MCTS_ITERS, MCTS_ROLLOUT
        );

        es.move(mv[0], mv[1]);

        repaint();

        // -------- YAKALAMA KONTROLÜ (Escaper hamlesinden sonra) --------
        if (sameCell(ch1, es) || sameCell(ch2, es)) {
            gameOver("Chasers kazandı! Tur: " + turn);
            return;
        }

        // -------- TUR LİMİTİ KONTROLÜ --------
        if (turn >= MAX_TURNS) {
            gameOver("Escaper kazandı! (Hayatta kaldı)");
        }
    }

    private void gameOver(String message) {
        timer.stop(); // Oyunu durdur
        repaint();
        JOptionPane.showMessageDialog(this, message);
        // İsterseniz System.exit(0) ile tamamen kapatabilirsiniz:
        // System.exit(0);
    }

    // ---------------- DRAW ----------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < Grid.ROWS; r++) {
            for (int c = 0; c < Grid.COLS; c++) {
                g.setColor(grid.map[r][c] == 1 ? Color.BLACK : Color.WHITE);
                g.fillRect(c * TILE, r * TILE, TILE, TILE);
                g.setColor(Color.GRAY);
                g.drawRect(c * TILE, r * TILE, TILE, TILE);
            }
        }

        // Chaser 1 - KIRMIZI
        g.setColor(Color.RED);
        g.fillOval(ch1.c * TILE + 6, ch1.r * TILE + 6, TILE - 12, TILE - 12);

        // Chaser 2 - SİYAH
        g.setColor(Color.BLACK);
        g.fillOval(ch2.c * TILE + 6, ch2.r * TILE + 6, TILE - 12, TILE - 12);

        // Escaper - MAVİ
        g.setColor(Color.BLUE);
        g.fillOval(es.c * TILE + 6, es.r * TILE + 6, TILE - 12, TILE - 12);

        // Bilgi Yazısı
        g.setColor(Color.BLACK);
        g.drawString("Turn: " + turn + " / " + MAX_TURNS, 10, 15);
    }

    // ---------------- UTIL ----------------
    private boolean sameCell(Agent a, Agent b) {
        return a.r == b.r && a.c == b.c;
    }
}