package density.tools;

import java.io.*;
import density.*;

public class ToBoolean {

    public static void main(String args[]) {
	String usage = "Usage: ToBoolean infile outfile";
	String infile = args[0], outfile = args[1];
	try {
	    final Grid g=GridIO.readGrid(infile);
	    Grid out = new Grid(g.getDimension(), g.getName()) {
		    public float eval(int r, int c) {
			return 1;
		    }
		    public boolean hasData(int r, int c) {
			return g.hasData(r, c);
		    }
		};
	    out.setNODATA_value(0);
	    new GridWriter(out, outfile).writeAll();
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }
}
