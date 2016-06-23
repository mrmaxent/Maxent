package density;

class CachedScaledFeature extends ScaledFeature {
    double[] vals=null;

    public CachedScaledFeature(Feature f, double min, double max) { 
	super(f, min, max);
	getvals();
    }

    public CachedScaledFeature(Feature f) {
	super(f);
	getvals();
    }

    void getvals() {
	n = f.n;
	if (vals==null) vals = new double[n];
	for (int p=0; p<n; p++)
	    vals[p] = (f.eval(p) - min) / scale;
    }

    public double eval(int p) { return vals[p]; }
    // ... and don't cache samples
}
