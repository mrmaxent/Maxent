package density.tools;

import java.io.*;
import density.*;

public class Threshold {

    public static void main(String args[]) {
	String usage = "Usage: Threshold infile threshold outfile";
	String infile = args[0], outfile = args[2];
	final double threshold = Double.parseDouble(args[1]);
	try {
	    final Grid g=GridIO.readGrid(infile);
	    Grid out = new Grid(g.getDimension(), g.getName()) {
		    public float eval(int r, int c) {
			return (g.eval(r,c) >= threshold) ? 1 : 0;
		    }
		    public boolean hasData(int r, int c) {
			return g.hasData(r, c);
		    }
		};
	    //	    out.setNODATA_value(0);
	    new GridWriter(out, outfile).writeAll();
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }
}
