/*
Copyright (c) 2016 Steven Phillips, Miro Dudik and Rob Schapire

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions: 

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software. 

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
*/

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

