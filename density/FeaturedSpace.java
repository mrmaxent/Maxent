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
import java.text.*;
import java.io.*;

public class FeaturedSpace {
    
    // terminology: distribution given by "density[i]/densityNormalizer", where 
    //   density[i] = exp(linearPredictor[i] - linearPredictorNormalizer)
    //   linearPredictor[i] = sum lambda_j feature[j].eval(i).

    int numPoints, numSamples, numTestSamples, numPointsForNormalizer;
    Sample[] samples, testSamples;
    //    boolean doClamp=false;
    // if true, then we're minimizing relative entropy from "bias" 
    // distribution, subject to the constrains.
    boolean biasIsBayesianPrior=false;  
    /*
    Feature testProb;
    */
    double[] density;
    double[] linearPredictor;
    Feature[] features;
    int numFeatures;
    FeatureGenerator[] featureGenerators;
    int numFeatureGenerators;
    double linearPredictorNormalizer=0.0;   // used to ensure that max e^.. <=1
    double densityNormalizer;
    Params params;

    Feature biasDiv = null, biasDist = null;
    double entropy = -1.0;

    //    NumberFormat nf = NumberFormat.getNumberInstance();
    //    { nf.setMinimumFractionDigits(4); nf.setGroupingUsed(false); }

    boolean hasAllData(Sample s) {
	for (int j=0; j<numFeatures; j++)
	    if (!features[j].hasData(s)) return false;
	return true;
    }
	
    public void recordTestSamples(Sample[] testSamples) { 
	ArrayList tmp = new ArrayList();
	for (int i=0; i<testSamples.length; i++)
		if (hasAllData(testSamples[i]))
		    tmp.add(testSamples[i]);
	this.testSamples = (Sample[]) tmp.toArray(new Sample[0]);
	numTestSamples = this.testSamples.length;
    }

    double getPrevalence(Params params) {
	getEntropy();
	double totPred = 0.0;
	Project proj = new Project(params);
	for (int i=0; i<numPointsForNormalizer; i++) {
	    double d = getDensity(i)/densityNormalizer;
	    totPred += proj.occurrenceProbability(d, entropy);
	}
	return totPred/numPointsForNormalizer;
    }

    double getEntropy() {
	if (entropy != -1.0) return entropy;
	entropy = 0.0;
	for (int i=0; i<numPointsForNormalizer; i++) {
	    double d = getDensity(i)/densityNormalizer;
	    if (d>0)
		entropy += - d * Math.log(d);
	}
	return entropy;
    }

