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
import gnu.getopt.*;
import java.text.*;
import java.util.Locale;

public class Convert {
    static boolean savemem = true;

    public static void main(String args[]) {
	int i, j, k, type;
	final double retainFraction = .6;
	String usage = "Usage: density.Convert [-t] indir insuffix outdir outsuffix";
	Getopt g = new Getopt("Show", args, "t");
	int c;
	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 't':  savemem = !savemem; break;
	    }
	}

	if (args.length-g.getOptind()!=4) {System.out.println(usage); System.exit(0); }
	final String indir = args[g.getOptind()], insuff = args[g.getOptind()+1], outdir = args[g.getOptind()+2], outsuff = args[g.getOptind()+3];

	File[] layerFiles = new File(indir).listFiles
	    (new FilenameFilter() { 
		    public boolean accept(File indir, String name) {
			return name.endsWith(insuff);
		    }});
	
	try {
	    if (outsuff.toLowerCase().equals("csv")) {
		toCsv(layerFiles,outdir,new File(indir).getName());
		return;
	    }
	    
	    for (i=0; i<layerFiles.length; i++) {
		System.out.println(layerFiles[i]);
		String inFile = layerFiles[i].getPath();
		File outFile = new File(outdir, layerFiles[i].getName().substring(0,layerFiles[i].getName().length()-insuff.length()) + outsuff);
		Grid grid = (savemem) ? readEfficient(inFile) :
		    GridIO.readGrid(inFile);
		System.out.println("Data type: " + grid.getTypeName());
		new GridWriter(grid, outFile).writeAll();
	    }
	}
	catch (IOException e) { Utils.fatalException("Error reading/writing files",e); }
    }

    static void toCsv(File[] grids, String outdir, String prefix) throws IOException {
	int ng = grids.length;
	final LazyGrid[] g = new LazyGrid[ng];

	PrintWriter out = density.Utils.writer(new File(outdir, prefix+".csv"));
	out.print("row,column");
	for (int i=0; i<ng; i++) {
	    g[i] = new LazyGrid(grids[i].getPath());
	    if (i>0 && !g[i].getDimension().equals(g[0].getDimension()))
		throw new RuntimeException("Files " + grids[0].getName() + " and " + grids[i].getName() + " have different geographic dimensions");
	    out.print("," + g[i].getName());
	}
	out.println();
	NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
	nf.setGroupingUsed(false);
	GridDimension dim = g[0].getDimension();
	int nrows = dim.getnrows(), ncols = dim.getncols();
	for (int r=0; r<nrows; r++)
	    for (int c=0; c<ncols; c++) {
		boolean hasdata = true;
		for (int i=0; i<ng; i++)
		    if (!g[i].hasData(r,c)) {
			hasdata = false;
			break;
		    }
		if (hasdata) {
		    out.print(r+","+c);
		    for (int i=0; i<ng; i++)
			out.print("," + nf.format(g[i].eval(r,c)));
		    out.println();
		}		    
	    }
	out.close();
    }	
	

    public static Grid readEfficient(String inFile) throws IOException {
	final LazyGrid g = new LazyGrid(inFile);
	GridDimension dim = g.getDimension();
	String name = g.getName();
	// only really need to look for min, max, isfloat if inFile is .asc
	boolean isfloat = false, started = false;
	int min = 0, max = 0;
	Utils.reportDoing("Determining type of file " + (new File(inFile)).getName());
	for (int r=0; r<dim.getnrows(); r++) {
	    if (isfloat) break;
	    if (Utils.interrupt) return null;
	    Utils.reportProgress((r*100)/dim.getnrows());
	    for (int c=0; c<dim.getncols(); c++) {
		if (!g.hasData(r,c)) continue;
		float v = g.eval(r,c);
		if (v != (int) v) {
		    isfloat = true;
		    break;
		}
		if (!started || min>v)
		    min = (int) v;
		if (!started || max<v)
		    max = (int) v;
		started = true;
	    }
	}
	g.initialize();
	if (isfloat) return g;
	if (min>-128 && max<128)
	    return new GridByte(dim, name) {
		    { NODATA_value = -128; }
		    public byte evalByte(int r, int c) {
			return (byte) g.eval(r,c);
		    }
		    public boolean hasData(int r, int c) {
			return g.hasData(r,c);
		    }
		};
	if (min>=0 && max<255)
	    return new GridUbyte(dim, name) {
		    { NODATA_value = 255; }
		    public short evalUbyte(int r, int c) {
			return (short) g.eval(r,c);
		    }
		    public boolean hasData(int r, int c) {
			return g.hasData(r,c);
		    }
		};
	if (min >= Short.MIN_VALUE && max < Short.MAX_VALUE)
	    return new GridShort(dim, name) {
		    { NODATA_value = 
			    (g.NODATA_value==(short) g.NODATA_value) ? 
			    (short) g.NODATA_value :
			    Short.MAX_VALUE; 
		    }
		    public short evalShort(int r, int c) {
			return (short) g.eval(r,c);
		    }
		    public boolean hasData(int r, int c) {
			return g.hasData(r,c);
		    }
		};
	return new GridInt(dim, name) {
		{ NODATA_value = 
			(g.NODATA_value==(int) g.NODATA_value) ? 
			(int) g.NODATA_value :
			Integer.MAX_VALUE; 
		}
		public int evalInt(int r, int c) {
		    return (int) g.eval(r,c);
		}
		public boolean hasData(int r, int c) {
		    return g.hasData(r,c);
		}
	    };
    }
}
	
		    
