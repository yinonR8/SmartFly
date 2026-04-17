import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseManager {

    private final String url = "jdbc:sqlserver://localhost:1433;databaseName=SmartFly;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";

    public List<Flight> getAllFlights()
    {
        ArrayList<Flight> flightList = new ArrayList<>();

        try(Connection conn = DriverManager.getConnection(url); Statement stat = conn.createStatement(); ResultSet res = stat.executeQuery("SELECT * FROM Flights"))
        {
        //רץ כל עוד יש שורה הבאה בטבלה
            while(res.next())
            {
            Flight f = new Flight();

            //שליפת הנתונים העמודות לאובייקט טיסה
            f.setFlightID(res.getInt("FlightId"));
            f.setDestination(res.getString("Destination"));
            f.setAirline(res.getString("AirLine"));
            f.setPrice(res.getDouble("Price"));
            f.setDurationMins(res.getInt("DurationMins"));
            f.setDepartureHour(res.getInt("DepartureHour"));
            f.setConnections(res.getInt("Connections"));
            f.setAirLineRating(res.getDouble("AirLineRating"));

            f.setExpUrban(res.getDouble("ExpUrban"));
            f.setExpNature(res.getDouble("ExpNature"));
            f.setExpBeach(res.getDouble("ExpBeach"));
            f.setExpHistory(res.getDouble("ExpHistory"));

            flightList.add(f);
            }
        }
        catch (SQLException e)
        {
        //אם הייתה שגיאה כלשהי במסד הנתונים
        System.out.println("Database Error");
        e.printStackTrace();
        }
        return flightList;
    }
    // פונקציית הנרמול המרכזית (Min-Max Scaling)
    public void normalizeFlights(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) return;

        double[] mins = new double[9];
        double[] maxs = new double[9];

        // 1. אתחול המערכים: נשים במינימום מספר ענק ובמקסימום מספר פצפון
        for (int i = 0; i < 9; i++) {
            mins[i] = Double.MAX_VALUE;
            maxs[i] = -Double.MAX_VALUE;
        }

        // 2. מעבר על כל הטיסות כדי למצוא את המינימום והמקסימום האמיתיים לכל אחד מ-9 הממדים
        for (Flight f : flights) {
            for (int i = 0; i < 9; i++) {
                double val = f.getDimensionValue(i);
                if (val < mins[i]) mins[i] = val;
                if (val > maxs[i]) maxs[i] = val;
            }
        }

        // עדכון כל הטיסות לערכים המנורמלים (בין 0 ל-1)
        for (Flight f : flights) {
            for (int i = 0; i < 9; i++) {
                double currentVal = f.getDimensionValue(i);
                double normalizedVal = 0;

                // מוודאים שאין חלוקה באפס (למקרה שכל הטיסות עולות בדיוק אותו דבר)
                if (maxs[i] - mins[i] != 0) {
                    normalizedVal = (currentVal - mins[i]) / (maxs[i] - mins[i]);
                }

                f.setDimensionValue(i, normalizedVal);
            }
        }
    }
}