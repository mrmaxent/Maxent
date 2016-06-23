package density;

public class ScaledFeature extends Feature {
    public double min, max, scale;
    Feature f;

    ScaledFeature(Feature f, double min, double max) { 
	super(f.n, f.name);
	this.f = f; 
	this.min = min; 
	this.max = max;
	scale = max - min;
    }

    public ScaledFeature(Feature f) {
	super(f.n, f.name);
	this.f = f;
	min = max = f.eval(0);
	for (int i=1; i<f.n; i++) {
	    double val = f.eval(i);
	    if (val < min)
		min = val;
	    if (val > max)
		max = val;
	}
	scale = max - min;
	if (scale==0) {
	    scale = 1;
//	    min = min-1;
	}
    }

    public double eval(int p) { return (f.eval(p) - min) / scale; }
    public double eval(Sample s) { return (f.eval(s) - min) / scale; }
    public boolean hasData(Sample s) { return f.hasData(s); }

    String description() {
	java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance();
	nf.setMinimumFractionDigits(4);
	return (name + ": lambda = " + nf.format(lambda) + " min = " + nf.format(min) + " max = " + nf.format(max));
    }
}
