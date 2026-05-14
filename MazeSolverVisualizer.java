import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;

public class MazeSolverVisualizer extends JFrame {

    // --- CONFIGURATION ---
    private final int ROWS = 21;
    private final int COLS = 21;
    private final int CELL_SIZE = 24;

    private Node[][] grid;
    private Node startNode, targetNode;
    private MazePanel mazePanel;

    private boolean isSolving = false;
    private int currentDelay = 20;

    // --- UI COMPONENTS ---
    private int nodesExplored = 0;
    private int pathLength = 0;
    private JLabel lblNodesExplored, lblPathLength, lblStatus;

    // --- MOUSE DRAG STATE VARIABLES ---
    private boolean isDraggingStart = false;
    private boolean isDraggingTarget = false;
    private boolean isDrawingWall = false;
    private boolean isErasingWall = false;

    public MazeSolverVisualizer() {
        setTitle("Ultimate Interactive Maze Solver - Final Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(new Color(18, 18, 18));

        initGrid();

        // --- LEFT SIDE: MAZE PANEL ---
        mazePanel = new MazePanel();
        mazePanel.setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        mazePanel.setBackground(new Color(18, 18, 18));
        setupMouseInteractions();

        JPanel mazeContainer = new JPanel(new GridBagLayout());
        mazeContainer.setBackground(new Color(18, 18, 18));
        mazeContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        mazeContainer.add(mazePanel);
        add(mazeContainer, BorderLayout.CENTER);

        // --- RIGHT SIDE: DASHBOARD ---
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BoxLayout(dashboardPanel, BoxLayout.Y_AXIS));
        dashboardPanel.setPreferredSize(new Dimension(280, ROWS * CELL_SIZE));
        dashboardPanel.setBackground(new Color(30, 30, 30));
        dashboardPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("SYSTEM DASHBOARD");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(255, 215, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tipLabel = new JLabel("<html><center>Drag nodes to move.<br>Left-Click: Draw | Right-Click: Erase</center></html>");
        tipLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tipLabel.setForeground(new Color(150, 150, 150));
        tipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Map Controls
        ModernButton btnGenerate = new ModernButton("🔄 Auto Generate Maze", new Color(41, 128, 185), new Color(52, 152, 219));
        btnGenerate.setToolTipText("Uses Recursive Backtracking to generate a perfect maze.");

        ModernButton btnBlank = new ModernButton("⬜ Blank Canvas", new Color(142, 68, 173), new Color(155, 89, 182));
        btnBlank.setToolTipText("Clears everything so you can draw your own walls.");

        ModernButton btnClear = new ModernButton("🗑️ Clear Path Only", new Color(127, 140, 141), new Color(149, 165, 166));
        btnClear.setToolTipText("Removes the colored paths but keeps your walls.");

        // Solvers
        ModernButton btnBFS = new ModernButton("▶ BFS (Shortest Path)", new Color(39, 174, 96), new Color(46, 204, 113));
        btnBFS.setToolTipText("Breadth-First Search: Guarantees shortest path, explores evenly like water.");

        ModernButton btnGreedy = new ModernButton("▶ Greedy (Fast/Blind)", new Color(211, 84, 0), new Color(230, 126, 34));
        btnGreedy.setToolTipText("Greedy Best-First: Rushes to target ignoring walls, often gets stuck.");

        ModernButton btnAStar = new ModernButton("⚡ A* (Smartest & Optimal)", new Color(192, 57, 43), new Color(231, 76, 60));
        btnAStar.setToolTipText("A-Star Search: Uses heuristics. Fast AND guarantees the shortest path.");

        // Live Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 2, 5));
        statsPanel.setBackground(new Color(40, 40, 40));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        lblStatus = new JLabel("Status: Idle 💤");
        lblNodesExplored = new JLabel("Nodes Explored: 0");
        lblPathLength = new JLabel("Path Length: 0");

        lblStatus.setForeground(new Color(52, 152, 219));
        lblNodesExplored.setForeground(Color.WHITE);
        lblPathLength.setForeground(Color.WHITE);

        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNodesExplored.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPathLength.setFont(new Font("Segoe UI", Font.BOLD, 12));

        statsPanel.add(lblStatus);
        statsPanel.add(lblNodesExplored);
        statsPanel.add(lblPathLength);

        // Speed Slider
        JLabel lblSpeed = new JLabel("Animation Speed:");
        lblSpeed.setForeground(Color.LIGHT_GRAY);
        lblSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSlider speedSlider = new JSlider(1, 100, currentDelay);
        speedSlider.setInverted(true);
        speedSlider.setBackground(new Color(30, 30, 30));
        speedSlider.addChangeListener(e -> currentDelay = speedSlider.getValue());

        // --- ADDING TO DASHBOARD ---
        dashboardPanel.add(title); dashboardPanel.add(Box.createVerticalStrut(5));
        dashboardPanel.add(tipLabel); dashboardPanel.add(Box.createVerticalStrut(10));

        dashboardPanel.add(btnGenerate); dashboardPanel.add(Box.createVerticalStrut(5));
        dashboardPanel.add(btnBlank); dashboardPanel.add(Box.createVerticalStrut(5));
        dashboardPanel.add(btnClear); dashboardPanel.add(Box.createVerticalStrut(15));

        dashboardPanel.add(btnBFS); dashboardPanel.add(Box.createVerticalStrut(5));
        dashboardPanel.add(btnGreedy); dashboardPanel.add(Box.createVerticalStrut(5));
        dashboardPanel.add(btnAStar); dashboardPanel.add(Box.createVerticalStrut(15));

        dashboardPanel.add(statsPanel); dashboardPanel.add(Box.createVerticalGlue());

        dashboardPanel.add(lblSpeed); dashboardPanel.add(speedSlider);

        add(dashboardPanel, BorderLayout.EAST);

        // --- BUTTON EVENTS ---
        btnGenerate.addActionListener(e -> { if (!isSolving) generateMaze(); });
        btnBlank.addActionListener(e -> { if (!isSolving) makeBlankGrid(); });
        btnClear.addActionListener(e -> { if (!isSolving) clearPathsOnly(); });

        btnBFS.addActionListener(e -> startSolver("BFS"));
        btnGreedy.addActionListener(e -> startSolver("GREEDY"));
        btnAStar.addActionListener(e -> startSolver("ASTAR"));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        generateMaze();
    }

    // --- MOUSE INTERACTION (DRAG & DROP) ---
    private void setupMouseInteractions() {
        mazePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isSolving) return;
                int c = e.getX() / CELL_SIZE; int r = e.getY() / CELL_SIZE;
                if (r < 0 || r >= ROWS || c < 0 || c >= COLS) return;
                Node clicked = grid[r][c];

                if (clicked == startNode) isDraggingStart = true;
                else if (clicked == targetNode) isDraggingTarget = true;
                else if (SwingUtilities.isLeftMouseButton(e)) { isDrawingWall = true; clicked.isWall = true; clearPathsOnly(); }
                else if (SwingUtilities.isRightMouseButton(e)) { isErasingWall = true; clicked.isWall = false; clearPathsOnly(); }
                mazePanel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDraggingStart = false; isDraggingTarget = false;
                isDrawingWall = false; isErasingWall = false;
            }
        });

        mazePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isSolving) return;
                int c = e.getX() / CELL_SIZE; int r = e.getY() / CELL_SIZE;
                if (r < 0 || r >= ROWS || c < 0 || c >= COLS) return;
                Node current = grid[r][c];

                if (isDraggingStart && !current.isWall && current != targetNode) {
                    startNode = current; clearPathsOnly();
                } else if (isDraggingTarget && !current.isWall && current != startNode) {
                    targetNode = current; clearPathsOnly();
                } else if (isDrawingWall && current != startNode && current != targetNode) {
                    current.isWall = true; clearPathsOnly();
                } else if (isErasingWall && current != startNode && current != targetNode) {
                    current.isWall = false; clearPathsOnly();
                }
                mazePanel.repaint();
            }
        });
    }

    // --- MODERN BUTTON DESIGN ---
    private class ModernButton extends JButton {
        public ModernButton(String text, Color normal, Color hover) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE); setBackground(normal);
            setFocusPainted(false); setBorderPainted(false); setOpaque(true);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(250, 32));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(hover); }
                public void mouseExited(MouseEvent e) { setBackground(normal); }
            });
        }
    }

    // --- UPDATE UI DASHBOARD ---
    private void updateUIState(String status, Color color, int explored, int path) {
        SwingUtilities.invokeLater(() -> {
            lblStatus.setText(status);
            lblStatus.setForeground(color);
            lblNodesExplored.setText("Nodes Explored: " + explored);
            lblPathLength.setText("Path Length: " + path);
        });
    }

    // --- NODE CLASS ---
    private class Node implements Comparable<Node> {
        int x, y;
        boolean isWall, visited, inFrontier, isPath;
        Node parent;
        int gCost, hCost;

        Node(int x, int y) { this.x = x; this.y = y; this.isWall = true; }
        int fCost() { return gCost + hCost; }

        void resetState() {
            visited = false; inFrontier = false; isPath = false; parent = null;
            gCost = 999999; hCost = 0;
        }

        @Override
        public int compareTo(Node other) {
            int compare = Integer.compare(this.fCost(), other.fCost());
            if (compare == 0) compare = Integer.compare(this.hCost, other.hCost);
            return compare;
        }
    }

    // --- GRID LOGIC ---
    private void initGrid() {
        grid = new Node[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) grid[r][c] = new Node(r, c);
        startNode = grid[1][1]; targetNode = grid[ROWS - 2][COLS - 2];
    }

    private void makeBlankGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c].isWall = false;
                grid[r][c].resetState();
            }
        }
        updateUIState("Status: Blank Canvas ⬜", new Color(155, 89, 182), 0, 0);
        mazePanel.repaint();
    }

    private void clearPathsOnly() {
        nodesExplored = 0; pathLength = 0;
        updateUIState("Status: Idle 💤", new Color(52, 152, 219), 0, 0);
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) grid[r][c].resetState();
        mazePanel.repaint();
    }

    private List<Node> getNeighbors(Node node, int step, boolean checkWalls) {
        List<Node> n = new ArrayList<>();
        int[][] dirs = {{-step, 0}, {step, 0}, {0, -step}, {0, step}};
        for (int[] d : dirs) {
            int nx = node.x + d[0], ny = node.y + d[1];
            if (nx >= 0 && nx < ROWS && ny >= 0 && ny < COLS) {
                if (!checkWalls || !grid[nx][ny].isWall) n.add(grid[nx][ny]);
            }
        }
        return n;
    }

    private void generateMaze() {
        initGrid();
        Stack<Node> stack = new Stack<>();
        startNode.isWall = false; startNode.visited = true; stack.push(startNode);
        Random rand = new Random();

        while (!stack.isEmpty()) {
            Node current = stack.peek();
            List<Node> n = getNeighbors(current, 2, false);
            List<Node> unvisited = new ArrayList<>();
            for (Node node : n) if (!node.visited) unvisited.add(node);

            if (!unvisited.isEmpty()) {
                Node next = unvisited.get(rand.nextInt(unvisited.size()));
                grid[current.x + (next.x - current.x)/2][current.y + (next.y - current.y)/2].isWall = false;
                next.isWall = false; next.visited = true; stack.push(next);
            } else stack.pop();
        }
        clearPathsOnly();
        startNode.isWall = false; targetNode.isWall = false;
        updateUIState("Status: Maze Generated 🎲", new Color(46, 204, 113), 0, 0);
    }

    // --- ALGORITHM RUNNER ---
    private void startSolver(String algorithm) {
        if (isSolving) return;
        clearPathsOnly();
        isSolving = true;
        updateUIState("Status: Solving... ⏳", new Color(241, 196, 15), 0, 0);

        new Thread(() -> {
            boolean found = false;
            switch (algorithm) {
                case "BFS": found = solveBFS(); break;
                case "GREEDY": found = solveGreedy(); break;
                case "ASTAR": found = solveAStar(); break;
            }
            if (found) {
                tracePath();
                updateUIState("Status: Path Found! ✅", new Color(46, 204, 113), nodesExplored, pathLength);
            } else {
                updateUIState("Status: NO PATH EXISTS! ❌", new Color(231, 76, 60), nodesExplored, 0);
            }
            isSolving = false;
        }).start();
    }

    private void sleep() {
        try { Thread.sleep(currentDelay); mazePanel.repaint(); } catch (InterruptedException e) {}
    }

    private void tracePath() {
        Node current = targetNode;
        pathLength = 0;
        while (current != null) {
            current.isPath = true;
            current = current.parent;
            if(current != null) pathLength++;
            updateUIState("Status: Tracing Path... 🖌️", new Color(241, 196, 15), nodesExplored, pathLength);
            sleep();
        }
    }

    // --- 1. BFS ---
    private boolean solveBFS() {
        Queue<Node> q = new LinkedList<>();
        q.add(startNode); startNode.visited = true;
        while (!q.isEmpty()) {
            Node curr = q.poll(); curr.inFrontier = false;
            if (curr == targetNode) return true;
            for (Node n : getNeighbors(curr, 1, true)) {
                if (!n.visited) {
                    n.visited = true; n.inFrontier = true; n.parent = curr; q.add(n);
                    nodesExplored++;
                    if(nodesExplored % 5 == 0) updateUIState("Status: Solving BFS... ⏳", new Color(241, 196, 15), nodesExplored, 0);
                }
            }
            sleep();
        }
        return false;
    }

    // --- 2. GREEDY ---
    private boolean solveGreedy() {
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> Integer.compare(a.hCost, b.hCost));
        startNode.hCost = Math.abs(startNode.x - targetNode.x) + Math.abs(startNode.y - targetNode.y);
        pq.add(startNode); startNode.visited = true;

        while (!pq.isEmpty()) {
            Node curr = pq.poll(); curr.inFrontier = false;
            if (curr == targetNode) return true;
            for (Node n : getNeighbors(curr, 1, true)) {
                if (!n.visited) {
                    n.visited = true; n.inFrontier = true; n.parent = curr;
                    n.hCost = Math.abs(n.x - targetNode.x) + Math.abs(n.y - targetNode.y);
                    pq.add(n);
                    nodesExplored++;
                    if(nodesExplored % 2 == 0) updateUIState("Status: Solving Greedy... ⏳", new Color(241, 196, 15), nodesExplored, 0);
                }
            }
            sleep();
        }
        return false;
    }

    // --- 3. A* (A-STAR) ---
    private boolean solveAStar() {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        startNode.gCost = 0; startNode.hCost = Math.abs(startNode.x - targetNode.x) + Math.abs(startNode.y - targetNode.y);
        pq.add(startNode); startNode.inFrontier = true;
        while (!pq.isEmpty()) {
            Node curr = pq.poll(); curr.inFrontier = false; curr.visited = true;
            if (curr == targetNode) return true;
            for (Node n : getNeighbors(curr, 1, true)) {
                if (n.visited) continue;
                int tentativeGCost = curr.gCost + 1;
                if (tentativeGCost < n.gCost) {
                    n.parent = curr; n.gCost = tentativeGCost;
                    n.hCost = Math.abs(n.x - targetNode.x) + Math.abs(n.y - targetNode.y);
                    if (!n.inFrontier) {
                        n.inFrontier = true; pq.add(n); nodesExplored++;
                        if(nodesExplored % 2 == 0) updateUIState("Status: Solving A*... ⏳", new Color(241, 196, 15), nodesExplored, 0);
                    }
                    else { pq.remove(n); pq.add(n); }
                }
            }
            sleep();
        }
        return false;
    }

    // --- RENDERER ---
    private class MazePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    Node node = grid[r][c];
                    if (node.isWall) g2.setColor(new Color(40, 44, 52));
                    else if (node == startNode) g2.setColor(new Color(46, 204, 113));
                    else if (node == targetNode) g2.setColor(new Color(231, 76, 60));
                    else if (node.isPath) g2.setColor(new Color(241, 196, 15));
                    else if (node.inFrontier) g2.setColor(new Color(52, 152, 219));
                    else if (node.visited) g2.setColor(new Color(75, 85, 95));
                    else g2.setColor(new Color(25, 25, 25));

                    int margin = 2;
                    g2.fillRoundRect(c * CELL_SIZE + margin, r * CELL_SIZE + margin, CELL_SIZE - margin * 2, CELL_SIZE - margin * 2, 6, 6);
                }
            }
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MazeSolverVisualizer());
    }
}
