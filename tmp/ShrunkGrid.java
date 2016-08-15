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