    double aucSD, aucmax;
    double getAUC(DoubleIterator dIterator) { 
	return getAUC(dIterator, testSamples); 
    }
    double getAUC() { return getAUC(null, testSamples); }
    double getTrainAUC() { return getAUC(null, samples); }
    double getAUC(DoubleIterator dIterator, Sample[] ss) {
	if (ss==null || ss.length==0) return 0;
	if (dIterator==null)
	    dIterator=new DoubleIterator(numPoints) {
		    double getNext() { return newDensity(i++); }
		};
	int numsamples = (ss==null) ? 0 : ss.length;
	ArrayList da = new ArrayList();
	for (int i=0; i<numsamples; i++)
	    if (hasAllData(ss[i]))
		da.add(new Double(getDensity(ss[i])));
	double[] d = new double[da.size()];
	for (int i=0; i<d.length; i++) d[i] = ((Double) da.get(i)).doubleValue();
	Arrays.sort(d);
	//	for (int i=0; i<d.length; i++)
	//	    System.out.println(d[i]/densityNormalizer + "," + 1);
	/*
	double dN = 0.0;
	int    cnt = 0;
	double[] rank = new double[numsamples];
	int[] less = new int[numsamples], equal = new int[numsamples];
	while(dIterator.hasNext()) {
	    double dCurrent=dIterator.getNext();
	    for (int i=0; i<d.length; i++) {
		if (dCurrent <  d[i]) { less[i]++; break; }
		if (dCurrent == d[i]) { equal[i]++; }
	    }
	    dN += dCurrent;
	    cnt++;
	}
	long auc_x_2 = 0;
	for (int i=0; i<numsamples; i++) {
	    auc_x_2 += ((long)(numsamples) - i) * less[i] * 2;
	    auc_x_2 += equal[i];
	}
	return auc_x_2 / ( ((long)numsamples) * cnt * 2.0);
	*/
	/*
	int cnt=0;
	while(dIterator.hasNext()) {
	    cnt++;
	    dIterator.getNext();
	}
	dIterator.reset();
	double[] y = new double[cnt];
	for (int i=0; i<cnt; i++)
	    y[i] = dIterator.getNext();
	*/
	double[] y = dIterator.getvals();
	//	for (int i=0; i<y.length; i++)
	//	    System.out.println(y[i]/densityNormalizer + "," + 0);
	Arrays.sort(y);
	// e104 = e10 * 4, e10 from Delong et al 1988, eqn (2).
	int dn=d.length, yn=y.length, dc=0, yc=0;
	long auc2=0, e104=0, e014=0, e114=0, aucmax2=0;
	double cum=0.0;
	while (dc<dn || yc<yn) {
	    double t = (dc<dn && (yc==yn || d[dc] < y[yc])) ? d[dc] : y[yc];
	    long de=0, ye=0;
	    for ( ; dc < dn && d[dc] == t; dc++) de++;
	    for ( ; yc < yn && y[yc] == t; yc++) ye++;
	    long g = dn-dc;           // positives above threshold
	    long l = (long) yc - ye;  // negatives below threshold
	    auc2 += de * l * 2 + de*ye;
	    aucmax2 += t * ye * (l * 2 + ye);
	    cum += t*ye;
	    e114 += de * l * 4 + de*ye;
	    e104 += de * (l*(l-1)*2 + ye*(ye-1)/2 + l*ye*2);
	    e014 += ye * (g*(g-1)*2 + de*(de-1)/2 + g*de*2);
	}
	double auc = auc2/(((long) dn)*yn*2.0);
	aucmax = aucmax2 / (cum*yn*2.0);
	double e10 = e104/(2.0*dn*yn*(yn-1)) - auc*auc;
	double e01 = e014/(2.0*dn*(dn-1)*yn) - auc*auc;
	double e11 = e114/(4.0*dn*yn) - auc*auc;
	double variance = ((yn-1)*e10 + (dn-1)*e01 + e11) / (((long) dn)*yn);
	aucSD = (dn==1) ? -1.0 : Math.sqrt(variance);
	//	System.out.println("auc " + auc + ", auc se " + aucSD);
	return auc;
    }
    
    double getLoss() {
	return getN1() + Math.log(densityNormalizer);
    }

    double getL1reg() {
	double result = 0.0;
	for (int j=0; j<numFeatures; j++)
	    result += Math.abs(features[j].lambda)*features[j].getSampleDeviation();
	return result;
    }

    public double getTestLoss() {
	return getN1(testSamples) + Math.log(densityNormalizer);
    }

    double getN1() {
	double sum=0.0;
	for (int i=0; i<numFeatures; i++)
	    sum += features[i].getLambda() * features[i].getSampleExpectation();
	return -sum + linearPredictorNormalizer;
    }

    double getN1(Sample[] ss) {
	double sum=0.0;
	for (int i=0; i<numFeatures; i++) {
	    Feature f = features[i];
	    double sum2=0.0;
	    int    cnt=0;
	    for (int j=0; j<ss.length; j++)
		if (f.hasData(ss[j])) {
		    cnt++;
		    sum2 += f.eval(ss[j]);
		}
	    sum += f.getLambda() * (sum2/cnt);
	}
	return -sum + linearPredictorNormalizer;
    }

    double getN1clamp(Sample[] ss) {
	double[] mean=new double[numFeatures];
	for (int j=0; j<numFeatures; j++) {
	    Feature f = features[j];
	    double sum=0.0;
	    int    cnt=0;
	    for (int i=0; i<ss.length; i++)
		if (f.hasData(ss[i])) {
		    cnt++;
		    sum += f.eval(ss[i]);
		}
	    mean[j]=sum/cnt;
	}
	double sum=0.0;
	for (int i=0; i<ss.length; i++) {
	    double lp = 0.0;
	    for (int j=0; j<numFeatures; j++)
		lp += features[j].getLambda() *
		    (features[j].hasData(ss[i]) ? features[j].eval(ss[i]) : mean[j]);
	    if (lp<linearPredictorNormalizer)
		sum += lp - linearPredictorNormalizer;
	}
	return -sum/ss.length;
    }

