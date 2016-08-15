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

import java.text.*;
//import java.awt.event.*;
import java.io.*;
import java.util.zip.*;

public abstract class Grid {
    public static final int SHORT = 0, FLOAT = 1, BYTE = 2, INT = 3, DOUBLE = 4, UBYTE = 5; 
    public static final String[] typeName = {"SHORT", "FLOAT", "SIGNEDBYTE", "INT", "DOUBLE", "BYTE"};
    // signedbyte has 16 bits, because .grd and .bil write them as shorts
    public static final int[] nbits = {16, 32, 16, 32, 64, 8};

    static boolean interpolateSamples = false;
    static int defaultNODATA_value = -9999;
    String name;
    private GridDimension dimension;
    double NODATA_value = defaultNODATA_value;

    public Grid() {};
    public Grid(GridDimension dim, String s) { dimension = dim; name = s; }

    public int getType() { 
	if (this instanceof GridByte) return BYTE;
	if (this instanceof GridShort) return SHORT;
	if (this instanceof GridInt) return INT;
	if (this instanceof GridDouble) return DOUBLE;
	if (this instanceof GridUbyte) return UBYTE;
	return FLOAT;
    }
    public String getTypeName() { return typeName[getType()]; }
    public int getNbits() { return nbits[getType()]; }
    public String getGrdTypeName() { return GridIO.grdTypeName[getType()]; }
    public boolean isFloatType() { return (getType()==FLOAT || getType()==DOUBLE); }
    public String getName() { return name; }
    public GridDimension getDimension() { return dimension; }
    public void setDimension(GridDimension dim) { dimension = dim; }
    public abstract float eval(int r, int c);
    public abstract boolean hasData(int r, int c);
    public int getNODATA_value() { return (int) NODATA_value; }
    public void setNODATA_value(double ndv) { NODATA_value = ndv; }
    public boolean hasData(double[] xy) { int[] rc = dimension.toRowCol(xy); return hasData(rc[0], rc[1]); }
    public float eval(double[] xy) { int[] rc = dimension.toRowCol(xy); return eval(rc[0], rc[1]); }
    public float evalSampleFromGrid(Sample s) {
	if (!hasData(s.getRow(),s.getCol()))
	    return (float) NODATA_value;
	if (interpolateSamples) return (float) interpolate(s.lon, s.lat);
	return Grid.this.eval(s.getRow(),s.getCol());
    }
   
    public int countData() {
	int cnt=0;
	for (int i=0; i<getDimension().nrows; i++)
	    for (int j=0; j<getDimension().ncols; j++)
		if (hasData(i,j)) cnt++;
	return cnt;
    }

    public double sum() { 
	double sum=0;
	for (int i=0; i<getDimension().nrows; i++)
	    for (int j=0; j<getDimension().ncols; j++)
		if (hasData(i,j)) 
		    sum += eval(i,j);
	return sum;
    }

    public float[] getVals() {
	float[] result = new float[countData()];
	for (int i=0, cnt=0; i<getDimension().nrows; i++)
	    for (int j=0; j<getDimension().ncols; j++)
		if (hasData(i,j)) 
		    result[cnt++] = eval(i,j);
	return result;
    }

    abstract class GridFeature extends LayerFeature {
	GridFeature(int n, String s, int t) { super(n,s,t); }
	public double eval(Sample s) {
	    if (!hasData(s)) 
		throw new RuntimeException("Attempt to evaluate " + this.name + " at sample with no value");
	    if (s.featureMap!=null)
		return ((Double) s.featureMap.get(this.name)).doubleValue();
	    if (interpolateSamples) return interpolate(s.lon, s.lat);
	    return Grid.this.eval(s.getRow(),s.getCol());
	}
	public double eval(Sample s, float val) {
	    if (interpolateSamples) return interpolate(s.lon, s.lat, val);
	    return (eval(s) == val) ? 1.0 : 0.0;
	}
	public boolean hasData(Sample s) { 
	    if (s.featureMap!=null) {
		if (!s.featureMap.containsKey(this.name))
		    Utils.fatalException("Sample missing data for variable " + this.name, null);
		return (s.featureMap.get(this.name) != null);
	    }
	    return Grid.this.hasData(s.getRow(),s.getCol());
	}
    }
	
