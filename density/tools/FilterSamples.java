package density.tools;

import java.io.IOException;
import density.*;
import java.util.Random;

public class FilterSamples {

    public static void main(String args[]) {
	String usage = "Usage: density.tools.FilterSamples samplesfile mindist outfile";
	if (args.length<3) { System.out.println(usage); return; }
	String samplefile = args[0], outfile = args[2];
	double mindist = Double.parseDouble(args[1]);
	boolean zeroseed = false;
	density.Utils.generator = new Random(zeroseed ? 0 : System.currentTimeMillis());
	try {
	    SampleSet ss = new SampleSet2(samplefile, null, null);
	    SampleSet ss2 = ss.spatialFilter(mindist);
	    ss2.write(outfile);
	}
	catch (IOException e) { 
	    System.err.println(e.toString());
	}
    }
}
