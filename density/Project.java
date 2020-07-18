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

import java.io.*;
import java.util.*;

public class Project {
    HashMap gridMap = new HashMap(), clampedGridMap = new HashMap();
    double linearPredictorNormalizer, densityNormalizer;
    int numPointsForNormalizer;
    String gridDir;
    GridSetFromFile gs = null;
    boolean doClamp;
    Grid priorDistribution=null;
    Layer[] needLayers; // used if projecting to a .csv
    double entropy = -1.0, prednoclamp, predclamp;
    boolean outGridHasData;
    Params params;
    double[][] raw2cum=null;
    ArrayList<Pfeature> allFeatures;

    public Project(Params params) { 
	this.params = params;
	doClamp = is("doClamp");
    }

    boolean is(String s) { return params.getboolean(s); }
    boolean cumulative() { return params.cumulative(); }

    Grid[] allGrids() {
	ArrayList result = new ArrayList();
	for (Iterator it = gridMap.keySet().iterator(); it.hasNext(); )
	    result.add(gridMap.get(it.next()));
	return (Grid[]) result.toArray(new Grid[0]);
    }

    ClampedGrid[] allClampedGrids() {
	return (ClampedGrid[]) clampedGridMap.values().toArray(new ClampedGrid[0]);
    }

    Grid getGrid(String s) throws IOException {
	if (!gridMap.containsKey(s))
	    gridMap.put(s, newGrid(s));
	return (Grid) gridMap.get(s);
    }

    Grid getClampedGrid(String s) throws IOException {
	if (!clampedGridMap.containsKey(s))
	    return getGrid(s);
	return (Grid) clampedGridMap.get(s);
    }

    Grid revGrid(final Grid g) {
	return new Grid(g.getDimension(), g.name) {
		public float eval(int r, int c) { return -g.eval(r,c); }
		public boolean hasData(int r, int c) { return g.hasData(r,c); }
	    };
    }

    public boolean mapping=false, exponent=false;
    public HashMap varmap;
    Grid mapGrid(final String filename) {
	return new Grid(new GridDimension(0,0,1,0,0), filename) {
		public float eval(int r, int c) { 
		    return (float) ((Double) varmap.get(filename)).doubleValue();
		}
		public boolean hasData(int r, int c) { return true; }
	    };
    }

    Grid newGrid(String filename) throws IOException {
	if (mapping) return mapGrid(filename);
	if (gs!=null) return gs.getGrid(filename);
	String path = Utils.getGridAbsolutePath(gridDir, filename);
	if (path==null) Utils.interrupt = true;
	if (Utils.interrupt) return null;
	String cfilename = Extractor.CachedGridFilename(path, true);
	return new LazyGrid(cfilename);
    }

    public void doProject(String lambdaFile, String gridDir, String outFile) throws IOException {
	doProject(lambdaFile, gridDir, outFile, null);
    }
    public void doProject(String lambdaFile, GridSetFromFile gs, String outFile) throws IOException {
	this.gs = gs;
	doProject(lambdaFile, (String) null, outFile, null);
    }
    public void doProject(String lambdaFile, String gridDir, String outFile, String clampedFileName) throws IOException {
	Utils.reportDoing("Projecting...");
	Grid[] outGrids = projectGrid(lambdaFile, gridDir);
	if (clampedFileName==null) outGrids = new Grid[] { outGrids[0] };
	if (!Utils.interrupt) {
	    outGrids[0].name = outGrids[0].name + " " + params.getString("outputformat") + " values";
	    String[] filenames = new String[] { outFile, clampedFileName };
	    GridWriter.writeGrids(outGrids, filenames, cumulative());
	}
	close();
    }

    public HashSet gridNames() {
	HashSet result = new HashSet();
	for (Grid g: allGrids()) {
	    result.add(g.getName());
	}
	return result;
    }

    void close() throws IOException {
	Grid[] grids = allGrids();
	for (int i=0; i<grids.length; i++)
	    if (grids[i] instanceof LazyGrid)
		((LazyGrid) grids[i]).close();
    }

