package density;

import java.util.*;

class HingeFeature extends Feature {
    double min, max, range;
    Feature f;

    public HingeFeature(Feature f, double min, double max, String s) {
	super(f.n, "'" + s);
	this.f = f;
	this.min = min;
	this.max = max;
	range = max-min;
    }

    double eval(double d) { return (d>min) ? (d-min)/range : 0.0; }
    public double eval(int i) { return eval(f.eval(i)); }
    public double eval(Sample s) { return eval(f.eval(s)); }
    public boolean hasData(Sample s) { return f.hasData(s); }
}
