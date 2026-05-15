import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    // ---------- Model (אותה ליבה כמו אצלך) ----------
    private KDTree tree;
    private int totalFlightsCount;

    // ---------- View (רכיבי המסך) ----------
    private Spinner<Integer> kSpinner;
    private Label totalLabel;
    private Label visitedLabel;
    private Label efficiencyLabel;
    private BarChart<String, Number> userChart;
    private ListView<String> resultsList;

    @Override
    public void start(Stage stage) {
        // === שלב 1: טעינת נתונים ובניית עץ (זהה ל-main הישן) ===
        DataBaseManager db = new DataBaseManager();
        List<Flight> flights = db.getAllFlights();
        if (flights.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "אין טיסות במסד הנתונים").showAndWait();
            return;
        }
        totalFlightsCount = flights.size();
        db.normalizeFlights(flights);
        tree = new KDTree();
        tree.Build(new ArrayList<>(flights));

        // === שלב 2: בניית הממשק ===
        stage.setTitle("SmartFly - מערכת המלצות טיסות");
        stage.setScene(new Scene(buildRoot(), 900, 600));
        stage.show();
    }

    /** בונה את כל מבנה המסך - Top-down כמו במחוון */
    private BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        root.setTop(buildControlPanel());
        root.setCenter(buildCenter());
        root.setBottom(buildStatsBar());
        root.setPadding(new Insets(10));
        return root;
    }

    /** סרגל עליון: בחירת K + כפתור סימולציה + כפתור פתיחת חלון סטטיסטיקה */
    private HBox buildControlPanel() {
        Label kLabel = new Label("מספר המלצות:");
        kSpinner = new Spinner<>(1, 20, 5);

        Button simulateBtn = new Button("הרץ סימולציה");
        simulateBtn.setOnAction(e -> runSimulation());

        // כפתור חדש: פתיחת חלון סטטיסטיקה נפרד
        Button statsBtn = new Button("סטטיסטיקה");
        statsBtn.setOnAction(e -> openStatisticsWindow());

        HBox box = new HBox(10, kLabel, kSpinner, simulateBtn, statsBtn);
        box.setPadding(new Insets(0, 0, 10, 0));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /** מרכז המסך: גרף וקטור משתמש משמאל, רשימת תוצאות מימין */
    private HBox buildCenter() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis(0, 1, 0.1);
        userChart = new BarChart<>(x, y);
        userChart.setTitle("וקטור העדפות המשתמש");
        userChart.setLegendVisible(false);
        userChart.setAnimated(false);

        resultsList = new ListView<>();

        HBox center = new HBox(10, userChart, resultsList);
        HBox.setHgrow(userChart, Priority.ALWAYS);
        HBox.setHgrow(resultsList, Priority.ALWAYS);
        return center;
    }

    /** סרגל תחתון: נתונים סטטיסטיים */
    private HBox buildStatsBar() {
        totalLabel = new Label("סה\"כ טיסות: " + totalFlightsCount);
        visitedLabel = new Label("צמתים שנבדקו: -");
        efficiencyLabel = new Label("יעילות גיזום: -");
        HBox box = new HBox(30, totalLabel, visitedLabel, efficiencyLabel);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    // ---------- Controller: סימולציה בודדת ----------
    private void runSimulation() {
        int k = kSpinner.getValue();
        double[] userVec = randomUserVector();
        Flight[] results = tree.getRecommendations(userVec, k);
        int visited = tree.getLastVisitedCount();

        // עדכון גרף וקטור המשתמש
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] names = {"מחיר", "משך", "שעה", "עצירות", "דירוג",
                "עירוני", "טבע", "חוף", "היסטוריה"};
        for (int i = 0; i < userVec.length; i++) {
            series.getData().add(new XYChart.Data<>(names[i], userVec[i]));
        }
        userChart.getData().setAll(series);

        // עדכון רשימת ההמלצות
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Flight f : results) {
            if (f != null) {
                int score = calcMatchScore(f, userVec);
                items.add(score + "% | " + f.toString());
            }
        }
        resultsList.setItems(items);

        // עדכון סטטיסטיקה תחתונה
        double efficiency = 100.0 * (1.0 - (double) visited / totalFlightsCount);
        visitedLabel.setText("צמתים שנבדקו: " + visited);
        efficiencyLabel.setText(String.format("יעילות גיזום: %.1f%%", efficiency));
    }

    /** פתיחת חלון סטטיסטיקה נפרד */
    private void openStatisticsWindow() {
        StatisticsWindow statsWindow = new StatisticsWindow(tree, totalFlightsCount, kSpinner.getValue());
        statsWindow.show();
    }

    // ---------- פונקציות עזר ----------
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

    public static void main(String[] args) {
        launch(args);
    }
}