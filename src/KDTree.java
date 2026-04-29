import java.util.ArrayList;
import java.util.List;

public class KDTree {

    //Number of Dimensions
    private static final int k = Flight.FlightDimension.values().length;
    private KDNode root;
    private int lastVisitedCount; // משתנה ששומר כמה צמתים בדקנו

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

    public double calculateEuclideanDistance(Flight currflight,double[] user)
    {
        double[] sumofgaps = new double[k];
        double temp=0;
        double finaldistance = 0;
        for(int i = 0; i < k ; i++)
        {
            temp = currflight.getDimensionValue(i) - user[i];
            if(i == Flight.FlightDimension.PRICE.ordinal() && temp > 0)
            {
                temp = temp*2;
            }
            if(i == Flight.FlightDimension.CONNECTIONS.ordinal() && temp >0)
            {
                temp = temp * 1.5;
            }
            sumofgaps[i] = temp;
            finaldistance += (temp * temp);
            temp=0;
        }
        finaldistance = Math.sqrt(finaldistance);
        return finaldistance;
    }

    private void searchNearest(KDNode node, double[] target, MaxHeap heap, int depth) {
        if (node == null) {
            return;
        }
        this.lastVisitedCount++; // בדקנו עוד טיסה
        //חישוב מרחק הטיסה הנוכחית והכנסתה לערמה
        double dist = calculateEuclideanDistance(node.getFlight(), target);
        heap.insert(node.getFlight(), dist);

        int dimension = depth % k;
        KDNode goodSide;
        KDNode badSide;

        if (target[dimension] < node.getFlight().getDimensionValue(dimension)) {
            goodSide = node.getLeft();
            badSide = node.getRight();
        } else {
            goodSide = node.getRight();
            badSide = node.getLeft();
        }

        searchNearest(goodSide, target, heap, depth + 1);

        double axisDistance = Math.abs(target[dimension] - node.getFlight().getDimensionValue(dimension));

        // מבחן הגיזום: יורדים לצד הרע רק אם הערימה לא מלאה או שיש פוטנציאל מתמטי למצוא טיסה טובה יותר
        if (!heap.isFull() || axisDistance < heap.getMaxDistance()) {
            searchNearest(badSide, target, heap, depth + 1);
        }
    }

    public Flight[] getRecommendations(double[] user, int numOfRecommendations) {
        this.lastVisitedCount = 0; // איפוס המונה בכל פעם שמתחילים סימולציה חדשה!
        MaxHeap heap = new MaxHeap(numOfRecommendations);
        searchNearest(this.root, user, heap, 0);

        // 1. שולפים את המערך הלא-ממוין מתוך הערימה
        Flight[] rawFlights = heap.getFlight();
        int actualSize = heap.getSize();

        // 2. מעתיקים רק את הטיסות האמיתיות (למקרה שהערימה לא התמלאה עד הסוף)
        Flight[] sortedResults = new Flight[actualSize];
        for (int i = 0; i < actualSize; i++) {
            sortedResults[i] = rawFlights[i];
        }

        // 3. ממיינים מהטיסה הטובה ביותר (מרחק קטן) לגרועה ביותר (מרחק גדול)
        // סיבוכיות המיון היא O(K log K) ולכן לא פוגעת ביעילות העץ!
        java.util.Arrays.sort(sortedResults, (f1, f2) -> {
            double dist1 = calculateEuclideanDistance(f1, user);
            double dist2 = calculateEuclideanDistance(f2, user);
            return Double.compare(dist1, dist2);
        });

        return sortedResults;
    }

    public KDNode getRoot() {
        return root;
    }

    public void setRoot(KDNode root) {
        this.root = root;
    }
    public int getLastVisitedCount() {
        return this.lastVisitedCount;
    }
}
