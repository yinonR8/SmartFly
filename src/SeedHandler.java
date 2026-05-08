import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class SeedHandler implements HttpHandler {

    private final DataBaseManager db;

    public SeedHandler(DataBaseManager db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        int count = Main.parseParam(exchange.getRequestURI().getQuery(), "count", 1000, 1, 100000);

        db.flightRandomCreation(count);  // insert into DB
        Main.rebuildTree();              // reload + renormalize + rebuild KD-tree

        String json  = "{\"inserted\":" + count + ",\"totalFlights\":" + Main.totalFlightsCount + "}";
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
