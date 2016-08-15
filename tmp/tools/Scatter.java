package density.tools;

import java.io.*;
import density.*;
import java.util.*;

public class Scatter {

    public static void main(String args[]) {
	String usage = "Usage: Scatter infile1 infile2 ...";
	if (args.length < 2) {
	    System.out.println(usage);
	    System.exit(0);
	}
	density.Utils.generator = new Random(0);
	try {
	    Grid[] g = new Grid[args.length];
	    for (int i=0; i<g.length; i++)
		g[i] = GridIO.readGrid(args[i]);
	    int nr = g[0].getDimension().getnrows();
	    int nc = g[0].getDimension().getncols();
	    for (int i=0; i<10000; i++) {
		int r = density.Utils.generator.nextInt(nr);
		int c = density.Utils.generator.nextInt(nc);
		boolean hasdata = true;
		for (int j=0; j<g.length; j++)
		    if (!g[j].hasData(r,c)) hasdata = false;
		if (hasdata) {
		    for (int j=0; j<g.length; j++)
			System.out.print(" " + g[j].eval(r,c));
		    System.out.println();
		}
	    }
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }
}
