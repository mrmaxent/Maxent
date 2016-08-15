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

public class Eval {
    static boolean debug = false, doMaxCorrelation = false;
    static HashMap presenceMap = new HashMap(), absenceMap = new HashMap();
    static String species, fileName;

    public static void main (String[] args) {
	if (args.length<2) {
	    System.out.println("Usage: Eval resultsdir evalFileLocations.csv");
	    System.exit(1);
	}
	String resultsDir = args[0], evalLocations = args[1];
	final String expt = (args.length>2) ? args[2] : null;
	Utils.readEvalFiles(evalLocations, null, presenceMap, absenceMap);
	File results = new File(resultsDir);
	String[] csvFiles = results.list(new FilenameFilter() { 
	    public boolean accept(File dir, String name) {
		if (expt!=null && name.indexOf(expt) == -1) return false;
		return name.endsWith(".csv");
	    }});
	Arrays.sort(csvFiles);
	System.out.println("region, species, expr, AUC, Correlation, Square error, Prevalence, Max Kappa, Threshold for Max Kappa,Logloss,PresenceLoss,AbsenceLoss,Misclassification");
	for (int i=0; i<csvFiles.length; i++) {
	    String[] foo = csvFiles[i].substring(0, csvFiles[i].length()-4).split("_");
	    String region=foo[0];
	    species=foo[foo.length-1].toLowerCase();
	    String expr1 = foo[1];
	    String expr = (foo.length == 3) ? foo[1] : (foo[1] + "_" + foo[2]);
	    fileName = new File(results, csvFiles[i]).getPath();
	    //	    debug = true; // species.equals("otsp4");
	    //	    if (!debug) continue;
	    //	    if (!expr.equals("base")) continue;
	    try {
		HashMap map = readPrediction(species, fileName, expr1, region);
		double auc = computeAUC(map);
		System.out.println(region + "," + species + "," + expr + "," + auc + "," + computeCorrelation(map) + "," + computeSquareError(map) + "," + (presenceSites.length / (double)(presenceSites.length + absenceSites.length)) + "," + Stats.maxKappa + "," + Stats.maxKappaThresh + "," + computeLogloss(map,2) + "," + computeLogloss(map,0) + "," + computeLogloss(map,1) + "," + computeMisClassification(map));
	    }
	    catch (Exception e) { 
		System.out.println("Error: " + region + " " + species + " " + " " + expr + " " + e.toString()); 
		e.printStackTrace();
	    }
	}
    }

    static boolean warp = false;
    static String warp(String s) {
	return ""+warp2(Double.parseDouble(s));
    }
    static double warp2(double x) {
	return 10 * Math.sqrt(x);
    }

    static String[] presenceSites, absenceSites;
    static HashMap readPrediction(String species, String predictionFile, String experiment, String region) {
	CsvOnePass csv = new CsvOnePass(predictionFile);
	HashMap map = new HashMap();
	while (true) {
	    String[] fields = csv.getRecord();
	    if (fields==null) break;
	    map.put(fields[1], warp?warp(fields[2]):fields[2]);
	}
	String sitename = experiment.equals("error") ?
	    "_" + experiment + "sites" : "_sites";
	presenceSites = (String[]) presenceMap.get(species + sitename);
	absenceSites = (String[]) absenceMap.get(species + "_sites");
	return map;
    }

    static double getDouble(HashMap map, String key) {
	if (map.get(key)==null) 
	    key = key.toLowerCase();
	return Double.parseDouble((String) map.get(key));
    }

    static double max(double x, double y) { return (x<y) ? y : x; }
    static double computeLogloss(HashMap map, int which) {
	double error = 0.0;
	int cnt = 0;
	if (which==0||which==2)
	    for (int i=0; i<presenceSites.length; i++) {
		double prob = max(getDouble(map, presenceSites[i]), Math.exp(-20));
		error += Math.log(prob);
		cnt++;
	    }
	if (which==1||which==2)
	    for (int i=0; i<absenceSites.length; i++) {
		double prob = max(1-getDouble(map, absenceSites[i]), Math.exp(-20));
		error += Math.log(prob);
		cnt++;
	}
	return -error / cnt;
    }

    static double computeMisClassification(HashMap map) {
	double error = 0.0;
	for (int i=0; i<presenceSites.length; i++)
	    if (getDouble(map, presenceSites[i]) < 0.5) error++;
	for (int i=0; i<absenceSites.length; i++)
	    if (getDouble(map, absenceSites[i]) >= 0.5) error++;
	return error / (presenceSites.length + absenceSites.length);
    }

    static double computeSquareError(HashMap map) {
	double error = 0.0;
	for (int i=0; i<presenceSites.length; i++)
	    error += (1-getDouble(map, presenceSites[i])) * (1-getDouble(map, presenceSites[i]));
	for (int i=0; i<absenceSites.length; i++)
	    error += getDouble(map, absenceSites[i]) * getDouble(map, absenceSites[i]);
	return error / (presenceSites.length + absenceSites.length);
    }

    static double computeAUC(HashMap map) {
	double[] presence = new double[presenceSites.length];
	double[] absence = new double[absenceSites.length];
	for (int i=0; i<presence.length; i++) {
	    try {
		presence[i] = getDouble(map, presenceSites[i]);
	    } catch (NullPointerException e) {
		System.out.println("Error reading prediction: site = " + presenceSites[i] + ", file is " + fileName);
	    }
	}
	for (int i=0; i<absence.length; i++)
	    absence[i] = getDouble(map, absenceSites[i]);
	return Stats.auc(presence, absence);
    }

    static double computeCorrelation(HashMap map) throws IOException {
	double[] pred = new double[presenceSites.length + absenceSites.length];
	double[] truth = new double[pred.length];
	PrintWriter out=null;
	if (debug) {
	    out = new PrintWriter(new FileOutputStream(species + ".csv"));
	    out.println("site,truth,prediction");
	}
	for (int i=0; i<presenceSites.length; i++) {
	    pred[i] = getDouble(map, presenceSites[i]);
	    if (debug) out.println(presenceSites[i] + "," + 1 + "," + pred[i]);
	    truth[i] = 1;
	}
	for (int i=0; i<absenceSites.length; i++) {
	    pred[i+presenceSites.length] = getDouble(map, absenceSites[i]);
	    if (debug) out.println(absenceSites[i] + "," + 0 + "," + pred[i+presenceSites.length]);
	    truth[i+presenceSites.length] = 0;
	}
	if (debug) out.close();
	return (doMaxCorrelation) ? Stats.maxCorrelation(pred, truth) 
	    : Stats.correlation(pred, truth);
    }
}
