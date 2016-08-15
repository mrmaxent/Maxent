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

import gnu.getopt.*;
import java.util.*;

// To do: 
//        experiments?
//        difference between pseud1 and pseud3?

public class PMedian {
    static final int L1=0, L2=1, max=3, geom=4, err=5;
    static HashMap presenceMap = new HashMap(), absenceMap = new HashMap(), poMap = new HashMap();
    static HashMap regionMap = new HashMap(), envVarNamesMap = new HashMap();
    static boolean[] isCategorical;
    static boolean debug=false, dontscale=false;
    static String[] envVarNames;
    static String baseDir = "/home/phillips/data/nceas";

    static HashMap categorical = new HashMap();
    static void initializeCategorical() { 
	categorical.put("awt", new String[0]);
	categorical.put("can", new String[] { "ontveg" });
	categorical.put("nsw", new String[] { "vegsys", "disturb", "soilfert" });
	categorical.put("nz", new String[] { "toxicats", "age" });
	categorical.put("sa", new String[0]);
	categorical.put("swi", new String[] { "calc", "sfroyy" });
    }

    public static void main (String[] args) {
	if (args.length<2) {
	    System.out.println("Usage: PMedian L1|L2|max|err evalFileLocations.csv");
	    System.exit(1);
	}
	String samplefile = "base/poenv_all";
	int c;
	Getopt g = new Getopt("Show", args, "s:b:");
	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 'b': baseDir = g.getOptarg(); break;
	    case 's': samplefile = g.getOptarg(); break;
	    }
	}
	initializeCategorical();
	String measure = args[g.getOptind()];
	int measuretype = 0;
	if (measure.equals("L1")) measuretype = L1;
	else if (measure.equals("L2")) measuretype = L2;
	else if (measure.equals("max")) measuretype = max;
	else if (measure.equals("err")) measuretype = err;
	else {
	    System.out.println("Usage: MakeRMedian L1|L2|max");
	    System.exit(0);
	}
	Utils.readEvalFiles(args[g.getOptind()+1], envVarNamesMap, presenceMap, absenceMap);
	String[] regions = new String[] { "awt", "can", "nsw", "nz", "sa", "swi" };
	for (int i=0; i<regions.length; i++) {
	    String region = regions[i];
	    readPOFile2(baseDir + "/samples/" + region + "/" + samplefile, poMap, region);
	}
	if (measuretype == err)
	    System.out.println("Region, Species, Fraction identical");
	else
	    System.out.println("Region,Species,pa_p distance to po,Range midpoint shift pa_p->po,Range size reduction,Range overlap index");
	for (Iterator it = poMap.keySet().iterator(); it.hasNext(); ) {
	    String species = (String) it.next();
	    String region = (String) regionMap.get(species);
	    setCategorical(region);
	    double[][] pap = (double[][]) presenceMap.get(species);
	    double[][] paa = (double[][]) absenceMap.get(species);
	    double[][] po = (double[][]) poMap.get(species);
	    double dPapPo = pmedianDistance(pap, po, measuretype);
	    //	    double dPapPaa = pmedianDistance(pap, paa, measuretype);
	    //	    double dPaaPap = pmedianDistance(paa, pap, measuretype);
	    //	    System.out.println(region + "," + species + "," + dPapPo + "," + dPapPaa + "," + dPaaPap + "," + fracExact);
	    String debugsp = "";
	    //	    if (species.equals(debugsp)) Utils.dump(pap); 
	    //	    if (species.equals(debugsp)) System.out.println(Csv.toCsvFormat(envVarNames));
	    //	    if (species.equals(debugsp)) Utils.dump(po);
	    //	    debug = (species.equals(debugsp));
	    double shift = rangeMidPointShift(pap, po, measuretype);
	    //	    debug = false;
	    double reduction = rangeSizeReduction(pap, po);
	    double overlap = rangeOverlap(pap, po);
	    if (measuretype == err)
		System.out.println(region + "," + species + "," + fracExact);
	    else
		System.out.println(region + "," + species + "," + dPapPo + "," + shift + "," + reduction + "," + overlap);
	    if (debug) System.exit(0);
	}
    }

    static void setCategorical(String region) {
	envVarNames = (String[]) envVarNamesMap.get(region);
	String[] categoricalNames = (String[]) categorical.get(region);
	isCategorical = new boolean[envVarNames.length];
	for (int i=0; i<isCategorical.length; i++)
	    for (int j=0; j<categoricalNames.length; j++)
		if (envVarNames[i].equals(categoricalNames[j]))
		    isCategorical[i] = true;
    }

    static double measure(double[] a, double[] b, double[] scale, int measuretype) {
	double[] d = new double[a.length];
	for (int i=0; i<a.length; i++) {
	    if (isCategorical[i])
		d[i] = (a[i] == b[i]) ? 0 : 1;
	    else if (scale[i]==0)
		d[i] = (a[i]==b[i]) ? 0 : 1;
	    else
		d[i] = (a[i] - b[i]) / scale[i];
	}
	return measure(d, measuretype);
    }

    static double measure(double[] a, int measuretype) {
	double x = 0.0;
	switch (measuretype) {
	case L1:
	    for (int i=0; i<a.length; i++)
		x += Math.abs(a[i]);
	    return x/a.length;
	case L2: 
	    for (int i=0; i<a.length; i++)
		x += a[i] * a[i];
	    return Math.sqrt(x)/a.length;
	case err:
	case max:
	    for (int i=0; i<a.length; i++) {
		if (Math.abs(a[i]) > x)
		    x = Math.abs(a[i]);
	    }
	    return x;
	case geom:
	    x = a[0];
	    for (int i=1; i<a.length; i++)
		x *= a[i];
	    return Math.pow(x, 1.0 / a.length);
	}
	return 0.0;
    }

    static double rangeMidPointShift(double[][] a, double[][] b, int measuretype) {
	double[] width = width(b);
	double[] midb = new double[width.length];
	for (int i=0; i<midb.length; i++)
	    midb[i] = (mins[i] + maxs[i])/2.0;
	if (debug) System.out.println(mins[midb.length-1] + " " + maxs[midb.length-1]);
	width = width(a);
	double[] mida = new double[width.length];
	double[] d = new double[mida.length];
	int cnt=0;
	for (int i=0; i<mida.length; i++) {
	    if (isCategorical[i]) continue;
	    mida[i] = (mins[i] + maxs[i])/2.0;
	    double shift = Math.abs(mida[i] - midb[i]);
	    d[cnt++] = (width[i] == 0) ? (shift==0?0:1) : shift/width[i];
	}
	double[] dd = new double[cnt];
	for (int i=0; i<cnt; i++)
	    dd[i] = d[i];
	return measure(dd, measuretype);
    }

    static double rangeSizeReduction(double[][] a, double[][] b) {
	double[] widtha = width(a), widthb = width(b);
	double[] d = new double[widtha.length];
	for (int i=0; i<widtha.length; i++) {
	    double delta = (widtha[i]==0)?1:widthb[i]/ widtha[i];
	    if (delta>1) delta=1;
	    d[i] = delta;
	}
	return 1 - measure(d, geom);
    }

    static double rangeOverlap(double[][] a, double[][] b) {
	double[] widtha = width(a);
	if (dontscale) Arrays.fill(widtha, 1.0);
	double[] amins = (double[]) mins.clone(), amaxs = (double[]) maxs.clone();
	double[] widthb = width(b);
	int dim = widtha.length;
	double[] d = new double[dim];
	for (int i=0; i<widtha.length; i++) {
	    if (isCategorical[i]) {
		HashSet sa = new HashSet(), sb = new HashSet();
		for (int j=0; j<a.length; j++)
		    sa.add(new Double(a[j][i]));
		for (int j=0; j<b.length; j++)
		    sb.add(new Double(b[j][i]));
		int cnt=0;
		for (Iterator it = sa.iterator(); it.hasNext(); )
		    if (sb.contains(it.next())) 
			cnt++;
		d[i] = cnt/(double) sa.size();
	    }
	    else {
		double o = Math.min(amaxs[i], maxs[i]) - Math.max(amins[i], mins[i]);
		d[i] = (o<0) ? 0 : (widtha[i]==0 ? 1 : o / widtha[i]);
	    }
	}
	return measure(d, L1);
    }

    static double[] mins, maxs;
    static double[] width(double[][] a) {
	double[] result = new double[a[0].length];
	HashSet[] allvals = new HashSet[a[0].length];
	for (int i=0; i<a[0].length; i++)
	    allvals[i] = new HashSet();
	mins = new double[a[0].length];
	maxs = new double[a[0].length];

	for (int i=0; i<a.length; i++)
	    for (int j=0; j<a[i].length; j++) {
		if (isCategorical[j])
		    allvals[j].add(new Double(a[i][j]));
		else {
		    if (i==0 || a[i][j] < mins[j]) mins[j] = a[i][j];
		    if (i==0 || a[i][j] > maxs[j]) maxs[j] = a[i][j];
		}
	    }
	for (int j=0; j<mins.length; j++) {
	    result[j] = isCategorical[j] ? allvals[j].size() : maxs[j] - mins[j];
	}
	return result;
    }

    static double fracExact;
    static double pmedianDistance(double[][] a, double[][] b, int measuretype) {
	double[] width = width(a);
	if (dontscale) Arrays.fill(width, 1.0);
	double[] dist = new double[a.length];
	int numExact = 0;
	for (int i=0; i<a.length; i++) {
	    double mind=0.0;
	    int best=0;
	    for (int j=0; j<b.length; j++) {
		double x = measure(a[i], b[j], width, measuretype);
		if (j==0 || x < mind) {
		    mind = x;
		    best = j;
		}
	    }
	    if (mind==0.0) numExact++;
	    dist[i] = mind;
	}
	fracExact = numExact / (double) a.length;
	return median(dist);
	// sum / a.length;
    }

    static double median(double[] d) {
	Arrays.sort(d);
	return d[d.length / 2];
    }

    

    static String[] POFileVars;
    static void readPOFile(String file, HashMap poMap, String region) {
	CsvOnePass csv = new CsvOnePass(file);
	int dim = csv.headers().length - 7;
	HashMap map = new HashMap();
	POFileVars = new String[dim];
	for (int i=0; i<dim; i++)
	    POFileVars[i] = csv.headers()[i+7];
	while (true) {
	    String[] fields = csv.getRecord();
	    if (fields==null) break;
	    double[] vals = new double[dim];
	    for (int i=0; i<dim; i++)
		vals[i] = Double.parseDouble(fields[i+7]);
	    String species = fields[1].toLowerCase();
	    if (!map.containsKey(species))
		map.put(species, new ArrayList());
	    ArrayList a = (ArrayList) map.get(species);
	    a.add(vals);
	}
	for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
	    String species = (String) it.next();
	    double[][] a = (double[][]) ((ArrayList) map.get(species)).toArray(new double[0][]);
	    poMap.put(species, a);
	    regionMap.put(species, region);
	}
    }

    static void readPOFile2(String file, HashMap poMap, String region) {
	CsvOnePass csv = new CsvOnePass(file);
	int dim = csv.headers().length - 7;
	HashMap map = new HashMap();
	int[] index = new int[dim];
	if (!envVarNamesMap.containsKey(region)) {
	    String[] vars = new String[dim];
	    for (int i=0; i<dim; i++) {
		vars[i] = csv.headers()[i+7];
		index[i] = i+7;
	    }
	    envVarNamesMap.put(region, vars);
	}
	else {
	    String[] vars = (String[]) envVarNamesMap.get(region), head = csv.headers();
	    for (int i=0; i<dim; i++) {
		index[i] = -1;
		//		System.out.println(vars.length + " " + head.length + " " + dim);
		for (int j=0; j<head.length; j++) {
		    if (vars[i].toLowerCase().equals(head[j].toLowerCase()))
			index[i] = j;
		}
		if (index[i]==-1) {
		    System.out.println("Error in readPOFile");
		    System.exit(1);
		}
	    }
	}		
	while (true) {
	    String[] fields = csv.getRecord();
	    if (fields==null) break;
	    double[] vals = new double[dim];
	    for (int i=0; i<dim; i++)
		vals[i] = Double.parseDouble(fields[index[i]]);
	    String species = fields[1].toLowerCase();
	    if (!map.containsKey(species))
		map.put(species, new ArrayList());
	    ArrayList a = (ArrayList) map.get(species);
	    a.add(vals);
	}
	for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
	    String species = (String) it.next();
	    double[][] a = (double[][]) ((ArrayList) map.get(species)).toArray(new double[0][]);
	    poMap.put(species, a);
	    regionMap.put(species, region);
	}
    }

}