    LayerFeature toFeature(final float[] point2val, final int layerType) {
	return new GridFeature(point2val==null ? 0 : point2val.length, name, layerType) {
		public double eval(int p) { 
		    return point2val[p];
		}
	    };
    }
    
    double interpolate(double lon, double lat) { return interpolate(lon, lat, (float) 0, false); }
    double interpolate(double lon, double lat, float target) { return interpolate(lon, lat, target, true); }
    double interpolate(double lon, double lat, float target, boolean match) {
	int rr = dimension.toRow(lat), cc=dimension.toCol(lon);
	double pixelLon = dimension.toX(cc), pixelLat = dimension.toY(rr);
	double xdiff = lon-pixelLon, ydiff = lat - pixelLat;
	int llr = (ydiff>0)?rr-1:rr;
	int llc = (xdiff<0)?cc-1:cc;
	double val=0.0;
	for (int r=llr; r<llr+2; r++)
	    for (int c=llc; c<llc+2; c++) {
		double v = hasData(r,c) ? eval(r,c) : eval(rr,cc);
		if (match) v = (v==target) ? 1.0 : 0.0;
		val += v * (1-Math.abs(dimension.toY(r)-lat)/dimension.cellsize)
		    * (1-Math.abs(dimension.toX(c)-lon)/dimension.cellsize);
	    }
	return val;
    }

    public float[] minmax() {
	float max=-1, min=-1;
	boolean first=true;
	for (int r=0; r<getDimension().nrows; r++)
	    for (int c=0; c<getDimension().ncols; c++) {
		if (!hasData(r,c)) continue;
		if (first || eval(r,c)>max)
		    max = eval(r,c);
		if (first || eval(r,c)<min)
		    min = eval(r,c);
		first = false;
	    }
	return new float[] {min, max};
    }

    public static int getbin(double x, double min, double max, int n) {
	int bin = (int) (n * (x-min) / (max-min));
	if (bin>=n) bin=n-1;
	if (bin<0) bin=0;
	return bin;
    }

    public double[] histogram(double min, double max, int n) {
	int[] cnts = new int[n];
	int cnt = 0, nr = dimension.getnrows(), nc = dimension.getncols();
	for (int r=0; r<nr; r++)
	    for (int c=0; c<nc; c++) {
		if (!hasData(r,c)) continue;
		cnt++;
		cnts[getbin(eval(r,c),min,max,n)]++;
	    }
	double[] result = new double[n];
	for (int i=0; i<n; i++) result[i] = cnts[i]/(double)cnt;
	return result;
    }

    static Grid vals2Grid(float[][] vals, String name) {
	GridDimension dim = new GridDimension(0,0,1,vals.length,vals[0].length);
	return vals2Grid(dim, vals, name, defaultNODATA_value);
    }

