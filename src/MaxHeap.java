public class MaxHeap {

    private Flight[] heap;
    private double[] distances;
    private int size;
    private int maxsize;

    public MaxHeap(int maxsize)
    {
        this.maxsize = maxsize;
        this.size = 0;
        this.heap = new Flight[maxsize];
        this.distances = new double[maxsize];
    }

    private int parent(int i)
    {
        return (i-1)/2;
    }

    private int leftChild(int i)
    {
        return (2 * i) + 1;
    }

    private int rightChild(int i)
    {
        return (2 * i) + 2;
    }

    private void swap(int i,int j)
    {
        Flight tempflight = heap[i];
        heap[i] = heap[j];
        heap[j] = tempflight;

        double tempdistance = distances[i];
        distances[i] = distances[j];
        distances[j] = tempdistance;
    }
    // פונקציה זו מעלה איבר כלפי מעלה בערימה כדי לשמור על חוקיות ערימת המקסימום.
    private void bubbleUp(int index)
    {
        while(index > 0 && distances[index] > distances[parent(index)])
        {
            swap(index,parent(index));
            index = parent(index);
        }
    }
    // מופעלת בעיקר כאשר מחליפים את השורש (הטיסה הגרועה ביותר) בטיסה חדשה וטובה יותר.
    private void bubbleDown(int index)
    {
        int maxindex = index;
        int left = leftChild(index);
        int right = rightChild(index);

        if(left < size && distances[left] > distances[maxindex])
            maxindex = left;

        if(right < size && distances[right] > distances[maxindex])
            maxindex = right;
// אם גילינו שאחד הבנים מכיל מרחק גדול יותר מההורה
        if(maxindex != index)
        {
            swap(index,maxindex);
            bubbleDown(maxindex);
        }
    }

    public void insert(Flight flight,double distance)
    {
        // מצב 1: יש עדיין מקום פנוי בערימה (טרם אספנו K טיסות)
        if(size < maxsize)
        {
            heap[size] = flight;
            distances[size] = distance;
            bubbleUp(size);
            size++;
        }
        // מצב 2: הערימה מלאה. עכשיו כל טיסה צריכה "להיאבק" על מקומה.
        else
        {
            if(distance < distances[0]){
                heap[0] = flight;
                distances[0] = distance;
                bubbleDown(0);
            }
        }
    }

    public boolean isFull() {
        return size == maxsize;
    }

    public double getMaxDistance() {
        if (size == 0) return Double.MAX_VALUE;
        return distances[0];
    }

    public Flight[] getFlight()
    {
        return heap;
    }

    public int getSize()
    {
        return size;
    }
}