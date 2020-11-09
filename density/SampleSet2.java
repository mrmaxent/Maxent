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

public class SampleSet2 extends SampleSet {
    Layer[] layers;
    int n;
    int[] layerToColumn;
    boolean samplesHaveData = false;
    HashMap datamap = new HashMap();
    String sampleFile;
    GridDimension dim;
    Params params;

    boolean missingFields() {
	for (int i=0; i<layerToColumn.length; i++)
	    if (layerToColumn[i] == -1)
		return true;
	return false;
    }
    String missingField() {
	for (int i=0; i<layerToColumn.length; i++)
	    if (layerToColumn[i] == -1)
		return layers[i].name;
	return null;
    }
	
    int numSamples() { return datamap.keySet().size(); }
    static String[] featureNames(String sampleFile) throws IOException {
	Csv csv = new Csv(sampleFile);
	String[] fields = csv.headers();
	if (fields.length < necessaryFields)
	    throw necessaryFieldsException();
	if (fields.length < SampleSet.firstEnvVar+1)
	    return new String[0];
	String[] result = new String[fields.length - SampleSet.firstEnvVar];
	for (int i=0; i<result.length; i++)
	    result[i] = fields[SampleSet.firstEnvVar+i];
	return result;
    }
    
    public SampleSet2(String sampleFile, GridDimension dim, Params p) throws IOException {
	this(sampleFile, new Layer[0], dim, p);
	read(null);
    }

    public SampleSet2(String sampleFile, Layer[] layers, GridDimension dim, Params p) throws IOException {
	this.layers = layers;
	n = layers.length;
	this.sampleFile = sampleFile;
	this.dim = dim;
	params = (p==null) ? new Params() : p;
	checkHeaders();  // sets samplesHaveData too
    }

    void checkHeaders() throws IOException {
	final String sampleFileName = new File(sampleFile).getName();
	String[] featureNames = featureNames(sampleFile);
	boolean[] used = new boolean[featureNames.length];
	for (int j=0; j<n; j++) {
	    String name = layers[j].getName();
	    for (int i=0; i<featureNames.length; i++) {
		if (name.equals(featureNames[i])) {
		    samplesHaveData = true;
		    used[i] = true;
		    break;
		}
	    }
	}
	for (int i=0; i<featureNames.length; i++)
	    if (!used[i]) {
		boolean unselected = false;
		if (params!=null && params.unusedLayers!=null)
		    for (int j=0; j<params.unusedLayers.length; j++)
			if (params.unusedLayers[j].equals(featureNames[i]))
			    unselected = true;
		if (!unselected)
		    Utils.warn2("Unused field " + featureNames[i] + " in " +
				sampleFileName,
				"unusedsamplefilefield");
	    }
    }
	
    public void read(String[] species) throws IOException {
	final String sampleFileName = new File(sampleFile).getName();
	Utils.reportDoing("Reading samples from " + sampleFileName);
	Utils.reportMemory("Read samples");
	String[] featureNames = featureNames(sampleFile);
	layerToColumn = new int[n];
	for (int j=0; j<n; j++) {
	    String name = layers[j].getName();
	    layerToColumn[j] = -1;
	    for (int i=0; i<featureNames.length; i++) {
		if (name.equals(featureNames[i])) {
		    layerToColumn[j] = i;
		    break;
		}
	    }
	}
	final HashSet speciesSet = (species==null) ? null : new HashSet();
	if (species != null)
	    for (int i=0; i<species.length; i++)
		speciesSet.add(species[i]);

	final Csv csv = new Csv(sampleFile);
	String[] fields = csv.headers();
	header = csv.headerString();
	String c2 = fields[xIndex], c3 = fields[yIndex];
	final boolean longFirst = !(c2.toLowerCase().indexOf("lat")!=-1 && c3.toLowerCase().indexOf("lat")==-1);
	if (!longFirst)
	    Utils.warn("interpreting " + c2 + " column of sample file as latitude");
	csv.apply(csv.new Applier() {
		public void process() {
		    String[] fields = csv.getCurrentRecord();
		    String spid = sanitizeSpeciesName(fields[SampleSet.speciesIndex].replaceAll("\"", ""));
		    if (speciesSet!=null && !speciesSet.contains(spid))
			return;
		    
		    double x=0.0, y=0.0;
		    int r=-1, c=-1;
		    try {
			x = Double.parseDouble(fields[SampleSet.xIndex]);
			y = Double.parseDouble(fields[SampleSet.yIndex]);
			if (!longFirst) {
			    double tmp = x;
			    x = y;
			    y = tmp;
			}
			if (dim!=null) {
			    int[] rc = dim.toRowCol(new double[] {x,y});
			    r = rc[0];
			    c = rc[1];
			}
		    } catch (NumberFormatException e) {
			x = 0.0;
			y = 0.0;
		    }
		    Sample s = new Sample(-1, r, c, y, x, spid, null);
		    
		    int goodvals = 0;
		    double[] data = new double[n];
		    for (int j=0; j<n; j++) {
			int idx = layerToColumn[j];
			data[j] = NODATA_value;
			if (idx!=-1 && idx<fields.length && !fields[SampleSet.firstEnvVar+idx].trim().equals("")) 
			    data[j] = Double.parseDouble(fields[SampleSet.firstEnvVar+idx]);
			if (data[j] == NODATA_value && idx!=-1) {
			    warnPartialData(x,y,sampleFileName,layers[j].name);
                            if (!params.allowpartialdata())
                              return;
                        }
			if (data[j] != NODATA_value)
			    goodvals++;
		    }
		    if (missingFields() && (dim==null || !dim.inBounds(r,c))) {
			Utils.warn2("Sample at "+x+", "+y+" in "+sampleFileName+
				    (samplesHaveData ?
				     " is missing a value for " +
				     missingField() +
				     ", skipping" :
				     " is outside the bounding box of environmental data, skipping"), 
				    "sampleoutofbounds");
			return;
		    }
		    datamap.put(s, data);
		    if (!speciesMap.containsKey(spid))
			speciesMap.put(spid, new ArrayList());
		    ((ArrayList) speciesMap.get(spid)).add(s);
		}
	    });
    }

    static void warnPartialData(double x, double y, String sampleFileName, String field) {
	Utils.warn2("Sample at "+x+", "+y+" in "+
		    sampleFileName+
		    " is missing some environmental data (e.g. " + field + ")", 
		    "samplemissingsomedata");
    }

    public void createMaps() {
	Sample[] s = getSamples();
	for (int i=0; i<s.length; i++) {
	    double[] data = (double[]) datamap.get(s[i]);
	    HashMap map = new HashMap();
	    for (int j=0; j<n; j++)
		map.put(layers[j].getName(), (data[j]==SampleSet.NODATA_value) ? null : new Double(data[j]));
	    s[i].featureMap = map;
	}
    }
}