    static Grid compressedGrid(final GridDimension dim, float[][] vals, String name, final double NODATA) { 
	final int nrows = vals.length, ncols = vals[0].length;
	final byte[][] counts = new byte[nrows][];
	final float[][] rleVals = new float[nrows][];
	final RunLengthEncoding rle = new RunLengthEncoding();
	byte[] tmpCounts = new byte[ncols];
	float[] tmpRleVals = new float[ncols];
	for (int i=0; i<nrows; i++) {
	    rle.compress(vals[i], tmpCounts, tmpRleVals);
	    if (rle.compressionRatio < .9) {
		counts[i] = rle.copyCounts(tmpCounts);
		rleVals[i] = rle.copyVals(tmpRleVals);
	    }
	    else {
		counts[i] = null;
		rleVals[i] = vals[i];
	    }
	    vals[i] = null;
	}
	return new Grid(dim, name) {
		int currentRow = -1;
		float[] row = new float[ncols];
		public float eval(int r, int c) { 
		    if (counts[r]==null) return rleVals[r][c];
		    if (r!=currentRow) {
			currentRow = r;
			rle.decompress(counts[r], rleVals[r], row);
		    }
		    return row[c]; 
		}
		public boolean hasData(int r, int c) { 
		    return dim.inBounds(r,c) && eval(r,c)!=(float) NODATA; }
	    };
    }
    static Grid compressedGrid(final GridDimension dim, short[][] vals, String name, final double NODATA) { 
	final int nrows = vals.length, ncols = vals[0].length;
	final byte[][] counts = new byte[nrows][];
	final short[][] rleVals = new short[nrows][];
	final RunLengthEncoding rle = new RunLengthEncoding();
	byte[] tmpCounts = new byte[ncols];
	short[] tmpRleVals = new short[ncols];
	for (int i=0; i<nrows; i++) {
	    rle.compress(vals[i], tmpCounts, tmpRleVals);
	    if (rle.compressionRatio < .9) {
		counts[i] = rle.copyCounts(tmpCounts);
		rleVals[i] = rle.copyVals(tmpRleVals);
	    }
	    else {
		counts[i] = null;
		rleVals[i] = vals[i];
	    }
	    vals[i] = null;
	}
	return new GridShort(dim, name) {
		int currentRow = -1;
		short[] row = new short[ncols];
		public short evalShort(int r, int c) { 
		    if (counts[r]==null) return rleVals[r][c];
		    if (r!=currentRow) {
			currentRow = r;
			rle.decompress(counts[r], rleVals[r], row);
		    }
		    return row[c]; 
		}
		public boolean hasData(int r, int c) { 
		    return dim.inBounds(r,c) && evalShort(r,c)!=NODATA; 
		}
	    };
    }

    static GridByte compressedGrid(final GridDimension dim, byte[][] vals, String name, final double NODATA) { 
	final int nrows = vals.length, ncols = vals[0].length;
	final byte[][] counts = new byte[nrows][];
	final byte[][] rleVals = new byte[nrows][];
	final RunLengthEncoding rle = new RunLengthEncoding();
	byte[] tmpCounts = new byte[ncols];
	byte[] tmpRleVals = new byte[ncols];
	for (int i=0; i<nrows; i++) {
	    rle.compress(vals[i], tmpCounts, tmpRleVals);
	    if (rle.compressionRatio < .9) {
		counts[i] = rle.copyCounts(tmpCounts);
		rleVals[i] = rle.copyVals(tmpRleVals);
	    }
	    else {
		counts[i] = null;
		rleVals[i] = vals[i];
	    }
	    vals[i] = null;
	}
	return new GridByte(dim, name) {
		{ NODATA_value = NODATA; }
		int currentRow = -1;
		byte[] row = new byte[ncols];
		public byte evalByte(int r, int c) {
		    if (counts[r]==null) return rleVals[r][c];
		    if (r!=currentRow) {
			currentRow = r;
			rle.decompress(counts[r], rleVals[r], row);
		    }
		    return row[c]; 
		}
		public boolean hasData(int r, int c) { 
		    return dim.inBounds(r,c) && evalByte(r,c)!=NODATA;
		}
	    };
    }

    public static Grid vals2Grid(final GridDimension dim, final float[][] vals, String name, final double NODATA) { 
	return vals2Grid(dim, vals, name, NODATA, false);
    }

