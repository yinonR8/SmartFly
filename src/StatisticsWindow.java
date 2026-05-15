import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * חלון נפרד להצגת סטטיסטיקה על ביצועי האלגוריתם.
 * מבצע N הרצות עם וקטורי משתמש אקראיים שונים ומציג ממוצעים.
 */
public class StatisticsWindow {

    private final KDTree tree;
    private final int totalFlightsCount;
    private final int k;
    private final Stage stage;
    private TextArea outputArea;
    private Label statusLabel;

    /**
     * @param tree עץ ה-KD שכבר נבנה במסך הראשי
     * @param totalFlightsCount סך הטיסות במסד הנתונים
     * @param k מספר ההמלצות לבקש בכל הרצה
     */
    public StatisticsWindow(KDTree tree, int totalFlightsCount, int k) {
        this.tree = tree;
        this.totalFlightsCount = totalFlightsCount;
        this.k = k;
        this.stage = new Stage();
        this.stage.setTitle("SmartFly - סטטיסטיקת אלגוריתם");
        this.stage.setScene(new Scene(buildRoot(), 700, 500));
    }

    public void show() {
        stage.show();
    }

    /** בונה את המסך של חלון הסטטיסטיקה */
    private VBox buildRoot() {
        // כותרת
        Label title = new Label("סטטיסטיקת ביצועי האלגוריתם");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // מידע על הגדרות נוכחיות
        Label info = new Label(String.format(
                "מספר טיסות במסד: %d   |   מספר המלצות לבקש (K): %d",
                totalFlightsCount, k
        ));
        info.setStyle("-fx-text-fill: #555555;");

        // כפתור הרצת 100 פעמים
        Button run100Btn = new Button("הרצת האלגוריתם 100 פעמים");
        run100Btn.setStyle("-fx-font-size: 13px; -fx-padding: 8 20 8 20;");
        run100Btn.setOnAction(e -> runBatch(100));

        // סטטוס הרצה
        statusLabel = new Label("ההרצה עוד לא התחילה");
        statusLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");

        HBox buttonRow = new HBox(15, run100Btn, statusLabel);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        // שדה תצוגה לתוצאות
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setText("לחץ על הכפתור למעלה כדי להריץ סימולציה...");
        outputArea.setStyle(
                "-fx-font-family: 'Consolas', monospace;" +
                        "-fx-font-size: 13px;"
        );
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        VBox root = new VBox(12, title, info, buttonRow, outputArea);
        root.setPadding(new Insets(15));
        return root;
    }

    /**
     * מריץ N סימולציות עם וקטורי משתמש אקראיים שונים,
     * צובר נתונים על ביצועי האלגוריתם ומציג ממוצעים.
     */
    private void runBatch(int n) {
        statusLabel.setText("רץ... אנא המתן");

        // משתנים לצבירת סטטיסטיקה
        double sumTopMatchScore = 0;    // סכום אחוזי ההתאמה של הטיסה הטובה ביותר
        long sumVisitedNodes = 0;       // סכום הצמתים שנבדקו בכל ההרצות
        long sumTimeNanos = 0;          // סכום זמני ריצה
        int bestScoreEver = 0;          // האחוז הגבוה ביותר שנמצא
        int worstTopScore = 100;        // האחוז הגרוע ביותר של "הטיסה הטובה ביותר"

        // ביצוע N הרצות
        for (int run = 0; run < n; run++) {
            double[] userVec = randomUserVector();

            // מדידת זמן הריצה של אלגוריתם ההמלצה
            long startTime = System.nanoTime();
            Flight[] results = tree.getRecommendations(userVec, k);
            long endTime = System.nanoTime();

            sumTimeNanos += (endTime - startTime);
            sumVisitedNodes += tree.getLastVisitedCount();

            // חישוב אחוז ההתאמה של הטיסה הטובה ביותר (הראשונה ברשימה)
            int topScore = 0;
            for (Flight f : results) {
                if (f != null) {
                    topScore = calcMatchScore(f, userVec);
                    break;
                }
            }

            sumTopMatchScore += topScore;
            if (topScore > bestScoreEver) bestScoreEver = topScore;
            if (topScore < worstTopScore) worstTopScore = topScore;
        }

        // חישוב ממוצעים סופיים
        double avgTopScore = sumTopMatchScore / n;
        double avgVisited = (double) sumVisitedNodes / n;
        double avgEfficiency = 100.0 * (1.0 - avgVisited / totalFlightsCount);
        double avgTimeMs = (sumTimeNanos / 1_000_000.0) / n;

        // בניית דוח מסכם
        String report = String.format(
                "═══════════ דוח סימולציה רבת-פעמים (N = %d הרצות) ═══════════%n%n" +
                        "  ▸ ממוצע אחוז התאמה - הטיסה הטובה ביותר:    %.2f%%%n%n" +
                        "  ▸ אחוז התאמה הגבוה ביותר שנמצא אי-פעם:     %d%%%n%n" +
                        "  ▸ אחוז התאמה הנמוך ביותר (worst case):      %d%%%n%n" +
                        "  ▸ ממוצע צמתים שנבדקו:                       %.1f / %d (%.2f%%)%n%n" +
                        "  ▸ ממוצע יעילות גיזום:                       %.2f%%%n%n" +
                        "  ▸ ממוצע זמן ריצה לסימולציה:                %.3f ms%n%n" +
                        "═══════════════════════════════════════════════════════════════",
                n, avgTopScore, bestScoreEver, worstTopScore,
                avgVisited, totalFlightsCount, (avgVisited / totalFlightsCount * 100),
                avgEfficiency, avgTimeMs
        );

        outputArea.setText(report);
        statusLabel.setText("ההרצה הסתיימה בהצלחה ✓");
        statusLabel.setStyle("-fx-text-fill: #2d8e2d; -fx-font-weight: bold;");
    }

    // ---------- פונקציות עזר (זהות לאלה ב-Main) ----------
    private double[] randomUserVector() {
        int dims = Flight.FlightDimension.values().length;
        double[] vec = new double[dims];
        for (int i = 0; i < dims; i++) {
            vec[i] = Math.round(Math.random() * 100.0) / 100.0;
        }
        return vec;
    }

    private int calcMatchScore(Flight flight, double[] userVec) {
        double dist = tree.calculateEuclideanDistance(flight, userVec);
        return (int) Math.max(0, Math.round(100 - (dist * 30)));
    }
}