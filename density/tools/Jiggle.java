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
import density.*;
import gnu.getopt.*;
import java.util.*;

public class Jiggle {
    double percent = 5;
    int replicates = 100;

    public static void main(String args[]) {
	try {
	    new Jiggle().go(args);
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }

    void go(String[] args) throws IOException {
	String predfile = null, occfile = null, biasfile = null;
	String[] species=null;
	int cc;
	Getopt g = new Getopt("Jiggle", args, "p:r:b:");
	String usage = "Usage: Jiggle [-p percent] [-r replicates] [-b bias]";
	while ((cc=g.getopt()) != -1) {
	    switch(cc) {
	    case 'p': percent = Double.parseDouble(g.getOptarg()); break;
	    case 'r': replicates = Integer.parseInt(g.getOptarg()); break;
	    case 'b': biasfile = g.getOptarg(); break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	if (args.length==0) { System.out.println(usage); System.exit(0); }
	final Grid bias = (biasfile==null) ? null : GridIO.readGrid(biasfile);
	final float[] minmax = (bias==null) ? null : bias.minmax();
        String csvfile = args[g.getOptind()];
	Csv csv = new Csv(csvfile);
	final int nlayers = csv.headers().length-3;
	final double[] min = new double[nlayers];
        final double[] max = new double[nlayers];
	for (int i=0; i<nlayers; i++) {
	    min[i] = 1000000;
	    max[i] = -1000000;
	}
	csv.apply(csv.new Applier() {
		public void process() {
		    for (int i=0; i<nlayers; i++) {
			double v = Double.parseDouble(get(i+3));
			if (v<min[i]) min[i] = v;
			if (v>max[i]) max[i] = v;
		    }}});
	for (int i=0; i<nlayers+3; i++)
	    System.out.print((i==0?"":",") + csv.headers()[i]);
	System.out.println();
	density.Utils.generator = new Random(System.currentTimeMillis());
	for (int i=0; i<replicates; i++) {
	    final int me = i;
	    csv = new Csv(csvfile);
	    csv.apply(csv.new Applier() {
		    public void process() {
			System.out.print(get(0) + "_" + me + "," + get(1) + "," + get(2));
			double r = 1.0;
			if (bias!=null) {
			    double b = bias.eval(new double[] { Double.parseDouble(get(1)), Double.parseDouble(get(2))});
			    r = (minmax[1]-b) / (minmax[1]-minmax[0]);
			}
			if (r < 0) System.out.println("Error: bias < 0: " + r);
			for (int i=0; i<nlayers; i++) {
			    double v = Double.parseDouble(get(i+3));
			    v = v + (2.0*(density.Utils.generator.nextDouble()-0.5) * percent) * (max[i] - min[i]) * r / 100.0;
			    System.out.print("," + v);
			}
			System.out.println();
		    }});
	}
    }
}
