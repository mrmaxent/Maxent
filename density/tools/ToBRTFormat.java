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

import gnu.getopt.*;
import density.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class ToBRTFormat {
    String[] fileNames;
    String samplesFile, species, gridDir, prefix="brt";
    int ngrids, nsamples=1000;

    void writeForPrediction() throws IOException {
	System.out.println("Writing data for prediction");
	Grid[] grids = new Grid[ngrids];
	PrintWriter out = density.Utils.writer(prefix+"_pred.csv");
	NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
	nf.setGroupingUsed(false);
	for (int i=0; i<ngrids; i++) {
	    grids[i] = new LazyGrid(fileNames[i]);
	    out.print((i==0?"":",") + grids[i].getName());
	}
	out.println();
	GridDimension dim = grids[0].getDimension();
	int nrows = dim.getnrows(), ncols = dim.getncols();
	final boolean[][] hasData = new boolean[nrows][ncols];
	for (int r=0; r<nrows; r++)
	    for (int c=0; c<ncols; c++) {
		boolean hasdata = true;
		for (int i=0; i<ngrids; i++)
		    if (!grids[i].hasData(r,c)) {
			hasdata = false;
			break;
		    }
		if (hasdata) {
		    for (int i=0; i<ngrids; i++)
			out.print((i==0?"":",") + nf.format(grids[i].eval(r,c)));
		    out.println();
		    hasData[r][c] = true;
		}		    
	    }
	out.close();
	Grid g = new Grid(dim, "tmp") {
		public float eval(int r, int c) { return 1; }
		public boolean hasData(int r, int c) { return hasData[r][c]; }
	    };
	new GridWriter(g, prefix+"_index.mxe").writeAll();		
    }

    void writeForTraining() throws IOException {
	System.out.println("Writing training data");
	PrintWriter out = density.Utils.writer(prefix+"_train.csv");
	Extractor extractor = new Extractor();
	Layer[] fileLayers = new Layer[ngrids];
	for (int i=0; i<ngrids; i++)
	    fileLayers[i] = new Layer(new File(fileNames[i]), "Continuous");
	extractor.extractSamples(fileNames, nsamples, samplesFile, null, fileLayers, species==null ? null : new String[] {species});
	Sample[] ss = extractor.train.getSamples(species);
	Feature[] f = extractor.toFeatures();
	out.print((species==null?"species":species) + ",weight");
	double backgroundWeight = ss.length / ((double) ss.length + nsamples);
	double speciesWeight = nsamples / ((double) ss.length + nsamples);
	for (int j=0; j<f.length; j++)
	    out.print("," + fileLayers[j].getName());
	out.println();
	for (int i=0; i<ss.length; i++) {
	    out.print("1," + speciesWeight);
	    for (int j=0; j<f.length; j++)
		out.print("," + f[j].eval(ss[i]));
	    out.println();
	}
	for (int i=0; i<f[0].getN(); i++) {
	    out.print("0," + backgroundWeight);
	    for (int j=0; j<f.length; j++)
		out.print("," + f[j].eval(i));
	    out.println();
	}
	out.close();
    }

    public static void main(String args[]) {
	new ToBRTFormat().go(args);
    }

    void go(String[] args) {
	String usage = "Usage: ToBRTFormat [-s species] [-n numbackground] [-p prefix] samplefile griddir";
	int c;
	Getopt g = new Getopt("ToBRTFormat", args, "s:n:p:");

	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 's': species = g.getOptarg(); break;
	    case 'n': nsamples = Integer.parseInt(g.getOptarg()); break;
	    case 'p': prefix = g.getOptarg(); break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	if (args.length -g.getOptind() < 2) {
	    System.out.println(usage);
	    System.exit(0);
	}
	samplesFile = args[g.getOptind()];
	gridDir = args[g.getOptind()+1];
	density.Utils.generator = new Random(0);
	try {
	    fileNames = density.Utils.gridFileNames(gridDir);
	    ngrids = fileNames.length;
	    writeForTraining();
	    writeForPrediction();
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	    System.exit(1);
	}
    }
}

