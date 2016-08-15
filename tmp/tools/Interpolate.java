package density.tools;

import density.*;
import java.io.*;

// Make single year slices for a species, interpolating from 
// decadal predictions

public class Interpolate {

    public static void main(String args[]) {
	if (args.length < 7) {
	    System.out.println("Usage: Interpolate filename1 filename2 y1 y2 interval outdir outprefix");
	    System.exit(0);
	}
	try {
	    new Interpolate().go(args);
	} catch (IOException e) {
	    System.out.println(e.toString());
	}
    }

    void go(String[] args) throws IOException {
	String filename[] = new String[] { args[0], args[1] };
	final int y0 = Integer.parseInt(args[2]);
	final int y1 = Integer.parseInt(args[3]);
	final int interval = Integer.parseInt(args[4]);
	String outdir = args[5], outprefix = args[6];

	final Grid[] grids = new Grid[2];
	boolean writeYear = true;
	
	for (int i=0; i<2; i++)
	    grids[i] = GridIO.readGrid(filename[i]);
	for (int y=y0; y<=y1; y+= interval) {
	    final int year = y;
	    Grid outGrid = new Grid(grids[0].getDimension(), "tmpname") {
		    public boolean hasData(int r, int c) {
			for (int i=0; i<grids.length; i++)
			    if (!grids[i].hasData(r,c)) 
				return false;
			return true;
		    }
		    public float eval(int r, int c) {
			double v0 = grids[0].eval(r,c);
			double v1 = grids[1].eval(r,c);
			return (float) (v0 + ((v1-v0) * (year-y0)) / (y1-y0));
		    }
		};
	    outGrid.setNODATA_value(9);
	    try {
		new GridWriter(outGrid, new File(outdir, outprefix + y).getPath()).writeAll();
	    }
	    catch (IOException e) {
		System.out.println("Error: " + e.toString());
		System.exit(1);
	    }
	}
    }
}

