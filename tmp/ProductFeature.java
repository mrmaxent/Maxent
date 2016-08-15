package density;

class ProductFeature extends Feature {
    Feature f1, f2;

    public ProductFeature(Feature f1, String s0, Feature f2, String s1) {
	super(f1.n, s0 + "*" + s1);
	this.f1 = f1;
	this.f2 = f2;
    }

    public double eval(int p) { return (f1.eval(p) * f2.eval(p)); }
    public double eval(Sample s) { return (f1.eval(s) * f2.eval(s)); }
    public boolean hasData(Sample s) { return f1.hasData(s) && f2.hasData(s); }

}
