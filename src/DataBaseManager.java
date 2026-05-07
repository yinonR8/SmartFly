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

        int numDimensions = Flight.FlightDimension.values().length;

        double[] mins = new double[numDimensions];
        double[] maxs = new double[numDimensions];

        for (int i = 0; i < numDimensions; i++) {
            mins[i] = Double.MAX_VALUE;
            maxs[i] = -Double.MAX_VALUE;
        }

        //  מעבר על כל הטיסות כדי למצוא את המינימום והמקסימום האמיתיים לכל אחד מ-9 הממדים
        for (Flight f : flights) {
            for (int i = 0; i < numDimensions; i++) {
                double val = f.getDimensionValue(i);
                if (val < mins[i]) mins[i] = val;
                if (val > maxs[i]) maxs[i] = val;
            }
        }

        // עדכון כל הטיסות לערכים המנורמלים (בין 0 ל-1)
        for (Flight f : flights) {
            for (int i = 0; i < numDimensions; i++) {
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
    public void flightRandomCreation(int amount) {
        String insertSQL = "INSERT INTO Flights (Destination, AirLine, Price, DurationMins, DepartureHour, Connections, AirLineRating, ExpUrban, ExpNature, ExpBeach, ExpHistory) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String[] destinations = {"London", "Paris", "New York", "Tokyo", "Rome", "Berlin", "Madrid", "Dubai", "Bangkok", "Athens"};
        String[] airlines = {"El Al", "Ryanair", "Wizz Air", "Lufthansa", "Delta", "Emirates", "United", "EasyJet", "Arkia"};

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            // ביטול אוטומציה כדי לאשר את כל הבאצ' ביחד (משפר ביצועים משמעותית!)
            conn.setAutoCommit(false);

            for (int i = 1; i <= amount; i++) {
                String dest = destinations[(int)(Math.random() * destinations.length)];
                String airline = airlines[(int)(Math.random() * airlines.length)];
                double price = 50 + (Math.random() * 950); // בין 50$ ל-1000$
                int duration = 60 + (int)(Math.random() * 840); // מ-1 עד 15 שעות
                int depHour = (int)(Math.random() * 24); // 0 עד 23
                int connections = (Math.random() > 0.6) ? (int)(Math.random() * 3) + 1 : 0; // 60% סיכוי לטיסה ישירה
                double rating = 1.0 + (Math.random() * 4.0); // דירוג 1.0 עד 5.0

                pstmt.setString(1, dest);
                pstmt.setString(2, airline);
                pstmt.setDouble(3, price);
                pstmt.setInt(4, duration);
                pstmt.setInt(5, depHour);
                pstmt.setInt(6, connections);
                pstmt.setDouble(7, rating);
                pstmt.setDouble(8, Math.random() * 10); // ציון אורבני 0-10
                pstmt.setDouble(9, Math.random() * 10); // ציון טבע 0-10
                pstmt.setDouble(10, Math.random() * 10); // ציון ים 0-10
                pstmt.setDouble(11, Math.random() * 10); // ציון היסטוריה 0-10

                pstmt.addBatch(); // הוספת השאילתה לאצווה

                // הרצת האצווה כל 1000 רשומות למניעת עומס על ה-RAM (Flush)
                if (i % 1000 == 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch(); // הרצת שארית הרשומות
            conn.commit(); // שמירה סופית במסד הנתונים
            System.out.println("Successfully inserted " + amount + " random flights.");

        } catch (SQLException e) {
            System.out.println("Database Error during random creation");
            e.printStackTrace();
        }
    }
}