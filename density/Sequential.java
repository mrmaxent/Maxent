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

import java.text.*;
import java.util.ArrayList;

class Sequential {
    FeaturedSpace X;
    int maxIterations, iteration;
    double reg, oldLoss;
    double terminationThreshold;
    long startTime;
    int parallelUpdateFrequency=10;
    static final double eps=1e-6;
    boolean verbose = false;

    NumberFormat nf = NumberFormat.getNumberInstance();
    {nf.setMinimumFractionDigits(6);}

    void reportStatus() {
	Utils.reportProgress(100*iteration/(double)maxIterations, "Gain is " + nf.format(Math.log(X.bNumPoints()) - oldLoss));
    }
    
    void setParallelUpdateFrequency(int i) { parallelUpdateFrequency = i; }

    public Sequential(FeaturedSpace X, Params params) {
        this.X = X;
	terminationThreshold = params.getdouble("convergenceThreshold");
	maxIterations = params.getint("maximumiterations");
	verbose = params.getboolean("verbose");

	Utils.reportMemory("Sequential");
	for (int i=0; i<X.numFeatures; i++) {
	    X.features[i].lastChange = X.features[i].lastExpectationUpdate = -1;
	    X.features[i].previousLambda = X.features[i].lambda;
	}
	setReg();
    }

    double getLoss() {
	return X.getLoss() + reg;
    }

    Feature getBestFeature() {
	double lb, bestLb = 1.0;
	Feature bestFeature = null;
	FeatureGenerator bestFeatureGen = null;
	int bestThr = -1;

	for (int i=0; i<X.numFeatures; i++) {
	    Feature f = X.features[i];
	    if (f.isActive() && !f.isGenerated() &&
		(lb = deltaLossBound(X.features[i])) < bestLb) {
		bestFeature = X.features[i];
		bestLb = lb;
	    }
	}
	for (int i=0; i<X.numFeatureGenerators; i++) {
	    FeatureGenerator featureGen = X.featureGenerators[i];
	    for (int j=featureGen.getFirstIndex(); j<featureGen.getLastIndex(); j++) {
		lb = deltaLossBound(featureGen.toFeature(j));
		if (lb < bestLb) {
		    bestFeatureGen = featureGen;
		    bestThr = j;
		    bestLb = lb;
		}
	    }
	}
	if (bestFeatureGen!=null
	    && (bestFeature=bestFeatureGen.getFeature(bestThr))==null) {
	    bestFeature = bestFeatureGen.exportFeature(bestThr);
	    Feature[] tmp = new Feature[X.numFeatures+1];
	    for (int i=0; i<X.numFeatures; i++) tmp[i] = X.features[i];
	    X.features=tmp;
	    X.features[X.numFeatures++] = bestFeature;
	}
	if (bestFeature==null)
	    Utils.warn("getBestFeature: returning null");
	return bestFeature;
    }

    double searchAlpha(Feature h, double alpha) {
	double initialLoss, currentLoss, tryLoss, initialAlpha=alpha;
	initialLoss = currentLoss = newLossChange(h, alpha);
	for (; ; alpha*=4) {
	    if (Utils.interrupt) return 0.0;
	    if ((tryLoss = newLossChange(h, alpha*4, 2)) >= currentLoss) break;
	    if (Double.isNaN(tryLoss) || Double.isInfinite(tryLoss)) {
		break;
	    }
	    currentLoss = tryLoss;
	}
	// this helps convergence time quite a bit: slightly more aggressive 
	if ((tryLoss = newLossChange(h, alpha*2)) < currentLoss && !Double.isNaN(tryLoss) && !Double.isInfinite(tryLoss)) {
	    currentLoss = tryLoss;
	    alpha *= 2;
	}
	return alpha;
    }

    //    double[] expAlphaH;
    // what's the loss if h.lambda += alpha? 
    double newLossChange(Feature h, double alpha) { 
	return newLossChange(h, alpha, 0);
    }
    // if square!=0, need this alpha to be old alpha value multiplied by 2^square
    // can return infinity, need to check when called
    double newLossChange(Feature h, double alpha, final int square) { 
	int i;
	double Z, lambda;
	for (i=0, Z=0.0; i<X.numPoints; i++)
	    Z += X.getDensity(i) * Math.exp(alpha * h.eval(i));

	lambda = h.getLambda();
	double result =
	    -alpha*h.getSampleExpectation()
	    +Math.log(Z)
	    +(Math.abs(lambda+alpha) - Math.abs(lambda))*h.getSampleDeviation();
	return result;
    }