    static Grid vals2Grid(final GridDimension dim, float[][] vals, String name, final double NODATA, boolean compress) {
	if (compress) return compressedGrid(dim, vals, name, NODATA);
	final int[] fD = firstDatas(vals, NODATA);
	final float[][] nv = getDatas(vals, NODATA, fD);
	return new Grid(dim, name) {
		int[] firstDatas = fD;
		float[][] newvals = nv;
		{ NODATA_value = NODATA; }
		public float eval(int r, int c) { 
		    return newvals[r][c-firstDatas[r]];
		}
		public boolean hasData(int r, int c) { 
		    return dim.inBounds(r,c) 
			&& c >= firstDatas[r]
			&& c-firstDatas[r] < newvals[r].length
			&& newvals[r][c-firstDatas[r]]!=(float) NODATA;
		}
	    };
    }

    static Grid vals2Grid(final GridDimension dim, final short[][] vals, String name, final double NODATA) {
	return vals2Grid(dim, vals, name, NODATA, false);
    }

    static Grid vals2Grid(final GridDimension dim, short[][] vals, String name, final double NODATA, boolean compress) {
	if (compress) return compressedGrid(dim, vals, name, NODATA);
	final int[] fD = firstDatas(vals, NODATA);
	final short[][] nv = getDatas(vals, NODATA, fD);
	return new GridShort(dim, name) {
		int[] firstDatas = fD;
		short[][] newvals = nv;
		{ NODATA_value = NODATA; }
		public short evalShort(int r, int c) { 
		    return newvals[r][c-firstDatas[r]];
		}
		public boolean hasData(int r, int c) { 
		    return dim.inBounds(r,c) 
			&& c >= firstDatas[r]
			&& c-firstDatas[r] < newvals[r].length
			&& newvals[r][c-firstDatas[r]]!=NODATA;
		}
	    };
    }

    static Grid vals2Grid(final GridDimension dim, final int[][] vals, String name, final double NODATA) {
	return vals2Grid(dim, vals, name, NODATA, false);
    }

    static Grid vals2Grid(final GridDimension dim, int[][] vals, String name, final double NODATA, boolean compress) {
	//	if (compress) return compressedGrid(dim, vals, name, NODATA);
	if (compress) Utils.warn("compressed int grid not yet implemented");
	final int[] fD = firstDatas(vals, NODATA);
	final int[][] nv = getDatas(vals, NODATA, fD);
	return new GridInt(dim, name) {
		int[] firstDatas = fD;
		int[][] newvals = nv;
		{ NODATA_value = NODATA; }
		public int evalInt(int r, int c) { 
		    return newvals[r][c-firstDatas[r]];
		}
		public boolean hasData(int r, int c) { 
		    return dim.inBounds(r,c) 
			&& c >= firstDatas[r]
			&& c-firstDatas[r] < newvals[r].length
			&& newvals[r][c-firstDatas[r]]!=NODATA;
		}
	    };
    }

    static Grid vals2Grid(final GridDimension dim, final double[][] vals, String name, final double NODATA) {
	return vals2Grid(dim, vals, name, NODATA, false);
    }

    static Grid vals2Grid(final GridDimension dim, double[][] vals, String name, final double NODATA, boolean compress) {
	//	if (compress) return compressedGrid(dim, vals, name, NODATA);
	if (compress) Utils.warn("Warning: compressed double grid not yet implemented");
	final int[] fD = firstDatas(vals, NODATA);
	final double[][] nv = getDatas(vals, NODATA, fD);
	return new GridDouble(dim, name) {
		int[] firstDatas = fD;
		double[][] newvals = nv;
		{ NODATA_value = NODATA; }
		public double evalDouble(int r, int c) { 
		    return newvals[r][c-firstDatas[r]];
		}
		public boolean hasData(int r, int c) { 
		    return dim.inBounds(r,c) 
			&& c >= firstDatas[r]
			&& c-firstDatas[r] < newvals[r].length
			&& newvals[r][c-firstDatas[r]]!=NODATA;
		}
	    };
    }

    static Grid vals2Grid(final GridDimension dim, final byte[][] vals, String name, final double NODATA, boolean isUbyte) {
	return vals2Grid(dim, vals, name, NODATA, false, isUbyte);
    }

