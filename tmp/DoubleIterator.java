package density;
import java.util.ArrayList;

abstract class DoubleIterator {
    int i=0, n;
    DoubleIterator(int n) { this.n = n; }
    boolean hasNext() { return i<n; }
    void reset() { i=0; }
    abstract double getNext();

    double[] getvals() { return getvals(1.0); }
    double[] getvals(double div) {
	reset();
	ArrayList a = new ArrayList();
	while (hasNext())
	    a.add(new Double(getNext()/div));
	double[] d = new double[a.size()];
	for (int i=0; i<d.length; i++) 
	    d[i] = ((Double) a.get(i)).doubleValue();
	return d;
    }
}