    double deriv(Feature h) {
	double N1 = h.getSampleExpectation(), W1 = h.getExpectation();
	double weightedBeta = h.getSampleDeviation();
	double unRegDeriv = W1 - N1;
	if (h.getLambda() > 0)
	    return unRegDeriv + weightedBeta;
	else if (h.getLambda() < 0)
	    return unRegDeriv - weightedBeta;
	else {
	    if (unRegDeriv + weightedBeta > 0.)
		return unRegDeriv + weightedBeta;
	    else if (unRegDeriv - weightedBeta < 0.)
		return unRegDeriv - weightedBeta;
	    else
		return 0.0;
	}
    }

    double[] findDerivs() {
	double[] deriv = new double[X.numFeatures];
	for (int i=0; i<X.numFeatures; i++)
	    deriv[i] = deriv(X.features[i]);
	return deriv;
    }

    double xTy(double[] x, double[] y) {
	double sum = 0.0;
	for (int i=0; i<x.length; i++) sum+=x[i]*y[i];
	return sum;
    }

    double newtonStep(double[] u) {  // newton step in direction u
	ArrayList tmp = new ArrayList();
	for (int j=0; j<X.numFeatures; j++)
	    if (u[j]!=0)
		tmp.add(X.features[j]);
	Feature[] h = (Feature[]) tmp.toArray(new Feature[0]);
	double[] uu = new double[h.length];
	for (int j=0, cnt=0; j<X.numFeatures; j++)
	    if (u[j]!=0) uu[cnt++] = u[j];
	if (verbose) Utils.echoln("Updating " + h.length + " features");

	double[] sum = new double[h.length];
	double uTHu = 0.0;
	double uTY  = 0.0;
	for (int i=0; i<X.numPoints; i++) {
	    double FTu = 0.0, density = X.getDensity(i);
	    for (int j=0; j<h.length; j++) {
		double val = h[j].eval(i);
		FTu += uu[j] * val;
		sum[j] += density * val;
	    }
	    uTHu += density * FTu * FTu;
	    uTY  += density * FTu;
	}
	for (int j=0; j<h.length; j++) {
	    h[j].expectation = sum[j] / X.densityNormalizer;
	    h[j].lastExpectationUpdate = iteration;
	}
	uTY  = uTY / X.densityNormalizer;
	uTHu = uTHu / X.densityNormalizer - uTY*uTY;
	if (uTHu<eps*eps) return 0.0;

	double[] deriv = findDerivs();
	double stepSize = -xTy(deriv,u) / uTHu;

	for (int j=0; j<X.numFeatures; j++) {
	    double lambdaj = X.features[j].getLambda();
	    if ((stepSize*u[j] + lambdaj) * lambdaj < 0) {
		if (verbose) Utils.echoln("Reducing stepsize from " + stepSize + " to " + (-lambdaj / u[j]));
		stepSize = -lambdaj / u[j];
	    }
	}
	if (verbose) Utils.echoln("Newton: returning " + stepSize + " ");
	return stepSize;
    }

    // newton step in direction of unit vector, 1 @ h
    // assumes that h has an up-to-date expectation
    double newtonStep(Feature h) {  
	double uTY = h.getExpectation();
	double uTHu = 0.0;
	for (int i=0; i<X.numPoints; i++)
	    uTHu += X.getDensity(i) * h.eval(i) * h.eval(i);
	uTHu = uTHu / X.densityNormalizer - uTY*uTY;
	if (uTHu<eps*eps) return 0.0;

	double stepSize = -deriv(h) / uTHu;
	double lambda = h.getLambda();
	if ((stepSize + lambda) * lambda < 0) {
	    if (verbose) Utils.echoln("Reducing newton stepsize from " + stepSize + " to " + (-lambda));
	    stepSize = -lambda;
	}
	reportTime("NewtonStep");
	return stepSize;
    }

