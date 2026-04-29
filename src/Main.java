import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {

    // משתנים סטטיים כדי שהשרת יוכל לגשת אליהם תמיד
    private static KDTree tree;
    private static int totalFlightsCount;

    public static void main(String[] args) throws IOException {
        System.out.println("--- Booting SmartFly Server ---");

        // 1. שלב ההכנה (Preprocessing) קורה פעם אחת בלבד כשהשרת עולה!
        System.out.println("Connecting to Database and loading flights...");
        DataBaseManager dbManager = new DataBaseManager();
        List<Flight> allFlights = dbManager.getAllFlights();

        if (allFlights.isEmpty()) {
            System.out.println("Error: No flights found. Check SQL connection.");
            return;
        }

        totalFlightsCount = allFlights.size();
        System.out.println("Loaded " + totalFlightsCount + " flights. Normalizing...");
        dbManager.normalizeFlights(allFlights);

        System.out.println("Building KD-Tree...");
        tree = new KDTree();
        tree.Build(new ArrayList<>(allFlights));
        System.out.println("KD-Tree built successfully!");

        // 2. הפעלת השרת על פורט 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // יצירת הנתיב שאליו המסך שלנו יפנה
        server.createContext("/api/simulate", new SimulationHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("\n✅ Server is running successfully on: http://localhost:8080");
        System.out.println("Waiting for the UI to send requests...");
    }

    // פונקציית העזר להגרלת המשתמש שלנו
    public static double[] generateRandomUserPreference() {
        int numDimensions = Flight.FlightDimension.values().length;
        double[] randomPref = new double[numDimensions];
        for (int i = 0; i < numDimensions; i++) {
            randomPref[i] = Math.round(Math.random() * 100.0) / 100.0;
        }
        return randomPref;
    }

    // זו המחלקה שמטפלת בבקשות שמגיעות מהמסך (הדפדפן)
    static class SimulationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // מאפשר לדפדפן (HTML) לגשת לשרת גם אם הם לא באותה תיקייה (CORS)
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // מגרילים משתמש חדש
            double[] userPref = generateRandomUserPreference();

            // מתחילים למדוד זמן ביצוע מנוע החיפוש!
            long startTime = System.currentTimeMillis();

            // משיכת הערך K מהבקשה של הדפדפן (ברירת מחדל 5 אם יש שגיאה)
            int k = 5;
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("k=")) {
                try {
                    k = Integer.parseInt(query.split("k=")[1]);
                } catch (Exception e) {
                    System.out.println("Invalid K received, using default 5.");
                }
            }

            // מפעילים את ה-KNN בעץ ה-KD לחפש את K הטובות ביותר
            Flight[] results = tree.getRecommendations(userPref, k);
            int visitedNodes = tree.getLastVisitedCount(); // שולפים את כמות הצמתים שנבדקו באמת!
            long endTime = System.currentTimeMillis();
            long executionTime = Math.max(1, endTime - startTime);

            // בונים את התשובה (מעבירים גם את המונה החדש)
            String jsonResponse = buildJson(userPref, results, executionTime, visitedNodes);
            // שולחים את התשובה למסך
            byte[] responseBytes = jsonResponse.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();

            System.out.println("Simulation requested by UI. Search completed in " + executionTime + "ms.");
        }

        // פונקציה קטנה שלוקחת את האובייקטים והופכת אותם לטקסט (JSON)
        private String buildJson(double[] userPref, Flight[] results, long time,int visitedNodes) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"executionTimeMs\":").append(time).append(",");
            json.append("\"totalFlights\":").append(totalFlightsCount).append(",");
            json.append("\"visitedNodes\":").append(visitedNodes).append(","); // הוספנו את המונה ל-JSON

            // וקטור המשתמש
            json.append("\"userVector\":[");
            for (int i = 0; i < userPref.length; i++) {
                json.append(userPref[i]).append(i < userPref.length - 1 ? "," : "");
            }
            json.append("],");

            // הטיסות המנצחות
            json.append("\"flights\":[");
            for (int i = 0; i < results.length; i++) {
                if (results[i] == null) continue;
                Flight f = results[i];

                // --- חישוב האחוז האמיתי! ---
                // לוקחים את המרחק האוקלידי האמיתי מהעץ
                double dist = tree.calculateEuclideanDistance(f, userPref);
                // ממירים לאחוז: מרחק 0 = 100%. ככל שהמרחק גדל, האחוז יורד.
                // הכפלנו ב-30 כדי לתת משקל הגיוני למרחק בסקאלה של 1 עד 100.
                int realMatchScore = (int) Math.max(0, Math.round(100 - (dist * 30)));

                json.append("{");
                json.append("\"id\":").append(f.getFlightID()).append(",");
                json.append("\"airline\":\"").append(f.getAirline()).append("\",");
                json.append("\"destination\":\"").append(f.getDestination()).append("\",");
                json.append("\"description\":\"").append(f.toString()).append("\",");
                json.append("\"matchScore\":").append(realMatchScore); // הוספנו את הציון ל-JSON
                json.append("}");

                if (i < results.length - 1 && results[i + 1] != null) json.append(",");
            }
            json.append("]");
            json.append("}");
            return json.toString();
        }    }
}