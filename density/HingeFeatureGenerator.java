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

class HingeFeatureGenerator extends SortedFeatureGenerator {

    double range=1.0;

    HingeFeatureGenerator(Sample[] samples_init, Feature feature_init) {
	super(samples_init, feature_init);
	thrFirst = 0;
	thrLast  = numThr;
    }

    public void setSampleExpectations(FeaturedSpace X) {
	double sum1=0.0, sum2=0.0, wsum1=0.0, wsum2=0.0;
	FeaturedSpace.SampleInfo thrInfo=
	    new FeaturedSpace.SampleInfo(0.0, 0.0, 0.0, X.biasInfo.max, numSamples);
	for (int i=numVals-1; i>=0; i--) {
	    if (isSample(sortedVals[i])) {
		double lval = 1/X.biasDiv.eval(getSample(sortedVals[i]));
		double val = feature.eval(getSample(sortedVals[i]))*lval;
		sum1 += val;
		sum2 += val*val;
		wsum1 += lval;
		wsum2 += lval*lval;
	    }
	    int iThr = valToThr[i];
	    if (iThr>=thrFirst && iThr<thrLast) {
		double avg, std;
		avg = std = thrInfo.max/2;
		if (numSamples>0) {
		    double csum1 = (sum1 - thr[iThr]*wsum1)/(maxVal-thr[iThr]);
			//			+ 0.0 + thrInfo.max;
		    double csum2 = (sum2 - 2*sum1*thr[iThr] + thr[iThr]*thr[iThr]*wsum2)/(maxVal-thr[iThr])/(maxVal-thr[iThr]);
			//			+ 0.0 + thrInfo.max*thrInfo.max;
		    avg = csum1/numSamples;
		    if (numSamples>1) {
			if (csum2 < numSamples*avg*avg) // due to floating point error
			    std = 0.0;
			else
			    std = Math.sqrt((csum2-numSamples*avg*avg)/(numSamples-1));
			if (std > thrInfo.max/2)
			    std = thrInfo.max/2;
			if (std < thrInfo.max/Math.sqrt(numSamples))
			    std = thrInfo.max/Math.sqrt(numSamples);
		    }
		}
		thrInfo.avg = avg;
		thrInfo.std = std;
		FeaturedSpace.Interval thrInterval = new FeaturedSpace.Interval(thrInfo, X.biasInfo, beta);
		sampleExpectation[iThr] = thrInterval.getMid();
		sampleDeviation[iThr] = thrInterval.getDev();
		if (sampleDeviation[iThr] < FeaturedSpace.minDeviation)
		    sampleDeviation[iThr] = FeaturedSpace.minDeviation;
	    }
	}
    }
	
    public void updateFeatureExpectations(FeaturedSpace X) {
	double sum = 0.0, wsum = 0.0;
	for (int i=numVals-1; i>=0; i--) {
	    if (isPoint(sortedVals[i])) {
		sum += cachedFeature.eval(sortedVals[i])*X.getDensity(sortedVals[i]);
		wsum += X.getDensity(sortedVals[i]);
	    }
	    int iThr = valToThr[i];
	    if (iThr>=thrFirst && iThr<thrLast)
		featureExpectations[iThr] = 
		    (sum - thr[iThr]*wsum)/X.densityNormalizer/(maxVal - thr[iThr]);
	}
	for (int i=thrFirst; i<thrLast; i++)
	    if (features[i] != null)
		features[i].expectation = featureExpectations[i];
    }
	
    double featureEval(int thrIndex, int i) {
	double threshold = thr[thrIndex];
	return (feature.eval(i)>threshold) ? (feature.eval(i)-threshold)/(maxVal-threshold) : 0.0;
    }

    double featureEval(int thrIndex, Sample s) {
	double threshold = thr[thrIndex];
	return (feature.eval(s)>threshold) ? (feature.eval(s)-threshold)/(maxVal-threshold) : 0.0;
    }

    public Feature exportFeature(int thrIndex) {
	if (features[thrIndex] == null) {
	    Feature f = new HingeFeature(feature, thr[thrIndex], maxVal, feature.name) {
		    boolean isGenerated() { return true; }
		};
	    features[thrIndex] = f;
	    f.sampleExpectation = getSampleExpectation(thrIndex);
	    f.sampleDeviation = getSampleDeviation(thrIndex);
	    f.expectation = getExpectation(thrIndex);
	    f.beta = beta;
	}
	else
	    Utils.warn("Error: exporting existing feature");
	return features[thrIndex];
    }

    public void outputDescription(PrintWriter out) {
	double mass = 0.0, slope = 0.0, last;
	    
	out.println(feature.name + ',' + nf.format(minVal) + ',' + nf.format(mass));
	last = minVal;
	for (int i=0; i<numThr; i++)
	    if (features[i] != null) {
		HingeFeature f = (HingeFeature) features[i];
		mass += (f.min-last)*slope;
		out.println(feature.name + ',' + nf.format(f.min) + ',' + nf.format(mass));
		slope += f.lambda/f.range;
		last = f.min;
	    }
	out.println(feature.name + ',' + nf.format(maxVal) + ',' + nf.format(mass+(maxVal-last)*slope));
    }
}
