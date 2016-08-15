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

import density.*;
import java.io.*;
import java.text.*;
import java.util.*;

// Make a 0-1 probability of presence surface, with highest probability 
// for points that are in the middle 60% of all env vars, fading with 
// multiplier of 0.3 at extreme for each variable.
public class Mid {
    String[] fileNames;
    String samplesFile, outFile;
    double[] min, max;
    int ngrids;
    Feature[] f;

    void extract() throws IOException {
	Extractor extractor = new Extractor();
	Layer[] fileLayers = new Layer[ngrids];
	for (int i=0; i<ngrids; i++)
	    fileLayers[i] = new Layer(new File(fileNames[i]), "Continuous");
	extractor.extractSamples(fileNames, 10000, null, null, fileLayers, new String[0]);
	f = extractor.toFeatures();
	min = new double[ngrids];
	max = new double[ngrids];
	for (int i=0; i<ngrids; i++) {
	    for (int j=0; j<f[i].getN(); j++) {
		if (j==0 || f[i].eval(j) < min[i])
		    min[i] = f[i].eval(j);
		if (j==0 || f[i].eval(j) > max[i])
		    max[i] = f[i].eval(j);
	    }
	    //	    System.out.println(f[i].name + " " + min[i] + " " + max[i]);
	}
    }

    double pred(double[] vals) {
	double p=1.0;
	for (int i=0; i<ngrids; i++) {
	    double s = (vals[i]-min[i]) / (max[i]-min[i]);
	    if (s < 0.3)
		p *= s+0.7;
	    if (s > 0.7)
		p *= (1.0-s)+0.7;
	}
	return p;
    }

    void apply() throws IOException {
	final Grid[] grids = new Grid[ngrids];
	for (int i=0; i<ngrids; i++)
	    grids[i] = new LazyGrid(fileNames[i]);
	Grid outGrid = new Grid(grids[0].getDimension(), "tmpname") {
		public boolean hasData(int r, int c) {
		    for (int i=0; i<grids.length; i++)
			if (!grids[i].hasData(r,c)) 
			    return false;
		    return true;
		}
		public float eval(int r, int c) {
		    double[] vals = new double[ngrids];
		    for (int i=0; i<grids.length; i++)
			vals[i] = grids[i].eval(r,c);
		    return (float) pred(vals);
		}
	    };
	new GridWriter(outGrid, outFile).writeAll();
    }

    Grid theGrid;
    PrintWriter paout, poout;
    Grid[] inputGrids;
    void sample() throws IOException {
	theGrid = GridIO.readGrid(outFile);
	inputGrids = new Grid[ngrids];
	for (int i=0; i<ngrids; i++) {
	    System.out.println("Reading input grid " + fileNames[i]);
	    inputGrids[i] = GridIO.readGrid(fileNames[i]);
	}
	paout = new PrintWriter(new FileOutputStream("pa.csv"));
	poout = new PrintWriter(new FileOutputStream("po.csv"));
	paout.println("Species,x,y,pa");
	poout.println("Species,x,y");
	sample(1);
	sample(2);
	sample(5);
	sample(10);
	sample(20);
	sample(50);
	sample(100);
	poout.close();
	paout.close();
    }

    // sample from output grid, with southern 25% sampled uniformly, then
    // fading linearly to 1/m times intensity in the northernmost 50%
    void sample(int m) throws IOException {
	PrintWriter paout2 = new PrintWriter(new FileOutputStream("pa" + m + ".csv"));
	paout2.print("Species" + m);
	for (int i=0; i<ngrids; i++)
	    paout2.print("," + inputGrids[i].getName());
	paout2.println();
	GridDimension dim = theGrid.getDimension();
	int nr = dim.getnrows(), nc = dim.getncols();
	//	int pacnt=0;
	for (int i=0; i<200; ) {
	    int r = density.Utils.generator.nextInt(nr);
	    double d = density.Utils.generator.nextDouble();
	    double fr = r / (double) nr;
	    if (fr < 0.5) {
		if (d > 1.0/m) continue;
	    }
	    else if (d > ((fr-0.5)/0.25) * (1.0-1.0/m) + 1.0/m)
		continue;
	    int c = density.Utils.generator.nextInt(nc);
	    if (!theGrid.hasData(r,c)) continue;
	    double val = theGrid.eval(r,c);
	    d = density.Utils.generator.nextDouble();
	    //	    if (pacnt<100) {
                paout.println("species" + m + "," + dim.toX(c) + "," + dim.toY(r) + "," + (d<val?1:0));
		paout2.print(d<val?1:0);
		for (int j=0; j<ngrids; j++)
		    paout2.print("," + inputGrids[j].eval(r,c));
		paout2.println();		
		//		pacnt++;
		//	    }
	    if (d > val) continue;
	    poout.println("species" + m + "," + dim.toX(c) + "," + dim.toY(r));
	    i++;
	}
	paout2.close();
    }
	
    public static void main(String args[]) {
	if (args.length < 2) {
	    System.out.println("Usage: Mid griddir outfile");
	    System.exit(0);
	}
	try {
	    new Mid().go(args);
	} catch (IOException e) {
	    System.out.println(e.toString());
	}
    }

    void go(String[] args) throws IOException {
	String gridDir = args[0];
	outFile = args[1];
	density.Utils.generator = new Random(0);
	try {
	    fileNames = density.Utils.gridFileNames(gridDir);
	    ngrids = fileNames.length;
	    extract();
	    apply();
	    sample();
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	    System.exit(1);
	}
    }
}

