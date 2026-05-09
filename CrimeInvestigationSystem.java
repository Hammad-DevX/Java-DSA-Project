import java.util.*;

// --- 1. CASE CLASS (Implements Comparable for Priority Queue) ---
class CrimeCase implements Comparable<CrimeCase> {
    int caseId;
    String description;
    int severity; // 1: High (Murder), 2: Medium (Robbery), 3: Low (Theft)
    String investigator;
    boolean isSolved;

    LinkedList<String> suspects;
    Stack<String> evidence;
    CrimeCase linkedCase;

    public CrimeCase(int caseId, String description, int severity) {
        this.caseId = caseId;
        this.description = description;
        this.severity = severity;
        this.investigator = "Unassigned";
        this.isSolved = false;
        this.suspects = new LinkedList<>();
        this.evidence = new Stack<>();
        this.linkedCase = null;
    }

    // Custom Comparison to sort by Severity (1 is highest priority)
    @Override
    public int compareTo(CrimeCase other) {
        return Integer.compare(this.severity, other.severity);
    }

    public void displayCaseDetails() {
        System.out.println("---------------------------------");
        System.out.println("Case ID     : #" + caseId + " | Priority Level: " + getSeverityString());
        System.out.println("Description : " + description);
        System.out.println("Investigator: " + investigator);
        System.out.println("Status      : " + (isSolved ? "✅ Solved" : "⏳ Active"));
        System.out.println("Suspects    : " + (suspects.isEmpty() ? "None" : suspects));
        System.out.println("Evidence    : " + (evidence.isEmpty() ? "None" : evidence));
        System.out.println("---------------------------------");
    }

    private String getSeverityString() {
        if (severity == 1) return "🚨 HIGH";
        if (severity == 2) return "⚠️ MEDIUM";
        return "🟢 LOW";
    }
}

// --- 2. MAIN SYSTEM CLASS ---
public class CrimeInvestigationSystem {

    private ArrayList<CrimeCase> allCasesDatabase = new ArrayList<>();
    // DSA: PriorityQueue for handling urgent cases first
    private PriorityQueue<CrimeCase> pendingCasesQueue = new PriorityQueue<>();
    // DSA: HashMap to track frequency of suspects across all cases
    private HashMap<String, Integer> suspectDatabase = new HashMap<>();

    private int caseCounter = 101;
    private Scanner scanner = new Scanner(System.in);

