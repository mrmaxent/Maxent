package density;

public class BinaryFeature extends Feature {
    Feature f;
    float target;
    boolean isFirstCategory=false;

    public BinaryFeature(Feature f, float target, String s) {
	super(f.n, "(" + s + "=" + target + ")");
	this.f = f;
	this.target = target;
    }

    public double eval(int i) { 
	if (f.eval(i) == target)
	    return 1.0;
	else
	    return 0.0;
    }

    public double eval(Sample s) { return f.eval(s, target); }
    public boolean hasData(Sample s) { return f.hasData(s); }
    boolean isBinary() { return true; }

    static public BinaryFeature[] makeAll (Feature f, String s) {
	double[] allvals = f.distinctVals();
	BinaryFeature[] result = new BinaryFeature[allvals.length];
	
	for (int i=0; i<allvals.length; i++)
	    result[i] = new BinaryFeature(f, (float) allvals[i], s);
	if (allvals.length>0) result[0].isFirstCategory=true;
	return result;
    }
}
