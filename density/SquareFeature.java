package density;

class SquareFeature extends Feature {
    Feature f;

    public SquareFeature(Feature f, String s) {
	super(f.n, s+"^2");
	this.f = f;
    }

    public double eval(int p) { double val = f.eval(p); return (val * val); }
    public double eval(Sample s) { double val = f.eval(s); return (val * val); }
    public boolean hasData(Sample s) { return f.hasData(s); }

}
