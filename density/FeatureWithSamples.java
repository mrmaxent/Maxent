package density;

class FeatureWithSamples extends Feature {
    float[] backgroundVals, sampleVals;
    int NODATA_value;

    public FeatureWithSamples(String name, float[] backgroundVals, float[] sampleVals, int nodata) {
	this.name = name;
	this.backgroundVals = backgroundVals;
	this.sampleVals = sampleVals;
	NODATA_value = nodata;
	n = backgroundVals.length;
    }

    public double eval(int p) { return backgroundVals[p]; }
    public double eval(Sample s) { return sampleVals[s.getPoint()]; }
    public boolean hasData(Sample s) { return (sampleVals[s.getPoint()] != NODATA_value); }
}
