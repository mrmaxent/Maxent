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

package density.tools;

// to do: check that all grids have the same header

import java.io.*;
import density.*;
import gnu.getopt.*;

public class Histogram {
    double[] min, max;
    double allmin=-9999, allmax=-9999;
    int nbins=100;

    public static void main(String args[]) {
	try {
	    new Histogram().go(args);
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }

    // should use Grid.minmax()
    void computeMinMax(int i, String filename, double[] min, double[] max) throws IOException {
	if (allmin==-9999 && allmax==-9999) {
	    LazyGrid g = new LazyGrid(filename);
	    GridDimension dim = g.getDimension();
	    boolean started = false;
	    int nr = dim.getnrows(), nc = dim.getncols();
	    for (int r=0; r<nr; r++)
		for (int c=0; c<nc; c++) {
		    if (!g.hasData(r,c)) continue;
		    double v = g.eval(r,c);
		    if (!started) { min[i] = max[i] = v; }
		    started = true;
		    if (min[i] > v) min[i] = v;
		    if (max[i] < v) max[i] = v;
		}
	}
	if (allmin != -9999) min[i] = allmin;
	if (allmax != -9999) max[i] = allmax;
    }

    void go(String[] args) throws IOException {
	String predfile = null, occfile = null;
	String[] species=null;
	int cc;
	Getopt g = new Getopt("Histogram", args, "b:p:c:s:m:M:");
	String usage = "Usage: Histogram [-b nbins] [-p predictionfile] [-c occurrenceswdfile] [-s species] [-m min] [-M max] env1 env2 ...";
	while ((cc=g.getopt()) != -1) {
	    switch(cc) {
	    case 'b': nbins = Integer.parseInt(g.getOptarg()); break;
	    case 'p': predfile = g.getOptarg(); break;
	    case 'c': occfile = g.getOptarg(); break;
	    case 's': species = new String[] { g.getOptarg() }; break;
	    case 'm': allmin = Double.parseDouble(g.getOptarg()); break;
	    case 'M': allmax = Double.parseDouble(g.getOptarg()); break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	if (args.length==0) { System.out.println(usage); System.exit(0); }
        int nlayers = args.length-g.getOptind();
	String[] filenames = new String[nlayers];
	String[] layername = new String[nlayers];
	min = new double[nlayers];
        max = new double[nlayers];
	int[][] cnt = new int[nlayers][nbins];
	double[][] occs = new double[nlayers][nbins];
        double[][] vals = new double[nlayers][nbins];
        double[][] pvals = new double[nlayers][nbins];
	System.out.print("Bin");
	LazyGrid[] grids = new LazyGrid[nlayers];
        for (int i=0; i<nlayers; i++) {
	    filenames[i] = args[i+g.getOptind()];
	    computeMinMax(i, filenames[i], min, max);
	    layername[i] = density.Utils.fileToLayer(filenames[i]);
	    System.out.print("," + layername[i]+" bin center" + "," + layername[i] + " fraction of region");
	    if (predfile != null) 
		System.out.print("," + layername[i] + " fraction of prediction");
	    if (occfile != null) 
		System.out.print("," + layername[i] + " fraction of occurrences");
	    grids[i] = new LazyGrid(filenames[i]);
        }
        System.out.println();
        LazyGrid preds = (predfile==null) ? null : new LazyGrid(predfile);
	GridDimension dim = grids[0].getDimension();
	int nr = dim.getnrows(), nc = dim.getncols();
	for (int r=0; r<nr; r++)
	    for (int c=0; c<nc; c++) {
		double p = (preds==null) ? 0.0 : preds.eval(r,c);
		for (int j=0; j<nlayers; j++) {
		    if (!grids[j].hasData(r,c)) continue;
		    double v = grids[j].eval(r,c);
		    int bin = getBin(j, v);
		    vals[j][bin] ++;
		    if (preds!=null && preds.hasData(r,c)) pvals[j][bin] += preds.eval(r,c);
		}
	    }
	if (occfile!=null) {
	    SampleSet2 ss = new SampleSet2(occfile, layers(layername), dim, null);
	    ss.read(species);
	    ss.createMaps();
	    Sample[] samples = ss.getSamples();
	    for (int i=0; i<samples.length; i++) {
		Sample s = samples[i];
		for (int j=0; j<nlayers; j++) {
		    Double val = (Double) s.featureMap.get(layername[j]);
		    if (val==null) continue;
		    int b = getBin(j, val);
		    //	         System.out.println(i + " " + layername[j] + " " + val + " " + b);
		    occs[j][b]++;
		}
	    }
        }
        for (int j=0; j<nlayers; j++) {
	    double sum = 0.0, sumpred = 0.0, sumocc = 0.0;
	    for (int b=0; b<nbins; b++) {
		sum += vals[j][b];
		sumpred += pvals[j][b];
		sumocc += occs[j][b];
	    }
	    for (int b=0; b<nbins; b++) {
		vals[j][b] /= sum;
		if (sumpred!=0) pvals[j][b] /= sumpred;
		if (sumocc!=0) occs[j][b] /= sumocc;
	    }
	}
        for (int b=0; b<nbins; b++) {
	    System.out.print(b);
	    for (int j=0; j<nlayers; j++) {
		System.out.print("," + (min[j] + (b+0.5) * (max[j] - min[j])/nbins));
		System.out.print("," + vals[j][b]);
		if (preds!=null)
		    System.out.print("," + pvals[j][b]);
		if (occfile!=null)
		    System.out.print("," + occs[j][b]);
	    }
	    System.out.println();
 	}
    }

    int getBin(int j, double v) {
	int bin = (int) ((v-min[j]) / (max[j]-min[j]) * nbins);
	if (bin>=nbins) bin=nbins-1;
	if (bin<0) return 0;
	return bin;
    }

    Layer[] layers(String[] layername) {
	Layer[] result = new Layer[layername.length];
	for (int i=0; i<layername.length; i++)
	    result[i] = new Layer(layername[i], Layer.UNKNOWN);
	return result;
    }
}
