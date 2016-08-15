/*
Copyright (c) 2016 Steven Phillips, Miro Dudik and Rob Schapire

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions: 

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software. 

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
*/

package density;

import java.util.*;
import java.lang.*;

abstract public class Feature {
    public static final int BINARY=0, LINEAR=1, SQUARE=2, PRODUCT=3, THR_GEN=4, THR=5,
        L_CONT=6, L_CAT=7, L_F_BIAS_OUT=8, L_DEBIAS_AVG=9, F_W_SAMPLES=10, HINGE_GEN=11, HINGE=12;
    static RuntimeException unknownFeatureTypeException(String type) {
	return new RuntimeException("Unknown feature type: "+type);
    }
    public int type() { return getType(this); }
    private static int getType(Feature f) {
	Class c = f.getClass();
	String name = c.getName();
	if (f instanceof LayerFeature) {
	    int layerType = ((LayerFeature) f).getLayerType();
	    switch (layerType) {
	    case Layer.CONT: return L_CONT;
	    case Layer.CAT:  return L_CAT;
	    case Layer.F_BIAS_OUT: return L_F_BIAS_OUT;
	    case Layer.DEBIAS_AVG: return L_DEBIAS_AVG;
	    default: throw unknownFeatureTypeException(name + " (type "+Layer.getTypeName(layerType)+")");
	    }
	}
	else if (f instanceof FeatureWithSamples)
	    return F_W_SAMPLES;
	else if (f instanceof FeatureWithSamplesAsPoints)
	    return getType(((FeatureWithSamplesAsPoints) f).f);
	else if (f instanceof CachedFeature)
	    return getType(((CachedFeature) f).f);
	else if (f instanceof ScaledFeature)
	    return getType(((ScaledFeature) f).f);
	else if (f instanceof CachedScaledFeature)
	    return getType(((CachedScaledFeature) f).f);
	else if (f instanceof ClampedFeature)
	    return getType(((ClampedFeature) f).f);
	else if (f instanceof LinearFeature)
	    return LINEAR;
	else if (f instanceof SquareFeature)
	    return SQUARE;
	else if (f instanceof ProductFeature)
	    return PRODUCT;
	else if (f instanceof ThrGeneratorFeature)
	    return THR_GEN;
	else if (f instanceof HingeGeneratorFeature)
	    return HINGE_GEN;
	else if (f instanceof ThresholdFeature)
	    return THR;
	else if (f instanceof HingeFeature)
	    return HINGE;
	else if (f instanceof BinaryFeature)
	    return BINARY;
	else
	    throw unknownFeatureTypeException(name);
    }

    double lambda=0.0, sampleExpectation, sampleDeviation, expectation;
    double previousLambda=0.0, contribution=0.0, previousContribution=0.0;
    double beta;
    int n, lastChange = -1, lastExpectationUpdate=-1;
    public String name;
    boolean active = true;


    public Feature() {}
    public Feature(int n, String s) { this.n=n; name=s; } 
    double getLambda() { return lambda; }
    void setLambda(double x) { lambda = x; }
    void increaseLambda(double x) { lambda += x; }
    double getBeta() { return beta; }
    void setBeta(double x) { beta = x; }
    boolean isBinary() { return false; }
    void setActive(boolean b) { active = b; }
    boolean isActive() { return active; }
    boolean isGenerated() { return false; }
    public int getN() { return n; }

    void describe() { System.out.println(description()); }
    String description() {
	java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance();
	nf.setMinimumFractionDigits(4);
	return name + " lambda = " + nf.format(lambda);
    }

    public abstract double eval(int p);
    public abstract double eval(Sample s);

    /* the following is overridden to interpolate binary features, see Grid.java */
    double eval(Sample s, float val) { return (eval(s) == val) ? 1.0 : 0.0; }
    public boolean hasData(Sample s) { return true; }
    double getSampleExpectation() { return sampleExpectation; }
    double getSampleDeviation() { return sampleDeviation; }
    double getExpectation() { return expectation; }

    public Grid toGrid() { 
	return new Grid(new GridDimension(0,0,1,n,1), name) {
		public float eval(int r, int c) { return (float) Feature.this.eval(r); }
		public boolean hasData(int r, int c) { return true; }
	    };
    }
    double[] distinctVals() {
	HashSet set = new HashSet();
	for (int i=0; i<n; i++) {
	    double val = eval(i);
	    if (!set.contains(new Double(val)))
		set.add(new Double(val));
	}
	Double[] vals = (Double[]) set.toArray(new Double[0]);
	Arrays.sort(vals);
	double[] result = new double[vals.length];
	for (int i=0; i<vals.length; i++)
	    result[i] = vals[i].doubleValue();
	return result;
    }
	
    private boolean isMask, maskComputed=false;
    synchronized boolean isMask() {
	if (maskComputed) return isMask;
	isMask = true;
	for (int j=1; j<n; j++) 
	    if (eval(j)!=eval(0)) {
		isMask = false;
		break;
	    }
	maskComputed = true;
	return isMask;
    }
}
