import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class SimulationHandler implements HttpHandler {

    private final KDTree tree;
    private final int totalFlightsCount;

    public SimulationHandler(KDTree tree, int totalFlightsCount) {
        this.tree = tree;
        this.totalFlightsCount = totalFlightsCount;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        int k            = Main.parseParam(exchange.getRequestURI().getQuery(), "k", 5, 1, 50);
        double[] userVec = Main.randomUserVector();
        Flight[] results = tree.getRecommendations(userVec, k);
        int visited      = tree.getLastVisitedCount();

        String json  = buildJson(userVec, results, visited);
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private String buildJson(double[] userVec, Flight[] results, int visited) {
        return "{"
                + "\"totalFlights\":"  + totalFlightsCount               + ","
                + "\"visitedNodes\":"  + visited                         + ","
                + "\"userVector\":"    + buildUserVectorJson(userVec)     + ","
                + "\"flights\":"       + buildFlightsJson(results, userVec)
                + "}";
    }

    private String buildUserVectorJson(double[] userVec) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < userVec.length; i++) {
            sb.append(userVec[i]);
            if (i < userVec.length - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private String buildFlightsJson(Flight[] results, double[] userVec) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < results.length; i++) {
            if (results[i] != null) {
                Flight f     = results[i];
                int score    = Main.calcMatchScore(tree, f, userVec);
                String comma = (i < results.length - 1 && results[i + 1] != null) ? "," : "";

                sb.append("{")
                        .append("\"airline\":\""     ).append(f.getAirline()    ).append("\",")
                        .append("\"destination\":\"" ).append(f.getDestination()).append("\",")
                        .append("\"description\":\"" ).append(f.toString()      ).append("\",")
                        .append("\"matchScore\":"    ).append(score             )
                        .append("}").append(comma);
            }
        }
        return sb.append("]").toString();
    }
}
