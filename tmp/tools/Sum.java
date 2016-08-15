package density.tools;

import java.io.*;
import density.*;

public class Sum {

    public static void main(String args[]) {
	String usage = "Usage: Sum infile1 infile2 ...";
	if (args.length==0) System.out.println(usage);
	if (args.length < 1) {
	    System.out.println(usage);
	    System.exit(0);
	}
	for (int i=0; i<args.length; i++) {
	    String infile = args[i];
	    double sum=0.0;
	    try {
		Grid g = GridIO.readGrid(infile);
		GridDimension dim = g.getDimension();
		for (int r=0; r<dim.getnrows(); r++)
		    for (int c=0; c<dim.getncols(); c++)
			if (g.hasData(r,c))
			    sum += g.eval(r,c);
	    } catch (IOException e) {}
	    System.out.println(infile + " " + sum);
	}
    }
}
