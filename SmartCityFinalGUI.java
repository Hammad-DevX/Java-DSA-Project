import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SmartCityFinalGUI extends JFrame {

    // --- Graph Data Structures ---
    static class Road {
        String dest;
        double baseDistance;
        double currentCost; // Ab direct cost bhi set ho sakti hai

        Road(String dest, double baseDistance) {
            this.dest = dest;
            this.baseDistance = baseDistance;
            this.currentCost = baseDistance; // By default same as base
        }
    }

    static class NodeCost {
        String name;
        double cost;
        NodeCost(String name, double cost) { this.name = name; this.cost = cost; }
    }

    private final Map<String, List<Road>> graph = new HashMap<>();
    private final Map<String, Point> nodeCoords = new HashMap<>();
    private List<String> currentPath = new ArrayList<>();
    private double currentTotalCost = 0.0;

    // --- GUI Components ---
    private JComboBox<String> startCombo, endCombo;
    private JComboBox<String> roadStartCombo, roadEndCombo;
    private JTextField valueField;
    private MapPanel mapPanel;
    private JTextArea logArea;

    public SmartCityFinalGUI() {
        setTitle("Smart City Navigator - Ultimate Edition");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        setupGraph();

        // --- MAP PANEL (Center) ---
        mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);

        // --- RIGHT SIDEBAR (Controls) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(290, 550));
        sidebar.setBackground(new Color(245, 245, 250));
        sidebar.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Route Planner Section
        JPanel routePanel = createSidebarSection("Route Planner", new Color(41, 128, 185));
        String[] nodes = nodeCoords.keySet().toArray(new String[0]);
        startCombo = new JComboBox<>(nodes);
        endCombo = new JComboBox<>(nodes);
        endCombo.setSelectedItem("Office");

        JButton findPathBtn = new JButton("Find Route");
        findPathBtn.setBackground(new Color(46, 204, 113));
        findPathBtn.setForeground(Color.WHITE);
        findPathBtn.setFocusPainted(false);

        routePanel.add(new JLabel("Start Point:"));
        routePanel.add(startCombo);
        routePanel.add(Box.createVerticalStrut(5));
        routePanel.add(new JLabel("End Point:"));
        routePanel.add(endCombo);
        routePanel.add(Box.createVerticalStrut(10));
        routePanel.add(findPathBtn);

        // 2. Traffic / Cost Override Section
        JPanel trafficPanel = createSidebarSection("Manual Road Update", new Color(192, 57, 43));
        roadStartCombo = new JComboBox<>(nodes);
        roadEndCombo = new JComboBox<>(nodes);
        valueField = new JTextField("2.0");

        // Naye 2 alag buttons banaye hain
        JButton btnMultiply = new JButton("Multiply (x)");
        btnMultiply.setBackground(new Color(230, 126, 34)); // Orange
        btnMultiply.setForeground(Color.WHITE);
        btnMultiply.setToolTipText("Multiplies the original base distance.");

        JButton btnExact = new JButton("Set Exact");
        btnExact.setBackground(new Color(231, 76, 60)); // Red
        btnExact.setForeground(Color.WHITE);
        btnExact.setToolTipText("Sets the road cost to exactly this value.");

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnMultiply);
        buttonsPanel.add(btnExact);

        trafficPanel.add(new JLabel("Road Start:"));
        trafficPanel.add(roadStartCombo);
        trafficPanel.add(Box.createVerticalStrut(5));
        trafficPanel.add(new JLabel("Road End:"));
        trafficPanel.add(roadEndCombo);
        trafficPanel.add(Box.createVerticalStrut(5));
        trafficPanel.add(new JLabel("Enter Value:"));
        trafficPanel.add(valueField);
        trafficPanel.add(Box.createVerticalStrut(10));
        trafficPanel.add(buttonsPanel);

        // 3. Logs Section
        JPanel logPanel = createSidebarSection("System Logs", new Color(44, 62, 80));
        logArea = new JTextArea(6, 20);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(10, 255, 10)); // Hacker green style
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(logArea);
        logPanel.add(scrollPane);

        sidebar.add(routePanel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(trafficPanel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(logPanel);

        add(sidebar, BorderLayout.EAST);

        // --- Event Listeners ---
        findPathBtn.addActionListener(e -> calculatePath());

        // Pass "true" for multiplier, "false" for exact value
        btnMultiply.addActionListener(e -> applyRoadUpdate(true));
        btnExact.addActionListener(e -> applyRoadUpdate(false));

        calculatePath(); // Initial run
    }

    private JPanel createSidebarSection(String title, Color borderColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColor, 2), title);
        border.setTitleFont(new Font("Arial", Font.BOLD, 12));
        border.setTitleColor(borderColor);
        panel.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(5, 5, 5, 5)));
        return panel;
    }

    private void setupGraph() {
        // [FIXED]: Ye Coordinates ab exactly 550x550 panel ke andar fit rahenge.
        // Screen ke bahar ab koi bhi node nahi jayega.
        nodeCoords.put("Home", new Point(50, 250));
        nodeCoords.put("Mall", new Point(160, 130));
        nodeCoords.put("Park", new Point(160, 370));
        nodeCoords.put("Station", new Point(270, 250));
        nodeCoords.put("Downtown", new Point(400, 250));
        nodeCoords.put("Airport", new Point(380, 80));
        nodeCoords.put("Office", new Point(500, 250));

        for (String node : nodeCoords.keySet()) {
            graph.put(node, new ArrayList<>());
        }

        addRoad("Home", "Mall", 5);
        addRoad("Home", "Park", 6);
        addRoad("Home", "Station", 8);
        addRoad("Mall", "Station", 4);
        addRoad("Park", "Station", 4);
        addRoad("Station", "Downtown", 5);
        addRoad("Downtown", "Office", 4);
        addRoad("Station", "Airport", 9);
        addRoad("Airport", "Office", 5);
        addRoad("Park", "Downtown", 8);
    }

    private void addRoad(String u, String v, double dist) {
        graph.get(u).add(new Road(v, dist));
        graph.get(v).add(new Road(u, dist));
    }

    // [NEW LOGIC]: Update logic for both Multiplier and Exact values
    private void applyRoadUpdate(boolean isMultiplier) {
        String u = (String) roadStartCombo.getSelectedItem();
        String v = (String) roadEndCombo.getSelectedItem();

        if (u.equals(v)) {
            JOptionPane.showMessageDialog(this, "Select two different locations!", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double val = Double.parseDouble(valueField.getText().trim());
            if (val <= 0) {
                JOptionPane.showMessageDialog(this, "Value must be greater than 0.", "Invalid Value", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean updated = false;
            for (Road r : graph.get(u)) {
                if (r.dest.equals(v)) {
                    r.currentCost = isMultiplier ? r.baseDistance * val : val;
                    updated = true;
                }
            }
            for (Road r : graph.get(v)) {
                if (r.dest.equals(u)) {
                    r.currentCost = isMultiplier ? r.baseDistance * val : val;
                }
            }

            if (updated) {
                String type = isMultiplier ? "multiplied by " + val + "x" : "set exactly to " + val;
                logArea.append("> Road [" + u + "-" + v + "] " + type + ".\n");
                calculatePath();
            } else {
                JOptionPane.showMessageDialog(this, "No direct road between " + u + " and " + v, "Road Not Found", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculatePath() {
        String start = (String) startCombo.getSelectedItem();
        String end = (String) endCombo.getSelectedItem();

        if (start.equals(end)) {
            currentPath.clear();
            currentPath.add(start);
            currentTotalCost = 0.0;
            logArea.append("> Start = End. You are at " + start + ".\n");
            mapPanel.repaint();
            return;
        }

        PriorityQueue<NodeCost> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        Map<String, Double> dists = new HashMap<>();
        Map<String, String> parent = new HashMap<>();

        graph.keySet().forEach(k -> dists.put(k, Double.MAX_VALUE));
        dists.put(start, 0.0);
        pq.add(new NodeCost(start, 0.0));

        while (!pq.isEmpty()) {
            NodeCost curr = pq.poll();
            if (curr.name.equals(end)) break;

            for (Road r : graph.get(curr.name)) {
                double newDist = dists.get(curr.name) + r.currentCost;
                if (newDist < dists.get(r.dest)) {
                    dists.put(r.dest, newDist);
                    parent.put(r.dest, curr.name);
                    pq.add(new NodeCost(r.dest, newDist));
                }
            }
        }

        currentPath.clear();
        for (String at = end; at != null; at = parent.get(at)) {
            currentPath.add(0, at);
        }

        if (currentPath.size() == 1 && !start.equals(end)) {
            logArea.append("> ERROR: No route from " + start + " to " + end + "!\n");
            currentPath.clear();
            currentTotalCost = 0.0;
        } else {
            currentTotalCost = dists.get(end);
            logArea.append(String.format("> Route: %s \n> Total Cost: %.1f\n", String.join(" -> ", currentPath), currentTotalCost));
        }
        mapPanel.repaint();
    }

    // --- CUSTOM MAP DRAWING (Dark Theme) ---
    class MapPanel extends JPanel {
        public MapPanel() {
            setBackground(new Color(30, 41, 59)); // Dark Slate background
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Draw Edges
            for (String u : graph.keySet()) {
                Point p1 = nodeCoords.get(u);
                for (Road r : graph.get(u)) {
                    Point p2 = nodeCoords.get(r.dest);

                    boolean isPath = false;
                    for (int i = 0; i < currentPath.size() - 1; i++) {
                        if ((currentPath.get(i).equals(u) && currentPath.get(i+1).equals(r.dest)) ||
                                (currentPath.get(i).equals(r.dest) && currentPath.get(i+1).equals(u))) {
                            isPath = true; break;
                        }
                    }

                    if (isPath) {
                        g2.setColor(new Color(16, 185, 129)); // Neon Green Path
                        g2.setStroke(new BasicStroke(5));
                    } else if (r.currentCost > r.baseDistance) {
                        g2.setColor(new Color(239, 68, 68, 200)); // Red for traffic/high cost
                        g2.setStroke(new BasicStroke(3));
                    } else {
                        g2.setColor(new Color(100, 116, 139)); // Slate Gray for normal
                        g2.setStroke(new BasicStroke(2));
                    }
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                    // Draw Weight Badges
                    String weightStr = String.format("%.1f", r.currentCost);
                    int midX = (p1.x + p2.x) / 2;
                    int midY = (p1.y + p2.y) / 2;

                    g2.setColor(new Color(15, 23, 42)); // Dark Badge background
                    g2.fillRoundRect(midX - 15, midY - 10, 30, 20, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 11));

                    // Center text logic
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(weightStr);
                    g2.drawString(weightStr, midX - (textWidth/2), midY + 4);
                }
            }

            // 2. Draw Nodes
            for (Map.Entry<String, Point> entry : nodeCoords.entrySet()) {
                Point p = entry.getValue();

                if (currentPath.contains(entry.getKey())) {
                    g2.setColor(new Color(56, 189, 248)); // Active Node Blue
                    g2.fillOval(p.x - 18, p.y - 18, 36, 36);
                } else {
                    g2.setColor(new Color(71, 85, 105)); // Inactive Gray
                    g2.fillOval(p.x - 15, p.y - 15, 30, 30);
                }

                g2.setColor(Color.WHITE);
                g2.fillOval(p.x - 5, p.y - 5, 10, 10);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(entry.getKey());
                g2.drawString(entry.getKey(), p.x - (textWidth/2), p.y - 22);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SmartCityFinalGUI().setVisible(true);
        });
    }
}
