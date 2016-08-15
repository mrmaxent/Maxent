package density.tools;

import java.io.*;
import density.*;

public class Mask {

    public static void main(String args[]) {
	String usage = "Usage: Mask infile maskfile outfile";
	if (args.length < 2) {
	    System.out.println(usage);
	    System.exit(0);
	}
	String infile = args[0], maskfile = args[1], outfile = args[2];
	try {
	    final Grid g = new LazyGrid(infile), m = new LazyGrid(maskfile);
	    Grid out = maskGrid(g,m);
	    new GridWriter(out, outfile).writeAll();
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }

    public static Grid maskGrid(final Grid g, final Grid m) {
	return new Grid(g.getDimension(), "Masked") {
		public float eval(int r, int c) {
		    return g.eval(r,c);
		}
		public boolean hasData(int r, int c) {
		    return (g.hasData(r,c) && m.hasData(r,c));
		}
	    };
    }
	
}