    // --- VALIDATION HELPER METHODS ---
    private int getValidIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[Error] Numbers only! Please try again.");
            }
        }
    }

    private int getValidIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = getValidIntInput(prompt);
            if (value >= min && value <= max) return value;
            System.out.println("[Error] Input must be between " + min + " and " + max + ".");
        }
    }

    private String getNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("[Error] Input cannot be blank!");
        }
    }

    // --- FEATURE 1: REPORT CRIME (PRIORITY QUEUE) ---
    public void reportCrime() {
        System.out.println("\n--- 📝 REPORT NEW CRIME ---");
        String desc = getNonEmptyString("Enter Crime Description: ");
        System.out.println("Select Severity Level:");
        System.out.println("1 = High (Murder/Terrorism)\n2 = Medium (Robbery/Assault)\n3 = Low (Theft/Fraud)");
        int severity = getValidIntInRange("Enter level (1-3): ", 1, 3);

        CrimeCase newCase = new CrimeCase(caseCounter++, desc, severity);
        allCasesDatabase.add(newCase);
        pendingCasesQueue.add(newCase); // PriorityQueue auto-sorts it

        System.out.println("[Success] Crime registered! Case ID: " + newCase.caseId);
    }

    // --- FEATURE 2: START INVESTIGATION ---
    public void startInvestigation() {
        System.out.println("\n--- 🔍 START INVESTIGATION ---");
        if (pendingCasesQueue.isEmpty()) {
            System.out.println("[Info] No pending cases waiting.");
            return;
        }

        // PriorityQueue populaties the most severe case first
        CrimeCase activeCase = pendingCasesQueue.poll();
        System.out.println("Assigned Case Details -> ID: #" + activeCase.caseId + " | Priority: Level " + activeCase.severity);

        String investigator = getNonEmptyString("Assign Investigator Name: ");
        activeCase.investigator = investigator;
        System.out.println("[Success] Officer " + investigator + " is handling Case #" + activeCase.caseId);
    }

    private CrimeCase findCaseById(int id) {
        for (CrimeCase c : allCasesDatabase) {
            if (c.caseId == id) return c;
        }
        return null;
    }

    // --- FEATURE 3: UPDATE CASE (SUSPECTS & EVIDENCE) ---
    public void updateCaseData() {
        System.out.println("\n--- 🔄 UPDATE CASE DATA ---");
        int id = getValidIntInput("Enter Case ID to update: ");
        CrimeCase c = findCaseById(id);

        if (c == null) {
            System.out.println("[Error] Case not found!");
            return;
        }
        if (c.isSolved) {
            System.out.println("[Warning] This case is solved. Cannot modify.");
            return;
        }

        System.out.println("1. Add Suspect\n2. Remove Suspect (Exonerate)\n3. Add Evidence\n4. Undo Last Evidence");
        int choice = getValidIntInRange("Choice (1-4): ", 1, 4);

        switch (choice) {
            case 1:
                String suspect = getNonEmptyString("Enter Suspect Name: ").toLowerCase();
                c.suspects.add(suspect);
                suspectDatabase.put(suspect, suspectDatabase.getOrDefault(suspect, 0) + 1);
                System.out.println("[Success] Suspect added.");
                break;
            case 2:
                String removeSuspect = getNonEmptyString("Enter Suspect Name to remove: ").toLowerCase();
                if (c.suspects.remove(removeSuspect)) {
                    // Update HashMap
                    int count = suspectDatabase.get(removeSuspect);
                    if (count == 1) suspectDatabase.remove(removeSuspect);
                    else suspectDatabase.put(removeSuspect, count - 1);
                    System.out.println("[Success] Suspect cleared from this case.");
                } else {
                    System.out.println("[Error] Suspect not found in this case.");
                }
                break;
            case 3:
                c.evidence.push(getNonEmptyString("Enter Evidence Detail: "));
                System.out.println("[Success] Evidence logged.");
                break;
            case 4:
                if (!c.evidence.isEmpty()) {
                    System.out.println("[Undo] Removed evidence: " + c.evidence.pop());
                } else {
                    System.out.println("[Error] No evidence to undo!");
                }
                break;
        }
    }

    // --- FEATURE 4: LINK CASES ---
    public void linkCases() {
        System.out.println("\n--- 🔗 LINK CASES ---");
        int parentId = getValidIntInput("Enter Parent Case ID: ");
        int childId = getValidIntInput("Enter Sub-case ID: ");

        if (parentId == childId) {
            System.out.println("[Error] Cannot link a case to itself!");
            return;
        }

        CrimeCase parent = findCaseById(parentId);
        CrimeCase child = findCaseById(childId);

        if (parent != null && child != null) {
            parent.linkedCase = child;
            System.out.println("[Success] Cases linked!");
        } else {
            System.out.println("[Error] Invalid Case IDs.");
        }
    }

    // --- FEATURE 5: TRACE HISTORY (RECURSION) ---
    public void traceHistoryMenu() {
        System.out.println("\n--- 📂 TRACE CASE HISTORY ---");
        int id = getValidIntInput("Enter Starting Case ID: ");
        CrimeCase startCase = findCaseById(id);

        if (startCase == null) {
            System.out.println("[Error] Case not found.");
            return;
        }
        System.out.println("\n[ Linkage Map ]");
        traceRecursively(startCase, 1, new HashSet<>());
    }

    private void traceRecursively(CrimeCase c, int depth, HashSet<Integer> visited) {
        if (c == null) return;
        String indent = "  ".repeat(depth - 1);
        if (visited.contains(c.caseId)) {
            System.out.println(indent + "-> [CYCLE] Loops back to Case #" + c.caseId);
            return;
        }
        visited.add(c.caseId);
        System.out.println(indent + "-> Case #" + c.caseId + ": " + c.description);
        traceRecursively(c.linkedCase, depth + 1, visited);
    }

    // --- FEATURE 6: SEARCH BY KEYWORD ---
    public void searchCases() {
        System.out.println("\n--- 🔎 SEARCH CASES ---");
        String keyword = getNonEmptyString("Enter keyword (e.g., Bank, Murder): ").toLowerCase();
        boolean found = false;

        for (CrimeCase c : allCasesDatabase) {
            if (c.description.toLowerCase().contains(keyword)) {
                c.displayCaseDetails();
                found = true;
            }
        }
        if (!found) System.out.println("[Info] No cases match that keyword.");
    }

    // --- FEATURE 7: SUSPECT FREQUENCY TRACKER ---
    public void viewSerialSuspects() {
        System.out.println("\n--- 🕵️ SERIAL SUSPECTS TRACKER ---");
        if (suspectDatabase.isEmpty()) {
            System.out.println("[Info] No suspects on record.");
            return;
        }
        System.out.println(String.format("%-20s | %s", "Suspect Name", "Involved Cases"));
        System.out.println("-----------------------------------");
        for (Map.Entry<String, Integer> entry : suspectDatabase.entrySet()) {
            if (entry.getValue() > 1) { // Only show serial suspects
                System.out.println(String.format("%-20s | %d times", entry.getKey(), entry.getValue()));
            }
        }
        System.out.println("\n(Note: Suspects involved in only 1 case are hidden from this view)");
    }

    // --- FEATURE 8: SOLVE CASE ---
    public void solveCase() {
        System.out.println("\n--- ✅ CLOSE CASE ---");
        int id = getValidIntInput("Enter Case ID: ");
        CrimeCase c = findCaseById(id);

        if (c != null) {
            if (c.isSolved) System.out.println("[Info] Already solved!");
            else {
                c.isSolved = true;
                System.out.println("[Success] Case closed!");
            }
        } else {
            System.out.println("[Error] Case not found.");
        }
    }

    // --- MAIN METHOD / MENU ---
    public static void main(String[] args) {
        CrimeInvestigationSystem system = new CrimeInvestigationSystem();

        while (true) {
            System.out.println("\n==============================================");
            System.out.println("   CRIME INVESTIGATION SYSTEM (CIS - PRO)   ");
            System.out.println("==============================================");
            System.out.println("1. Report Crime (Priority Queue)");
            System.out.println("2. Start Investigation (Fetch Highest Priority)");
            System.out.println("3. Update Case Data (Suspects/Evidence)");
            System.out.println("4. Link Case to Another");
            System.out.println("5. Trace Case History");
            System.out.println("6. Search Cases by Keyword");
            System.out.println("7. View Serial Suspects (HashMap Analytics)");
            System.out.println("8. Mark Case as Solved");
            System.out.println("9. Exit System");

            int choice = system.getValidIntInRange("\nSelect Option (1-9): ", 1, 9);

            switch (choice) {
                case 1: system.reportCrime(); break;
                case 2: system.startInvestigation(); break;
                case 3: system.updateCaseData(); break;
                case 4: system.linkCases(); break;
                case 5: system.traceHistoryMenu(); break;
                case 6: system.searchCases(); break;
                case 7: system.viewSerialSuspects(); break;
                case 8: system.solveCase(); break;
                case 9:
                    System.out.println("\n--- 🚪 EXITING ---");
                    System.out.println("Saving records... Shutting down.");
                    System.exit(0);
            }
        }
    }
}
