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
