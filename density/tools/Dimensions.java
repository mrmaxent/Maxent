package density.tools;

import java.io.*;
import density.*;
import gnu.getopt.*;
import java.util.*;

public class Dimensions {

    public static void main(String args[]) {
	boolean categories = false;
	double threshold=-1;
	String usage = "Usage: Dimensions file1 file2 ...";
	if (args.length==0) System.out.println(usage);
	for (String f: args) {
	    try {
		System.out.println(f);
		Grid grid = new LazyGrid(f);
		GridDimension dim = grid.getDimension();
		System.out.println("ncols         " + dim.getncols());
		System.out.println("nrows         " + dim.getnrows());
		System.out.println("xllcorner     " + dim.getxllcorner());
		System.out.println("yllcorner     " + dim.getyllcorner());
		System.out.println("cellsize      " + dim.getcellsize());
		System.out.println();
	    }
	    catch (IOException e) { 
		System.out.println("Error: " + e);
		System.exit(0);
	    }
	}
    }
}
