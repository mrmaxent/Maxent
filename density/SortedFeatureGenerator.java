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

// add correct eval to toFeature

import java.util.*;
import java.io.*;
import java.text.*;

abstract class SortedFeatureGenerator implements FeatureGenerator {
    Feature feature, cachedFeature;
    double beta;
    int numPoints;

    Sample[] samples;
    int numSamples;
    Sample firstSample, lastSample;

    int thrFirst = -1, thrLast = -1;
    public int getFirstIndex() { return thrFirst; }
    public int getLastIndex()  { return thrLast; }

    int[] sortedVals;
    int numVals;

    int[] thrToVal;
    int[] valToThr;
    double[] thr;
    int numThr;
    double minVal, maxVal;

    Feature[] features;
    double[] sampleExpectation;
    double[] sampleDeviation;
    double[] featureExpectations;
    NumberFormat nf = NumberFormat.getNumberInstance();
    
    double eval(int i) { return  ((i<0) ? feature.eval(samples[-i-1]) : feature.eval(i)); }
    double evalCached(int i) { return  ((i<0) ? cachedFeature.eval(samples[-i-1]) : cachedFeature.eval(i)); }
    boolean isSample(int i) { return (i<0); }
    Sample getSample(int i) { return samples[-i-1]; }
    boolean isPoint(int i) { return (i>=0); }
    
    // according to the feature value
    class SampleAndPointComparator implements java.util.Comparator {
	public int compare(Object o1, Object o2) {
	    double f1 = evalCached(((Integer) o1).intValue());
	    double f2 = evalCached(((Integer) o2).intValue());
	    if (f1<f2) return -1;
	    if (f1>f2) return  1;
	    return 0;
	}
    }

    static int maxDigits = 6;
    public static double precision(double f) {
	f = Math.abs(f);
	double firstSig = Math.floor(Math.log(f)/Math.log(10));
	double currentPower = Math.pow(10.0, firstSig);
	int lastTwoDigits = 0, currentDigit;
	for (int i=1; i<=maxDigits; i++) {
	    currentDigit  = (int)(f/currentPower);
	    lastTwoDigits = (lastTwoDigits*10+currentDigit) % 100;
	    if (i>=2)
		switch (lastTwoDigits) {
		case 00:
		case 01:
		case 99:
		case 98: return currentPower*50.0;
		}
	    f = f - currentDigit*currentPower;
	    currentPower = currentPower/10.0;
	}
	return currentPower*5.0;
    }

    SortedFeatureGenerator(Sample[] samples_init, Feature feature_init) {
	feature = feature_init;
	beta = feature.beta;
	numPoints = feature.n;
	ArrayList a = new ArrayList();
	for (int i=0; i<samples_init.length; i++)
	    if (feature.hasData(samples_init[i]))
		a.add(samples_init[i]);
	samples = (Sample[]) a.toArray(new Sample[0]);
	numSamples = samples.length;
	numVals = numPoints + numSamples;

	cachedFeature = new CachedFeature(feature);
	Integer[] sortedValsObj = new Integer[numVals];
	for (int i=0; i<numPoints; i++)
	    sortedValsObj[i] = new Integer(i);
	for (int i=0; i<numSamples; i++)
	    sortedValsObj[numPoints + i] = new Integer(-i-1);
	Arrays.sort(sortedValsObj, new SampleAndPointComparator());

	sortedVals = new int[numVals];
	double prec = Double.POSITIVE_INFINITY, currentPrec;
	for (int i=0; i<numVals; i++) {
	    sortedVals[i] = ((Integer) sortedValsObj[i]).intValue();
	    currentPrec = precision(evalCached(sortedVals[i]));
	    if (currentPrec < prec)
		prec = currentPrec;
	}

	minVal = evalCached(sortedVals[0]);
	maxVal = evalCached(sortedVals[numVals-1]);
	double lastVal = minVal, val;
	numThr = 0;
	for (int i=0; i<numVals; i++) {
	    val = evalCached(sortedVals[i]);
	    if (val - lastVal > prec) {
		lastVal = val;
		numThr++;
	    }
	}

	thrToVal = new int[numThr];
	valToThr = new int[numVals];
	thr = new double[numThr];
	lastVal = minVal;
	firstSample = null; lastSample = null;
	for (int i=0, iThr=0; i<numVals; i++) {
	    val = evalCached(sortedVals[i]);
	    if (isSample(sortedVals[i])) {
		lastSample = getSample(sortedVals[i]);
		if (firstSample==null) firstSample = lastSample;
	    }
	    if (val - lastVal > prec) {
		thr[iThr] =(val+lastVal)/2.0;
		lastVal = val;
		thrToVal[iThr] = i;
		valToThr[i] = iThr;
		iThr++;
	    }
	    else
		valToThr[i] = -1;
	}
	//	cachedFeature = null;

	features = new Feature[numThr];
	Arrays.fill(features, null);
	    
	sampleExpectation = new double[numThr];
	sampleDeviation = new double[numThr];
	featureExpectations = new double[numThr];

	nf.setMinimumFractionDigits(8);
	nf.setGroupingUsed(false);
    }

    double getSampleExpectation(int thrIndex) { return sampleExpectation[thrIndex]; }
    double getSampleDeviation(int thrIndex) { return sampleDeviation[thrIndex]; }
    double getExpectation(int thrIndex) { return featureExpectations[thrIndex]; }
    
    double getLambda(int thrIndex) {
	if (features[thrIndex] == null)
	    return 0.0;
	return features[thrIndex].getLambda();
    }
	
    public Feature getFeature(int thrIndex) {
	return features[thrIndex];
    }

    abstract double featureEval(int thrIndex, int i);
    abstract double featureEval(int thrIndex, Sample s);
	
    public Feature toFeature(final int thrIndex) {
	final SortedFeatureGenerator g = this;
	return new Feature() {
		double getLambda()            { return g.getLambda(thrIndex); }
		double getBeta()              { return g.beta; }
		double getSampleExpectation() { return g.getSampleExpectation(thrIndex); }
		double getSampleDeviation()   { return g.getSampleDeviation(thrIndex); }
		double getExpectation()       { return g.getExpectation(thrIndex); }
		public double eval(int i)     { return g.featureEval(thrIndex,i); }
		public double eval(Sample s)         { return g.featureEval(thrIndex,s); }
	    };
    }
}