    public Grid[] projectGrid(final String lambdaFile, String gridDir) throws IOException {
	if (gridDir!=null && new File(gridDir).isFile())
	    gs = new GridSetFromFile(gridDir, needLayers);
	this.gridDir = gridDir;
	BufferedReader in = new BufferedReader(new File(lambdaFile).exists() ?
					       new FileReader(lambdaFile):
					       new StringReader(lambdaFile));
	String gridname = new File(lambdaFile).getName().replaceAll(".lambdas", "");
	allFeatures = new ArrayList();
	ArrayList allLambdas = new ArrayList();
	ArrayList allGrids = new ArrayList();
	String line;
	while ((line=in.readLine()) != null) {
	    StringTokenizer st = new StringTokenizer(line, ",");
	    String descr = st.nextToken();
	    double lambda = Double.parseDouble(st.nextToken());
	    double min=0.0, max=1.0;
	    if (st.hasMoreTokens()) {
		min = Double.parseDouble(st.nextToken());
		max = Double.parseDouble(st.nextToken());
	    }
	    Pfeature g=null;
	    int index;
	    if ((index = descr.indexOf('*')) != -1) { // product
		String f0 = descr.substring(0,index);
		String f1 = descr.substring(index+1);
		g = new ProductGrid(getClampedGrid(f0), getClampedGrid(f1));
	    }
	    else if ((index = descr.indexOf("^2")) != -1) { // quadratic
		String f0 = descr.substring(0,index);
		g = new SquareGrid(getClampedGrid(f0));
	    }
	    else if ((index = descr.indexOf("^p")) != -1) { // polyhedral
		String f0 = descr.substring(0,index);
		g = new PolyhedralGrid(getClampedGrid(f0));
	    }
	    else if ((index = descr.indexOf('=')) != -1) { // binary
		String f0 = descr.substring(1,index);
		double val = Double.parseDouble(descr.substring(index+1, descr.length()-1));
		Grid gg = getGrid(f0);
		if (Utils.interrupt) return null;
		g = new BinaryGrid(gg, val);
	    }
	    else if ((index = descr.indexOf('<')) != -1) { // threshold
		String f0 = descr.substring(index+1, descr.length()-1);
		double val = Double.parseDouble(descr.substring(1,index));
		g = new ThresholdGrid(getGrid(f0), val);
	    }
	    else if ((index = descr.indexOf('\'')) != -1) { // hinge
		String f0 = descr.substring(index+1);
		//		g = new HingeGrid(getGrid(f0), min, max);
		allLambdas.add(new Double(lambda));
		HingeGrid gg = new HingeGrid(getGrid(f0), min, max);
		gg.name = descr;
		allFeatures.add(gg);
	    }
	    else if ((index = descr.indexOf('`')) != -1) { // revhinge
		String f0 = descr.substring(index+1);
		//		g = new HingeGrid(getGrid(f0), min, max);
		allLambdas.add(new Double(lambda));
		HingeGrid gg = new HingeGrid(revGrid(getGrid(f0)), -max, -min);
		gg.name = descr;
		allFeatures.add(gg);
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
		Grid gg = getGrid(descr);
		if (Utils.interrupt) return null;
		g = new LinearGrid(gg);
		clampedGridMap.put(descr, new ClampedGrid(gg, min, max));
	    }
	    if (g!=null) {
		Pfeature gg = new ScaledGrid(g, min, max);
		allLambdas.add(new Double(lambda));
		allFeatures.add(gg);
		gg.name = descr;
	    }
	}

	final Grid[] grids = allGrids();
	final Pfeature[] features = (Pfeature[]) allFeatures.toArray(new Pfeature[0]);
	final double[] lambdas = new double[features.length];
	final double[] abslambdas = new double[features.length];
	for (int i=0; i<lambdas.length; i++) {
	    lambdas[i] = ((Double) allLambdas.get(i)).doubleValue();
	    abslambdas[i] = Math.abs(((Double) allLambdas.get(i)).doubleValue());
	}

	GridDimension dim = grids[0].getDimension();
	for (int i=1; i<grids.length; i++) {
	    if (!grids[i].getDimension().equals(dim))
		throw new IOException("Files " + grids[0].getName() + " and " + grids[i].getName() + " in directory " + gridDir + " have different geographic dimensions");
	}

	final double dN = densityNormalizer, lPN = linearPredictorNormalizer;
	final ClampedGrid[] cgrids = allClampedGrids();

	Grid outGrid = new Grid(dim, gridname) {
		{ if (cumulative() && raw2cum==null)
			raw2cum = readCumulativeIndex(Runner.raw2cumfile(lambdaFile)); 
		}
		final boolean fade = is("fadeByClamping");
		final boolean minclamp = is("minClamping");
		final boolean strictClamp = !is("extrapolate");
		public boolean hasData(int r, int c) {
		    outGridHasData=false;
		    for (int i=0; i<grids.length; i++)
			if (!grids[i].hasData(r,c)) 
			    return false;
		    if (priorDistribution!=null && !priorDistribution.hasData(r,c))
			return false;
		    outGridHasData=true;
		    return true;
		}
		boolean wouldExtrapolate(int r, int c) {
		    for (int i=0; i<cgrids.length; i++)
			if (cgrids[i].wouldClamp(r,c))
			    return true;
		    return false;
		}
		double plainsum(int r, int c) {
		    double sum=0.0;
		    for (int i=0; i<features.length; i++)
			if (lambdas[i] != 0.0)
			    sum += lambdas[i] * features[i].eval(r,c);
		    return sum;
		}
		double pred(int r, int c, boolean complaints) {
		    double sum = plainsum(r,c);
		    if (exponent) return sum-lPN;
		    double pred = (Math.exp(sum-lPN) / dN);
		    if (pred>1) pred=1;
		    if (priorDistribution!=null)
			pred *= priorDistribution.eval(r,c);
		    if (entropy!=-1 && params.logistic())
			pred = logistic(pred, entropy);
		    if (entropy!=-1 && params.cloglog())
			pred = cloglog(pred, entropy);
		    if (Double.isNaN(pred) || Double.isInfinite(pred) || Float.isInfinite((float) pred)) {
			double newPred = (sum>lPN) ? (cumulative()?100:1.0) : 0.0;
			if (complaints)
			    Utils.warn("Setting prediction at row " + r + " column " + c + " to " + newPred + ", was " + pred + "; sum=" + sum + " lPN " + lPN + " pred=" + pred + " dN=" + dN + " entropy=" + entropy);
			pred = newPred;
		    }
		    if (cumulative())
			return interpolateCumulative(raw2cum, pred);
		    return pred;
		}

		public float eval(int r, int c) {
		    doClamp = is("doClamp");
		    double pred = predclamp = pred(r, c, true);
		    if (doClamp) {
			doClamp = false;
			prednoclamp = pred(r, c, false);
			doClamp = true;
		    } else
			prednoclamp = predclamp;
		    if (prednoclamp < pred && minclamp) pred = prednoclamp;
		    if (strictClamp && wouldExtrapolate(r,c))
			pred = exponent ? -100.0 : 0.0;
		    if (fade) {
			pred -= Math.abs(predclamp-prednoclamp);
			if (pred<0 && !exponent) pred=0;
		    }
		    return (float) pred;
		}
	    };
	
	Grid clampGrid = new Grid(dim, "Clamping") {
		public boolean hasData(int r, int c) {
		    return outGridHasData;
		}
		public float eval(int r, int c) {
		    return (float) Math.abs(predclamp - prednoclamp);
		}
	    };
	return new Grid[] { outGrid, clampGrid };
    }
	    
