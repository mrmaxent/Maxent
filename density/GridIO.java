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
import java.util.*;
import java.util.zip.*;

public class GridIO {
    public static boolean compressGrids = true; 
    static int keepEvery = 1;
    public static int maxRowsAndCols=-1;
    public static HashMap GRDTypeNames = new HashMap();
    static { GRDTypeNames.put("SMALLINT", "SHORT");
      GRDTypeNames.put("SINGLE", "FLOAT");
      GRDTypeNames.put("LONGINT", "INT");
      GRDTypeNames.put("INTEGER", "INT");
      GRDTypeNames.put("INT2BYTES", "SHORT");
      GRDTypeNames.put("FLT4BYTES", "FLOAT");
      GRDTypeNames.put("INT4BYTES", "INT");
      GRDTypeNames.put("FLT8BYTES", "DOUBLE");
      GRDTypeNames.put("INT2B", "SHORT");
      GRDTypeNames.put("FLT4B", "FLOAT");
      GRDTypeNames.put("INT4B", "INT");
      GRDTypeNames.put("FLT8B", "DOUBLE");
      GRDTypeNames.put("INT1B", "BYTE");
      // latest datatypes below, as of 1/2012.  
      // Only a subset of all .grd datatypes (esp. no unsigned versions)
      // and I don't use the byteorder field or the nbands & bandorder fields
      GRDTypeNames.put("INT1S", "BYTE");
      GRDTypeNames.put("INT2S", "SHORT");
      GRDTypeNames.put("INT4S", "INT");
      GRDTypeNames.put("FLT4S", "FLOAT");
      GRDTypeNames.put("FLT8S", "DOUBLE");
    }
    // note that signed bytes are written as shorts for .grd format
    // Also, Diva 5.2.0.2 barfs on INT1B, trying to read beyond end of file,
    // so we write unsigned bytes as shorts too.
    public static final String[] grdTypeName = {"INT2BYTES", "FLT4BYTES", "INT2BYTES", "INT4BYTES", "FLT8BYTES", "INT2BYTES"};

    public static Grid readGrid(File file) throws IOException {
	return readGrid(file.getPath());
    }
    public static Grid readGrid(String fileName) throws IOException {
	Utils.reportDoing("Reading file " + (new File(fileName)).getName());
	Grid g=null;
	String lower = fileName.toLowerCase();
	try {
	    if (lower.endsWith(".asc")) 
		g = readASC(fileName);
	    else if (lower.endsWith(".mxe"))
		g = readMXE(fileName);
	    else if (lower.endsWith(".grd") || lower.endsWith(".gri") || lower.endsWith(".bil"))
		g = readGRD(fileName);
	} catch (Exception e) {
	    Utils.fatalException("Error in file " + fileName, e);
	}
	if (g==null)
	    throw new IOException("File " + fileName + ": suffix not recognized");
	if (Utils.interrupt) return null;

	Utils.echoln(g.getTypeName() + ", " + g.countData() + " pixels with data");
	Utils.reportMemory("readGrid");
	return g;
    }

    static class GRDHeader {
	GridDimension dim;
	int nativetype;
	double NODATA_value;
	String name;
	//	public GRDHeader(GridDimension d, int n, int v) { dim=d; nativetype=n; NODATA_value=v; }

	String getHdr(String field, HashMap map, String filename) {
	    if (!map.containsKey(field))
		//		throw new TerminalException("File " + filename + " missing field " + field, null);
	    Utils.fatalException("File " + filename + " missing field " + field, null);
	    return (String) map.get(field);
	}
	void BILHeader(String fileName) throws IOException {
	    HashMap map = new HashMap();
	    BufferedReader in = new BufferedReader(new FileReader(fileName));
	    String line;
	    while ((line = in.readLine()) != null) {
		String[] fields = line.replaceAll("\\s+"," ").split(" ");
		if (fields.length>1)
		    map.put(fields[0].toUpperCase(Locale.US), fields[1]);
	    }
	    int nr = Integer.parseInt(getHdr("NROWS", map, fileName));
	    int nc = Integer.parseInt(getHdr("NCOLS", map, fileName));
	    int nbands = Integer.parseInt(getHdr("NBANDS", map, fileName));
	    int nbits = Integer.parseInt(getHdr("NBITS", map, fileName));
	    double minx = Double.parseDouble(getHdr("ULXMAP", map, fileName));
	    double maxy = Double.parseDouble(getHdr("ULYMAP", map, fileName));
	    double xdim = Double.parseDouble(getHdr("XDIM", map, fileName));
	    double ydim = Double.parseDouble(getHdr("YDIM", map, fileName));
	    NODATA_value = Double.parseDouble(getHdr("NODATA", map, fileName));
	    if (nbands!=1)
		Utils.fatalException("File " + fileName + ": multiple bands not supported", null);
	    if (xdim!=ydim)
		Utils.fatalException("File " + fileName + ": different x and y resolution not supported", null);
	    dim = new GridDimension(minx-xdim/2.0, maxy-ydim*(nr-0.5), xdim, nr, nc);
	    name = new File(fileName).getName().replaceAll(".hdr", "");
	    boolean isfloat = (map.containsKey("PIXELTYPE") && map.get("PIXELTYPE").equals("FLOAT"));
	    if (nbits==8) nativetype = Grid.BYTE;
	    else if (nbits==16) nativetype = Grid.SHORT;
	    else if (nbits==32) nativetype = isfloat?Grid.FLOAT:Grid.INT;
	    else if (nbits==64) nativetype = Grid.DOUBLE;
	    else Utils.fatalException("File " + fileName + ": unsupported data type " + nbits + " bits", null);
	}

