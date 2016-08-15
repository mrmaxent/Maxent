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

public class Correlation {

    public static void main(String args[]) {
	String usage = "Usage: Correlation gridfile1 gridfile2";
	if (args.length < 2) {
	    System.out.println(usage);
	    System.exit(0);
	}
	try {
	    new Correlation().go(args[0], args[1]);
	}
	catch (IOException e) { 
	    System.out.println(e.toString());
	    System.exit(1);
	}
    }

    void go(String gridfile1, String gridfile2) throws IOException {
	Grid[] grid = new Grid[2];
	grid[0] = GridIO.readGrid(gridfile1);
	grid[1] = GridIO.readGrid(gridfile2);
	GridDimension dim = grid[0].getDimension();
	int nrows=dim.getnrows(), ncols=dim.getncols(), max=nrows*ncols;
	double[][] pred = new double[2][max];
	int cnt=0;
	for (int r=0; r<nrows; r++)
	    for (int c=0; c<ncols; c++) {
		if (grid[0].hasData(r,c) && grid[1].hasData(r,c)) {
		    pred[0][cnt] = grid[0].eval(r,c);
		    pred[1][cnt++] = grid[1].eval(r,c);
		}
	    }
	double[][] predd = new double[2][cnt];
	for (int i=0; i<cnt; i++)
	    for (int j=0; j<2; j++)
		predd[j][i] = pred[j][i];
	System.out.println(density.tools.Stats.correlation(predd[0], predd[1]));
    }
}
