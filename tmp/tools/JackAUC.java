package density.tools;

import java.io.*;
import density.*;
import java.text.*;
import java.util.*;

public class JackAUC {

    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
    { nf.setMinimumFractionDigits(3); 
    nf.setMaximumFractionDigits(3);
    }

    public static void main(String args[]) {
	try {
	    new JackAUC().go(args);
	} catch (IOException e) { 
	    System.out.println("Error: " + e.toString());
	}
    }

    void go(String[] args) throws IOException {
	String usage = "Usage: JackAUC species occurrencefile directory";
	if (args.length<3) { System.out.println(usage); return; }
	String species = args[0];
	String occfile = args[1];
	String dir = args[2];
	SampleSet2 ss = new SampleSet2(occfile, new Layer[0], null, null);
	ss.read(new String[] { species });
	Sample[] s = ss.getSamples(species);
	double[] fracs = new double[s.length];
	for (int i=0; i<s.length; i++) {
	    int sum=0, cnt=0;
	    Grid g = GridIO.readGrid(new File(dir, species + "_j" + (i+1) + ".mxe").getPath());
	    double x = s[i].getLon(), y = s[i].getLat();
	    double val = g.eval(new double[] {x, y});
	    GridDimension dim = g.getDimension();
	    for (int r=0; r<dim.getnrows(); r++)
		for (int c=0; c<dim.getncols(); c++)
		    if (g.hasData(r,c)) {
			cnt++;
			double v = g.eval(r,c);
			//			if (c == dim.getncols() / 2)
			//			    System.out.println(r + " " + v + " " + val + " " + sum + " " + cnt);
			if (v<val) sum+=2;
			if (v==val) sum++;
		    }
	    double frac = sum / (2.0 * cnt);
	    System.out.println((i+1) + " " + nf.format(val) + " " + nf.format(frac) + (frac<0.5?"***":""));
	    fracs[i] = frac;
	}
	System.out.println("Avg " + nf.format(Stats.mean(fracs)) + "  Stddev " + nf.format(Stats.stddev(fracs)));
    }
}