    double logistic(double raw, double entropy) {
	return logistic(raw, entropy, params.getDefaultprevalence());
    }
    static double logistic(double raw, double entropy, double dp) {
	double v = raw * Math.exp(entropy);
	return dp*v / ((1-dp)+dp*v);
    }
    static double cloglog(double raw, double entropy) {
	return 1-Math.exp(-raw * Math.exp(entropy));
    }
    double occurrenceProbability(double raw, double entropy) {
        return params.logistic() ? logistic(raw, entropy) : cloglog(raw, entropy);
    }

    /*
    static Grid toCumulativeCareful(final Grid grid, final String raw2cumfile, boolean tryit) throws IOException {
	Utils.reportDoing("Making cumulative projection");
	GridDimension dim = grid.getDimension();
	//	long spaceNeeded = 9 * dim.getnrows() * (long) dim.getncols();  // 1 boolean, 1 float, 1 int
	//	boolean hasSpace = Utils.testMemory(spaceNeeded);
	Utils.reportMemory("toCumulativeCareful");
	//	if (!tryit || (raw2cumfile!=null && !hasSpace)) {  
	// approximate the cumulative transform
	    Utils.echoln("Interpolating from " + raw2cumfile);
	    return new Grid(dim, grid.getName()) {
		    double[][] raw2cum;
		    { raw2cum = readCumulativeIndex(raw2cumfile); }
		    public boolean hasData(int r, int c) { return grid.hasData(r,c); }
		    public float eval(int r, int c) { 
			double val = grid.eval(r,c);
			return (float) interpolateCumulative(raw2cum, val);
		    }
		};
	    //	}
	    //	else
	    //	    return toCumulative(grid);
    }
    */
    // Assumes returned grid will be accessed exactly once, from top left
    /*  There's a bug here somewhere, causes striping of results 
        (perhaps an issue with nodatas?)

    static Grid toCumulative(Grid grid) {
	int nrows = grid.getDimension().getnrows();
	int ncols = grid.getDimension().getncols();
    	final float[] vals = new float[nrows*ncols];
	final boolean[][] hasData = new boolean[nrows][ncols];
	int cnt = 0;
	for (int i=0; i<nrows; i++) {
	    if (Utils.interrupt) return null;
	    Utils.reportProgress((i*100)/(double)nrows);
	    for (int j=0; j<ncols; j++) {
		hasData[i][j] = grid.hasData(i,j);
		if (hasData[i][j])
		    vals[cnt++] = grid.eval(i,j);
	    }
	}
	int[] index = new int[cnt];
	for (int i=0; i<index.length; i++) index[i] = i;
	FloatIndexSort.sort1(index, 0, cnt, vals);
	double totWeight=0.0, sum=0.0;
	for (int i=0; i<cnt; i++)
	    totWeight += vals[i];
	for (int i=0; i<cnt; ) {
	    int j;
	    for (j=i+1; j<cnt; j++)
		if (vals[index[i]]!=vals[index[j]])
		    break;
	    sum += vals[index[i]] * (j-i);
	    while (i<j)
		vals[index[i++]] = (float) (100 * sum / totWeight);
	}
	return new Grid(grid.getDimension(), grid.getName()) {
		int cnt = 0;
		public boolean hasData(int r, int c) { return hasData[r][c]; }
		public float eval(int r, int c) { 
		    if (cnt >= vals.length)
			throw new RuntimeException("Project: unexpected reuse of cumulative grid");
		    return vals[cnt++]; 
		}
	    };
    }
    */