    /*
    void recordTestProb(Feature f) {
	testProb = f;
    }

    double getTestRE() {
	double sum=0.0;
	for (int i=0; i<numPoints; i++) {
 	    double val = testProb.eval(i);
 	    sum += val*(linearPredictor[i]-Math.log(val));
	}
	return -sum + linearPredictorNormalizer + Math.log(densityNormalizer);
    }
    */

    public double linearPredictor(Sample s) {
	double lp = 0.0;
	for (int i=0; i<numFeatures; i++) {
	    if (!features[i].hasData(s)) return -1;
	    lp += features[i].getLambda() * features[i].eval(s);
	}
	//	if (doClamp && lp>linearPredictorNormalizer)
	//	    return linearPredictorNormalizer;
	//	else
	    return lp;
    }

    public double getDensity(Sample s) {
	double lp = linearPredictor(s);
	if (lp==-1) return -1;
	return ((biasDist==null)?1.0:biasDist.eval(s)) * Math.exp(lp - linearPredictorNormalizer);
    }

    public double newDensity(int p) {
	double lp = 0.0;
	for (int i=0; i<numFeatures; i++) {
	    lp += features[i].getLambda() * features[i].eval(p);
	}
	//	if (doClamp && lp>linearPredictorNormalizer)
	//	    lp = linearPredictorNormalizer;
	return biasDist.eval(p)*Math.exp(lp - linearPredictorNormalizer);
    }

    public FeaturedSpace(Sample[] samples, Feature[] f, Params params) { //, boolean doClamp) {
        this.params = params;
	this.numSamples = samples.length;
	this.samples = samples;
	//	this.doClamp = doClamp;
	numPoints = numPointsForNormalizer = (f.length==0) ? 0 : f[0].n;
	minDeviation = 0.001;	
	if (params != null)
	    minDeviation *= params.getBetamultiplier();

	ArrayList featuresList = new ArrayList();
	ArrayList featureGeneratorsList = new ArrayList();

	for (int i=0; i<f.length; i++)
	    // keep the linear features around but inactive, so they 
	    // are written in writeFeatureWeights and used for clamping
	    if (f[i].isActive() || f[i].type()==Feature.LINEAR)
		switch (f[i].type()) {
		case Feature.THR_GEN:
		    featureGeneratorsList.add(new ThrFeatureGenerator(samples, f[i])); break;
		case Feature.HINGE_GEN:
		    featureGeneratorsList.add(new HingeFeatureGenerator(samples, f[i])); break;
		case Feature.L_F_BIAS_OUT:
		    biasDist = f[i]; break;
		case Feature.L_DEBIAS_AVG:
		    biasDiv = f[i]; break;
		default:
		    featuresList.add(f[i]); break;
		}

	features = (Feature[]) featuresList.toArray(new Feature[0]);
	numFeatures = features.length;

	featureGenerators = (FeatureGenerator[]) featureGeneratorsList.toArray(new FeatureGenerator[0]);
	numFeatureGenerators = featureGenerators.length;

	setBiasDiv(biasDiv);   // also sets sample expectations

	density = new double[numPoints];
	Utils.reportMemory("Density");
	linearPredictor = new double[numPoints];
	Utils.reportMemory("linearPredictor");
	setLinearPredictor();
	setBiasDist(biasDist); // also sets density
    }

    Feature scaledBiasDist(final Feature biasDist) {
	double max=0.0;
	for (int i=0; i<numPoints; i++)
	    if (biasDist.eval(i) > max) max = biasDist.eval(i);
	final double themax = max;
	return new Feature(numPoints, "scaled bias") {
		public double eval(int p) { 
		    return biasDist.eval(p) / themax; 
		}
		public double eval(Sample s) { 
		    return biasDist.eval(s) / themax; 
		}
	    };
    }

    void setBiasDist(Feature biasDistInit) {
	biasDist = (biasDistInit==null) ? new ConstFeature(1.0, numPoints) : scaledBiasDist(biasDistInit);
	for (int i=0; i<numPoints; i++)
	    if (biasDist.eval(i) <= 0) {
		//		System.out.println(i + " " + biasDist.eval(i) + " " + biasDist);
		Utils.warn2("Bias has zero or negative values, ignoring bias grid", "ignorebias");
		biasDist = new ConstFeature(1.0, numPoints);
		//		break;
	    }
	setDensity();
    }
    void setBiasDiv(Feature biasDivInit) {
	biasDiv  = (biasDivInit==null) ? new ConstFeature(1.0, numPoints) : biasDivInit;
	setSampleExpectations();
    }

