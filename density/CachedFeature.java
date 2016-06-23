package density;

class CachedFeature extends Feature {
    float[] vals=null;
    Feature f;

    public CachedFeature(Feature f) {
	this.f = f;
	name = f.name;
	n = f.n;
	vals = new float[n];
	for (int p=0; p<n; p++)
	    vals[p] = (float) f.eval(p);
    }

    public double eval(int p) { return vals[p]; }
    public double eval(Sample s) { return f.eval(s); }  // don't cache for samples
    public boolean hasData(Sample s) { return f.hasData(s); }
}
