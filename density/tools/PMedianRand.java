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
import density.tools.*;
import gnu.getopt.*;

public class PMedianRand {
    static HashMap poMap = new HashMap(), randMap = new HashMap(), randMap2 = new HashMap();
    static String[] regions = new String[] { "awt", "can", "nsw", "nz", "sa", "swi" };
    static String samplefile = "base/poenv_all";
    static String baseDir = "/home/phillips/data/nceas";
    public static void main (String[] args) {
	int measure = PMedian.L1;
	int c;
	Getopt g = new Getopt("PMedianRand", args, "d2");
	String usage = "PMedianRand [-d] [-l] evalfiles.csv [baseDir]";

	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 'd': PMedian.dontscale = true; break;
	    case '2': measure = PMedian.L2; break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}

	PMedian.initializeCategorical();
	Utils.readEvalFiles(args[g.getOptind()], PMedian.envVarNamesMap, PMedian.presenceMap, PMedian.absenceMap);
	if (args.length>g.getOptind()+1) baseDir = args[g.getOptind()+1];
	for (int i=0; i<regions.length; i++) {
	    String region = regions[i];
	    String filename = baseDir + "/samples/" + region + "/" + samplefile;
	    System.out.println("Reading " + filename);
	    PMedian.readPOFile2(filename, poMap, region);
	    filename = baseDir + "/background/" + region + "10000";
	    System.out.println("Reading " + filename);
	    PMedian.readPOFile2(filename, randMap2, region);
	    if (randMap2.get("background")==null) {
		Object o = randMap2.keySet().toArray(new Object[0])[0];
		randMap.put(region, randMap2.get(o));
	    }
	    else
		randMap.put(region, randMap2.get("background"));
	}
	System.out.println("Region,Species,pmedian rand to po,range overlap rand to po,pmedian rand to pa presence,range overlap rand to pa presence,pmedian rand to pa,range overlap rand to pa");
	for (Iterator it = poMap.keySet().iterator(); it.hasNext(); ) {
	    String species = (String) it.next();
	    PMedian.setCategorical((String) PMedian.regionMap.get(species));
	    String region = (String) PMedian.regionMap.get(species);
	    double[][] po = (double[][]) poMap.get(species);
	    double[][] rand = (double[][]) randMap.get(region);
	    double[][] pap = (double[][]) PMedian.presenceMap.get(species);
	    double[][] pa = union((double[][]) PMedian.presenceMap.get(species),
				  (double[][]) PMedian.absenceMap.get(species));
	    double[][][] totest = new double[][][] { po, rand, pap, pa };
	    String[] what = new String[] { "po for " + species, "rand for " + region, "pap for " + species, "pa for " + species };
	    for (int i=0; i<totest.length; i++)
		if (totest[i] == null)
		    System.out.println("No data found for " + what[i]);
	    double dRandPo = PMedian.pmedianDistance(rand, po, measure);
	    double overlap = PMedian.rangeOverlap(rand, po);
	    double dRandPap = PMedian.pmedianDistance(rand, pap, measure);
	    double overlapRandPap = PMedian.rangeOverlap(rand, pap);
	    double dRandPa = PMedian.pmedianDistance(rand, pa, measure);
	    double overlapRandPa = PMedian.rangeOverlap(rand, pa);
	    System.out.println(region + "," + species + "," + dRandPo + "," + overlap + "," + dRandPap + "," + overlapRandPap + "," + dRandPa + "," + overlapRandPa);
	}
    }
    
    static double[][] union(double[][] a, double[][] b) {
	if (a==null) a = new double[0][0];
	if (b==null) b = new double[0][0];
	double[][] c = new double[a.length + b.length][];
	for (int i=0; i<a.length; i++)
	    c[i] = a[i];
	for (int i=0; i<b.length; i++)
	    c[i+a.length] = b[i];
	return c;
    }
}