        double get2d(GetIniValueParser parser, String f1, String f2) {
	    return Double.parseDouble(get2(parser, f1, f2));
	}
        int get2i(GetIniValueParser parser, String f1, String f2) {
	    return Integer.parseInt(get2(parser, f1, f2));
	}
        String get2(GetIniValueParser parser, String f1, String f2) {
	    String s = parser.getValue("GeoReference", f1);
	    if (s.equals("")) 
		s = parser.getValue("GeoReference", f2);
	    //	    System.out.println(f1 + " " + f2 + " " + s);
	    return s;
	}

	public GRDHeader(String fileName) throws IOException { 
	    if (fileName.toLowerCase().endsWith(".hdr")) { BILHeader(fileName); return; }
	    GetIniValueParser parser = new GetIniValueParser(fileName);
	    int ncols = get2i(parser,"Columns","ncols");
	    int nrows = get2i(parser,"Rows","nrows");
	    double minx = get2d(parser,"MinX","xmin");
	    double maxx = get2d(parser,"MaxX","xmax");
	    double miny = get2d(parser,"MinY","ymin");
	    double maxy = get2d(parser,"MaxY","ymax");
	    double cellsizex = (maxx-minx)/ncols, cellsizey = (maxy-miny)/nrows;
	    if ((cellsizex-cellsizey) > cellsizex * .000001)
		Utils.fatalException("Grid file " + fileName + ": different x and y resolution not supported", null);
	    String datatype = parser.getValue("Data", "DataType").toUpperCase(Locale.US);
	    nativetype = -1;
	    if (GRDTypeNames.containsKey(datatype))
		datatype = (String) GRDTypeNames.get(datatype);
	    for (int i=0; i<Grid.typeName.length; i++)
		if (Grid.typeName[i].equals(datatype))
		    nativetype = i;
	    if (nativetype==-1) 
		Utils.fatalException("Grid file " + fileName + ": unsupported data type " + datatype, null);
	    NODATA_value = Double.parseDouble(parser.getValue("Data", "NoDataValue"));
	    name = parser.getValue("General", "Title");
	    dim = new GridDimension(minx, miny, cellsizex, nrows, ncols);
	}
    }

    static String root(String fileName) {
	return (fileName.indexOf(".")==-1) ? fileName : fileName.substring(0, fileName.lastIndexOf("."));
    }

    static String griFile(String fileName) throws IOException {
	String root = root(fileName);
	if (fileName.toLowerCase().endsWith(".bil")) {
	    if (new File(fileName).exists())
		return fileName;
	    return missingFile("data", fileName, null);
	}
	if (new File(root+".GRI").exists()) 
	    return root+".GRI";
	if (new File(root+".gri").exists()) 
	    return root+".gri";
	return missingFile("data", root+".gri", null);
    }

    static String grdFile(String fileName) throws IOException {
	String root = root(fileName);
	if (fileName.toLowerCase().endsWith(".bil")) {
	    if (new File(root+".HDR").exists()) 
		return root+".HDR";
	    if (new File(root+".hdr").exists()) 
		return root+".hdr";
	    return missingFile("header", root+".hdr", null);
	}
	if (new File(root+".GRD").exists()) 
	    return root+".GRD";
	if (new File(root+".grd").exists()) 
	    return root+".grd";
	return missingFile("header", root+".grd", null);
    }

    static String missingFile(String type, String missing, String forfile) throws IOException {
	throw new IOException("Missing " + 
			 (type==null?"":type+" ") + "file " +
			 (missing==null?"":(missing+" ")) + 
			      (forfile==null?"":("for " + forfile)));
    }

    static Grid readGRD(String fileName) throws IOException {
	String grd = grdFile(fileName), gri = griFile(fileName);
	if (Utils.interrupt) return null;
	GRDHeader grdh = new GRDHeader(grd);
	com.macfaq.io.LittleEndianInputStream objin = new com.macfaq.io.LittleEndianInputStream(new FileInputStream(gri));
	Grid result = readBinary(grdh.name, grdh.dim, grdh.NODATA_value, grdh.nativetype, objin);
	objin.close();
	return result;
    }

