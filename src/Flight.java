public class Flight {

    //Private Variables
    private int flightID;
    private String destination;
    private String airline;
    private double price;
    private int durationMins;
    private int departureHour;
    private int connections;

    //KD-Tree Dimensions
    private double expUrban;
    private double expNature;
    private double expBeach;
    private double expHistory;

    public Flight(int flightID,String destination,String airline,double price,int durationMins,int departureHour,int connections,double expUrban,double expNature,double expBeach,double expHistory)
    {
    this.flightID = flightID;
    this.destination = destination;
    this.airline = airline;
    this.price = price;
    this.durationMins = durationMins;
    this.departureHour = departureHour;
    this.connections = connections;
    this.expUrban = expUrban;
    this.expNature = expNature;
    this.expBeach = expBeach;
    this.expHistory = expHistory;
    }

    //Default Constructor
    public Flight() {}


    //ToString
    @Override
    public String toString()
    {
        return String.format("Flight #%d: %s To %s | Price %.2f | Stops %d",
                flightID,airline,destination,price,connections);
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
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDurationMins() {
        return durationMins;
    }

    public void setDurationMins(int durationMins) {
        this.durationMins = durationMins;
    }

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public int getDepartureHour() {
        return departureHour;
    }

    public void setDepartureHour(int departureHour) {
        this.departureHour = departureHour;
    }

    public double getExpUrban() {
        return expUrban;
    }

    public void setExpUrban(double expUrban) {
        this.expUrban = expUrban;
    }

    public double getExpNature() {
        return expNature;
    }

    public void setExpNature(double expNature) {
        this.expNature = expNature;
    }

    public double getExpBeach() {
        return expBeach;
    }

    public void setExpBeach(double expBeach) {
        this.expBeach = expBeach;
    }

    public double getExpHistory() {
        return expHistory;
    }

    public void setExpHistory(double expHistory) {
        this.expHistory = expHistory;
    }
}