    public static class SampleInfo {
	public double avg, std;
	public double min, max;
	public int sample_cnt;
	SampleInfo(double avg, double std, double min, double max, int sample_cnt) {
	    this.avg = avg;
	    this.std = std;
	    this.min = min;
	    this.max = max;
	    this.sample_cnt = sample_cnt;
	}
	SampleInfo(SampleInfo init) {
	    avg = init.avg;
	    std = init.std;
	    min = init.min;
	    max = init.max;
	    sample_cnt = init.sample_cnt;
	}
	public String toString() { 
	    return avg + " " + std + " " + min + " " + max + " " + sample_cnt; 
	}
    }

    public static class Interval {
	public double low, high;
	Interval(double low, double high) {
	    this.low = low;
	    this.high = high;
	}
	Interval(SampleInfo f, double beta) {
	    if (f.sample_cnt==0) {
		low = f.min;
		high = f.max;
	    }
	    else {
		low = f.avg - beta/Math.sqrt(f.sample_cnt)*f.std;
		high = f.avg + beta/Math.sqrt(f.sample_cnt)*f.std;
		//		if (low<f.min) low = f.min;
		//		if (high>f.max) high = f.max;
	    }
	}
	Interval(Interval a, Interval b) {
	    //	    if (a.low<0 || b.low<0) {
	    if (b.low<0) {
		low = Double.POSITIVE_INFINITY;
		high = Double.NEGATIVE_INFINITY;
		Utils.warn("Error: Dividing intervals that include negatives");
	    }
	    else {
		low = a.low/b.high;
		high = a.high/b.low;
	    }
	}
	Interval(SampleInfo dividedFeature, SampleInfo dividedBias, double beta) {
	    this(new Interval(dividedFeature, beta),
		 new Interval(dividedBias, beta));
	}
	double getMid() { return 0.5*(low+high); }
	double getDev() { return 0.5*(high-low); }
    }

    SampleInfo getDividedSampleInfo(Feature f, Feature bias) {
	double min=Double.POSITIVE_INFINITY, max=Double.NEGATIVE_INFINITY;
	for (int i=0; i<f.n; i++) {
	    double val = f.eval(i)/bias.eval(i);
	    if (val<min) min = val;
	    if (val>max) max = val;
	}
	double avg=0.0, std=0.0;
	int cnt=0;
	for (int j=0; j<numSamples; j++)
	    if (f.hasData(samples[j])) {
		double val = f.eval(samples[j])/bias.eval(samples[j]);
		avg+= val;
		std+= val*val;
		cnt++;
	    }
	if (cnt==0) {
	    avg = (min+max)/2;
	    std = 0.5*(max-min);
	}
	else if (cnt==1)
	    std = 0.5*(max-min);
	else {
	    avg /= cnt;
	    if (std < cnt*avg*avg) // due to floating point error
		std = 0.0;
	    else
		std = Math.sqrt((std-cnt*avg*avg)/(cnt-1));
	    if (std > 0.5*(max-min))
		std = 0.5*(max-min);
	    /*
	    if (std < (max-min)/Math.sqrt(cnt))
	        std = (max-min)/Math.sqrt(cnt);
	    */
	}
	return new SampleInfo(avg, std, min, max, cnt);
    }
    
    SampleInfo biasInfo = null;
    public static double minDeviation = 0.001;
    void setSampleExpectations() {
	Utils.echoln(numSamples + " samples");
	biasInfo = getDividedSampleInfo(new ConstFeature(1.0,biasDiv.n), biasDiv);
	for (int i=0; i<numFeatures; i++) {
	    Feature f = features[i];
	    SampleInfo fInfo = getDividedSampleInfo(f, biasDiv);
	    Interval fInterval = new Interval(fInfo, biasInfo, f.beta);
	    f.sampleExpectation = fInterval.getMid();
	    f.sampleDeviation   = 
		// Math.sqrt(fInterval.getDev()*fInterval.getDev() + 1.0/fInfo.sample_cnt);
		fInterval.getDev();
	    if (f.sampleDeviation < minDeviation) {
		int m = fInfo.sample_cnt;
		if (f.type() == Feature.BINARY && f.sampleExpectation == 1 && m > 0)
		    f.sampleDeviation = 1 / (2 * (double) m);
		// Math.sqrt(m / ((double) (m+1)*(m+1))); // as if m 1's, 1 zero.
		else
		    f.sampleDeviation = minDeviation;
	    }
	    if (fInfo.sample_cnt==0)
		f.setActive(false);
	}
	for (int j=0; j<numFeatureGenerators; j++)
	    featureGenerators[j].setSampleExpectations(this);
    }
	
