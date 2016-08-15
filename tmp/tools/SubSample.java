package density.tools;

// uses density.Convert.readEfficient to save memory, could take a flag to 
// override, as Convert does

import java.io.*;
import density.*;

public class SubSample {

    public static void main(String args[]) {
	String usage = "Usage: SubSample indir outdir interval outputtype";
	if (args.length<4) {
	    System.out.println(usage);
	    System.exit(0);
	}
	String indir = args[0], outdir = args[1], outputtype = args[3];
	int interval = Integer.parseInt(args[2]);
	try {
	    new SubSample().go(indir, outdir, interval, outputtype);
	}
	catch (IOException e) { 
	    System.out.println(e.toString());
	    System.exit(1);
	}
    }

    void go(String indir, String outdir, final int interval, String outputtype) throws IOException {

	File[] layerFiles = new File(indir).listFiles(density.Utils.filenameFilter);
	java.util.Arrays.sort(layerFiles);
	for (int i=0; i<layerFiles.length; i++) {
	    System.out.println("Processing " + layerFiles[i]);
	    String inFile = layerFiles[i].getPath();
	    final Grid grid = Convert.readEfficient(inFile);
	    GridDimension dim = grid.getDimension();
	    Grid g=null;
	    int nrows = (int) Math.ceil(dim.getnrows() / (double) interval);
	    int ncols = (int) Math.ceil(dim.getncols() / (double) interval);
	    GridDimension dim2 = new GridDimension(dim.getxllcorner(), dim.getyllcorner(), dim.getcellsize()*interval, nrows, ncols);
	    if (grid instanceof GridByte) {
		g = new GridByte(dim2, grid.getName()) {
			public byte evalByte(int r, int c) {
			    return ((GridByte) grid).evalByte(r*interval, c*interval);
			}
			public boolean hasData(int r, int c) {
			    return grid.hasData(r*interval, c*interval);
			}
		    };
	    }
	    else if (grid instanceof GridShort)
		g = new GridShort(dim2, grid.getName()) {
			public short evalShort(int r, int c) {
			    return ((GridShort) grid).evalShort(r*interval, c*interval);
			}
			public boolean hasData(int r, int c) {
			    return grid.hasData(r*interval, c*interval);
			}
		    };
	    else if (grid instanceof GridInt)
		g = new GridInt(dim2, grid.getName()) {
			public int evalInt(int r, int c) {
			    return ((GridInt) grid).evalInt(r*interval, c*interval);
			}
			public boolean hasData(int r, int c) {
			    return grid.hasData(r*interval, c*interval);
			}
		    };
	    else if (grid instanceof GridDouble)
		g = new GridDouble(dim2, grid.getName()) {
			public double evalDouble(int r, int c) {
			    return ((GridDouble) grid).evalDouble(r*interval, c*interval);
			}
			public boolean hasData(int r, int c) {
			    return grid.hasData(r*interval, c*interval);
			}
		    };
	    else if (grid instanceof GridUbyte)
		g = new GridUbyte(dim2, grid.getName()) {
			public short evalUbyte(int r, int c) {
			    return ((GridUbyte) grid).evalUbyte(r*interval, c*interval);
			}
			public boolean hasData(int r, int c) {
			    return grid.hasData(r*interval, c*interval);
			}
		    };
	    else g = new Grid(dim2, grid.getName()) {
		    public float eval(int r, int c) {
			return grid.eval(r*interval, c*interval);
		    }
		    public boolean hasData(int r, int c) {
			return grid.hasData(r*interval, c*interval);
		    }
		};

	    g.setNODATA_value(grid.getNODATA_value());
	    File outputFile = new File(outdir, grid.getName() + "." + outputtype);
	    new GridWriter(g, outputFile).writeAll();
	}
    }
}