    void setReg() {
	reg = X.getL1reg();
    }

    double doParallelUpdate(int interval) {
	double[] alpha = new double[X.numFeatures];
	double[] contribdelta = new double[X.numFeatures];
	Feature[] f = X.features;
	for (int j=0; j<X.numFeatures; j++) {
	    if (f[j].isBinary() || f[j].lambda==0.0)
		alpha[j] = 0.0;
	    else
		alpha[j] = (f[j].lambda - f[j].previousLambda);
	    f[j].previousLambda = f[j].lambda;
	    contribdelta[j] = f[j].contribution - f[j].previousContribution;
	    if (contribdelta[j] < 0) contribdelta[j] = 0;
	    f[j].previousContribution = f[j].contribution;
	}
	double stepSize = newtonStep(alpha);
	for (int j=0; j<X.numFeatures; j++) {
	    double lambdaj = f[j].getLambda();
	    alpha[j] *= stepSize;
	    if (alpha[j] != -lambdaj && Math.abs(alpha[j]+lambdaj) < eps) {
		if (verbose) Utils.echoln("Zeroing out " + f[j].name);
		alpha[j] = -lambdaj;
	    }
	}
	double losswas = getLoss();
	Feature[] toUpdate = featuresToUpdate();
	double lossnow = increaseLambda(alpha, toUpdate);
	if (verbose) Utils.echoln("Loss was " + losswas + ", now " + lossnow + ", delta " + nf.format(lossnow-losswas));
	if (lossnow > losswas) {
	    if (verbose) Utils.echoln("Undoing...");
	    for (int j=0; j<X.numFeatures; j++)
		alpha[j] = -alpha[j];
	    lossnow = increaseLambda(alpha, toUpdate);
	    if (verbose) Utils.echoln(" loss back to " + lossnow);
	}
	else {
	    double deltaloss = losswas - lossnow;
	    double contribsum = 0.0;
	    for (int j=0; j<X.numFeatures; j++)
		if (alpha[j] != 0.0)
		    contribsum += contribdelta[j];
	    for (int j=0; j<X.numFeatures; j++) {
		if (alpha[j] != 0.0 && contribsum > 0)
		    f[j].contribution += deltaloss * contribdelta[j] / contribsum;
	    }
	}
	reportTime("parallelUpdate");
	return lossnow;
    }

    double goodAlpha(Feature h) {
        double W0, W1, N0, N1;
        N1 = h.getSampleExpectation();
        W1 = h.getExpectation();
        W0 = 1-W1;
        N0 = 1-N1;

	if (W0<eps || W1<eps) return 0; // constant feature
	double lambda = h.getLambda();
	double alpha;
	double beta1 = h.getSampleDeviation();
	if (!(N1 - beta1 > eps && 
	      (alpha = Math.log((N1 - beta1) * W0 / ((N0 + beta1) * W1)))
	      + lambda > 0.)
	    &&
	    !(N0 - beta1 > eps &&
	      (alpha = Math.log((N1 + beta1) * W0 / ((N0 - beta1) * W1)))
	      + lambda < 0.) )
	    alpha = -lambda;
	if (Double.isNaN(alpha) || Double.isInfinite(alpha))
	    Utils.fatalException("goodAlpha: returning "
				 + alpha + " for pi[f]="+nf.format(N1)+"+-"+nf.format(beta1)+", q[f]="+nf.format(W1), null);
	return alpha;
    }

    static double D(double p0, double p1, double q0, double q1) {
	if (q0<eps && p0<eps || q1<eps && p1<eps) return 0.0;
	if (q0<eps || q1<eps) return Double.POSITIVE_INFINITY;
	return p0*Math.log(p0/q0) + p1*Math.log(p1/q1);
    }
    
    public double deltaLossBound(Feature h) {
	if (!h.isActive()) return 0;
        double W0, W1, N0, N1;
        N1 = h.getSampleExpectation();
	if (N1==-1) return 0; // h doesn't have data for any samples
        W1 = h.getExpectation();
        W0 = 1-W1;
        N0 = 1-N1;
	double lambda = h.getLambda();
	double alpha = goodAlpha(h);
	double beta1 = h.getSampleDeviation();
	if (Double.isInfinite(alpha)) return 0;
	double bound =
	    -N1*alpha
	    +Math.log(W0 + W1*Math.exp(alpha))
	    +beta1*(Math.abs(lambda + alpha) - Math.abs(lambda)); 
	return (Double.isNaN(bound) ? 0 : bound);
    }