    // This is very close to sqrt(p/m), slightly bigger if p small (near 1/m)
    double findEpsilon(double p, int m) {
	if (p>0.5) return findEpsilon(1-p, m);
	if (p<0.99/m) return findEpsilon(1.0/m, m);
	double target = 1 / (2.0 * m);  // want RE(p+eps||p) to be equal to this
	double upper = 1-1e-10, lower = p+1e-10;
	while (upper-lower > .01/m) {
	    double mid = (upper+lower)/2.0;
	    if (RE(mid, p) < target)
		lower = mid;
	    else
		upper = mid;
	}
	return (upper+lower)/2.0 - p;
    }
    double RE(double q, double p) {
	return q * Math.log(q/p) + (1-q) * Math.log((1-q) / (1-p));
    }


    //  Need to merge shared parts of this with the other initializer for FeaturedSpace, 
    //  and with Project.java.
    HashMap clampedBaseFeatures;
    public FeaturedSpace(GridSet gs, String lambdaFile, boolean clamp) {
	this(gs, lambdaFile, clamp, false);
    }
    public FeaturedSpace(GridSet gs, String lambdaFile, boolean clamp, boolean renormalize) {
	clampedBaseFeatures = new HashMap();
	BufferedReader in;
	ArrayList allFeatures = new ArrayList();
	String line;
	try {
	    in = new BufferedReader(new FileReader(lambdaFile));
	    while ((line=in.readLine()) != null) {
		StringTokenizer st = new StringTokenizer(line, ",");
		String descr = st.nextToken();
		double lambda = Double.parseDouble(st.nextToken());
		double min=0.0, max=1.0;
		if (st.hasMoreTokens()) {
		    min = Double.parseDouble(st.nextToken());
		    max = Double.parseDouble(st.nextToken());
		}
		boolean isLinear = false;
		Feature f=null;
		int index;
		if ((index = descr.indexOf('*')) != -1) { // product
		    String f0 = descr.substring(0,index);
		    String f1 = descr.substring(index+1);
		    Feature fc0 = clampedBaseFeatures.containsKey(f0) ?
			(Feature) clampedBaseFeatures.get(f0) : gs.getFeature(f0);
		    Feature fc1 = clampedBaseFeatures.containsKey(f1) ?
			(Feature) clampedBaseFeatures.get(f1) : gs.getFeature(f1);
		    f = new ProductFeature(fc0, f0, fc1, f1);
		}
		else if ((index = descr.indexOf("^2")) != -1) { // quadratic
		    String f0 = descr.substring(0,index);
		    Feature fc = clampedBaseFeatures.containsKey(f0) ?
			(Feature) clampedBaseFeatures.get(f0) : gs.getFeature(f0);
		    f = new SquareFeature(fc, f0);
		}
		else if ((index = descr.indexOf("^p")) != -1) { // polyhedral
		    String f0 = descr.substring(0,index);
		    Feature fc = clampedBaseFeatures.containsKey(f0) ?
			(Feature) clampedBaseFeatures.get(f0) : gs.getFeature(f0);
		    f = new PolyhedralFeature(fc, f0);
		}
		else if ((index = descr.indexOf('=')) != -1) { // binary
		    String f0 = descr.substring(1,index);
		    float val = Float.parseFloat(descr.substring(index+1, descr.length()-1));
		    f = new BinaryFeature(gs.getFeature(f0), val, f0);
		}
		else if ((index = descr.indexOf('<')) != -1) { // threshold
		    String f0 = descr.substring(index+1, descr.length()-1);
		    double val = Double.parseDouble(descr.substring(1,index));
		    f = new ThresholdFeature(gs.getFeature(f0), val, f0);
		}
		else if ((index = descr.indexOf('\'')) != -1) { // hinge
		    String f0 = descr.substring(index+1);
		    f = new HingeFeature(gs.getFeature(f0), min, max, f0);
		}
		else if ((index = descr.indexOf('`')) != -1) { // hinge
		    String f0 = descr.substring(index+1);
		    f = new HingeFeature(Runner.revFeature(gs.getFeature(f0)), -max, -min, f0);
		}
		else if (descr.equals("linearPredictorNormalizer"))
		    linearPredictorNormalizer = lambda;
		else if (descr.equals("densityNormalizer"))
		    densityNormalizer = lambda;
		else if (descr.equals("numBackgroundPoints"))
		    numPointsForNormalizer = (int) lambda;
		else if (descr.equals("entropy"))
		    entropy = lambda;
		else { // linear
		    f = gs.getFeature(descr);
		    if (clamp) clampedBaseFeatures.put(descr, new ClampedFeature(f, min, max));
		    isLinear = true;
		}
		if (f!=null && (isLinear || lambda!=0.0)) {
		    Feature ff = (f instanceof HingeFeature) ? f : new ScaledFeature(f, min, max);
		    if (clamp)
			ff = new ClampedFeature(ff, 0.0, 1.0);
		    ff.setLambda(lambda);
		    allFeatures.add(ff);
		}
	    }
	}
	catch (IOException e) {
	    Utils.fatalException("Error reading .lambdas file " + lambdaFile, e);
	}

	numPoints = numPointsForNormalizer = gs.getNumPoints();
	numFeatures = allFeatures.size();
	features = (Feature[]) allFeatures.toArray(new Feature[0]);

	numFeatureGenerators = 0;
	featureGenerators = new FeatureGenerator[0];

	density = new double[numPoints];
	linearPredictor = new double[numPoints];
	double tmpLinearPredictorNormalizer = linearPredictorNormalizer;
	double tmpDensityNormalizer = densityNormalizer;
	setLinearPredictor();
	linearPredictorNormalizer = tmpLinearPredictorNormalizer;
	/*
	if (clamp)
	    for (int i=0; i<numPoints; i++)
		if (linearPredictor[i] > linearPredictorNormalizer)
		    linearPredictor[i] = linearPredictorNormalizer;
	*/
	setDensity(new Feature[0]); // don't need the feature expectations
	if (!renormalize) 
	    densityNormalizer = tmpDensityNormalizer;
    }

