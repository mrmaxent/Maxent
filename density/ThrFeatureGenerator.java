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

class ThrFeatureGenerator extends SortedFeatureGenerator {

    ThrFeatureGenerator(Sample[] samples_init, Feature feature_init) {
	super(samples_init, feature_init);
	if (numSamples>0) {
	    int iThr;
	    double val;
	    val = feature.eval(firstSample);
	    for (iThr=0; iThr<numThr; iThr++)
		if (val<=thr[iThr]) break;
	    thrFirst = iThr;
	    val = feature.eval(lastSample);
	    for (iThr=numThr; iThr>0; iThr--)
		if (val>thr[iThr-1]) break;
	    thrLast = iThr;
	}
    }

    public void setSampleExpectations(FeaturedSpace X) {
	double sum1=0.0, sum2=0.0;
	FeaturedSpace.SampleInfo thrInfo=
	    new FeaturedSpace.SampleInfo(0.0, 0.0, 0.0, X.biasInfo.max, numSamples);
	for (int i=numVals-1; i>=0; i--) {
	    if (isSample(sortedVals[i])) {
		double val = 1.0/X.biasDiv.eval(getSample(sortedVals[i]));
		sum1 += val;
		sum2 += val*val;
	    }
	    int iThr = valToThr[i];
	    if (iThr>=thrFirst&& iThr<thrLast) {
		double avg, std;
		if (numSamples==0) {
		    avg = std = thrInfo.max/2;
		} else if (numSamples==1) {
		    avg = sum1;
		    std = thrInfo.max/2;
		} else {
		    avg = sum1/numSamples;
		    if (sum2 < numSamples*avg*avg) // due to floating point error
			std = 0.0;
		    else
			std = Math.sqrt((sum2-numSamples*avg*avg)/(numSamples-1));
		    if (std > thrInfo.max/2)
			std = thrInfo.max/2;
		    /*
		    if (std < 1.0/Math.sqrt(numSamples))
			std = 1.0/Math.sqrt(numSamples);
		    */
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
	double sum = 0.0;
	for (int i=numVals-1; i>=0; i--) {
	    if (isPoint(sortedVals[i]))
		sum += X.getDensity(sortedVals[i]);
	    int iThr = valToThr[i];
	    if (iThr>=thrFirst && iThr<thrLast)
		featureExpectations[iThr] = sum/X.densityNormalizer;
	}
	for (int i=thrFirst; i<thrLast; i++)
	    if (features[i] != null)
		features[i].expectation = featureExpectations[i];
    }
	
    double featureEval(int thrIndex, int i) {
	return feature.eval(i) > thr[thrIndex] ? 1.0 : 0.0; 
    }

    double featureEval(int thrIndex, Sample s) {
	return feature.eval(s) > thr[thrIndex] ? 1.0 : 0.0;
    }

    public Feature exportFeature(int thrIndex) {
	if (features[thrIndex] == null) {
	    Feature f = new ThresholdFeature(feature, thr[thrIndex], feature.name) {
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
	int idx;
	double mass = 0.0;
	    
	out.println(feature.name + ',' + nf.format(minVal) + ',' + nf.format(mass));
	for (int i=0; i<numThr; i++)
	    if (features[i] != null) {
		ThresholdFeature f = (ThresholdFeature) features[i];
		out.println(feature.name + ',' + nf.format(f.threshold) + ',' + nf.format(mass));
		mass += f.lambda;
		out.println(feature.name + ',' + nf.format(f.threshold) + ',' + nf.format(mass));
	    }
	out.println(feature.name + ',' + nf.format(maxVal) + ',' + nf.format(mass));
    }
}