    double lasttime = 0.0;
    double timeIncrement = 0.0;
    double getTime() {
	double time = (System.currentTimeMillis()-startTime) / 1000.0;	
	timeIncrement = (lasttime == 0.0) ? time : time - lasttime;
	lasttime = time;
	return time;
    }
    void reportTime(String s) {
	if (verbose) Utils.echoln(s + ": " + getTime() + " " + nf.format(timeIncrement));
    }

    void describeIteration(Feature h, double newLoss, double oldLoss, double alpha, double lossBound, int cnt) {
	Utils.echoln(cnt + ": time = " + 
			   nf.format(getTime()) + " loss = " + nf.format(newLoss) + ((X.numTestSamples==0)?"":(" testLoss " + nf.format(X.getTestLoss()))));
	if (h!=null) {
	    double N1 = h.getSampleExpectation();
	    double W1 = h.getExpectation();
	    Utils.echoln(h.description());
	    Utils.echoln("alpha = " + nf.format(alpha) 
			  + " lossBound = " + nf.format(lossBound)
			  + " W1 = " + nf.format(W1) 
			  + " N1 = " + nf.format(N1)
			  + " deltaLoss = " + nf.format(newLoss-oldLoss));
	}
    }

    void setFeatureExpectation(Feature h) {
	double sum = 0.0;
	for (int i=0; i<X.numPoints; i++)
	    sum += X.getDensity(i) * h.eval(i);
	h.expectation = sum / X.densityNormalizer;
	h.lastExpectationUpdate = iteration;
    }

    double increaseLambda(double[] alpha, Feature[] toUpdate) {
	X.increaseLambda(alpha, toUpdate);
	for (int j=0; j<toUpdate.length; j++)
	    toUpdate[j].lastExpectationUpdate = iteration;
	setReg();
	return getLoss();
    }

    double increaseLambda(Feature h, double alpha, Feature[] toUpdate) {
	reg += (Math.abs(h.lambda + alpha) - Math.abs(h.lambda))*h.getSampleDeviation();
	X.increaseLambda(h, alpha, toUpdate);
	reportTime("increaseLambda");
	for (int j=0; j<toUpdate.length; j++)
	    toUpdate[j].lastExpectationUpdate = iteration;
	return getLoss();
    }

    int updateCycle=20, recentChange=10, topSelect=5;
    Feature[] featuresToUpdate() {
	ArrayList toUpdate = new ArrayList();
	double[] dlb = new double[X.numFeatures];
	for (int j=0; j<X.numFeatures; j++) {
	    Feature f = X.features[j];
	    int last = f.lastChange;
	    if (f.isActive() && !f.isGenerated() &&
		(iteration < last+recentChange || (j%updateCycle == iteration%updateCycle)))
		toUpdate.add(f);
	    dlb[j] = deltaLossBound(f);
	}
	int[] orderedDlb = DoubleIndexSort.sort(dlb);
	for (int j=0; j<X.numFeatures && j<topSelect; j++) {
	    Feature f = X.features[orderedDlb[j]];
	    if (f.isActive() && !f.isGenerated() && !toUpdate.contains(f))
		toUpdate.add(f);
	}
	Feature[] result = (Feature[]) toUpdate.toArray(new Feature[0]);
	if (verbose) Utils.echoln("Updating " + result.length + " features");
	return result;
    }

