package density.tools;
import gnu.getopt.*;
import java.io.*;
import java.util.*;
import density.*;

public class Coarsen {

    public static void main(String args[]) {
	try {
	    new Coarsen().go(args);
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e.toString());
	    System.exit(0);
	}
    }

    void go(String[] args) throws IOException {
	if (args.length<3) {
	    System.out.println("Usage: Coarsen radius outdir file1 file2...");
	    System.exit(0);
	}
	final int radius = Integer.parseInt(args[0]);
	final int reduce = 2*radius+1;
	String outdir = args[1];
	for (int i=2; i<args.length; i++) {
	    String infile = args[i];
	    String name = new File(infile).getName();
	    System.out.println(name);
	    final Grid old = GridIO.readGrid(infile);
	    GridDimension dim = old.getDimension();
	    int rr = (int) Math.ceil(dim.getnrows()/(double)reduce);
	    int cc = (int) Math.ceil(dim.getncols()/(double)reduce);
	    GridDimension newdim = new GridDimension(dim.getxllcorner(), dim.getyllcorner(), dim.getcellsize()*reduce, rr, cc);
	    Grid newg = new Grid(newdim, "Coarsened") {
		    public boolean hasData(int r, int c) {
			for (int rr=r*reduce; rr<(r+1)*reduce; rr++)
			    for (int cc=c*reduce; cc<(c+1)*reduce; cc++)
				if (old.hasData(rr,cc))
				    return true;
			return false;
		    }
		    public float eval(int r, int c) {
			double sum=0;
			for (int rr=r*reduce; rr<(r+1)*reduce; rr++)
			    for (int cc=c*reduce; cc<(c+1)*reduce; cc++)
				if (old.hasData(rr,cc))
				    sum += old.eval(rr,cc);
			return (float) sum;
		    }
		};
	    new GridWriter(newg, new File(outdir, name)).writeAll();
	}
    }
}
