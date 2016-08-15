package density;

import java.util.*;

class ThresholdFeature extends Feature {
    double threshold;
    Feature f;

    public ThresholdFeature(Feature f, double threshold, String s) {
	super(f.n, "(" + threshold + "<" + s + ")");
	this.f = f;
	this.threshold = threshold;
    }

    public double eval(int i) { return (f.eval(i) > threshold) ? 1.0 : 0.0; }
    public double eval(Sample s) { return (f.eval(s) > threshold) ? 1.0 : 0.0; }
    public boolean hasData(Sample s) { return f.hasData(s); }
    boolean isBinary() { return true; }
}