    double doSequentialUpdate(Feature h) {
	double alpha=0.0, newLoss=oldLoss, oldLambda=h.getLambda();
	if (!h.isGenerated() && h.lastExpectationUpdate!=iteration-1)
	    setFeatureExpectation(h);
	h.lastChange = iteration;
	Feature[] toUpdate = featuresToUpdate();
	double dlb = deltaLossBound(h);

	if (h.isBinary()) {
	    alpha = goodAlpha(h);
	    alpha = reduceAlpha(alpha);
	    newLoss = increaseLambda(h, alpha, toUpdate);
	}
	else {
	    alpha = newtonStep(h);
	    alpha = reduceAlpha(alpha);
	    newLoss = increaseLambda(h, alpha, toUpdate);
	    
	    if (newLoss - oldLoss > dlb) {
		if (verbose) Utils.echoln("Undoing: newLoss " + newLoss + ", oldLoss " + oldLoss + ", deltaLossBound " + dlb);
		increaseLambda(h, -alpha, new Feature[] { h });
		alpha = searchAlpha(h, goodAlpha(h));
		alpha = reduceAlpha(alpha);
		reportTime("Search");
		newLoss = increaseLambda(h, alpha, toUpdate);
	    }
	}

	boolean undoing = (alpha * oldLambda < 0);
	if (undoing) {
	    if (verbose) 
		Utils.echo("Undoing contribution: was " + h.contribution);
	    double frac = Math.abs(alpha / oldLambda);
	    h.contribution += (newLoss - oldLoss);
	    if (h.contribution < 0.0)
		h.contribution = 0.0;
	    // This seems sensible, but makes too big a difference
	    // if (frac>1) frac=1; // shouldn't happen
	    // h.contribution *= (1-frac);
	    if (verbose) 
		Utils.echoln(" now " + h.contribution);
	}
	else
	    h.contribution += (oldLoss - newLoss);
	if (verbose) describeIteration(h, newLoss, oldLoss, alpha, dlb, iteration);
	return newLoss;
    }

    double reduceAlpha(double alpha) {
	if (iteration<10) return alpha / 50;
	if (iteration<20) return alpha / 10;
	if (iteration<50) return alpha / 3;
	return alpha;
    }

    int convergenceTestFrequency = 20;
    double previousLoss = Double.POSITIVE_INFINITY;
    boolean terminationTest(double newLoss) {
	if (iteration == 0) {
	    previousLoss = newLoss;
	    return false;
	}
	//	if (newLoss <= 0) return true;
	if (iteration%convergenceTestFrequency!=0) return false;
	if (previousLoss-newLoss < terminationThreshold) {
	    if (verbose) Utils.echoln("Converged after " + iteration + " iterations");
	    return true;
	}
	double gain=Math.log(X.bNumPoints()) - newLoss, maxgain=10000;
	if (gain > maxgain) {
	    Utils.echoln("Terminated because gain exceeded " + maxgain);
	    // otherwise it's possible for loss to numerically overflow
	    return true;
	}
	previousLoss = newLoss;
	return false;
    }

    public double run() {
        Feature h;

	Utils.echoln("Initial loss: " + getLoss());
	//	Utils.echoln("X.getLoss " + X.getLoss() + " reg " + reg + " getN1 " + X.getN1() + " densityNormalizer " + X.densityNormalizer + " X.numPoints " + X.bNumPoints() + " linearPredictorNormalizer " + X.linearPredictorNormalizer);
	if (X.numTestSamples>0) 
	    Utils.echoln("Initial test loss: " + X.getTestLoss());
	startTime = System.currentTimeMillis();
	/* don't use binary features (from discrete variables) that are zero */
	/* as it causes overfitting because of zero sample deviation */

	double newLoss = getLoss();
        for (iteration=0; iteration < maxIterations; iteration++) {
	    if (Utils.interrupt) return 0.0;
	    oldLoss = newLoss;
	    reportStatus();
	    
	    if ((iteration>0) && (parallelUpdateFrequency>0) && (iteration%parallelUpdateFrequency==0))
		newLoss = doParallelUpdate(parallelUpdateFrequency);
	    else {
		Feature f = getBestFeature();
		if (f==null) break;
		newLoss = doSequentialUpdate(f);
	    }
	    if (terminationTest(newLoss))
		break;
	}
	describeIteration(null, newLoss, -1, -1, -1, iteration);
	//        Utils.echoln("Loss " + (X.getLoss() + reg) + " unreg " + X.getLoss() + " reg " + reg);
	//	for (int j=0; j<X.numFeatures; j++)
	//	    Utils.echoln("unreg... " + X.features[j].name + " " + X.features[j].getSampleDeviation());

	//	Utils.echoln("Total time: " + nf.format((System.currentTimeMillis()-startTime)/1000.0));
	return getLoss();
    }
}