    static Grid readMXE(String fileName) throws IOException {
	ObjectInputStream objin = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
	GridDimension dimension = new GridDimension(objin);
	int NODATA_value = objin.readInt();
	final int nativeType = objin.readInt();
	String name = new File(fileName).getName();
	name = name.substring(0, name.length()-4);
	Grid result = readBinary(name, dimension, NODATA_value, nativeType, objin);
	objin.close();
	return result;
    }

    static Grid readBinary(String name, GridDimension dimension, double NODATA_value, int nativeType, DataInput objin) throws IOException {
	GridDimension dim = shrink(dimension);
	final int k = keepEvery;
	switch (nativeType) {
	case (Grid.SHORT): {
	    short[][] vals = new short[dim.nrows][dim.ncols];
	    for (int i=0; i<dimension.nrows; i++) {
		Utils.reportProgress((i*100)/(double)dimension.nrows);
		for (int j=0; j<dimension.ncols; j++)
		    vals[i/k][j/k] = objin.readShort();
	    }
	    return Grid.vals2Grid(dim, vals, name, NODATA_value, compressGrids);
	}
	case (Grid.BYTE): 
	case (Grid.UBYTE):
	    {
	    byte[][] vals = new byte[dim.nrows][dim.ncols];
	    for (int i=0; i<dimension.nrows; i++) {
		Utils.reportProgress((i*100)/(double)dimension.nrows);
		for (int j=0; j<dimension.ncols; j++)
		    vals[i/k][j/k] = objin.readByte();
	    }
	    return Grid.vals2Grid(dim, vals, name, NODATA_value, compressGrids, nativeType==Grid.UBYTE);
	}
	case (Grid.INT): {
	    int[][] vals = new int[dim.nrows][dim.ncols];
	    for (int i=0; i<dimension.nrows; i++) {
		Utils.reportProgress((i*100)/(double)dimension.nrows);
		for (int j=0; j<dimension.ncols; j++)
		    vals[i/k][j/k] = objin.readInt();
	    }
	    return Grid.vals2Grid(dim, vals, name, NODATA_value, compressGrids);
	}
	case (Grid.DOUBLE): {
	    double[][] vals = new double[dim.nrows][dim.ncols];
	    for (int i=0; i<dimension.nrows; i++) {
		Utils.reportProgress((i*100)/(double)dimension.nrows);
		for (int j=0; j<dimension.ncols; j++) {
		    vals[i/k][j/k] = objin.readDouble();
		    if (Double.isNaN(vals[i/k][j/k])) 
			vals[i/k][j/k] = NODATA_value; 
		}
	    }
	    return Grid.vals2Grid(dim, vals, name, NODATA_value, compressGrids);
	}
	default: 
	    float[][] vals = new float[dim.nrows][dim.ncols];
	    for (int i=0; i<dimension.nrows; i++) {
		Utils.reportProgress((i*100)/(double)dimension.nrows);
		for (int j=0; j<dimension.ncols; j++) {
		    vals[i/k][j/k] = objin.readFloat();
		    if (Float.isNaN(vals[i/k][j/k])) 
			vals[i/k][j/k] = (float) NODATA_value; 
		}
	    }
	    return Grid.vals2Grid(dim, vals, name, NODATA_value, compressGrids);
	}
    }

    static GridDimension shrink(GridDimension dim) {
	if (maxRowsAndCols==-1 || dim.ncols <= maxRowsAndCols || dim.nrows <= maxRowsAndCols)
	    return dim;
	double mr = dim.nrows/(double)maxRowsAndCols;
	double mc = dim.ncols/(double)maxRowsAndCols;
	keepEvery = (int) ((mr<mc) ? Math.ceil(mr) : Math.ceil(mc));
	return new GridDimension(dim.xllcorner, dim.yllcorner,
				 dim.cellsize*keepEvery,
				 (int) Math.ceil(dim.nrows / (double) keepEvery),
				 (int) Math.ceil(dim.ncols / (double) keepEvery));
    }
	
    static double getDouble(String s) {
	try {
	    return Double.parseDouble(s);
	} catch (NumberFormatException e) {
	    // deal with decimal commas, without localization headaches
	    return Double.parseDouble(s.replace(',', '.'));
	}
    }
	