    void describe() {
	for (int i=0; i<numFeatures; i++)
	    if (features[i].lambda!=0)
		features[i].describe();
    }
    
    void setDensityNormalizer(double x) { densityNormalizer = x; }
    public double getDensityNormalizer() { return densityNormalizer; }

    public double getLinearPredictorNormalizer() { return linearPredictorNormalizer; }
    void setLinearPredictorNormalizer() {
	linearPredictorNormalizer= (numPoints==0) ? 0.0 : linearPredictor[0];
	for (int i=1; i<numPoints; i++)
	    if (linearPredictor[i]>linearPredictorNormalizer)
		linearPredictorNormalizer=linearPredictor[i];
    }
    void setLinearPredictorNormalizer(double x) { linearPredictorNormalizer = x; }

    double[] getWeights() { return getWeights(false); }
    double[] getWeights(boolean destructive) {
 	double[] result = destructive ? linearPredictor : new double[numPoints];
	for (int i=0; i<numPoints; i++) 
	    result[i] = getDensity(i) / densityNormalizer;
	return result;
    }

    void increaseLambda(Feature h, double alpha) { increaseLambda(h, alpha, features); }
    void increaseLambda(Feature h, double alpha, Feature[] updateFeatures) {
	if (alpha==0) return;
	h.increaseLambda(alpha);
	for (int i=0; i<numPoints; i++) {
	    linearPredictor[i] += alpha * h.eval(i);
	    if (i==0 || (linearPredictor[i]>linearPredictorNormalizer))
		linearPredictorNormalizer=linearPredictor[i];
	}
	setDensity(updateFeatures);
    }

    double getDensity(int i) { 
	if (biasIsBayesianPrior)
	    return biasDist.eval(i) * density[i];
	return density[i]; 
    }

    void setDensity() {
	ArrayList toUpdate=new ArrayList();
	for (int i=0; i<features.length; i++)
	    if (features[i].isActive() && !features[i].isGenerated())
		toUpdate.add(features[i]);
	setDensity((Feature[]) toUpdate.toArray(new Feature[0]));
    }

    double bNumPoints() { // number of points, factoring in bias
	double np=0;
	for (int i=0; i<numPoints; i++)
	    np += biasDist.eval(i);
	return np;
    }

