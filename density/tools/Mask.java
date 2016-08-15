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