    static GridDimension getDimension(BufferedReader in) throws IOException {
	int ncols = Integer.parseInt(nextHeaderVal(in));
	if (ncols>1000000) 
	    Utils.warn("Warning: ascii file has more than a million columns");
	int nrows = Integer.parseInt(nextHeaderVal(in));
	double xllcorner = getDouble(nextHeaderVal(in));
	double yllcorner = getDouble(nextHeaderVal(in));
	double cellsize = getDouble(nextHeaderVal(in));
	return new GridDimension(xllcorner, yllcorner, cellsize, nrows, ncols);
    }
    // must call this getNODATA_value immediately after getDimension
    static double getNODATA_value(BufferedReader in) throws IOException {
	// need the float, as grid is read as floats
	return (float) getDouble(nextHeaderVal(in));
    }

    static Grid readASC(String fileName) throws IOException {
	/* read header */
	BufferedReader in = new BufferedReader(new FileReader(fileName));
	GridDimension dim = getDimension(in), dim2 = shrink(dim);
	double NODATA_value = getNODATA_value(in);
	short[][] vals=null;
	float[][] floatvals=null;
	boolean isShort = true;
	if (NODATA_value == (short) NODATA_value)
	    vals = new short[dim2.nrows][];
	else {
	    isShort = false;
	    floatvals = new float[dim2.nrows][dim2.ncols];
	}
	short min = Short.MAX_VALUE, max = Short.MIN_VALUE;
	MyScanner scanner = new MyScanner(in);
	for (int i=0; i<dim.nrows; i++) {
	    int ii = (keepEvery==1) ? i : i / keepEvery;
	    if (isShort) 
		if (keepEvery==1 || i%keepEvery==0)
		    vals[ii] = new short[dim2.ncols];
	    if (Utils.interrupt) {
		in.close();
		return null;
	    }
	    Utils.reportProgress((i*100)/dim.nrows);
	    for (int j=0; j<dim.ncols; j++) {
		String s = scanner.next();
		if (keepEvery!=1 && (i%keepEvery!=0 || j%keepEvery!=0))
		    continue;
		int jj = (keepEvery==1) ? j : j / keepEvery;
		if (isShort) {
		    try { 
			vals[ii][jj] = Short.parseShort(s); 
			if (vals[ii][jj] != NODATA_value && vals[ii][jj] < min) 
			    min = vals[ii][jj];
			if (vals[ii][jj] != NODATA_value && vals[ii][jj] > max) max = vals[ii][jj];
		    }
		    catch (NumberFormatException e) {
			isShort = false;
			floatvals = new float[dim2.nrows][dim2.ncols];
			for (int iii=0; iii<ii; iii++)
			    for (int jjj=0; jjj<dim2.ncols; jjj++)
				floatvals[iii][jjj] = vals[iii][jjj];
			for (int jjj=0; jjj<jj; jjj++)
			    floatvals[ii][jjj] = vals[ii][jjj];
			vals = null;
		    }
		}
		if (!isShort) {
		    try {
			floatvals[ii][jj] = Float.parseFloat(s);
		    } catch (NumberFormatException e) {
			// deal with decimal commas, without localization headaches
			floatvals[ii][jj] = Float.parseFloat(s.replace(',', '.'));
		    }
		    if (Double.isNaN(floatvals[ii][jj])) 
			floatvals[ii][jj] = (float) NODATA_value;
		}
	    }
	}
	in.close();
	String name = new File(fileName).getName();
	name = name.substring(0, name.length()-4);
	if (isShort && min > -128  && max < 128) {
	    byte[][] byteVals = new byte[dim2.nrows][];
	    for (int r=0; r<dim2.nrows; r++) {
		byteVals[r] = new byte[dim2.ncols];
		for (int c=0; c<dim2.ncols; c++)
		    byteVals[r][c] = (vals[r][c]==NODATA_value) ? (byte) -128 : (byte) (vals[r][c]);
		vals[r] = null;
	    }
	    Grid result = Grid.vals2Grid(dim2, byteVals, name, -128, compressGrids, false);
	    return result;
	}
	if (isShort && min >= 0  && max < 255) {
	    byte[][] byteVals = new byte[dim2.nrows][];
	    for (int r=0; r<dim2.nrows; r++) {
		byteVals[r] = new byte[dim2.ncols];
		for (int c=0; c<dim2.ncols; c++)
		    byteVals[r][c] = (vals[r][c]==NODATA_value) ? (byte) 255 : (byte) (vals[r][c]);
		vals[r] = null;
	    }
	    Grid result = Grid.vals2Grid(dim2, byteVals, name, 255, compressGrids, true);
	    return result;
	}
	Grid result = isShort ? 
	    Grid.vals2Grid(dim2, vals, name, NODATA_value, compressGrids) :
	    Grid.vals2Grid(dim2, floatvals, name, NODATA_value, compressGrids);
	return result;
    }
	
    static String nextHeaderVal(BufferedReader in) throws IOException {
	String s = in.readLine();
	StringTokenizer st = new StringTokenizer(s);
	st.nextToken();
	return st.nextToken();
    }
}
