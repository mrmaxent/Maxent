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

public class FromBRTFormat {
    
    public static void main(String args[]) {
	try {
	    new FromBRTFormat().go(args);
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	    System.exit(1);
	}
    }

    void go(String[] args) throws IOException {
	String usage = "Usage: FromBRTFormat [-p prefix]";
	String prefix = "brt", outputType = "asc", predictionsfile=null;
	String outfile = null;
	int c;
	Getopt g = new Getopt("FromBRTFormat", args, "p:t:TP:o:h");
	boolean useTheoMap = false, firstLineHeader=true;
	final int[] theoMap = new int[] { 0, 9, 10, 17, 18, 15, 11, 12, 14, 13, 16 };
	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 'p': prefix = g.getOptarg(); break;
	    case 't': outputType = g.getOptarg(); break;
	    case 'T': useTheoMap = true; firstLineHeader=false; break;
	    case 'h': firstLineHeader=false; break;
	    case 'P': predictionsfile = g.getOptarg(); break;
	    case 'o': outfile = g.getOptarg(); break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	String index = prefix+"_index.mxe";
	if (predictionsfile==null)
	    predictionsfile = prefix+".predictions";
	if (outfile==null)
	    outfile = prefix + "." + outputType;
	final LazyGrid indexGrid = new LazyGrid(index);
	GridDimension dim = indexGrid.getDimension();
	int nrows = dim.getnrows(), ncols = dim.getncols();
	final BufferedReader predin = new BufferedReader(new FileReader(predictionsfile));
	if (firstLineHeader) predin.readLine();
	final boolean useMap = useTheoMap;
	Grid grid = new Grid(dim, "BRT_prediction") {
		int lastr=-1, lastc=-1;
		float lastVal=-1;
		public float eval(int r, int c) { 
		    if (r!=lastr || c!=lastc) {
			try {
			    String pred = predin.readLine();
			    if (useMap) 
				lastVal = theoMap[Integer.parseInt(pred)];
			    else {
				String[] fields = pred.split(" ");
				lastVal = Float.parseFloat(fields.length>1?fields[1]:pred);
			    }
			    lastr=r;
			    lastc=c;
			} catch (IOException e) {
			    System.out.println("Error: " + e.toString());
			    System.exit(1);
			}
		    }
		    return lastVal;
		}
		public boolean hasData(int r, int c) { return indexGrid.hasData(r,c); }
	    };
	new GridWriter(grid, outfile).writeAll();
    }
}
