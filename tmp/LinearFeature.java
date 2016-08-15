package density;

class LinearFeature extends Feature {
    Feature f;

    public LinearFeature(Feature f, String s) {
	super(f.n, s);
	this.f = f;
    }

    public double eval(int p) { return f.eval(p); }
    public double eval(Sample s) { return f.eval(s); }
    public boolean hasData(Sample s) { return f.hasData(s); }

}
