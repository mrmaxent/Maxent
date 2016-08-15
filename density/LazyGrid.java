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

public class LazyGrid extends Grid {
    private int row, nativeType, readingColumn;
    private boolean isMXE, isGRD;
    private float[] currentRow;
    private ObjectInputStream objin;
    private BufferedReader in;
    private String fileName;

    public LazyGrid(String fileName) throws IOException {
	this.fileName = fileName;
	initialize();
    }

    public LazyGrid() {}

    void initialize() throws IOException {
	isMXE = false;
	isGRD = false;
	String lower = fileName.toLowerCase();
	if (lower.endsWith(".asc"))
	    LazyGridAsc(fileName);
	else if (lower.endsWith(".grd") || lower.endsWith(".gri") || lower.endsWith(".bil"))
	    LazyGridGrd(fileName);
	else
	    LazyGridMXE(fileName);
	row=-1;
	currentRow = new float[getDimension().getncols()];
	readRow();
	name = new File(fileName).getName();
	name = name.substring(0, name.length()-4);
    }

    private void LazyGridMXE(String fileName) throws IOException {
	isMXE = true;
	objin = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
	setDimension(new GridDimension(objin));
	NODATA_value = objin.readInt();
	nativeType = objin.readInt();
    }

    private void readRow() throws IOException {
	readingColumn=0;
	if (isMXE) readRowMXE();
	else if (isGRD) readRowGRD();
	else readRowAsc();
	row++;
    }

    public boolean hasData(int r, int c) { 
	//	if (nativeType == Grid.FLOAT)  
	// need this for DIVA grids
	return eval(r,c)!=(float) NODATA_value; 
	//	else
	//	    return eval(r,c)!=NODATA_value; 
    }
    public float eval(int r, int c) {
	try {
	    while (r>row) readRow();
	}
	catch (Exception e) {
	    Utils.fatalException("Error reading file " + fileName + " row " + r + " column " + readingColumn, e);
	    //	    throw new TerminalException("Error reading file " + fileName + " row " + r + " column " + c, e.toString());
	    return (float) 0.0;
	}
	if (r < row) throw new RuntimeException("LazyGrid: trying to read from a row after it has been processed");
	return currentRow[c];
    }

    private void readRowMXE() throws IOException {
	for (int j=0; j<getDimension().ncols; j++) {
	    readingColumn = j;
	    switch (nativeType) {
	    case (Grid.SHORT):
		currentRow[j] = (float) objin.readShort(); break;
	    case (Grid.BYTE):
		currentRow[j] = (float) objin.readByte(); break;
	    case (Grid.UBYTE):
		short s = (short) objin.readByte();
		currentRow[j] = (s>=0) ? s : (s+256); break;
	    case (Grid.DOUBLE):
		currentRow[j] = (float) objin.readDouble(); break;
	    case (Grid.INT):
		currentRow[j] = (float) objin.readInt(); break;
	    default: 
		currentRow[j] = objin.readFloat(); break;
	    }
	}
    }

    private void readRowGRD() throws IOException {
	for (int j=0; j<getDimension().ncols; j++) {
	    readingColumn = j;
	    switch (nativeType) {
	    case (Grid.SHORT):
		currentRow[j] = (float) leobjin.readShort(); break;
	    case (Grid.BYTE):
		currentRow[j] = (float) leobjin.readByte(); break;
	    case (Grid.UBYTE):
		short s = (short) leobjin.readByte();
		currentRow[j] = (s>=0) ? s : (s+256); break;
	    case (Grid.DOUBLE):
		currentRow[j] = (float) leobjin.readDouble(); 
		if (Float.isNaN(currentRow[j])) currentRow[j] = (float) NODATA_value;
		break;
	    case (Grid.INT):
		currentRow[j] = (float) leobjin.readInt(); break;
	    default: 
		currentRow[j] = leobjin.readFloat(); 
		if (Float.isNaN(currentRow[j])) currentRow[j] = (float) NODATA_value;
		break;
	    }
	}
    }

    private com.macfaq.io.LittleEndianInputStream leobjin;
    private void LazyGridGrd(String fileName) throws IOException {
	String grd=GridIO.grdFile(fileName), gri=GridIO.griFile(fileName);
	GridIO.GRDHeader grdh = new GridIO.GRDHeader(grd);
	leobjin = new com.macfaq.io.LittleEndianInputStream(new FileInputStream(gri));
	setDimension(grdh.dim);
	NODATA_value = grdh.NODATA_value;
	nativeType = grdh.nativetype;
	isGRD = true;
    }

    private MyScanner scanner;
    private void LazyGridAsc(String fileName) throws IOException {
	in = new BufferedReader(new FileReader(fileName));
	setDimension(GridIO.getDimension(in));
	NODATA_value = GridIO.getNODATA_value(in);
	scanner = new MyScanner(in);
	//	scanner.useLocale(Locale.US);
    }

    private void readRowAsc() throws IOException {
	for (int j=0; j<getDimension().getncols(); j++)
	    currentRow[readingColumn=j] = scanner.nextFloat();
    }

    void close() throws IOException {
	if (isMXE) objin.close();
	else if (isGRD) leobjin.close();
	else in.close();
    }

}
