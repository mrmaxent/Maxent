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
import java.util.*;

public class Scatter {

    public static void main(String args[]) {
	String usage = "Usage: Scatter infile1 infile2 ...";
	if (args.length < 2) {
	    System.out.println(usage);
	    System.exit(0);
	}
	density.Utils.generator = new Random(0);
	try {
	    Grid[] g = new Grid[args.length];
	    for (int i=0; i<g.length; i++)
		g[i] = GridIO.readGrid(args[i]);
	    int nr = g[0].getDimension().getnrows();
	    int nc = g[0].getDimension().getncols();
	    for (int i=0; i<10000; i++) {
		int r = density.Utils.generator.nextInt(nr);
		int c = density.Utils.generator.nextInt(nc);
		boolean hasdata = true;
		for (int j=0; j<g.length; j++)
		    if (!g[j].hasData(r,c)) hasdata = false;
		if (hasdata) {
		    for (int j=0; j<g.length; j++)
			System.out.print(" " + g[j].eval(r,c));
		    System.out.println();
		}
	    }
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }
}
