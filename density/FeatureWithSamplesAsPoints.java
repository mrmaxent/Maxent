package density;

class FeatureWithSamplesAsPoints extends Feature {
    Feature f;
    Sample[] ss;

    public FeatureWithSamplesAsPoints(Feature f, Sample[] ss) {
	super(f.n + ss.length, f.name);
	this.f = f;
	this.ss = ss;
    }

    public double eval(int p) {
	return (p<f.n) ? f.eval(p) : f.eval(ss[p-f.n]);
    }
    public double eval(Sample s) { return f.eval(s); }
    public boolean hasData(Sample s) { return f.hasData(s); }
}
