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

public class Count {

    public static void main(String args[]) {
	boolean categories = false;
	double threshold=-1;
	int cc;
	Getopt g = new Getopt("Count", args, "t:c");
	String usage = "Usage: Count [-t threshold] [-c] file1 file2 ...";
	while ((cc=g.getopt()) != -1) {
	    switch(cc) {
	    case 't': threshold = Double.parseDouble(g.getOptarg()); break;
	    case 'c': categories = true; break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	if (args.length==0) System.out.println(usage);
	try {
	    HashMap<Float,Integer> map = new HashMap();
	    for (int i=g.getOptind(); i<args.length; i++) {
		Grid grid = new LazyGrid(args[i]);
		GridDimension dim = grid.getDimension();
		int cnt = 0, nr = dim.getnrows(), nc = dim.getncols(), tcnt=0;
		double sum = 0.0;
		for (int r=0; r<nr; r++)
		    for (int c=0; c<nc; c++) {
			if (!grid.hasData(r,c)) continue;
			cnt++;
			float v = grid.eval(r,c);
			sum += v;
			if ((threshold==-1) || (v >= threshold)) 
			    tcnt++;
			if (categories) {
			    if (!map.containsKey(v))
				map.put(v, 0);
			    map.put(v, map.get(v) + 1);
			}
		    }
		System.out.println(args[i] + " cnt " + cnt + " out of " + (nr*nc) + " fraction above threshold " + (tcnt/(double) cnt) + " sum " + sum);
		if (categories) 
		    for (Float v: map.keySet()) 
			System.out.println("Category " + v + ": " + map.get(v));
	    }
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }
}
