void main() {
    DataBaseManager db = new DataBaseManager();
    List<Flight> flights = db.getAllFlights();
    System.out.println("Flights loaded: " + flights.size());
    for (Flight f : flights)
    {
        System.out.println(f);
    }
}
