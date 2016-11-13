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

// Assumes each sample maps feature names to doubles
public class NceasApply {
    static String[] headers;
    static String outputformat = "cloglog";

    static Sample[] readSamples(String sampleFile) throws IOException {
	final Csv csv = new Csv(sampleFile);
	headers = csv.headers();
	int i;
	for (i=0; i<headers.length; i++) if (headers[i].equals("")) break;
	final int last = i;  // deals with nz, which has blank fields at end
	final ArrayList samples = new ArrayList();
	csv.apply(csv.new Applier() {
		public void process() {
		    HashMap map = new HashMap();
		    for (int j=0; j<4; j++)
			map.put(headers[j], csv.get(j));
		    for (int j=4; j<last; j++) {
			Double val = new Double(Double.parseDouble(csv.get(j)));
			map.put(headers[j], val);
			map.put(headers[j]+"a", val);
		    }
		    samples.add(new Sample(0, 0, 0, 0, 0, "", map));
		}
	    });
	return (Sample[]) samples.toArray(new Sample[0]);
    }

    public static void main(String[] args) {
	String usage = "Usage: NceasApply [-c] [-l] lambdaFile [lambdaFile2...] sampleFile outPrefix";
	if (args.length<3) {
	    System.out.println(usage);
	    System.exit(0);
	}
	int c;
	Getopt g = new Getopt("NceasApply", args, "lrc");
	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 'c': outputformat = "cumulative"; break;
	    case 'l': outputformat = "logistic"; break;
	    case 'r': outputformat = "raw"; break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	try {
	    String sampleFile = args[args.length-2], outPrefix = args[args.length-1];
	    Sample[] samples = readSamples(sampleFile);
	    for (int i=g.getOptind(); i<args.length-2; i++) {
		String lambdaFile = args[i];
		System.out.println(lambdaFile);
		String species = new File(lambdaFile).getName().replaceAll(".lambdas","").toLowerCase();
		GridSetDummy gs = new GridSetDummy();
		FeaturedSpace X = new FeaturedSpace(gs, lambdaFile, true);
		PrintWriter out = Utils.writer(outPrefix + species + ".csv");
		double[][] raw2cum=null;
		raw2cum = Project.readCumulativeIndex(Runner.raw2cumfile(lambdaFile));
		out.println("dataset,siteid,pred");
		double entropy = X.entropy;
		for (int j=0; j<samples.length; j++) {
		    double val = Math.exp(X.linearPredictor(samples[j]) - X.getLinearPredictorNormalizer()) / X.getDensityNormalizer();
		    switch (outputformat) {
		       case "cloglog": val = Project.cloglog(val, entropy); break;
		       case "logistic": val = Project.logistic(val, entropy, 0.5); break;
		       case "cumulative": val = Project.interpolateCumulative(raw2cum, val); break;
		       default: 
			double max = raw2cum[0][raw2cum[0].length-1];
			if (val > max) val = max;
		    }
		    out.println(samples[j].featureMap.get("dataset") + "," + samples[j].featureMap.get("siteid") + "," + val);
		}
		out.close();
	    }
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	    System.exit(1);
	}
    }

}
