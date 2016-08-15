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

public class Plus {

    public static void main(String args[]) {
	String usage = "Usage: Plus outfile infile1 infile2 ...";
	if (args.length < 2) {
	    System.out.println(usage);
	    System.exit(0);
	}
	String outfile = args[0];
	final int nf = args.length-1;
	final boolean average = true;
	String[] infiles = new String[nf];
	for (int i=0; i<nf; i++)
	    infiles[i] = args[i+1];
	try {
	    final Grid[] g = new Grid[nf];
	    for (int i=0; i<nf; i++)
		g[i] = new LazyGrid(infiles[i]);
	    Grid out = new Grid(g[0].getDimension(), "Plus") {
		    public float eval(int r, int c) {
			double sum=0.0;
			for (int i=0; i<nf; i++)
			    sum += g[i].eval(r,c);
			if (average) sum /= nf;
			return (float) sum;
		    }
		    public boolean hasData(int r, int c) {
			for (int i=0; i<nf; i++)
			    if (!g[i].hasData(r,c)) return false;
			return true;
		    }
		};
	    out.setNODATA_value(0);
	    new GridWriter(out, outfile).writeAll();
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }
}
