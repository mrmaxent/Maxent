package density.tools;

import java.io.*;
import density.*;

public class Union {

    public static void main(String args[]) {
	String usage = "Usage: Union outfile infile1 infile2 ...";
	if (args.length < 2) {
	    System.out.println(usage);
	    System.exit(0);
	}
	String outfile = args[0];
	final int nf = args.length-1;
	String[] infiles = new String[nf];
	for (int i=0; i<nf; i++)
	    infiles[i] = args[i+1];
	try {
	    final Grid[] g = new Grid[nf];
	    for (int i=0; i<nf; i++)
		g[i] = GridIO.readGrid(infiles[i]);
	    Grid out = new Grid(g[0].getDimension(), "Union") {
		    public float eval(int r, int c) {
			return 1;
		    }
		    public boolean hasData(int r, int c) {
			for (int i=0; i<nf; i++)
			    if (g[i].hasData(r,c)) return true;
			return false;
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
