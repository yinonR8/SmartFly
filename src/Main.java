import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static KDTree tree;
    static DataBaseManager db;
    static int totalFlightsCount;

    public static void main(String[] args) throws IOException {
        db = new DataBaseManager();
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
        server.createContext("/api/simulate", new SimulationHandler(tree, totalFlightsCount));
        server.createContext("/api/seed",     new SeedHandler(db));
        server.setExecutor(null);
        server.start();

        System.out.println("Server running on http://localhost:8080");
    }

    static double[] randomUserVector() {
        int dims = Flight.FlightDimension.values().length;
        double[] vec = new double[dims];
        for (int i = 0; i < dims; i++) {
            vec[i] = Math.round(Math.random() * 100.0) / 100.0;
        }
        return vec;
    }

    static synchronized void rebuildTree() {
        List<Flight> flights = db.getAllFlights();
        totalFlightsCount = flights.size();
        db.normalizeFlights(flights);
        tree = new KDTree();
        tree.Build(new ArrayList<>(flights));
        System.out.println("KD-Tree rebuilt with " + totalFlightsCount + " flights.");
    }

    static int parseParam(String query, String key, int defaultVal) {
        if (query != null && query.contains(key + "=")) {
            try {
                for (String part : query.split("&")) {
                    if (part.startsWith(key + "=")) {
                        return Integer.parseInt(part.split("=", 2)[1]);
                    }
                }
            } catch (NumberFormatException e) {
                // fall through to default
            }
        }
        return defaultVal;
    }

    static int calcMatchScore(KDTree tree, Flight flight, double[] userVec) {
        double dist = tree.calculateEuclideanDistance(flight, userVec);
        return (int) Math.max(0, Math.round(100 - (dist * 30)));
    }

    static int parseParam(String query, String key, int defaultVal, int min, int max) {
        int value = parseParam(query, key, defaultVal);
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
