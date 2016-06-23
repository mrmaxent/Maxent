package density.tools;

import java.io.*;
import java.util.*;
import density.*;

public class Addconstant {

    // add a constant to each non-nodata cell
    public static void main(String args[]) {
	String usage = "Usage: Addconstant offset outdir infile1 infile2 ...";
	double offset = Double.parseDouble(args[0]);
	String outdir = args[1];
	try {
	   for (int i=2; i<args.length; i++)
	      new Addconstant().add(offset, args[i], outdir);
        }
        catch (IOException e) { 
	   System.out.println("Error: " + e);
	   System.exit(0);
	}
    }

    void add(final double offset, String infile, String outdir) throws IOException {
      final Grid g = new LazyGrid(infile);
      Grid out = new Grid(g.getDimension(), "tmp") {
         public boolean hasData(int r, int c) {
            return g.hasData(r, c);
         }
         public float eval(int r, int c) {
	     return (float) (g.eval(r,c) + offset);
         }
      };
      File outfile = new File(outdir, new File(infile).getName());
      new GridWriter(out, outfile).writeAll();
   }
}

