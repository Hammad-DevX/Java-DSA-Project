import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FinalLibraryRecommender extends JFrame {

    // ==========================================
    // 🧠 1. THREAD-SAFE CORE ENGINE
    // ==========================================
    private final Map<String, Set<String>> userToBooks = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> globalBookPopularity = new ConcurrentHashMap<>();

    // ==========================================
    // 🎨 2. GUI COMPONENTS
    // ==========================================
    private JTextArea logArea;
    private JList<String> recList;
    private JComboBox<String> userSelector;
    private JLabel statusLabel;
    private ScheduledExecutorService liveTrafficExecutor;

    // Premium Netflix / Dark Mode Theme
    private final Color BG_DARK = new Color(18, 18, 18);
    private final Color PANEL_DARK = new Color(28, 28, 28);
    private final Color ACCENT_RED = new Color(229, 9, 20);
    private final Color TEXT_WHITE = new Color(240, 240, 240);
    private final Color LOG_GREEN = new Color(0, 230, 118);

    public FinalLibraryRecommender() {
        setTitle("🧠 AI Recommendation Engine - Live V2.0");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(BG_DARK);

        setupUI();
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_DARK);

        JLabel titleLabel = new JLabel("🎬 AI COLLABORATIVE FILTERING - LIVE MATRIX", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 26));
        titleLabel.setForeground(ACCENT_RED);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        statusLabel = new JLabel("Status: OFFLINE 🔴  ", SwingConstants.RIGHT);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(5, 5, 5));
        logArea.setForeground(LOG_GREEN);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(createCustomBorder("📡 Live Data Stream (Producer)"));
        add(logScroll, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(BG_DARK);
        rightPanel.setPreferredSize(new Dimension(380, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 15));

        JPanel selectionPanel = new JPanel(new BorderLayout(10, 5));
        selectionPanel.setBackground(BG_DARK);
        JLabel selectLabel = new JLabel("Target User:");
        selectLabel.setForeground(TEXT_WHITE);
        selectLabel.setFont(new Font("Arial", Font.BOLD, 15));

        String[] users = {"Ali (Active)", "Sara (Active)", "Omar (Active)", "Zain (Cold Start - New)"};
        userSelector = new JComboBox<>(users);
        userSelector.setBackground(PANEL_DARK);
        userSelector.setForeground(TEXT_WHITE);
        userSelector.setFont(new Font("Arial", Font.PLAIN, 14));

        selectionPanel.add(selectLabel, BorderLayout.WEST);
        selectionPanel.add(userSelector, BorderLayout.CENTER);
        rightPanel.add(selectionPanel, BorderLayout.NORTH);

        recList = new JList<>(new String[]{"Data pending... Start traffic!"});
        recList.setBackground(PANEL_DARK);
        recList.setForeground(TEXT_WHITE);
        recList.setFont(new Font("Arial", Font.BOLD, 15));

        JScrollPane recScroll = new JScrollPane(recList);
        recScroll.setBorder(createCustomBorder("⭐ Auto-Sync Recommendations"));
        rightPanel.add(recScroll, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        controlPanel.setBackground(BG_DARK);

        JButton btnStart = createStyledButton("▶ START LIVE ENGINE");
        JButton btnStop = createStyledButton("⏹ STOP ENGINE");

        controlPanel.add(btnStart);
        controlPanel.add(btnStop);
        rightPanel.add(controlPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        btnStart.addActionListener(e -> startLiveSimulation());
        btnStop.addActionListener(e -> stopSimulation());
        userSelector.addActionListener(e -> calculateRecommendations());
    }

    private TitledBorder createCustomBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2), title);
        border.setTitleColor(TEXT_WHITE);
        border.setTitleFont(new Font("Consolas", Font.BOLD, 14));
        return border;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT_RED);
        btn.setForeground(TEXT_WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ==========================================
    // ⚙️ 4. CORE ALGORITHM (WITH DATA SPARSITY FIX)
    // ==========================================
    private void addInteraction(String user, String book) {
        userToBooks.computeIfAbsent(user, k -> ConcurrentHashMap.newKeySet()).add(book);
        globalBookPopularity.computeIfAbsent(book, k -> new AtomicInteger(0)).incrementAndGet();
    }

    private void calculateRecommendations() {
        String selectedRaw = (String) userSelector.getSelectedItem();
        if (selectedRaw == null) return;
        String targetUser = selectedRaw.split(" ")[0];

        Vector<String> newDisplayData = new Vector<>();
        Set<String> targetBooks = userToBooks.getOrDefault(targetUser, Collections.emptySet());

        // EDGE CASE 1: EMPTY SYSTEM
        if (globalBookPopularity.isEmpty()) {
            newDisplayData.add("⚠️ No data in system yet.");
            newDisplayData.add("👉 Please click 'Start Live Engine'.");
            recList.setListData(newDisplayData);
            return;
        }

        // EDGE CASE 2: PURE COLD START (User has 0 books)
        if (targetBooks.isEmpty()) {
            newDisplayData.add("❄️ PURE COLD START!");
            newDisplayData.add("Showing Global Trending:");
            newDisplayData.add("--------------------------------");

            List<Map.Entry<String, AtomicInteger>> trending = new ArrayList<>(globalBookPopularity.entrySet());
            trending.sort((a, b) -> b.getValue().get() - a.getValue().get());

            for (int i = 0; i < Math.min(6, trending.size()); i++) {
                newDisplayData.add("🔥 " + trending.get(i).getKey() + " (" + trending.get(i).getValue() + " reads)");
            }
            recList.setListData(newDisplayData);
            return;
        }

        // STANDARD ALG: JACCARD SIMILARITY
        Map<String, Double> bookScores = new HashMap<>();

        for (String otherUser : userToBooks.keySet()) {
            if (otherUser.equals(targetUser)) continue;

            Set<String> otherBooks = userToBooks.get(otherUser);
            Set<String> intersection = new HashSet<>(targetBooks);
            intersection.retainAll(otherBooks);

            if (intersection.isEmpty()) continue;

            Set<String> union = new HashSet<>(targetBooks);
            union.addAll(otherBooks);

            double similarity = (double) intersection.size() / union.size();

            for (String book : otherBooks) {
                if (!targetBooks.contains(book)) {
                    bookScores.put(book, bookScores.getOrDefault(book, 0.0) + similarity);
                }
            }
        }

        // 🔥 THE FIX: DATA SPARSITY FALLBACK 🔥
        // If Jaccard doesn't find matches yet, recommend trending books the user hasn't read!
        if (bookScores.isEmpty()) {
            newDisplayData.add("📉 Sparse Data / Analyzing Tastes...");
            newDisplayData.add("Trending books you haven't read:");
            newDisplayData.add("--------------------------------");

            List<Map.Entry<String, AtomicInteger>> trending = new ArrayList<>(globalBookPopularity.entrySet());
            trending.sort((a, b) -> b.getValue().get() - a.getValue().get());

            int addedCount = 0;
            for (Map.Entry<String, AtomicInteger> entry : trending) {
                if (!targetBooks.contains(entry.getKey())) {
                    newDisplayData.add("💡 " + entry.getKey() + " (" + entry.getValue() + " reads)");
                    addedCount++;
                }
                if (addedCount >= 5) break;
            }

            if (addedCount == 0) {
                newDisplayData.add("You have read all available books!");
            }
            recList.setListData(newDisplayData);
            return;
        }

        // Display Ranked Recommendations
        List<Map.Entry<String, Double>> sortedBooks = new ArrayList<>(bookScores.entrySet());
        sortedBooks.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        newDisplayData.add("🎯 Tailored based on Shared Tastes:");
        newDisplayData.add("--------------------------------");
        for (int i = 0; i < Math.min(8, sortedBooks.size()); i++) {
            newDisplayData.add(String.format("⭐ %s (Match Score: %.2f)",
                    sortedBooks.get(i).getKey(), sortedBooks.get(i).getValue()));
        }

        recList.setListData(newDisplayData);
    }

    // ==========================================
    // 🌐 5. LIVE TRAFFIC (SMART SIMULATOR)
    // ==========================================
    private void startLiveSimulation() {
        if (liveTrafficExecutor != null && !liveTrafficExecutor.isShutdown()) return;

        statusLabel.setText("Status: LIVE & SYNCING 🟢  ");
        statusLabel.setForeground(LOG_GREEN);
        log("🚀 SYSTEM ONLINE: Initiating smart data stream...");

        liveTrafficExecutor = Executors.newSingleThreadScheduledExecutor();

        String[] activeUsers = {"Ali", "Sara", "Omar", "Aisha", "Bilal"};
        String[] techBooks = {"Clean_Code", "Design_Patterns", "Java_Concurrency", "System_Design"};
        String[] fictionBooks = {"Dune", "1984", "The_Hobbit", "Harry_Potter"};
        Random rand = new Random();

        liveTrafficExecutor.scheduleAtFixedRate(() -> {
            String u = activeUsers[rand.nextInt(activeUsers.length)];
            String b;

            // 🔥 THE FIX: BIAS THE TRAFFIC TO CREATE OVERLAPS FASTER 🔥
            // Ali & Sara prefer Tech Books. Omar & Aisha prefer Fiction.
            if (u.equals("Ali") || u.equals("Sara")) {
                b = techBooks[rand.nextInt(techBooks.length)];
            } else if (u.equals("Omar") || u.equals("Aisha")) {
                b = fictionBooks[rand.nextInt(fictionBooks.length)];
            } else {
                // Bilal reads anything
                b = rand.nextBoolean() ? techBooks[rand.nextInt(techBooks.length)] : fictionBooks[rand.nextInt(fictionBooks.length)];
            }

            addInteraction(u, b);

            SwingUtilities.invokeLater(() -> {
                log("[STREAM] User '" + u + "' checked out: " + b);
                calculateRecommendations();
            });
        }, 0, 800, TimeUnit.MILLISECONDS);
    }

    private void stopSimulation() {
        if (liveTrafficExecutor != null) {
            liveTrafficExecutor.shutdown();
            statusLabel.setText("Status: OFFLINE 🔴  ");
            statusLabel.setForeground(Color.GRAY);
            log("🛑 SYSTEM HALTED: Stream closed.");
        }
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new FinalLibraryRecommender().setVisible(true));
    }
}
