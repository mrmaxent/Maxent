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

package density;

import java.io.*;
import java.util.*;

public class GridSetFromFile extends GridSet {
    int nvars;
    double[][] allvals;
    String[] varnames;
    String fileName;

    public GridSetFromFile(String filename, Layer[] layers) throws IOException {
	this.fileName = filename;
	this.layers = layers;
	Utils.reportDoing("Reading file " + (new File(fileName)).getName());
	Utils.reportMemory("readGrid");
	final Csv csv = new Csv(fileName);
	if (layers!=null)
	    for (int i=0; i<layers.length; i++)
		if (!csv.hasField(layers[i].getName()) || csv.fieldIndex(layers[i].getName()) < SampleSet.firstEnvVar)
		    throw new IOException("File " + fileName + " missing variable " + layers[i].getName());
	nvars = (layers==null) ? csv.headers().length - SampleSet.firstEnvVar : layers.length;
	final int[] varindex = new int[nvars];
	varnames = new String[nvars];
	for (int i=0; i<nvars; i++) {
	    if (layers==null) {
		varnames[i] = csv.headers()[i+SampleSet.firstEnvVar];
		varindex[i] = i+SampleSet.firstEnvVar;
	    }
	    else {
		varnames[i] = layers[i].getName();
		varindex[i] = csv.fieldIndex(varnames[i]);
	    }
	}
	
	if (nvars<1)
	    throw new IOException("File " + fileName + " missing environmental variable columns");
	final ArrayList valsa = new ArrayList(), coordsa = new ArrayList();
	try {
	    csv.apply(csv.new Applier() {
                    boolean warnedNodata = false;
		    public void process() {
			double[] vals = new double[nvars];
			double[] coords = new double[] { 
			    Double.parseDouble(csv.get(SampleSet.xIndex)),
			    Double.parseDouble(csv.get(SampleSet.yIndex)) };
                        boolean skip = false;
			for (int i=0; i<vals.length; i++) {
			    vals[i] = Double.parseDouble(csv.get(varindex[i]));
			    if (vals[i] == SampleSet.NODATA_value) {
//				Utils.warn2("File " + (new File(fileName).getName()) + " has value " + SampleSet.NODATA_value + ", treating as an ordinary data value", "nodataInBackground");
	                        if (!warnedNodata)
				   Utils.warn2("File " + (new File(fileName).getName()) + " has value " + SampleSet.NODATA_value + ", treating as no-data value", "nodataInBackground");
                                warnedNodata = true;
                                skip = true;
                            }
			//			double[] vals = csv.getDoubles(SampleSet.firstEnvVar);
			//			String species = csv.get(SampleSet.speciesIndex);
			//			if (species.equals(SampleSetWithGrids.background))
			}
                        if (!skip) {
			   valsa.add(vals);
			   coordsa.add(coords);
			}
		    }
		});
	} catch (NumberFormatException e) {
	    throw new IOException("File " + fileName + ": " + e.getMessage());
	}
	allvals = (double[][]) valsa.toArray(new double[0][]);
	dimension = new GridDimension(0,0,1,1,allvals.length);
	dimension.coords = (double[][]) coordsa.toArray(new double[0][]);
	dimension.coordNames = new String[] { 
	    csv.headers()[SampleSet.xIndex], csv.headers()[SampleSet.yIndex]};
    }

    Grid getGrid(final String name) {
	int i;
	for (i=0; i<varnames.length; i++)
	    if (varnames[i].equals(name)) break;
	if (i==varnames.length) 
	    throw new RuntimeException("Can't find grid named " + name);
	final int index=i;
	return new Grid(dimension, name) {
		public float eval(int r, int c) { 
		    return (float) allvals[c][index]; 
		}
		public boolean hasData(int r, int c) { return true; }
	    };
    }

    public Feature[] toFeatures() {
	if (features!=null) return features;
	features = new Feature[nvars];
	for (int i=0; i<nvars; i++) {
	    final int index = i;
	    features[i] = new LayerFeature(getNumPoints(), varnames[i], (layers==null) ? Layer.UNKNOWN : layers[i].getType()) {
		    public double eval(int p) {
			return allvals[p][index];
		    }
		    public double eval(Sample s) {
			if (!hasData(s)) 
			    throw new RuntimeException("Attempt to evaluate " + this.name + " at sample with no value");
			return ((Double) s.featureMap.get(this.name)).doubleValue();
		    }
		    public boolean hasData(Sample s) { 
			if (s.featureMap==null || 
			    !s.featureMap.containsKey(this.name))
			    Utils.fatalException("Sample missing data for variable " + this.name, null);
			return (s.featureMap.get(this.name) != null);
		    }
		};
	    featureNameMap.put(features[i].name, features[i]);
	}
	return features;
    }

    public int getNumPoints() { return allvals.length; }
}

