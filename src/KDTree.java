import java.util.ArrayList;
import java.util.List;

public class KDTree {

    //Number of Dimensions
    private static final int k = 9;
    private KDNode root;

    public KDTree()
    {
        this.root = null;
    }

    public void Build(ArrayList<Flight> flights)
    {
        int depth =0;
        
        if(flights ==null || flights.isEmpty())
            return;
        this.root =  BuildRecursive(flights,depth);
    }

    public KDNode BuildRecursive(List<Flight> flights, int currdepth)
    {
        if(flights ==null || flights.isEmpty())
            return null;

        int dimension = currdepth % k;
        int median = flights.size()/2;
        flights.sort((f1,f2) -> Double.compare(f1.getDimensionValue(dimension),f2.getDimensionValue(dimension)));
        KDNode node = new KDNode(flights.get(median));

        node.setLeft(BuildRecursive(flights.subList(0,median),currdepth + 1));
        node.setRight(BuildRecursive(flights.subList(median + 1, flights.size()),currdepth + 1));

        return node;
    }

    private double calculateEuclideanDistance(Flight currflight,double[] user)
    {
        double[] sumofgaps = new double[9];
        double temp=0;
        double finaldistance = 0;
        for(int i = 0; i < 9 ; i++)
        {
            temp = currflight.getDimensionValue(i) - user[i];
            sumofgaps[i] = temp;
            finaldistance += (temp * temp);
            temp=0;
        }
        finaldistance = Math.sqrt(finaldistance);
        return finaldistance;
    }

    public KDNode getRoot() {
        return root;
    }

    public void setRoot(KDNode root) {
        this.root = root;
    }
}
