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
