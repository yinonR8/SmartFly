import java.util.Map;
import java.util.EnumMap;
public class Flight {

    //Private Variables

    //Text Variables
    private int flightID;
    private String destination;
    private String airline;

    //KD-Tree Dimensions
    private Map<FlightDimension,Double> Dimensions;
    public Flight(int flightID, String destination, String airline, double price, int durationMins, int departureHour, int connections, double expUrban, double expNature, double expBeach, double expHistory) {

        //Text Variables
        this.flightID = flightID;
        this.destination = destination;
        this.airline = airline;

        //Enum Dictionary
        this.Dimensions = new EnumMap<>(FlightDimension.class);
        this.Dimensions.put(FlightDimension.PRICE, price);
        this.Dimensions.put(FlightDimension.DURATION_MINS, (double) durationMins);
        this.Dimensions.put(FlightDimension.DEPARTURE_HOUR, (double) departureHour);
        this.Dimensions.put(FlightDimension.CONNECTIONS, (double) connections);
        this.Dimensions.put(FlightDimension.EXP_URBAN, expUrban);
        this.Dimensions.put(FlightDimension.EXP_NATURE, expNature);
        this.Dimensions.put(FlightDimension.EXP_BEACH, expBeach);
        this.Dimensions.put(FlightDimension.EXP_HISTORY, expHistory);
    }
    //Enum For The FlightDimensions
    public enum FlightDimension
    {
        PRICE,DURATION_MINS,DEPARTURE_HOUR,CONNECTIONS,EXP_URBAN,EXP_NATURE,EXP_BEACH,EXP_HISTORY
    }

    //Default Constructor
    public Flight() {}


    public double getDimensionValue(int axis) {
        // ממיר את המספר של הציר ל-Enum המתאים (למשל 0 הופך ל-PRICE)
        FlightDimension dim = FlightDimension.values()[axis];
        return this.Dimensions.get(dim);
    }
    //ToString
    @Override
    public String toString()
    {
        return String.format("Flight #%d: %s To %s | Price %.2f | Stops %d",
                flightID,airline,destination,getPrice(),getConnections());
    }

    //Getters And Setters

    public int getFlightID() {
        return flightID;
    }

    public void setFlightID(int flightID) {
        this.flightID = flightID;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public double getPrice() {
        return this.Dimensions.get(FlightDimension.PRICE);
    }

    public void setPrice(double price) {
        this.Dimensions.put(FlightDimension.PRICE,price);
    }

    public int getDurationMins() {
        return this.Dimensions.get(FlightDimension.DURATION_MINS).intValue();
    }

    public void setDurationMins(int durationMins) {
        this.Dimensions.put(FlightDimension.DURATION_MINS,(double)durationMins);
    }

    public int getConnections() {
        return this.Dimensions.get(FlightDimension.CONNECTIONS).intValue();
    }

    public void setConnections(int connections) {
        this.Dimensions.put(FlightDimension.CONNECTIONS,(double)connections);
    }

    public int getDepartureHour() {
        return this.Dimensions.get(FlightDimension.DEPARTURE_HOUR).intValue();
    }

    public void setDepartureHour(int departureHour) {
        this.Dimensions.put(FlightDimension.DEPARTURE_HOUR,(double)departureHour);
    }

    public double getExpUrban() {
        return this.Dimensions.get(FlightDimension.EXP_URBAN);
    }

    public void setExpUrban(double expUrban) {
        this.Dimensions.put(FlightDimension.EXP_URBAN,expUrban);
    }

    public double getExpNature() {
        return this.Dimensions.get(FlightDimension.EXP_NATURE);
    }

    public void setExpNature(double expNature) {
        this.Dimensions.put(FlightDimension.EXP_NATURE,expNature);
    }

    public double getExpBeach() {
        return this.Dimensions.get(FlightDimension.EXP_BEACH);
    }

    public void setExpBeach(double expBeach) {
        this.Dimensions.put(FlightDimension.EXP_BEACH,expBeach);
    }

    public double getExpHistory() {
        return this.Dimensions.get(FlightDimension.EXP_HISTORY );
    }

    public void setExpHistory(double expHistory) {
        this.Dimensions.put(FlightDimension.EXP_HISTORY,expHistory);
    }
}