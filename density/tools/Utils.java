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

public class Utils {

    static void readEvalFiles(String file, HashMap envVarNamesMap, HashMap presenceMap, HashMap absenceMap) {
	String filename = "";
	try {
	    density.Csv csv = new density.Csv(file);
	    while (true) {
		String[] f = csv.getRecord();
		if (f==null) break;
		for (int j=0; j<f.length; j++) f[j] = f[j].trim();
		filename = f[0];
		readEvalFile(f[0], Integer.parseInt(f[1]), Integer.parseInt(f[2]),
			     Integer.parseInt(f[3]), Integer.parseInt(f[4]), Integer.parseInt(f[5]), f[6], f[7],
			     envVarNamesMap, presenceMap, absenceMap);
	    }
	} catch (Exception e) {
	    System.out.println("Error reading " + filename + ": " + e.toString());
	    e.printStackTrace();
	}
    }

    static HashMap sitevals = new HashMap();
    static void readEvalFile(String file, int firstvar, int lastvar, int firstSpecies, int lastSpecies, int siteidloc, String region, String experiment, HashMap envVarNamesMap, HashMap presenceMap, HashMap absenceMap) throws IOException {
	if (experiment.equals("error")) {
	    readEvalFilePO(file, firstvar, lastvar, siteidloc, region, experiment, envVarNamesMap, presenceMap);
	    return;
	}
	//	System.out.println("Reading eval file " + file);
	density.Csv csv = new density.Csv(file);
	if (lastSpecies==-1 && firstSpecies != -1) lastSpecies = csv.headers().length;
	if (lastvar==-1 && firstvar!=-1) lastvar = csv.headers().length;
	int nspecies = lastSpecies - firstSpecies;
	int dim = lastvar-firstvar;
	//	System.out.println(file + ": dim = " + dim);
	String[] speciesNames = new String[nspecies];
	String[] envVarNames = new String[dim];
	ArrayList[] speciesvals = new ArrayList[nspecies], speciesabsvals = new ArrayList[nspecies];
	ArrayList[] speciesPresenceSites = new ArrayList[nspecies], speciesAbsenceSites = new ArrayList[nspecies];
	//	sitevals = new HashMap();
	for (int i=0; i<nspecies; i++) {
	    speciesNames[i] = csv.headers()[i+firstSpecies].toLowerCase();
	    //	    System.out.println(speciesNames[i]);
	    speciesvals[i] = new ArrayList();
	    speciesabsvals[i] = new ArrayList();
	    speciesPresenceSites[i] = new ArrayList();
	    speciesAbsenceSites[i] = new ArrayList();
	}
	for (int i=0; i<dim; i++)
	    envVarNames[i] = csv.headers()[i+firstvar].toLowerCase();
	if (envVarNamesMap!=null)
	    envVarNamesMap.put(region, envVarNames);
	while (true) {
	    String[] fields = csv.getRecord();
	    if (fields==null || fields.length<2) break;
	    String siteid = fields[siteidloc];
	    double[] vals = new double[dim];
	    for (int i=0; i<dim; i++)
		vals[i] = Double.parseDouble(fields[firstvar+i]);
	    sitevals.put(siteid, vals);
	    for (int i=0; i<nspecies; i++) {
		if (fields[i+firstSpecies].equals("1")) {
		    speciesvals[i].add(vals);
		    speciesPresenceSites[i].add(siteid.trim());
		}
		else if (fields[i+firstSpecies].equals("0")) {
		    speciesabsvals[i].add(vals);
		    speciesAbsenceSites[i].add(siteid.trim());
		}
		else
		    System.out.println("Unexpected " + fields[i+firstSpecies] + " in " + file);
	    }
	}
	String sitename = "_sites";
	for (int i=0; i<nspecies; i++) {
	    presenceMap.put(speciesNames[i],
			    speciesvals[i].toArray(new double[0][]));
	    presenceMap.put(speciesNames[i] + sitename,
			    speciesPresenceSites[i].toArray(new String[0]));
	    absenceMap.put(speciesNames[i],
			   speciesabsvals[i].toArray(new double[0][]));
	    absenceMap.put(speciesNames[i] + sitename,
			   speciesAbsenceSites[i].toArray(new String[0]));
	}
    }

    static void readEvalFilePO(String file, int firstvar, int lastvar, int siteidloc, String region, String experiment, HashMap envVarNamesMap, HashMap presenceMap) throws IOException {
	density.Csv csv = new density.Csv(file);
	if (lastvar==-1 && firstvar!=-1) lastvar = csv.headers().length;
	int dim = lastvar-firstvar;
	String[] envVarNames = new String[dim];
	HashMap speciesPresenceSites = new HashMap();
	HashMap speciesvals = new HashMap();
	for (int i=0; i<dim; i++)
	    envVarNames[i] = csv.headers()[i+firstvar].toLowerCase();
	if (envVarNamesMap!=null)
	    envVarNamesMap.put(region, envVarNames);
	while (true) {
	    String[] fields = csv.getRecord();
	    if (fields==null || fields.length<2) break;
	    String siteid = fields[siteidloc];
	    String species = fields[1].toLowerCase();
	    double[] vals = new double[dim];
	    for (int i=0; i<dim; i++)
		vals[i] = Double.parseDouble(fields[firstvar+i]);
	    sitevals.put(siteid, vals);
	    if (!speciesPresenceSites.containsKey(species))
		speciesPresenceSites.put(species, new ArrayList());
	    if (!speciesvals.containsKey(species))
		speciesvals.put(species, new ArrayList());
	    ((ArrayList) speciesPresenceSites.get(species)).add(siteid);
	    ((ArrayList) speciesvals.get(species)).add(vals);
	}
	String[] species = (String[]) speciesvals.keySet().toArray(new String[0]);
	for (int i=0; i<species.length; i++) {
	    presenceMap.put(species[i]+"_" + experiment, 
			    ((ArrayList) speciesvals.get(species[i])).toArray(new double[0][]));
	    presenceMap.put(species[i]+"_" + experiment + "sites", 
			    ((ArrayList) speciesPresenceSites.get(species[i])).toArray(new String[0]));
	}
    }

    static void dump(double[][] a) {
	for (int i=0; i<a.length; i++)
	    dump(a[i]);
	System.out.println();
    }

    static void dump(double[] a) {
	for (int i=0; i<a.length; i++)
	    System.out.print((i>0?",":"") + a[i]);
	System.out.println();
    }

    static void checkExists(String filename) {
	if (!new File(filename).exists()) 
	    error("missing file/directory " + filename);
    }
    static void error(String s) {
	System.err.println("Error: " + s);
	System.exit(1);
    }
}
