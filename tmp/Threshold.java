package density;

import java.util.*;
import java.io.*;

public class Threshold {

    public static void main(String args[]) {
	String usage = "Usage: density.Threshold infile threshold outfile";
	if (args.length<2) { System.out.println(usage); return; }
	try {
	    new Threshold().applyThreshold(args[0], Double.parseDouble(args[1]), args[2]);
	} catch (IOException e) { 
	    System.out.println(e.toString()); 
	}
    }

    long dcnt, pcnt;
    double applyThreshold(String infile, final double threshold, String outfile) throws IOException {
	final LazyGrid g = new LazyGrid(infile);
	Grid out = new GridByte(g.getDimension(), g.getName()) {
		{ dcnt=0; pcnt=0; NODATA_value = 2; }
		public byte evalByte(int r, int c) {
		    dcnt++;
		    if (g.eval(r,c) < threshold) return (byte) 0;
		    pcnt++;
		    return (byte) 1;
		}
		public boolean hasData(int r, int c) {
		    return g.hasData(r,c);
		}
	    };
	new GridWriter(out, outfile).writeAll();
	return (dcnt==0) ? 0 : pcnt/(double) dcnt;
    }

    double applyThreshold(String infile, double threshold) throws IOException {
	String pre = infile.substring(0, infile.length()-4);
	String suf = infile.substring(infile.length()-4, infile.length());
	String outfile = pre + "_thresholded" + suf;
	return applyThreshold(infile, threshold, outfile);
    }

}
