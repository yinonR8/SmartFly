import java.util.ArrayList;

public class KDTree {

    private static final int k = 8;
    private KDNode root;

    public KDTree(KDNode root)
    {
        this.root = null;
    }

    public KDTree Build(ArrayList<Flight> flights)
    {
        int depth =0;

        if(flights.isEmpty())
            return null;

        BuildRecursive(flights,depth);
        return null; //Change
    }

    public KDTree BuildRecursive(ArrayList<Flight> flights,int currdepth)
    {
        return null; //Change
    }

    public KDNode getRoot() {
        return root;
    }

    public void setRoot(KDNode root) {
        this.root = root;
    }
}
