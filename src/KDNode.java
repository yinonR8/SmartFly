public class KDNode {
    private Flight flight;
    private KDNode left;
    private KDNode right;

    public KDNode(Flight flight)
    {
        this.flight = flight;
        left = null;
        right = null;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public KDNode getLeft() {
        return left;
    }

    public void setLeft(KDNode left) {
        this.left = left;
    }

    public KDNode getRight() {
        return right;
    }

    public void setRight(KDNode right) {
        this.right = right;
    }
}
