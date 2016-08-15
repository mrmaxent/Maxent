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

public class CsvToGrid {

    public static void main(String args[]) {
	String usage = "Usage: CsvToGrid csvFile xcol ycol gridFileForDimension outFile";
	if (args.length < 5) {
	    System.out.println(usage);
	    System.exit(0);
	}
	String csvFile = args[0], gridFile = args[3], outfile = args[4];
	final String xcol = args[1], ycol = args[2];
	try {
	    final GridDimension dim = GridIO.readGrid(gridFile).getDimension();
	    Csv csv = new Csv(csvFile);
	    int nr = dim.getnrows(), nc = dim.getncols();
	    final boolean[][] vals = new boolean[nr][nc];
	    csv.apply(csv.new Applier() {
		    public void process() {
			double x = Double.parseDouble(get(xcol));
			double y = Double.parseDouble(get(ycol));
			if (dim.inBounds(x,y))
			    vals[dim.toRow(y)][dim.toCol(x)] = true;
		    }});
	    Grid out = new Grid(dim, "grid") {
		    public float eval(int r, int c) {
			return vals[r][c] ? 1 : 0;
		    }
		    public boolean hasData(int r, int c) {
			return true;
		    }
		};
	    new GridWriter(out, outfile).writeAll();
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }
}