    void setDensity(Feature[] toUpdate) {
	double[] sum = new double[toUpdate.length];
	Arrays.fill(sum, 0.0);
	densityNormalizer=0.0;
	for (int i=0; i<numPoints; i++) {
	    double ddensity = biasDist.eval(i)*Math.exp(linearPredictor[i] - linearPredictorNormalizer);
	    density[i] = ddensity;
	    for (int j=0; j<toUpdate.length; j++)
		sum[j] += ddensity * toUpdate[j].eval(i);
	    densityNormalizer += ddensity;
	}
	for (int j=0; j<toUpdate.length; j++)
	    toUpdate[j].expectation = sum[j] / densityNormalizer;
	for (int j=0; j<numFeatureGenerators; j++)
	    featureGenerators[j].updateFeatureExpectations(this);
    }

    void setDensityNormalizer(DoubleIterator d) {
	d.reset();
	densityNormalizer = 0.0;
	numPointsForNormalizer = 0;
	while(d.hasNext()) {
	    densityNormalizer += d.getNext();
	    numPointsForNormalizer++;
	}
    }

    void setLinearPredictor() { setLinearPredictor(false); }
    void setLinearPredictor(boolean report) {
	Arrays.fill(linearPredictor, 0);
	for (int j=0; j<numFeatures; j++) {
	    if (report) Utils.reportProgress(j*100/(double) numFeatures);
	    if (features[j].getLambda() != 0)
		for (int i=0; i<numPoints; i++)
		    linearPredictor[i] += features[j].getLambda() * features[j].eval(i);
	}
	setLinearPredictorNormalizer();
    }

    void increaseLambda(double[] alpha) { increaseLambda(alpha, features); }
    void increaseLambda(double[] alpha, Feature[] toUpdate) {
	for (int j=0; j<numFeatures; j++) {
	    if (alpha[j]==0) continue;
	    features[j].increaseLambda(alpha[j]);
	    for (int i=0; i<numPoints; i++)
		linearPredictor[i] += alpha[j] * features[j].eval(i);
	}
	setLinearPredictorNormalizer();
	setDensity(toUpdate);
    }

    void writeFeatureWeights(String outFile) throws IOException {
	PrintWriter out= Utils.writer(outFile);
	writeWeights(out);
    }

    String writeFeatureWeights() throws IOException {
	StringWriter out = new StringWriter();
	writeWeights(new PrintWriter(out));
	return out.toString();
    }

    void writeWeights(PrintWriter out) throws IOException {
	boolean written=false;  // want at least one feature, even if 0
	for (int i=0; i<numFeatures; i++) {
	    if (features[i].lambda==0 
		&& (written || i<numFeatures-1)
		&& (!(features[i].type()==Feature.LINEAR))
		&& (!(features[i].type()==Feature.BINARY) || 
		    !((BinaryFeature)features[i]).isFirstCategory))
		continue; // need linear features for base feature clamping
	    // and need first binary category so that masks work if they 
	    // are given as categorical
	    written=true;
	    if (features[i] instanceof ScaledFeature)
		out.println(features[i].name + ", " + features[i].lambda + ", "
			    + ((ScaledFeature) features[i]).min + ", " + ((ScaledFeature) features[i]).max);
	    else if (features[i] instanceof HingeFeature) {
		if (features[i].name.endsWith("__rev"))
		    out.println(features[i].name.replaceAll("'","`").replaceAll("__rev","") + ", " + features[i].lambda + ", "
				+ (-((HingeFeature) features[i]).max) + ", " + (-((HingeFeature) features[i]).min));
		else 
		    out.println(features[i].name + ", " + features[i].lambda + ", "
				+ ((HingeFeature) features[i]).min + ", " + ((HingeFeature) features[i]).max);
	    }
	    else
		out.println(features[i].name + ", " + features[i].lambda + ", 0.0, 1.0");
	}
	out.println("linearPredictorNormalizer, " + linearPredictorNormalizer);
	out.println("densityNormalizer, " + densityNormalizer);
	out.println("numBackgroundPoints, " + numPointsForNormalizer);
	out.println("entropy, " + getEntropy());
	out.close();
    }

    double[][] coords;
    int numCoords() { return coords.length; }
    void setXY(double[][] xy) { coords = xy; }
    double getX(int i) { return coords[i][0]; }
    double getY(int i) { return coords[i][1]; }
}
