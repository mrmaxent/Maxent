package density;

import java.io.*;

public class GridDimension {
    double xllcorner, yllcorner, cellsize;
    int nrows, ncols;
    String[] coordNames=null;
    double[][] coords=null;

    public int getncols() { return ncols; }
    public int getnrows() { return nrows; }
    public double getxllcorner() { return xllcorner; }
    public double getyllcorner() { return yllcorner; }
    public double getcellsize() { return cellsize; }

    public void write(ObjectOutputStream out) throws IOException {
	out.writeDouble(xllcorner);
	out.writeDouble(yllcorner);
	out.writeDouble(cellsize);
	out.writeInt(nrows);
	out.writeInt(ncols);
    }
    public GridDimension(ObjectInputStream in) throws IOException {
	xllcorner = in.readDouble();
	yllcorner = in.readDouble();
	cellsize = in.readDouble();
	nrows = in.readInt();
	ncols = in.readInt();
    }
    public GridDimension(double xll, double yll, double cs, int nr, int nc) {
	xllcorner = xll;
	yllcorner = yll;
	cellsize = cs;
	nrows = nr;
	ncols = nc;
    }
    
    public boolean equals(Object o) {
	GridDimension gd = (GridDimension) o;
	if (gd.xllcorner != xllcorner) return false;
	if (gd.yllcorner != yllcorner) return false;
	if (gd.cellsize != cellsize) return false;
	if (gd.nrows != nrows) return false;
	if (gd.ncols != ncols) return false;
	return true;
    }

    
    public int toRow(double y) { return nrows - 1 - (int) Math.floor((y-yllcorner) / cellsize); }
    public int toCol(double x) { return (int) Math.floor((x-xllcorner) / cellsize); }
    public int[] toRowCol(double[] xy) { return new int[] { toRow(xy[1]), toCol(xy[0]) }; }
    public double toX(int c) { return xllcorner + (c+.5)*cellsize; } // center of pixel
    public double toY(int r) { return yllcorner + (nrows-r-.5)*cellsize; }
    public double toYMax(int r) { return yllcorner + (nrows-r)*cellsize; }
    public double toYMin(int r) { return yllcorner + (nrows-r-1)*cellsize; }
    public double[] toXY(int[] rc) { return new double[] { toX(rc[1]), toY(rc[0]) }; }
    public double[] toXY(int r, int c) { return new double[] { toX(c), toY(r) }; }
    public boolean inBounds(int r, int c) { return (r>=0 && c>=0 && r<nrows && c<ncols); }
    public boolean inBounds(double x, double y) {
	int r = toRow(y), c = toCol(x);
	return (r>=0 && c>=0 && r<nrows && c<ncols);
    }
}
