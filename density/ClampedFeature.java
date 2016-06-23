package density;

public class ClampedFeature extends Feature {
    double min, max;
    Feature f;

    public ClampedFeature(Feature f) { this(f, 0, 1); }
    public ClampedFeature(Feature f, double min, double max) {
	super(f.n, f.name);
        this.f = f;
	this.min = min;
	this.max = max;
    }

    public double eval(int p) {
	double val = f.eval(p);
	if (val<min) return min;
	if (val>max) return max;
	return val;
    }

    public double eval(Sample s) {
	double val = f.eval(s);
	if (val<min) return min;
	if (val>max) return max;
	return val;
    }

    public boolean hasData(Sample s) { return f.hasData(s); }
}