    static Grid vals2Grid(final GridDimension dim, byte[][] vals, String name, final double NODATA, boolean compress, boolean isUbyte) {
	if (compress) {
	    Grid g = compressedGrid(dim, vals, name, NODATA);
	    return isUbyte ? gridByte2gridUbyte((GridByte) g) : g;
	}
	final int[] fD = firstDatas(vals, NODATA);
	final byte[][] nv = getDatas(vals, NODATA, fD);
	Grid g = new GridByte(dim, name) {
		int[] firstDatas = fD;
		byte[][] newvals = nv;
		{ NODATA_value = NODATA; }
		public byte evalByte(int r, int c) { 
		    return newvals[r][c-firstDatas[r]];
		}
		public boolean hasData(int r, int c) { 
		    return dim.inBounds(r,c) 
			&& c >= firstDatas[r]
			&& c-firstDatas[r] < newvals[r].length
			&& newvals[r][c-firstDatas[r]]!=NODATA;
		}
	    };
	return isUbyte ? gridByte2gridUbyte((GridByte) g) : g;
    }

    // Uses g.eval(..) even at pixels with no data
    static GridUbyte gridByte2gridUbyte(final GridByte g) {
	return new GridUbyte(g.getDimension(), g.getName()) {
		{ NODATA_value = g.getNODATA_value(); }
		public boolean hasData(int r, int c) { 
		    return getDimension().inBounds(r,c) && evalUbyte(r,c) != NODATA_value; 
		}
		public short evalUbyte(int r, int c) {
		    short s = g.evalByte(r,c);
		    return (s>=0) ? s : (short) (s + 256);
		}
	    };
    }

    static int firstData(float[] vals, double NODATA) {
	for (int i=0; i<vals.length; i++)
	    if (vals[i] != NODATA) return i;
	return vals.length;
    }
    static int lastData(float[] vals, double NODATA) {
	for (int i=vals.length-1; i>=0; i--)
	    if (vals[i] != NODATA) return i;
	return -1;
    }
    static int[] firstDatas(float[][]vals, double NODATA) {
	int[] result = new int[vals.length];
	for (int i=0; i<vals.length; i++)
	    result[i] = firstData(vals[i], NODATA);
	return result;
    }
    static float[] getData(float[] vals, double NODATA, int first, int last) {
	if (first==-1 || last==-1) return new float[0];
	float[] result = new float[last-first+1];
	for (int i=first; i<=last; i++)
	    result[i-first] = vals[i];
	return result;
    }
    static float[][] getDatas(float[][] vals, double NODATA, int[] firsts) {
	float[][] result = new float[vals.length][];
	for (int i=0; i<vals.length; i++)
	    result[i] = getData(vals[i], NODATA, firsts[i], lastData(vals[i], NODATA));
	return result;
    }

    static int firstData(short[] vals, double NODATA) {
	for (int i=0; i<vals.length; i++)
	    if (vals[i] != NODATA) return i;
	return vals.length;
    }
    static int lastData(short[] vals, double NODATA) {
	for (int i=vals.length-1; i>=0; i--)
	    if (vals[i] != NODATA) return i;
	return -1;
    }
    static int[] firstDatas(short[][]vals, double NODATA) {
	int[] result = new int[vals.length];
	for (int i=0; i<vals.length; i++)
	    result[i] = firstData(vals[i], NODATA);
	return result;
    }
    static short[] getData(short[] vals, double NODATA, int first, int last) {
	if (first==-1 || last==-1) return new short[0];
	short[] result = new short[last-first+1];
	for (int i=first; i<=last; i++)
	    result[i-first] = vals[i];
	return result;
    }
    static short[][] getDatas(short[][] vals, double NODATA, int[] firsts) {
	short[][] result = new short[vals.length][];
	for (int i=0; i<vals.length; i++)
	    result[i] = getData(vals[i], NODATA, firsts[i], lastData(vals[i], NODATA));
	return result;
    }

