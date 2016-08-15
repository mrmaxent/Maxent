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
import gnu.getopt.*;

public class AvgStderr {
    HashMap gridMap = new HashMap(); 
    double linearPredictorNormalizer, densityNormalizer;
    int numPointsForNormalizer;
    String gridDir;
    GridSet gs = null;
    boolean didClamp, wroteLowerci;
    static boolean averagesOnly=false;
    String suffix="mxe", species="";
    GridDimension dim;
    double lowercifrac = 0.95;
    Params params;
    Layer[] needLayers;

    boolean wroteLowerci() { return wroteLowerci; }

    public AvgStderr(Params params, Layer[] layers) { 
	this.params = params; 
	this.needLayers = layers;
    }

    public static void main(String args[]) {
	String usage = "Usage: density.AvgStderr grid1 grid2 ... outPrefix suffix, or density.AvgStderr -l outdir species n gridDir outPrefix suffix";
	int c;
	Getopt g = new Getopt("AvgStderr", args, "la");
	boolean lambdas = false;
	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 'l': lambdas = true; break;
	    case 'a': averagesOnly = true; break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	int arg0 = g.getOptind();
	int nargs = args.length-arg0;
	if (nargs < 4 || lambdas && nargs < 6) {
	    System.out.println(usage);
	    System.exit(0);
	}
	try {
	    if (lambdas) {
		new AvgStderr(new Params(), null).process(args[arg0], args[arg0+1], Integer.parseInt(args[arg0+2]), args[arg0+3], args[arg0+4], args[arg0+5]);
	    }
	    else {
		int num = nargs-2;
		String[] gridFiles = new String[num];
		String outPrefix = args[arg0 + num];
		String suffix = args[arg0 + num+1];
		if (!suffix.startsWith(".")) suffix = "." + suffix;
		AvgStderr avgstderr = new AvgStderr(new Params(), null);
		avgstderr.suffix = suffix;
		for (int i=0; i<num; i++)
		    gridFiles[i] = args[arg0 + i];
		avgstderr.go(gridFiles, outPrefix);
	    }
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	    System.exit(1);
	}
    }

    void process(String outdir, String species, int n, String gridDir, String outPrefix, String suffix) throws IOException {
	if (!suffix.startsWith(".")) suffix = "." + suffix;
	this.suffix = suffix;
	this.species = species;
	Grid[] grids = new Grid[n];
	Project[] projects = new Project[n];
	for (int i=0; i<n; i++) {
	    String lambdafile = new File(outdir, species+"_"+i+".lambdas").getPath();
	    projects[i] = new Project(params);
	    projects[i].needLayers = needLayers;
	    projects[i].gridMap = projects[0].gridMap;  // use same grids
	    grids[i] = projects[i].projectGrid(lambdafile, gridDir)[0];
	}
	process(grids, outPrefix, outdir);
	//	for (int i=0; i<n; i++)
	projects[0].close();  // close the input grids
    }

    public void go(String[] gridFiles, String outPrefix) throws IOException {
	int numg = gridFiles.length;
	LazyGrid[] grids = new LazyGrid[numg];
	for (int i=0; i<numg; i++)
	    grids[i] = new LazyGrid(gridFiles[i]);
	process(grids, outPrefix, ".");
    }
    
    
    boolean hasData;
    abstract class Mygrid extends Grid {
	public Mygrid(GridDimension dim, String name) { super(dim, name); }
	public abstract double eval();
	public boolean hasData(int r, int c) { return hasData; }
	public float eval(int r, int c) { return (float) eval(); }
    }

    double[] vals;
    Grid[] theGrids;
    void makevals(int r, int c) {
	hasData = true;
	for (int i=0; i<vals.length; i++) {
	    if (!theGrids[i].hasData(r,c)) {
		hasData = false;
		break;
	    }
	    vals[i] = theGrids[i].eval(r,c);
	}
    }
    
    void process(Grid[] grids, String outPrefix, String outDir) throws IOException {
	Utils.reportDoing("Creating average grid, stddev grid etc.");
	int numg = grids.length;
	dim = grids[0].getDimension();
	int nr = dim.getnrows(), nc = dim.getncols();
	vals = new double[numg];
	outPrefix = new File(outDir, outPrefix).getPath();
	theGrids = grids;

	Grid avg = new Grid(dim, species+" avg") { 
		public boolean hasData(int r, int c) {
		    makevals(r,c);
		    return hasData;
		}
		public float eval(int r, int c) { return (float) mean(vals); } };
	Grid stddev = new Mygrid(dim, species+" stddev") { 
		public double eval() { return stddev(vals); } };
	Grid min = new Mygrid(dim, species+" min") { 
		public double eval() { return min(vals); } };
	Grid max = new Mygrid(dim, species+" max") { 
		public double eval() { return max(vals); } };
	Grid median = new Mygrid(dim, species+" median") { 
		public double eval() { return median(vals); } };
	Grid lowerci = new Mygrid(dim, species+" lowerci") { 
		public double eval() { return lowerci(vals); } };

	Grid[] outgrids = new Grid[] { avg, stddev, min, max, median, lowerci };
	if (averagesOnly) outgrids = new Grid[] { avg };

	wroteLowerci=true;
	if (numg < 1/(1.0-lowercifrac)) {
	    outgrids = new Grid[] { avg, stddev, min, max, median };
	    wroteLowerci=false;
	}
	String[] names = new String[] { "avg", "stddev", "min", "max", "median", "lowerci" };
	String[] filenames = new String[outgrids.length];
	for (int i=0; i<filenames.length; i++)
	    filenames[i] = outPrefix+"_"+names[i]+suffix;
	GridWriter.writeGrids(outgrids, filenames, false);
    }

    public static double mean(double[] x) {
	double sum=0.0;
	for (int i=0; i<x.length; i++) sum += x[i];
	return sum/x.length;
    }
    public static double min(double[] x) {
	double min=x[0];
	for (int i=1; i<x.length; i++)
	    if (x[i] < min) min = x[i];
	return min;
    }
    public static double max(double[] x) {
	double max=x[0];
	for (int i=1; i<x.length; i++)
	    if (x[i] > max) max = x[i];
	return max;
    }
    // note: changes ordering of x
    public static double median(double[] x) {
	Arrays.sort(x);
	return x[x.length/2];
    }
    // note: changes ordering of x
    double lowerci(double[] x) {
	Arrays.sort(x);
	return x[(int) (x.length * (1.0-lowercifrac))];
    }
    public static double variance(double[] x) {
	double sum=0.0, mean=mean(x);
	for (int i=0; i<x.length; i++) sum += x[i]*x[i];
	double var=sum/x.length - mean*mean;
	return (var < 0.0 ? 0 : var);
    }
    public static double stddev(double[] x) { 
	return Math.sqrt(variance(x));
    }
    public static double stderr(double[] x) { 
	double vv = variance(x) * (x.length / (x.length-1));
	return Math.sqrt(vv);
    }
}