    abstract class Pfeature {
	abstract double eval(int r, int c);
	String name;
    }
    class ProductGrid extends Pfeature {
	Grid g0, g1;
	public ProductGrid(Grid g0, Grid g1) { this.g0=g0; this.g1=g1; }
	public double eval(int r, int c) { return g0.eval(r,c) * (double) g1.eval(r,c); }
    }
    class SquareGrid extends Pfeature {
	Grid g0;
	public SquareGrid(Grid g0) { this.g0=g0; }
	public double eval(int r, int c) { 
	    double val = g0.eval(r,c); 
	    return val * val;
	}
    }
    class PolyhedralGrid extends Pfeature {
	Grid g0;
	public PolyhedralGrid(Grid g0) { this.g0=g0; }
	public double eval(int r, int c) { 
	    double val = g0.eval(r,c); 
	    return (val - val * val)/4.0;
	}
    }
    class LinearGrid extends Pfeature {
	Grid g;
	public LinearGrid(Grid g) { this.g=g; }
	public double eval(int r, int c) { return g.eval(r,c); }
    }
    class BinaryGrid extends Pfeature {
	Grid g0;  
	double val;
	public BinaryGrid(Grid g0, double val) { this.g0=g0; this.val = val; }
	public double eval(int r, int c) { return (g0.eval(r,c) == val) ? 1 : 0; }
    }
    class ThresholdGrid extends Pfeature {
	Grid g0;
	double val;
	public ThresholdGrid(Grid g0, double val) { this.g0=g0; this.val = val; }
	public double eval(int r, int c) { return (g0.eval(r,c) >= val) ? 1 : 0; }
    }
    class HingeGrid extends Pfeature {
	Grid g0;
	double min, max;

