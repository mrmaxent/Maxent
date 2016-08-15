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

/*   WARNING
   This class does not preserve data types of input grids
*/

public class Reduce {

    public static void main(String args[]) {
	String usage = "Usage: Reduce indir outdir [size]";
	final String indir = args[0], outdir = args[1];
	int targetSize=1500000;
	double interval1=-1;
	if (args.length>2) targetSize = Integer.parseInt(args[2]);
	if (args.length>3) interval1=targetSize; // hack

	File[] layerFiles = new File(indir).listFiles(density.Utils.filenameFilter);
	
	java.util.Arrays.sort(layerFiles);
	for (int i=0; i<layerFiles.length; i++) {
	    System.out.println(layerFiles[i]);
	    String inFile = layerFiles[i].getPath();
	    Grid grid1=null;
	    try {
		grid1 = GridIO.readGrid(inFile);
	    }
	    catch (IOException e) { 
		System.out.println("Error reading/writing files " + e);
		System.exit(0);
	    }
	    final Grid grid = grid1;
	    GridDimension dim = grid.getDimension();
	    if (i==0 && interval1==-1)
		interval1 = Math.sqrt(grid.countData() / (double) targetSize);
	    final double interval = interval1;
	    Grid g=null;
	    if (interval < 1.0) g=grid;
	    else {
		int nrows = (int) Math.ceil(dim.getnrows() / (double) interval);
		int ncols = (int) Math.ceil(dim.getncols() / (double) interval);
		GridDimension dim2 = new GridDimension(dim.getxllcorner(), dim.getyllcorner(), dim.getcellsize()*interval, nrows, ncols);
		float[][] vals = new float[nrows][ncols];
		double[][] distance = new double[nrows][ncols];
		for (int r=0; r<nrows; r++)
		    for (int c=0; c<ncols; c++) {
			vals[r][c] = grid.getNODATA_value();
			distance[r][c] = -1;
		    }
		for (int r=0; r<dim.getnrows(); r++)
		    for (int c=0; c<dim.getncols(); c++)
			if (grid.hasData(r,c)) {
			    double[] xy = dim.toXY(r, c);
			    int[] rc2 = dim2.toRowCol(xy);
			    int r2=rc2[0], c2=rc2[1];
			    double[] xy2 = dim2.toXY(rc2);
			    double dist = (xy[0]-xy2[0])*(xy[0]-xy2[0]) + (xy[1]-xy2[1])*(xy[1]-xy2[1]);
			    if (distance[r2][c2]==-1 || distance[r2][c2] > dist) {
				distance[r2][c2] = dist;
				vals[r2][c2] = grid.eval(r,c);
			    }
			}
		g = Grid.vals2Grid(dim2, vals, grid.getName(), grid.getNODATA_value());
	    }
	    System.out.println("#cells was " + grid.countData() + ", now " + g.countData() + ", interval " + interval);
	    File outputFile = new File(outdir, grid.getName() + ".mxe");
	    try { new GridWriter(g, outputFile).writeAll(); }
	    catch (IOException e) { 
		System.out.println("Error writing output " + e); 
		System.exit(0);
	    }
	}
    }
}
