package density;

class PolyhedralFeature extends SquareFeature {

    public PolyhedralFeature(Feature f, String s) {
	super(f, s);
	name = s+"^p";
    }

    public double eval(int p) { double val = f.eval(p); return (val - val * val)/4.0; }
    public double eval(Sample s) { double val = f.eval(s); return (val - val * val)/4.0; }
    public boolean hasData(Sample s) { return f.hasData(s); }

}