	public HingeGrid(Grid g0, double min, double max) { 
	    this.g0=g0; 
	    this.min = min;
	    this.max = max;
	}
	public double eval(int r, int c) { 
	    double x = g0.eval(r,c);
	    double val = (x <= min) ? 0.0 : (x-min)/(max-min);
	    if (!doClamp) return val;
	    if (x>max) return 1.0;
	    return val;
	}
    }
    class ClampedGrid extends Grid {
	double min, max;
	Grid g;
	public ClampedGrid(Grid g, double min, double max) {
	    this.g = g; this.min = min; this.max = max;
	}
	public boolean hasData(int r, int c) { return g.hasData(r,c); }
	public boolean wouldClamp(int r, int c) {
	    if (!hasData(r,c)) return false;
	    float val = g.eval(r,c);
	    return (val<min) || (val>max);
	}
	public float eval(int r, int c) {
	    float val = g.eval(r,c);
	    if (!doClamp) return val;
	    if (val<min) return (float) min;
	    if (val>max) return (float) max;
	    return val;
	}
    }
    class ScaledGrid extends Pfeature {
	Pfeature g0;
	double min, max;
	public ScaledGrid(Pfeature g0, double min, double max) {
	    this.g0 = g0; this.min = min; this.max = max;
	}
	public double eval(int r, int c) {
	    double val = ((g0.eval(r,c) - min) / (max-min));
	    if (!doClamp) return val;
	    if (val<0) return 0.0;
	    if (val>1) return 1.0;
	    return val;
	}
    }

    public static void main(String args[]) {
	if (args.length < 3) {
	    System.out.println("Usage: density.Project lambdaFile gridDir outFile [args]");
	    System.exit(0);
	}
	String lambdaFile = args[0];
	String gridDir = args[1];
	String outFile = args[2];
	try {
	    Params params = new Params();
	    Utils.applyStaticParams(params);
	    if (args.length>3) {
		String[] subargs = new String[args.length-3];
		for (int i=0; i<subargs.length; i++)
		    subargs[i] = args[i+3];
		params.readFromArgs(subargs);
	    }
	    String clampout = params.getboolean("writeClampGrid") ? 
		outFile.substring(0, outFile.length()-4) + "_clamping" + outFile.substring(outFile.length()-4):
		null;
	    Project project = new Project(params);
	    project.doProject(lambdaFile, gridDir, outFile, clampout);
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	    System.exit(1);
	}
    }


    public static double[][] readCumulativeIndex(String filename) throws IOException {
	final ArrayList raw = new ArrayList(), cumulative = new ArrayList();
	final Csv csv = new Csv(filename);
	csv.apply(csv.new Applier() {
		public void process() {
		    String[] fields = csv.getCurrentRecord();
		    raw.add(new Double(Double.parseDouble(fields[0].replaceAll(",", "."))));
		    cumulative.add(new Double(Double.parseDouble(fields[1].replaceAll(",", "."))));
		}});
	return rawcumaa2raw2cum(raw, cumulative);
    }

    static double[][] rawcumaa2raw2cum(ArrayList raw, ArrayList cumulative) {
	return new double[][] { Utils.doubleArrayList2Array(raw), 
				Utils.doubleArrayList2Array(cumulative) };
    }

    static double interpolateCumulative(double[][] raw2cum, double t) {
	return interpolateCumulative(raw2cum[0], raw2cum[1], t);
    }
    static double interpolateCumulative(double[] raw, double[] cumulative, double t) {
	int index = Arrays.binarySearch(raw, t);
	if (index>=0) return cumulative[index];
	index = -index - 1;
	if (index==0)
	    return (t / raw[0]) * cumulative[0];
	if (index==raw.length) return cumulative[raw.length-1];
	double frac = (t-raw[index-1]) / (raw[index] - raw[index-1]);
	double result = cumulative[index-1] + frac * (cumulative[index] - cumulative[index-1]);
	return result;
    }
}
