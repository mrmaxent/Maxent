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
import gnu.getopt.*;

public class Reproject {

    // simple projection: use center point of each grid cell
    public static void main(String args[]) {
	double xll=0.0, yll=0.0, cellsize=0.0;
	int nr=0, nc=0;
	int c;
	Getopt g = new Getopt("Show", args, "f:x:y:r:c:s:");
	String usage = "Usage: Reproject [-xyrcs] outdir infile1 infile2 ...";
	GridDimension dimension = null;
	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 'x': xll = Double.parseDouble(g.getOptarg()); break;
	    case 'y': yll = Double.parseDouble(g.getOptarg()); break;
	    case 'r': nr = Integer.parseInt(g.getOptarg()); break;
	    case 'c': nc = Integer.parseInt(g.getOptarg()); break;
	    case 's': cellsize = Double.parseDouble(g.getOptarg()); break;
	    case 'f': {
		try {
		    Grid grid = GridIO.readGrid(g.getOptarg());
		    dimension = grid.getDimension();
		} catch (IOException e) { 
		    System.out.println(e.toString());
		    System.exit(1);
		}
		break;
	    }
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	args = density.Utils.getArgs(args, g.getOptind());
	if (args.length < 2) {
	    System.out.println(usage);
	    System.exit(0);
	}
	String[] files = new String[args.length-1];
	for (int f=0; f<files.length; f++)
	    files[f] = args[f + 1];
	String outdir = args[0];
	for (int f=0; f<files.length; f++) {
	    String infile = files[f];
	    System.out.println(infile);
	    String outfile = new File(outdir, new File(infile).getName()).getPath();
	    try {
		final Grid grid = GridIO.readGrid(infile);
		final GridDimension dim = grid.getDimension();
		final GridDimension dim2 = (dimension==null)?new GridDimension(xll,yll,cellsize,nr,nc):dimension;
		Grid gg = new Grid(dim2, "tmp") {
			public boolean hasData(int r, int c) {
			    return grid.hasData(dim2.toXY(r,c));
			}
			public float eval(int r, int c) {
			    return grid.eval(dim2.toXY(r,c));
			}
		    };
		new GridWriter(gg, outfile).writeAll();
	    }
	    catch (IOException e) { 
		System.out.println("Error: " + e);
		System.exit(0);
	    }
	}
    }
}
