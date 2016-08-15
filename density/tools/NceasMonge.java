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
import java.io.*;
import java.util.*;
import density.*;

// add siteids to predictions
public class NceasMonge {

    static String[][] readSamples(String sampleFile) throws IOException {
	final Csv csv = new Csv(sampleFile);
	final ArrayList samples = new ArrayList();
	csv.apply(csv.new Applier() {
		public void process() {
		    String[] foo = new String[] { csv.get(0), csv.get(1) };
		    samples.add(foo);
		}
	    });
	return (String[][]) samples.toArray(new String[0][]);
    }

    public static void main(String[] args) {
	if (args.length<3) {
	    System.out.println("Usage: NceasApply predFile [predFile2...] sampleFile outPrefix");
	    System.exit(0);
	}
	try {
	    String sampleFile = args[args.length-2], outPrefix = args[args.length-1];
	    String[][] samples = readSamples(sampleFile);
	    for (int i=0; i<args.length-2; i++) {
		String predFile = args[i];
		String species = new File(predFile).getName().replaceAll("_.*","").toLowerCase();
		System.out.println(predFile + " " + species);
		PrintWriter out = density.Utils.writer(outPrefix + species + ".csv");
		out.println("dataset,siteid,pred");
		BufferedReader in = new BufferedReader(new FileReader(predFile));
		in.readLine();
		for (int j=0; j<samples.length; j++) {
		    double val = Double.parseDouble(in.readLine());
		    out.println(samples[j][0] + "," + samples[j][1] + "," + val);
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
