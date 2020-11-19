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

public class Extractor extends GridSet {
    static String cacheDirectoryName = "maxent.cache";
    static String readingFile = null;
    String[] fileNames;
    public int numBackground;
    int nr, nc, ngrids, ntrain, ntest, nsamples, nRandomTarget;
    public Rc[] randextract, sampleextract;
    LazyGrid[] grids;
    boolean[] trainNeedsGrid, testNeedsGrid;
    static Params params=null;

    public int getNumPoints() { return numBackground; } // dunno if needed

    public class Rc {
	public int r, c;
	public double[] vals;
	boolean isTrain;
	Sample sample;
	public Rc(int r, int c, Sample s, int n, boolean isTrain) { 
	    this.r = r; 
	    this.c = c; 
	    sample = s;
	    vals = new double[n];
	    this.isTrain = isTrain;
	}
	public Rc(int r, int c, double[] vals) {
	    this.r = r;
	    this.c = c;
	    this.vals = vals;
	}
    }

    public double getX(int i) { // x value for i'th random sample
	return dimension.toX(randextract[i].c);
    }
    public double getY(int i) {
	return dimension.toY(randextract[i].r);
    }
	
    Layer[] namesToLayers(String[] fileNames) {
	Layer[] result = new Layer[fileNames.length];
	for (int i=0; i<fileNames.length; i++)
	    result[i] = new Layer(new File(fileNames[i]), "Continuous");
	return result;
    }

    public static String CachedGridFilename(String filename, boolean makeit) throws IOException {
	String lower = filename.toLowerCase();
	if (lower.endsWith(".mxe") || lower.endsWith(".grd") || lower.endsWith(".bil") || (params!=null && !params.getboolean("cache")))
	    return filename;
	File file = new File(filename);
	File cachedir = new File(file.getParent(), cacheDirectoryName);
	if (!cachedir.exists()) 
	    if (!cachedir.mkdir()) {
		return filename;
	    }
	String prefix = file.getName().substring(0,file.getName().length()-4);
	File newfile = new File(cachedir, prefix+".mxe");
	File meta = new File(cachedir, prefix+".info");
	if (!cachedIsCurrent(file, newfile, meta)) {
	    if (!makeit) return filename;
	    Utils.echoln("Converting file " + filename + " size " + new File(filename).length() + ", max memory = " + Runtime.getRuntime().maxMemory());
	    Grid g = 
		(new File(filename).length() > 0.25*Runtime.getRuntime().maxMemory()) ?
		Convert.readEfficient(filename) :
		GridIO.readGrid(filename);
	    //	    Grid g = new LazyGrid(filename);  doesn't give grid type
	    if (Utils.interrupt) return null;
	    new GridWriter(g, newfile).writeAll();
	    PrintWriter out = Utils.writer(meta);
	    out.println("file " + file.getAbsolutePath());
	    out.println("modified " + file.lastModified());
	    out.println("length " + file.length());
	    out.close();
	}
	return newfile.getPath();
    }
    
    static boolean cachedIsCurrent(File file, File newfile, File meta) throws IOException {
	if (!newfile.exists() || !meta.exists()) return false;
	BufferedReader in = new BufferedReader(new FileReader(meta));
	boolean result = (file.getAbsolutePath().toLowerCase().equals(getMetaField(in).toLowerCase())
			  && file.lastModified() == Long.parseLong(getMetaField(in))
			  && file.length() == Long.parseLong(getMetaField(in)));
	in.close();
	return result;
    }	

    static String getMetaField(BufferedReader in) throws IOException {
	String[] s = in.readLine().split(" ");
	String result = s[1];
	for (int i=2; i<s.length; i++)
	    result += " " + s[i];
	return result;
    }

    public void extractSamples(String[] files, int nRandom, String trainsamples, String testsamples, String[] species) throws IOException {
	Layer[] ll = namesToLayers(files);
	extractSamples(files, nRandom, trainsamples, testsamples, ll, species);
    }

