import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static KDTree tree;
    private static int totalFlightsCount;

    public static void main(String[] args) throws IOException {
        DataBaseManager db = new DataBaseManager();
        List<Flight> flights = db.getAllFlights();

        if (flights.isEmpty()) {
            System.out.println("No flights found. Check DB connection.");
            return;
        }

        totalFlightsCount = flights.size();
        db.normalizeFlights(flights);

        tree = new KDTree();
        tree.Build(new ArrayList<>(flights));

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/simulate", new SimulationHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("Server running on http://localhost:8080");
    }

    private static double[] randomUserVector() {
        int dims = Flight.FlightDimension.values().length;
        double[] vec = new double[dims];
        for (int i = 0; i < dims; i++) {
            vec[i] = Math.round(Math.random() * 100.0) / 100.0;
        }
        return vec;
    }

    /**
     * קורא את ערך K מה-URL של הבקשה.
     * אם אין ערך תקין, מחזיר 5 כברירת מחדל.
     */
    private static int parseK(String query) {
        int k = 5;
        if (query != null && query.contains("k=")) {
            try {
                k = Integer.parseInt(query.split("k=")[1]);
            } catch (NumberFormatException e) {
                k = 5;
            }
        }
        return k;
    }

    /**
     * מחשב ציון התאמה באחוזים בין טיסה למשתמש.
     * מרחק 0 = 100%, ככל שהמרחק גדל האחוז יורד.
     */
    private static int calcMatchScore(Flight flight, double[] userVec) {
        double dist = tree.calculateEuclideanDistance(flight, userVec);
        return (int) Math.max(0, Math.round(100 - (dist * 30)));
    }

    // -----------------------------------------------------------
    // SimulationHandler: מטפל בכל בקשה שמגיעה מהדפדפן
    // -----------------------------------------------------------
    static class SimulationHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            int k            = parseK(exchange.getRequestURI().getQuery());
            double[] userVec = randomUserVector();
            Flight[] results = tree.getRecommendations(userVec, k);
            int visited      = tree.getLastVisitedCount();

            String json  = buildJson(userVec, results, visited);
            byte[] bytes = json.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }

        /**
         * בונה את תשובת ה-JSON המלאה הכוללת:
         * סך הטיסות, כמות הצמתים שנבדקו, וקטור המשתמש, ורשימת הטיסות.
         */
        private String buildJson(double[] userVec, Flight[] results, int visited) {
            String userVectorJson = buildUserVectorJson(userVec);
            String flightsJson   = buildFlightsJson(results, userVec);

            return "{"
                    + "\"totalFlights\":"  + totalFlightsCount + ","
                    + "\"visitedNodes\":"  + visited           + ","
                    + "\"userVector\":"    + userVectorJson    + ","
                    + "\"flights\":"       + flightsJson
                    + "}";
        }

        /**
         * בונה את ה-JSON של וקטור המשתמש בפורמט מערך.
         * לדוגמה: [0.12, 0.75, 0.33, ...]
         */
        private String buildUserVectorJson(double[] userVec) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < userVec.length; i++) {
                sb.append(userVec[i]);
                if (i < userVec.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        /**
         * בונה את ה-JSON של רשימת הטיסות המומלצות.
         * כל טיסה כוללת: חברה, יעד, תיאור וציון התאמה.
         */
        private String buildFlightsJson(Flight[] results, double[] userVec) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < results.length; i++) {
                if (results[i] != null) {
                    Flight f     = results[i];
                    int score    = calcMatchScore(f, userVec);
                    String comma = (i < results.length - 1 && results[i + 1] != null) ? "," : "";

                    sb.append("{")
                            .append("\"airline\":\""      ).append(f.getAirline()    ).append("\",")
                            .append("\"destination\":\"" ).append(f.getDestination()).append("\",")
                            .append("\"description\":\"" ).append(f.toString()      ).append("\",")
                            .append("\"matchScore\":"    ).append(score             )
                            .append("}").append(comma);
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }
}