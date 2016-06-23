package density;

class ConstFeature extends Feature {
    double c;
    public ConstFeature(double c, int n) {
	this.c = c;
	this.n = n;
    }
    public double eval(int p) { return c; }
    public double eval(Sample s) { return c; }
}