    public void extractSamples(String[] files, int nRandom, String trainsamples, String testsamples, Layer[] layers, String[] species) throws IOException {
	if (params==null) params = new Params();
	this.layers = layers;
	this.fileNames = files;
	nRandomTarget = nRandom;
	ngrids = files.length;
	String[] cfiles = new String[ngrids];
	grids = new LazyGrid[ngrids];
	//  check headers before compressing grids
	for (int i=0; i<ngrids; i++) {
	    Utils.reportDoing("Checking header of " + files[i]);
	    readingFile = files[i];
	    grids[i] = new LazyGrid(files[i]);
	    if (i==0) dimension = grids[0].getDimension();
	    else if (!grids[i].getDimension().equals(dimension))
		throw new RuntimeException("Files " + files[0] + " and " + files[i] + " have different geographic dimensions");
	}
	nsamples=0;
	if (trainsamples!=null && !trainsamples.equals("")) {
	    readingFile = trainsamples;
	    train = new SampleSet2(trainsamples, layers, dimension, params);
	    train.params = params;
	    train.read(species);
	    if (train.missingFields())
		nsamples += (ntrain = train.numSamples());
	}
	if (testsamples!=null && !testsamples.equals("")) {
	    readingFile = testsamples;
	    test = new SampleSet2(testsamples, layers, dimension, params);
	    test.params = params;
	    // if replicates, test samples may end in _0, _1 etc
	    test.read(params.getReplicates()==0?species:null);
	    if (test.missingFields())
		nsamples += (ntest = test.numSamples());
	}
	for (int i=0; i<ngrids; i++) {
	    readingFile = files[i];
	    cfiles[i] = CachedGridFilename(files[i], true);
	    readingFile = cfiles[i];
	    if (Utils.interrupt) {
		readingFile = null;
		return;
	    }
	    grids[i] = new LazyGrid(cfiles[i]);
	}
	nr = dimension.getnrows();
	nc = dimension.getncols();

	Utils.reportMemory("Extractor");
	numBackground = readSamples(nRandom);
	
	if (Utils.interrupt) {
	    readingFile = null;
	    return;
	}
	features = new Feature[ngrids];
	final float[][] vals = new float[numBackground][ngrids];
	int c=0;
	for (int i=0; i<numBackground; i++)
	    vals[c++] = randextract[i].vals;

	readingFile = null;
	for (int i=0; i<ngrids; i++) {
	    final int index = i;
	    features[i] = new LayerFeature(numBackground, layers[i].getName(), layers[i].getType()) {
		    public double eval(int p) {
			return vals[p][index];
		    }
		    public double eval(Sample s) {
			if (!hasData(s)) 
			    throw new RuntimeException("Attempt to evaluate " + this.name + " at sample with no value");
			return ((Double) s.featureMap.get(this.name)).doubleValue();
		    }
		    public boolean hasData(Sample s) { 
			if (s.featureMap==null || 
			    !s.featureMap.containsKey(this.name))
			    Utils.fatalException("Sample at " + s.lon + ":" + s.lat + " unexpectedly missing data for variable " + this.name, null);
			return (s.featureMap.get(this.name) != null);
		    }
		};
	    featureNameMap.put(layers[i].getName(), features[i]);
	}
    }


    boolean[] addSamplesToExtract(SampleSet2 s, int start, boolean isTrain) {
	if (s==null || !s.missingFields()) return null;
	readingFile = s.sampleFile;
	Sample[] ss = s.getSamples();
	for (int i=0; i<ss.length; i++) {
	    Sample sss = ss[i];
	    sampleextract[start+i] = new Rc(sss.row, sss.col, sss, ngrids, isTrain);
	}
	boolean[] need = new boolean[ngrids];
	for (int i=0; i<need.length; i++)
	    need[i] = (s.layerToColumn[i] == -1);
	return need;
    }

