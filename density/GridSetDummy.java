package density;
import java.util.ArrayList;

// Assumes each sample maps feature names to doubles
public class GridSetDummy extends GridSet {
    ArrayList featureNames = new ArrayList();
    ArrayList features = new ArrayList();

    public int getNumPoints() { return 0; }
    Feature getFeature(final String s) {
	if (!featureNames.contains(s)) {
	    featureNames.add(s);
	    features.add(new Feature(0, s) {
		    public double eval(int p) { return 0.0; }
		    public double eval(Sample ss) { 
			if (ss.featureMap.containsKey(s)) {
			    Object o = ss.featureMap.get(s);
			    return ((Double) o).doubleValue();
			}
			throw new RuntimeException("Didn't find " + s + " in GridSetDummy");
		    }
		});
	}
	return (Feature) features.get(featureNames.indexOf(s));
    }
}
