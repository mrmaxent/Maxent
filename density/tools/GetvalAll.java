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

import java.util.*;
import java.io.*;
import density.*;

public class GetvalAll {

    public static void main(String args[]) {
	String usage = "Usage: GetvalAll directory";
	if (args.length<1) { System.out.println(usage); return; }
	String dir = args[0];
	String[] f = new File(dir).list();
	int vars = f.length;
	String[] fileNames = new String[vars];
	for (int i=0; i<vars; i++)
	    fileNames[i] = new File(dir, f[i]).getPath();
	LazyGrid[] g = new LazyGrid[vars];
	try {
	    for (int v=0; v<vars; v++)
		g[v] = new LazyGrid(fileNames[v]);
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e.toString());
	    System.exit(0);
	}
	GridDimension d = g[0].getDimension();
	int rows = d.getnrows(), cols = d.getncols();
	System.out.print("X\tY");
	for (int v=0; v<vars; v++)
	    System.out.print("\t" + g[v].getName());
	System.out.println();
	for (int r=0; r<rows; r++)
	    for (int c=0; c<cols; c++) {
		boolean doit = true;
		for (int v=0; v<vars; v++)
		    if (!g[v].hasData(r,c))
			doit = false;
		if (!doit) continue;
		System.out.print(d.toX(c) + "\t" + d.toY(r));
		for (int v=0; v<vars; v++)
		    System.out.print("\t" + (g[v].hasData(r,c) ? g[v].eval(r,c) : -9999));
		System.out.println();
	    }
    }
}
