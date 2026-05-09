import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MazeSolverVisualizer extends JFrame {

    // --- CONFIGURATION ---
    private final int ROWS = 25;
    private final int COLS = 25;
    private final int CELL_SIZE = 24;

    private Node[][] grid;
    private Node startNode;
    private Node targetNode;

    private MazePanel mazePanel;
    private boolean isSolving = false;
    private int currentDelay = 30; // Default animation speed

    public MazeSolverVisualizer() {
        setTitle("Maze Solver Visualizer - Pro Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(new Color(30, 30, 30));

        initGrid();

        // --- LEFT SIDE: MAZE VISUALIZER ---
        mazePanel = new MazePanel();
        mazePanel.setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        mazePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mazeContainer = new JPanel(new BorderLayout());
        mazeContainer.setBackground(new Color(40, 44, 52));
        mazeContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mazeContainer.add(mazePanel, BorderLayout.CENTER);
        add(mazeContainer, BorderLayout.CENTER);

        // --- RIGHT SIDE: DASHBOARD ---
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BoxLayout(dashboardPanel, BoxLayout.Y_AXIS));
        dashboardPanel.setPreferredSize(new Dimension(260, ROWS * CELL_SIZE));
        dashboardPanel.setBackground(new Color(33, 37, 43));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        // 1. Map Controls Section
        JPanel mapSection = createSectionPanel("Map Controls");
        JButton btnGenerate = createStyledButton("🔄 Generate New Maze", new Color(0, 122, 204));
        JButton btnClear = createStyledButton("🗑️ Clear Path Only", new Color(108, 117, 125));
        mapSection.add(btnGenerate);
        mapSection.add(Box.createVerticalStrut(10));
        mapSection.add(btnClear);

        // 2. Algorithm Section
        JPanel algoSection = createSectionPanel("Solvers");
        JButton btnBFS = createStyledButton("▶ Run BFS (Shortest)", new Color(40, 167, 69));
        JButton btnDFS = createStyledButton("▶ Run DFS (Fast/Blind)", new Color(220, 53, 69));
        JButton btnAStar = createStyledButton("⚡ Run A* (Smart)", new Color(253, 126, 20));
        algoSection.add(btnBFS);
        algoSection.add(Box.createVerticalStrut(10));
        algoSection.add(btnDFS);
        algoSection.add(Box.createVerticalStrut(10));
        algoSection.add(btnAStar);

        // 3. Settings Section
        JPanel settingsSection = createSectionPanel("Animation Speed");
        JSlider speedSlider = new JSlider(1, 100, currentDelay);
        speedSlider.setInverted(true);
        speedSlider.setBackground(new Color(33, 37, 43));
        speedSlider.addChangeListener(e -> currentDelay = speedSlider.getValue());
        settingsSection.add(speedSlider);

        // Add all sections to dashboard
        dashboardPanel.add(mapSection);
        dashboardPanel.add(Box.createVerticalStrut(15));
        dashboardPanel.add(algoSection);
        dashboardPanel.add(Box.createVerticalStrut(15));
        dashboardPanel.add(settingsSection);
        dashboardPanel.add(Box.createVerticalGlue()); // Push everything up

        // 4. Color Legend
        JPanel legendPanel = createSectionPanel("Color Legend");
        legendPanel.setLayout(new GridLayout(3, 2, 5, 5));
        legendPanel.add(createLegendItem("Start/End", new Color(40, 167, 69)));
        legendPanel.add(createLegendItem("Wall", new Color(25, 25, 25)));
        legendPanel.add(createLegendItem("Frontier", new Color(0, 122, 204)));
        legendPanel.add(createLegendItem("Visited", new Color(180, 180, 180)));
        legendPanel.add(createLegendItem("Final Path", new Color(255, 193, 7)));
        dashboardPanel.add(legendPanel);

        add(dashboardPanel, BorderLayout.EAST);

        // --- BUTTON ACTIONS ---
        btnGenerate.addActionListener(e -> {
            if (!isSolving) { generateMaze(); mazePanel.repaint(); }
        });
        btnClear.addActionListener(e -> {
            if (!isSolving) { clearPaths(); mazePanel.repaint(); }
        });
        btnBFS.addActionListener(e -> startSolver("BFS"));
        btnDFS.addActionListener(e -> startSolver("DFS"));
        btnAStar.addActionListener(e -> startSolver("ASTAR"));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        generateMaze(); // Run on startup
    }

    // --- UI HELPER METHODS ---
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(33, 37, 43));
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title);
        border.setTitleColor(Color.WHITE);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 35));
        return btn;
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setBackground(new Color(33, 37, 43));
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        JLabel textLabel = new JLabel(label);
        textLabel.setForeground(Color.LIGHT_GRAY);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        p.add(colorBox);
        p.add(textLabel);
        return p;
    }

    // --- NODE CLASS ---
    private class Node implements Comparable<Node> {
        int x, y;
        boolean isWall, visited, inFrontier, isPath;
        Node parent;
        int gCost, hCost;

        Node(int x, int y) {
            this.x = x; this.y = y; this.isWall = true;
        }

        int fCost() { return gCost + hCost; }

        void resetState() {
            visited = false; inFrontier = false; isPath = false; parent = null;
            gCost = 0; hCost = 0;
        }

        @Override
        public int compareTo(Node other) {
            int compare = Integer.compare(this.fCost(), other.fCost());
            if (compare == 0) compare = Integer.compare(this.hCost, other.hCost);
            return compare;
        }
    }

    // --- ALGORITHM LOGIC ---
    private void initGrid() {
        grid = new Node[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) grid[r][c] = new Node(r, c);
        startNode = grid[1][1]; targetNode = grid[ROWS - 2][COLS - 2];
    }

    private void clearPaths() {
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) grid[r][c].resetState();
    }

    private List<Node> getNeighbors(Node node, int step, boolean checkWalls) {
        List<Node> neighbors = new ArrayList<>();
        int[][] dirs = {{-step, 0}, {step, 0}, {0, -step}, {0, step}};
        for (int[] dir : dirs) {
            int nx = node.x + dir[0], ny = node.y + dir[1];
            if (nx > 0 && nx < ROWS - 1 && ny > 0 && ny < COLS - 1) {
                Node neighbor = grid[nx][ny];
                if (!checkWalls || !neighbor.isWall) neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    private void generateMaze() {
        initGrid();
        Stack<Node> stack = new Stack<>();
        startNode.isWall = false; startNode.visited = true;
        stack.push(startNode);
        Random rand = new Random();

        while (!stack.isEmpty()) {
            Node current = stack.peek();
            List<Node> neighbors = getNeighbors(current, 2, false);
            List<Node> unvisited = new ArrayList<>();
            for (Node n : neighbors) if (!n.visited) unvisited.add(n);

            if (!unvisited.isEmpty()) {
                Node next = unvisited.get(rand.nextInt(unvisited.size()));
                grid[current.x + (next.x - current.x)/2][current.y + (next.y - current.y)/2].isWall = false;
                next.isWall = false; next.visited = true;
                stack.push(next);
            } else stack.pop();
        }
        clearPaths();
        startNode.isWall = false; targetNode.isWall = false;
    }

    private void startSolver(String algorithm) {
        if (isSolving) return;
        clearPaths(); isSolving = true;
        new Thread(() -> {
            boolean found = false;
            switch (algorithm) {
                case "BFS": found = solveBFS(); break;
                case "DFS": found = solveDFS(); break;
                case "ASTAR": found = solveAStar(); break;
            }
            if (found) tracePath();
            isSolving = false;
        }).start();
    }

    private void sleep() {
        try { Thread.sleep(currentDelay); mazePanel.repaint(); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void tracePath() {
        Node current = targetNode;
        while (current != null) {
            current.isPath = true; current = current.parent; sleep();
        }
    }

    private boolean solveBFS() {
        Queue<Node> queue = new LinkedList<>(); queue.add(startNode); startNode.visited = true;
        while (!queue.isEmpty()) {
            Node current = queue.poll(); current.inFrontier = false;
            if (current == targetNode) return true;
            for (Node neighbor : getNeighbors(current, 1, true)) {
                if (!neighbor.visited) {
                    neighbor.visited = true; neighbor.inFrontier = true; neighbor.parent = current; queue.add(neighbor);
                }
            }
            sleep();
        }
        return false;
    }

    private boolean solveDFS() {
        Stack<Node> stack = new Stack<>(); stack.push(startNode); startNode.visited = true;
        while (!stack.isEmpty()) {
            Node current = stack.pop(); current.inFrontier = false;
            if (current == targetNode) return true;
            for (Node neighbor : getNeighbors(current, 1, true)) {
                if (!neighbor.visited) {
                    neighbor.visited = true; neighbor.inFrontier = true; neighbor.parent = current; stack.push(neighbor);
                }
            }
            sleep();
        }
        return false;
    }

    private boolean solveAStar() {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        startNode.gCost = 0; startNode.hCost = Math.abs(startNode.x - targetNode.x) + Math.abs(startNode.y - targetNode.y);
        openSet.add(startNode); startNode.inFrontier = true;
        while (!openSet.isEmpty()) {
            Node current = openSet.poll(); current.inFrontier = false; current.visited = true;
            if (current == targetNode) return true;
            for (Node neighbor : getNeighbors(current, 1, true)) {
                if (neighbor.visited) continue;
                int tentativeGCost = current.gCost + 1;
                if (tentativeGCost < neighbor.gCost || !neighbor.inFrontier) {
                    neighbor.gCost = tentativeGCost; neighbor.hCost = Math.abs(neighbor.x - targetNode.x) + Math.abs(neighbor.y - targetNode.y); neighbor.parent = current;
                    if (!neighbor.inFrontier) { neighbor.inFrontier = true; openSet.add(neighbor); }
                }
            }
            sleep();
        }
        return false;
    }

    // --- CUSTOM RENDERING PANEL ---
    private class MazePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    Node node = grid[r][c];

                    if (node.isWall) g.setColor(new Color(25, 25, 25));
                    else if (node == startNode || node == targetNode) g.setColor(new Color(40, 167, 69));
                    else if (node.isPath) g.setColor(new Color(255, 193, 7));
                    else if (node.inFrontier) g.setColor(new Color(0, 122, 204));
                    else if (node.visited) g.setColor(new Color(180, 180, 180));
                    else g.setColor(Color.WHITE);

                    g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    g.setColor(new Color(50, 50, 50)); // Subtle Grid lines
                    g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MazeSolverVisualizer());
    }
}
