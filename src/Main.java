import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Welcome to SmartFly Recommendation System ---");

        // 1. משיכת נתונים מה-SQL
        System.out.println("Connecting to Database...");
        DataBaseManager dbManager = new DataBaseManager();
        List<Flight> allFlights = dbManager.getAllFlights();

        if(allFlights.isEmpty()) {
            System.out.println("Error: No flights found in the database. Please check your SQL connection.");
            return;
        }

        System.out.println("Found " + allFlights.size() + " flights. Normalizing data...");
        dbManager.normalizeFlights(allFlights);

        // 2. בניית עץ ה-KD
        System.out.println("Building KD-Tree...");
        KDTree tree = new KDTree();
        tree.Build(new ArrayList<>(allFlights));
        System.out.println("KD-Tree built successfully!");

        // 3. הגדרת משאלת המשתמש
        // [מחיר, זמן טיסה, שעת המראה, עצירות, דירוג, אורבני, טבע, חוף, היסטוריה]
        double[] userPreference = generateRandomUserPreference();
        int k = 5; // מבקשים את 5 הטיסות הטובות ביותר

        System.out.println("\nSearching for top " + k + " best flights for the user...");

        Flight[] recommendations = tree.getRecommendations(userPreference, k);

        // 5. הדפסת התוצאות למסך
        System.out.println("\n--- TOP " + k + " RECOMMENDATIONS ---");
        for (int i = 0; i < recommendations.length; i++) {
            if (recommendations[i] != null) {
                System.out.println((i + 1) + ". " + recommendations[i].toString());
            }
        }
    }
    public static double[] generateRandomUserPreference() {
        int numDimensions = Flight.FlightDimension.values().length;
        double[] randomPref = new double[numDimensions];

        System.out.println("\n--- Generating Random User Preferences ---");

        for (int i = 0; i < numDimensions; i++) {
            randomPref[i] = Math.round(Math.random() * 100.0) / 100.0;
        }

        System.out.println("Price: " + randomPref[Flight.FlightDimension.PRICE.ordinal()] +
                ", Duration: " + randomPref[Flight.FlightDimension.DURATION_MINS.ordinal()] +
                ", Departure: " + randomPref[Flight.FlightDimension.DEPARTURE_HOUR.ordinal()] +
                ", Stops: " + randomPref[Flight.FlightDimension.CONNECTIONS.ordinal()] +
                "\nRating: " + randomPref[Flight.FlightDimension.AIRLINE_RATING.ordinal()] +
                ", Urban: " + randomPref[Flight.FlightDimension.EXP_URBAN.ordinal()] +
                ", Nature: " + randomPref[Flight.FlightDimension.EXP_NATURE.ordinal()] +
                ", Beach: " + randomPref[Flight.FlightDimension.EXP_BEACH.ordinal()] +
                ", History: " + randomPref[Flight.FlightDimension.EXP_HISTORY.ordinal()]);

        return randomPref;
    }
}