    // read up to n background points, return number read
    int readSamples(int n) {
	sampleextract = new Rc[nsamples];
	randextract = new Rc[n];
	trainNeedsGrid = addSamplesToExtract(train, 0, true);
	testNeedsGrid = addSamplesToExtract(test, ntrain, false);
	Arrays.sort(sampleextract, new Comparator() {
		public int compare(Object o1, Object o2) {
		    Rc rc1 = (Rc) o1, rc2 = (Rc) o2;
		    if (rc1.r < rc2.r) return -1;
		    if (rc1.r > rc2.r) return 1;
		    if (rc1.c < rc2.c) return -1;
		    if (rc1.c > rc2.c) return 1;
		    return 0;
		}
	    });
	Utils.reportDoing("Extracting random background and sample data");
	int sample=0;
	int chosen=0;
	long seen=0;
	float[] vals = new float[ngrids];
	boolean[] hasData = new boolean[ngrids];
	for (int r=0; r<nr; r++) {
	    Utils.reportProgress((r*100)/(double)nr);
	    if (Utils.interrupt) return 0;
	    for (int c=0; c<nc; c++) {
		while (sample < nsamples) {
		    Rc rc = sampleextract[sample];
		    if(rc.r != r || rc.c != c) break;
		    extractSample(rc);
		    sample++;
		}
		if (n==0) continue;
		boolean hasAllData = true;
		for (int j=0; j<ngrids; j++) {
		    if (hasData[j] = grids[j].hasData(r,c))
			vals[j] = grids[j].eval(r,c);
		    else {
			hasAllData = false;
			break;
		    }
		}
		if (hasAllData) {
		    seen++;
		    if (chosen<n)
			randextract[chosen++] = 
			    new Rc(r,c,(float[]) vals.clone());
		    else {
			long rnd = (seen < Integer.MAX_VALUE) ?
			    Utils.generator.nextInt((int) seen) :
			    Utils.generator.nextLong() % seen;
			if(rnd < n)
			    randextract[(int) rnd] =
				new Rc(r,c,(float[]) vals.clone());
		    }
		}
	    }
	}
	Utils.echoln(seen + " points with values for all grids");
	if (train!=null) train.createMaps();
	if (test!=null) test.createMaps();
	makeCoords(chosen);
	return chosen;
    }

    void makeCoords(int chosen) {
	dimension.coords = new double[chosen][2];
	for (int i=0; i<chosen; i++) {
	    dimension.coords[i][0] = getX(i);
	    dimension.coords[i][1] = getY(i);
	}
    }

    void extractSample(Rc rc) {
	SampleSet2 ss = rc.isTrain?train:test;
	boolean[] need = rc.isTrain?trainNeedsGrid:testNeedsGrid;
	Sample s = rc.sample;
	double[] data = (double[]) ss.datamap.get(s);
	boolean warned = false;
	int cntmissing = 0;
	for (int j=0; j<ngrids; j++) {
	    if (!need[j] && data[j]==SampleSet.NODATA_value)
		cntmissing++;
	    if (!need[j]) continue;
	    if (!grids[j].hasData(rc.r, rc.c)) {
		cntmissing++;
		data[j] = SampleSet.NODATA_value;
		if (warned) continue;
		SampleSet2.warnPartialData(s.getLon(), s.getLat(), 
					   new File(ss.sampleFile).getName(), 
					   layers[j].getName());
		warned = true;
	    }
	    else
		data[j] = grids[j].eval(rc.r, rc.c);
	}
	rc.vals = data;
	if (cntmissing == ngrids || cntmissing>0 && !params.allowpartialdata()) {
	    Utils.warn("Skipping sample at "+s.getLon()+", "+s.getLat() + ((cntmissing==ngrids) ? " which has no environmental data" : ""));
	    ss.removeSample(s);
	}
    }

}
