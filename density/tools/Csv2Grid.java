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
import java.util.*;

public class Csv2Grid {
    
    public static void main(String args[]) {
	try {
	    new Csv2Grid().go(args);
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	    System.exit(1);
	}
    }

    void go(String[] args) throws IOException {
	String usage = "Usage: Csv2Grid [-r rowname] [-c colname] templateGrid csvFile convertColName outFile";
	String row = "row", col = "column";
	boolean geographic = false;
	int c;
	Getopt g = new Getopt("FromBRTFormat", args, "r:c:x:y:");
	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 'r': row = g.getOptarg(); break;
	    case 'c': col = g.getOptarg(); break;
	    case 'x': col = g.getOptarg(); geographic = true; break;
	    case 'y': row = g.getOptarg(); geographic = true; break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	if (args.length-g.getOptind() < 4) {
	    System.out.println(usage); 
	    System.exit(0);
	}
	String templateGrid = args[g.getOptind()];
	String csvfile = args[g.getOptind()+1];
	final String convertColName = args[g.getOptind()+2];
	String outfile = args[g.getOptind()+3];
	final GridDimension dim = new LazyGrid(templateGrid).getDimension();
	int nrows = dim.getnrows(), ncols = dim.getncols();
	final CsvOnePass csv = new CsvOnePass(csvfile);
	final int rowi = csv.fieldIndex(row), coli = csv.fieldIndex(col);
	final boolean convertGeographic = geographic;
	Grid grid = new Grid(dim, "fromCsv") {
		int lastr=-1, lastc=-1;
		float lastVal=-1;
		void readFromCsv(int r, int c) {
		    if (lastr>r || (lastr==r&&lastc>=c)) return;
		    if (csv.getRecord()==null)
			lastr = lastc = Integer.MAX_VALUE;
		    else {
			if (convertGeographic) {
			    double x = csv.getDouble(coli);
			    double y = csv.getDouble(rowi);
			    lastr = dim.toRow(y);
			    lastc = dim.toCol(x);
			} else {
			    lastr = Integer.parseInt(csv.get(rowi));
			    lastc = Integer.parseInt(csv.get(coli));
			}
			lastVal = (float) csv.getDouble(convertColName);
		    }
		}
		public float eval(int r, int c) { 
		    readFromCsv(r,c);
		    return lastVal;
		}
		public boolean hasData(int r, int c) { 
		    readFromCsv(r,c);
		    return (lastr==r && lastc==c);
		}
	    };
	new GridWriter(grid, outfile).writeAll();
    }
}
