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
