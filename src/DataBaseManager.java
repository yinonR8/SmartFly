import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseManager {

    private String url = "jdbc:sqlserver://localhost:1433;databaseName=SmartFly;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";

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
}
