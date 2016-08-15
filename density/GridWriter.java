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
import java.text.*;
import java.util.*;
import java.util.zip.*;
import com.macfaq.io.LittleEndianOutputStream;

public class GridWriter {
    public static final int ASC=0, MXE=1, GRD=2, CSV=3;
    int outputFormat;
    boolean scientific = true;
    int minfracdigits = 4, maxfracdigits = 4;
    static String scientificPattern = "0.#####E0";

    Grid grid;
    GridDimension dimension;
    DataOutput out;
    PrintWriter printout;
    double minWritten, maxWritten;
    boolean started, isInt;
    NumberFormat nf;
    String fileName, grdfile, grifile;

    public GridWriter(Grid grid, String fileName, boolean notscientific) throws IOException {
	this(grid, fileName);
	scientific = !notscientific;
    }

    public GridWriter(Grid grid, File file) throws IOException {
	this(grid, file.getPath());
    }

    public GridWriter(Grid grid, String fileName) throws IOException {
	this.grid = grid;
	this.fileName = fileName;
	dimension = grid.getDimension();
	init();
    }

    void init() throws IOException {
	Utils.reportDoing("Writing file " + fileName);
	String lower = fileName.toLowerCase();
	if (lower.endsWith(".asc")) writeASCinit();
	else if (lower.endsWith(".mxe")) writeMXEinit();
	else if (lower.endsWith(".grd") || lower.endsWith(".gri") || lower.endsWith(".bil")) 
	    writeGRDinit();
	else if (lower.endsWith(".csv")) writeCSVinit();
	else {
	    fileName = fileName+".asc";
	    writeASCinit();
	}
    }

    void writeHeader(PrintWriter out, GridDimension dim, int NODATA_value) {
	out.println("ncols         " + dim.ncols);
	out.println("nrows         " + dim.nrows);
	out.println("xllcorner     " + dim.xllcorner);
	out.println("yllcorner     " + dim.yllcorner);
	out.println("cellsize      " + dim.cellsize);
	out.println("NODATA_value  " + NODATA_value);
    }

