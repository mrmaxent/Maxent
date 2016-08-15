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

package density;

import java.io.*;

public class ShrunkGrid extends LazyGrid {
    int keepEvery = 1;
    LazyGrid grid;

    public ShrunkGrid(LazyGrid g, int maxRowsAndCols) throws IOException {
	grid = g;
	GridDimension dim = grid.getDimension();
	if (maxRowsAndCols!=-1 && dim.ncols > maxRowsAndCols && dim.nrows > maxRowsAndCols) {
	    double mr = dim.nrows/(double)maxRowsAndCols;
	    double mc = dim.ncols/(double)maxRowsAndCols;
	    keepEvery = (int) ((mr<mc) ? Math.ceil(mr) : Math.ceil(mc));
	}
	setDimension(new GridDimension(dim.getxllcorner(), dim.getyllcorner(), dim.getcellsize()*keepEvery, dim.getnrows()/keepEvery, dim.getncols()/keepEvery));
    }

    public float eval(int r, int c) {
	return grid.eval(r*keepEvery, c*keepEvery);
    }
    public boolean hasData(int r, int c) {
	return grid.hasData(r*keepEvery, c*keepEvery);
    }
    public void initialize() throws IOException { grid.initialize(); }
    public void close() throws IOException { grid.close(); }
}