    static int firstData(int[] vals, double NODATA) {
	for (int i=0; i<vals.length; i++)
	    if (vals[i] != NODATA) return i;
	return vals.length;
    }
    static int lastData(int[] vals, double NODATA) {
	for (int i=vals.length-1; i>=0; i--)
	    if (vals[i] != NODATA) return i;
	return -1;
    }
    static int[] firstDatas(int[][]vals, double NODATA) {
	int[] result = new int[vals.length];
	for (int i=0; i<vals.length; i++)
	    result[i] = firstData(vals[i], NODATA);
	return result;
    }
    static int[] getData(int[] vals, double NODATA, int first, int last) {
	if (first==-1 || last==-1) return new int[0];
	int[] result = new int[last-first+1];
	for (int i=first; i<=last; i++)
	    result[i-first] = vals[i];
	return result;
    }
    static int[][] getDatas(int[][] vals, double NODATA, int[] firsts) {
	int[][] result = new int[vals.length][];
	for (int i=0; i<vals.length; i++)
	    result[i] = getData(vals[i], NODATA, firsts[i], lastData(vals[i], NODATA));
	return result;
    }

    static int firstData(byte[] vals, double NODATA) {
	for (int i=0; i<vals.length; i++)
	    if (vals[i] != NODATA) return i;
	return vals.length;
    }
    static int lastData(byte[] vals, double NODATA) {
	for (int i=vals.length-1; i>=0; i--)
	    if (vals[i] != NODATA) return i;
	return -1;
    }
    static int[] firstDatas(byte[][]vals, double NODATA) {
	int[] result = new int[vals.length];
	for (int i=0; i<vals.length; i++)
	    result[i] = firstData(vals[i], NODATA);
	return result;
    }
    static byte[] getData(byte[] vals, double NODATA, int first, int last) {
	if (first==-1 || last==-1) return new byte[0];
	byte[] result = new byte[last-first+1];
	for (int i=first; i<=last; i++)
	    result[i-first] = vals[i];
	return result;
    }
    static byte[][] getDatas(byte[][] vals, double NODATA, int[] firsts) {
	byte[][] result = new byte[vals.length][];
	for (int i=0; i<vals.length; i++)
	    result[i] = getData(vals[i], NODATA, firsts[i], lastData(vals[i], NODATA));
	return result;
    }

    static int firstData(double[] vals, double NODATA) {
	for (int i=0; i<vals.length; i++)
	    if (vals[i] != NODATA) return i;
	return vals.length;
    }
    static int lastData(double[] vals, double NODATA) {
	for (int i=vals.length-1; i>=0; i--)
	    if (vals[i] != NODATA) return i;
	return -1;
    }
    static int[] firstDatas(double[][]vals, double NODATA) {
	int[] result = new int[vals.length];
	for (int i=0; i<vals.length; i++)
	    result[i] = firstData(vals[i], NODATA);
	return result;
    }
    static double[] getData(double[] vals, double NODATA, int first, int last) {
	if (first==-1 || last==-1) return new double[0];
	double[] result = new double[last-first+1];
	for (int i=first; i<=last; i++)
	    result[i-first] = vals[i];
	return result;
    }
    static double[][] getDatas(double[][] vals, double NODATA, int[] firsts) {
	double[][] result = new double[vals.length][];
	for (int i=0; i<vals.length; i++)
	    result[i] = getData(vals[i], NODATA, firsts[i], lastData(vals[i], NODATA));
	return result;
    }

    public abstract class Applier {
	public abstract void process(int r, int c, float v);
    }
    public void apply(Applier applier) {
	GridDimension dim = getDimension();
	for (int r=0; r<dim.getnrows(); r++)
	    for (int c=0; c<dim.getncols(); c++)
		if (hasData(r,c))
		    applier.process(r,c,eval(r,c));
    }
}