    void writeMXEinit() throws IOException {
	outputFormat = MXE;
	ObjectOutputStream out1 = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)));
	dimension.write(out1);
	out1.writeInt(grid.getNODATA_value());
	out1.writeInt(grid.getType());
	out = out1;
    }

    void writeGRDinit() throws IOException {
	outputFormat = GRD;
	started=false;
	minWritten=maxWritten=0.0;
	String root = (fileName.indexOf(".")==-1) ? fileName : fileName.substring(0, fileName.lastIndexOf("."));
	if (fileName.toLowerCase().endsWith(".bil")) {
	    grdfile = root+".hdr"; grifile = root+".bil";
	}
	else {
	    grdfile = root+".grd"; grifile = root+".gri";
	}
	out = new LittleEndianOutputStream(new FileOutputStream(grifile));
    }

    void writeGRDheader() throws IOException {
	PrintWriter outh = Utils.writer(grdfile);
	if (grifile.endsWith(".bil")) {
	    outh.println("BYTEORDER     I");
	    outh.println("LAYOUT        BIL");
	    outh.println("NROWS         " + dimension.getnrows());
	    outh.println("NCOLS         " + dimension.getncols());
	    outh.println("NBANDS        1");
	    outh.println("NBITS         " + grid.getNbits());
	    if (grid.isFloatType())
		outh.println("PIXELTYPE     FLOAT");
	    int rowlength = dimension.getncols() * grid.getNbits()/8;
	    outh.println("BANDROWBYTES  " + rowlength);
	    outh.println("TOTALROWBYTES " + rowlength);
	    outh.println("BANDGAPBYTES  0");
	    outh.println("NODATA        " + (grid.getType()==Grid.UBYTE ? -1 : (int) grid.getNODATA_value()));
	    outh.println("ULXMAP        " + (dimension.getxllcorner() + dimension.getcellsize()/2.0));
	    outh.println("ULYMAP        " + (dimension.getyllcorner() + dimension.getcellsize() * (dimension.getnrows()-0.5)));
	    outh.println("XDIM          " + dimension.getcellsize());
	    outh.println("YDIM          " + dimension.getcellsize());
	}
	else {
	    outh.println("[GeoReference]");
	    outh.println("Columns=" + dimension.getncols());
	    outh.println("Rows=" + dimension.getnrows());
	    outh.println("MinX=" + dimension.getxllcorner());
	    outh.println("MinY=" + dimension.getyllcorner());
	    outh.println("MaxX=" + (dimension.getxllcorner() + dimension.getncols() * dimension.getcellsize()));
	    outh.println("MaxY=" + (dimension.getyllcorner() + dimension.getnrows() * dimension.getcellsize()));
	    outh.println("ResolutionX=" + dimension.getcellsize());
	    outh.println("ResolutionY=" + dimension.getcellsize());
	    outh.println("[Data]");
	    outh.println("DataType=" + grid.getGrdTypeName());
	    outh.println("MinValue=" + minWritten);
	    outh.println("MaxValue=" + maxWritten);
	    // workaround for Diva bug: smaller than NoDataValue considered nodata
	    outh.println("NoDataValue=" + (grid.getType()==Grid.UBYTE ? -1 : grid.getNODATA_value()));
	    outh.println("[General]");
	    outh.println("Creator=MaxEnt");
	    outh.println("Version=" + Utils.version);
	    outh.println("Title=" + grid.getName());
	    outh.println("Created=" + (new Date()).toString());
	}
	outh.close();
    }

    public void close() throws IOException {
	if (out!=null && out instanceof ObjectOutputStream) 
	    ((ObjectOutputStream) out).close(); 
	if (out!=null && out instanceof LittleEndianOutputStream) 
	    ((LittleEndianOutputStream) out).close(); 
	if (printout!=null) printout.close();
	if (outputFormat==GRD && !Utils.interrupt)
	    writeGRDheader();
	if (Utils.interrupt) {
	    String toDelete = (outputFormat==GRD) ? grifile : fileName;
	    File toDeleteFile = new File(toDelete);
	    if (toDeleteFile.exists()) {
		Utils.reportDoing("Interrupted, deleting " + toDelete);
		toDeleteFile.delete();
	    }
	}
    }

    public void writeAll() throws IOException {
	for (int i=0; i<dimension.nrows; i++) {
	    if (Utils.interrupt) {
		close();
		return;
	    }
	    Utils.reportProgress(i*100/(double)dimension.nrows);
	    for (int j=0; j<dimension.ncols; j++)
		write(i,j);
	}
	close();
    }

    public void write(int i, int j) throws IOException {
	switch (outputFormat) {
	case MXE: writeBinary(i,j); break;
	case GRD: writeBinary(i,j); break;
	case CSV: writeCSV(i,j); break;
	case ASC: writeASC(i,j); break;
	default: throw new IOException("Unexpected output format in GridWriter:write");
	}
    }

    void writeBinary(int i, int j) throws IOException {
	if (grid.hasData(i,j)) {
	    double val = grid.eval(i,j);
	    if (!started || val<minWritten) minWritten=val;
	    if (!started || val>maxWritten) maxWritten=val;
	    started = true;
	}
	switch (grid.getType()) {
	case Grid.SHORT: 
	    out.writeShort(grid.hasData(i,j) ? ((GridShort) grid).evalShort(i,j) : grid.getNODATA_value());
	    break;
	case Grid.INT: 
	    out.writeInt(grid.hasData(i,j) ? ((GridInt) grid).evalInt(i,j) : grid.getNODATA_value());
	    break;
	case Grid.BYTE:
	    if (outputFormat==GRD) // make signed byte into short
		out.writeShort(grid.hasData(i,j) ? ((GridByte) grid).evalByte(i,j) : grid.getNODATA_value());
	    else 
		out.writeByte(grid.hasData(i,j) ? ((GridByte) grid).evalByte(i,j) : grid.getNODATA_value());
	    break;
	case Grid.UBYTE:
	    if (grid.hasData(i,j)) {
		short val = ((GridUbyte) grid).evalUbyte(i,j);
		if (outputFormat==GRD) // diva 5.2.0.2 barfs on INT1B, so make byte into short
		    out.writeShort(val);
		else 
		    out.writeByte(val>127 ? val-256 : val);
	    }
	    else {
		if (outputFormat==GRD)
		    out.writeShort(-1); // diva thinks everything is nodata if NODATAvalue = 255
		else 
		    out.writeByte(grid.getNODATA_value());
	    }
	    break;
	case Grid.FLOAT:
	    out.writeFloat(grid.hasData(i,j) ? grid.eval(i,j) : grid.getNODATA_value());
	    break;
	case Grid.DOUBLE:
	    out.writeDouble(grid.hasData(i,j) ? ((GridDouble) grid).evalDouble(i,j) : grid.getNODATA_value());
	    break;
	}
    }

    NumberFormat getNF(Grid grid) {
	NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
	nf.setGroupingUsed(false);
	if (scientific && (grid.getTypeName().equals("FLOAT") || 
			   grid.getTypeName().equals("DOUBLE"))) {
	    ((DecimalFormat) nf).applyPattern(scientificPattern);
	}
	else {
	    nf.setMinimumFractionDigits(minfracdigits);
	    nf.setMaximumFractionDigits(maxfracdigits);
	}
	return nf;
    }

    void writeCSVinit() throws IOException {
	outputFormat = CSV;
	printout = Utils.writer(fileName);
	if (dimension.coordNames!=null)
	    printout.print(dimension.coordNames[0] + "," + dimension.coordNames[1] + ",");
	printout.println(grid.name);
    }

    void writeCSV(int i, int j) throws IOException {
	if (dimension.coords!=null)
	    printout.print(dimension.coords[j][0] + "," + dimension.coords[j][1]+",");
	if (grid.hasData(i,j))
	    printout.println(grid.eval(i,j)); // nf.format(grid.eval(r,c)));
	else
	    printout.println(grid.getNODATA_value());
    }
    
    void writeASCinit() throws IOException {
	outputFormat = ASC;
	printout = Utils.writer(fileName);
	writeHeader(printout, dimension, grid.getNODATA_value());
	nf = getNF(grid);
	isInt = grid instanceof GridByte || grid instanceof GridUbyte || grid instanceof GridInt || grid instanceof GridShort;
    }

    void writeASC(int i, int j) throws IOException {
	if (j!=0) printout.print(" ");
	if (grid.hasData(i,j)) {
	    if (isInt) printout.print((int) grid.eval(i,j));
	    else printout.print(nf.format(grid.eval(i,j)));
	}
	else
	    printout.print(grid.getNODATA_value());
	if (j==dimension.ncols-1)
	    printout.println();
    }


    public static void writeGrids(Grid[] grids, String[] filenames, boolean notscientific) throws IOException {
	int num = grids.length;
	GridWriter[] writers = new GridWriter[num];
	for (int i=0; i<num; i++)
	    writers[i] = new GridWriter(grids[i], filenames[i], notscientific);
	//	Utils.reportDoing("Writing file " + filenames[0]);
	int nr = grids[0].getDimension().getnrows(), nc = grids[0].getDimension().getncols();
	for (int r=0; r<nr; r++) {
	    if (Utils.interrupt) break;
	    Utils.reportProgress((r*100)/(double) nr);
	    for (int c=0; c<nc; c++)
		for (int i=0; i<num; i++)
		    writers[i].write(r,c);
        }
	for (int i=0; i<num; i++)
	    writers[i].close();
    }

}
