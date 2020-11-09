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

// To do:  
//   remove backgroundIterator from here and FeaturedSpace
//   remove DoubleIterator class

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.imageio.*;
import java.io.*;
import gnu.getopt.*;
import java.util.*;
import ptolemy.plot.*;
import java.text.*;

public class Runner {
    GridSet gs;
    SampleSet sampleSet, testSampleSet=null;
    String[] projectPrefix = null;
    Params params;
    CsvWriter results;
    GUI gui=null;
    String theSpecies;
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
    ArrayList projectedGrids;
    String writtenGrid;
    double aucmax, applyThresholdValue;
    boolean samplesAddedToFeatures;
    String raw2cumfile=null;
    ParallelRun parallelRunner;
    double[][] coords;
    HashMap<String,Integer> speciesCount;

    // common parameters
    boolean is(String s) { return params.getboolean(s); }
    boolean occurrenceProbability() { return params.occurrenceProbability(); }
    boolean cumulative() { return params.cumulative(); }
    int replicates() { return params.getint("replicates"); }
    int replicates(String species) {
	if (speciesCount==null || !cv() || speciesCount.get(species)==null || speciesCount.get(species) > replicates())
	    return replicates();
	return speciesCount.get(species);
    }
    int threads() { return params.getint("threads"); }
    String outDir() { return params.getString("outputdirectory"); }
    String biasFile() { return params.getString("biasFile"); }
    String testSamplesFile() { return params.getString("testSamplesFile"); }
    String environmentalLayers() { return params.getString("environmentalLayers"); }
    String projectionLayers() { return params.getString("projectionLayers"); }
    double betaMultiplier() { return params.getdouble("betaMultiplier"); }
    String outputFileType() { return "."+params.getString("outputFileType"); }
    boolean cv() { return params.getString("replicatetype").equals("crossvalidate"); }
    boolean bootstrap() { return params.getString("replicatetype").equals("bootstrap"); }
    boolean subsample() { return params.getString("replicatetype").equals("subsample"); }

    static String raw2cumfile(String lambdafile) {
	return lambdafile.replaceAll(".lambdas$", "_omission.csv");
    }

    public static class MaxentRunResults {
	double gain, time;
	int iterations;
	FeaturedSpace X;
	String[] featureTypes;
	void removeBiasDistribution() {
	    if (X.biasDist != null)
		X.setBiasDist(null);
	}
	MaxentRunResults(double gain, int iter, FeaturedSpace X, double time, String[] types) {
	    this.gain=gain; this.iterations=iter; this.X=X; this.time=time; featureTypes=types;
	}
    }

    public Runner(Params params) {	
	this.params = params; 
	nf.setGroupingUsed(false); 
	nf.setMinimumFractionDigits(3); 
	nf.setMaximumFractionDigits(3);
    }

    void popupError(String s, Throwable e) { Utils.popupError(s, e); }

    void stop() { 
	Utils.echoln("Interrupted");
	Utils.interrupt = true; 
    }

    Layer[] allLayers=null;

    Layer[] trueFeatureLayers() {
	ArrayList result = new ArrayList();
	for (int i=0; i<allLayers.length; i++) {
	    int type=allLayers[i].getType();
	    if (type==Layer.CONT || type==Layer.CAT)
		result.add(allLayers[i]);
	}
	return (Layer[]) result.toArray(new Layer[0]);
    }

    boolean isTrueBaseFeature(Feature f) {
	int type=f.type();
	return type==Feature.L_CONT || type==Feature.L_CAT;
    }

    Feature[] getTrueBaseFeatures(Feature[] f) {
	ArrayList result=new ArrayList();
	for (int i=0; i<f.length; i++)
	    if (isTrueBaseFeature(f[i]))
		result.add(f[i]);
	return (Feature []) result.toArray(new Feature[0]);
    }
    
    boolean gridsFromFile() {
	return new File(environmentalLayers()).isFile();
    }

    GridSet initializeGrids() {
	String dir=environmentalLayers();
	boolean isFile = gridsFromFile();
	String[] layers=params.layers;
	String[] layerTypes=params.layerTypes;
	GridSet result=null;
	ArrayList allLayersList = new ArrayList();

	for (int i=0; i<layers.length; i++) {
	    Layer layer = new Layer(layers[i],layerTypes[i]);
	    allLayersList.add(layer);
	}
	try {
	    ArrayList full = new ArrayList();
	    ArrayList fileLayersList = new ArrayList();

	    for (int i=0; i<layers.length; i++) {
		Layer layer = new Layer(layers[i],layerTypes[i]);
		// 10/17/2006: commented out next line as only makes dupes
		// allLayersList.add(layer);
		fileLayersList.add(layer);
		if (!isFile && !dir.equals(""))
		    full.add(Utils.getGridAbsolutePath(dir, layers[i]));
	    }
	
	    if (!biasFile().equals("")) {
		File f = new File(biasFile());
		if (f.exists()) {
		    Layer layer = new Layer(f,params.getint("biasType"));
		    allLayersList.add(layer);
		    full.add(f.getAbsolutePath());
		    fileLayersList.add(layer);
		}
		else {
		    popupError("Error opening bias file " + biasFile(), null);
		    return null;
		}
	    }

	    String[] fileNames = (String[]) full.toArray(new String[0]);
	    Layer[] fileLayers = (Layer[]) fileLayersList.toArray(new Layer[0]);
	    allLayers = (Layer[]) allLayersList.toArray(new Layer[0]);

	    String[] selectSpecies = params.species;
	    if (isFile) {
		result = new GridSetFromFile(dir, allLayers);
		if (!params.getString("samplesFile").equals("")) {
		    result.train = new SampleSet2(params.getString("samplesFile"), fileLayers, null, params);
		    if (!result.train.samplesHaveData) {
			popupError("Samples need to be in SWD format when background data is in SWD format",null);
			return null;
		    }
		    result.train.params = params;
		    result.train.read(selectSpecies);
		    result.train.createMaps();
		}
		if (!testSamplesFile().equals("")) {
		    result.test = new SampleSet2(testSamplesFile(), fileLayers, null, params);
		    if (!result.test.samplesHaveData) {
			popupError("Test samples need to be in SWD format when background data is in SWD format",null);
			return null;
		    }
		    result.test.params = params;
		    // if replicates, test samples may end in _0, _1 etc
		    result.test.read(replicates()==0?selectSpecies:null);
		    //		    System.out.println("tt");
		    result.test.createMaps();
		}
	    }
	    else {
		if (fileNames.length==0) return null;
		result = new Extractor();
		((Extractor) result).params = params;
		((Extractor) result).extractSamples(fileNames, params.getint("maximumBackground"), params.getString("samplesFile"), testSamplesFile(), fileLayers, selectSpecies);
	    }
	}
	catch (Exception e) {
	    String msg = "Error reading files";
	    if (Extractor.readingFile!=null)
		msg = "Error reading file " + Extractor.readingFile;
	    popupError(msg, e);
	    Utils.interrupt = true;
	    return null;
	}
	return result;
    }

    /**
     * Clean up after a run is completed
     */
    public void end() {
	Utils.echoln("Ending");
	if (results!=null) { results.close(); results=null; }
	if (htmlout!=null) { htmlout.close(); htmlout=null; }
	Utils.closeLog();
	Utils.disposeProgressMonitor();
    }

    /**
     * Start a run, as if the "Run" button was pressed
     */
    public synchronized void start() {
	int j;

	Utils.applyStaticParams(params);
	if (params.layers==null)
	    params.setSelections();
	if (cv() && replicates()>1 && params.getRandomtestpoints() != 0) {
	    Utils.warn2("Resetting random test percentage to zero because cross-validation in use", "skippingHoldoutBecauseCV");
	    params.setRandomtestpoints(0);
	}
	if (subsample() && replicates()>1 && params.getint("randomTestPoints") <= 0 && !is("manualReplicates")) {
	    popupError("Subsampled replicates require nonzero random test percentage", null);
	    return;
	}
	if (!cv() && replicates()>1 && !params.getboolean("randomseed") && !is("manualReplicates")) {
	    Utils.warn2("Setting randomseed to true so that replicates are not identical", "settingrandomseedtrue");
	    params.setValue("randomseed", true);
	}

	if (outDir()==null || outDir().trim().equals("")) {
	    popupError("An output directory is needed", null);
	    return;
	}
	if (!(new File(outDir()).exists())) {
	    popupError("Output directory does not exist", null);
	    return;
	}
	if (!biasFile().equals("") && gridsFromFile()) {
	    popupError("Bias grid cannot be used with SWD-format background", null);
	    return;
	}
	if (is("perSpeciesResults") && replicates()>1) {
	    Utils.warn2("PerSpeciesResults is not supported with replicates>1, setting perSpeciesResults to false", "unsettingPerSpeciesResults");
	    params.setValue("perSpeciesResults", false);
	}
	// other parameter consistency checks?
	try { Utils.openLog(outDir(),params.getString("logFile")); }
	catch (IOException e) { 
	    popupError("Error opening log file", e); 
	    return;
	}
	Utils.startTimer();
	Utils.echoln(new Date().toString());
	Utils.echoln("MaxEnt version "+Utils.version);
	Utils.interrupt = false;
	if (threads()>1)
	    parallelRunner = new ParallelRun(threads());
	Thread.currentThread().setPriority(Thread.NORM_PRIORITY-1);
	if (params.layers == null || params.layers.length==0) {
	    popupError("No environmental layers selected", null);
	    return;
	}
	if (params.species.length==0) { 
	    popupError("No species selected", null);
	    return;
	}
	if (Utils.progressMonitor!=null)
	    Utils.progressMonitor.setMaximum(100);
	
	Utils.generator = new Random(!params.isRandomseed() ? 0 : System.currentTimeMillis());
	gs = initializeGrids();
	if (Utils.interrupt || gs==null) return;

	SampleSet2 sampleSet2 = gs.train;

	if (projectionLayers().length()>0) {
	    String[] dirs = projectionLayers().trim().split(",");
	    projectPrefix = new String[dirs.length];
	    for (int i=0; i<projectPrefix.length; i++)
		projectPrefix[i] = (new File(dirs[i].trim())).getPath();
	}

	if (!testSamplesFile().equals("")) {
	    testSampleSet = gs.test;
	}

	if (Utils.interrupt) return;
	if (is("removeDuplicates")) 
	    sampleSet2.removeDuplicates(gridsFromFile() ? null : gs.getDimension());
	
	Feature[] baseFeatures;
	baseFeatures = (gs==null) ? null : gs.toFeatures();
	coords = gs.getDimension().coords;
	if (baseFeatures==null || baseFeatures.length==0 || baseFeatures[0].n==0) {
	    popupError("No background points with data in all layers", null);
	    return;
	}

	// note.
	boolean addSamplesToFeatures = samplesAddedToFeatures = 
	    is("addSamplesToBackground") && 
	    (sampleSet2.samplesHaveData || (gs instanceof Extractor));
	
	if (addSamplesToFeatures) 
	    Utils.echoln("Adding samples to background in feature space");

	Feature[] features=null;
	if (!addSamplesToFeatures) {
	    features = makeFeatures(baseFeatures);
	    if (Utils.interrupt) return;
	}

	sampleSet=sampleSet2;
	speciesCount = new HashMap();
	if (replicates()>1 && !is("manualReplicates")) {
	    if (cv()) {
		for (String s: sampleSet.getNames())
		    speciesCount.put(s, sampleSet.getSamples(s).length);
		testSampleSet = sampleSet.splitForCV(replicates());
	    } 
	    else
		sampleSet.replicate(replicates(), bootstrap());
	    ArrayList<String> torun = new ArrayList();
	    for (String s: sampleSet.getNames())
		if (s.matches(".*_[0-9]+$"))
		    torun.add(s);

	    params.species = torun.toArray(new String[0]);
	}
	if (testSamplesFile().equals("") && params.getint("randomTestPoints")!=0) {
	    SampleSet train=null;
	    if (!is("randomseed")) Utils.generator = new Random(11111);
	    testSampleSet = 
		sampleSet.randomSample(params.getint("randomTestPoints"));
	}
	if (Utils.interrupt) return;

	writeLog();
	if (!is("perSpeciesResults")) {
	    try { results = new CsvWriter(new File(outDir(), "maxentResults.csv"), is("appendtoresultsfile")); }
	    catch (IOException e) {
		popupError("Problem opening results file", e);
		return;
	    }
	}
	for (int sample=0; sample<params.species.length; sample++) {
	    theSpecies = params.species[sample];
	    if (Utils.interrupt) return;
	    if (is("perSpeciesResults")) {
		try { results = new CsvWriter(new File(outDir(),theSpecies + "Results.csv")); }
		catch (IOException e) {
		    popupError("Problem opening " + theSpecies + " results file", e);
		    return;
		}
	    }
	    String suffix = outputFileType();
	    File f = new File(outDir(), theSpecies + suffix);
	    File lf = new File(outDir(), theSpecies + ".lambdas");
	    String lambdafile = lf.getAbsolutePath();

	    Sample[] sss = sampleSet.getSamples(theSpecies);
	    if (!params.allowpartialdata())
		sss = withAllData(baseFeatures, sss);
	    final Sample[] ss = sss;
	    if (ss.length == 0) {
		Utils.warn2("Skipping " + theSpecies + " because it has 0 training samples", "skippingBecauseNoTrainingSamples");
		continue; 
	    }
	    if (testSampleSet!=null) {
		int len = testSampleSet.getSamples(theSpecies).length;
		if (len == 0) {
		    //		    if (MaxEnt.bootstrapBetaResults!=null)
		    //			MaxEnt.bootstrapBetaResults.add(new Double(0.0));
		    Utils.warn2("Skipping " + theSpecies + " because it has 0 test samples", "skippingBecauseNoTestSamples");
		    continue;
		}
	    }
	    Utils.reportMemory("getSamples");

	    if (lf.exists()) {
		if (is("skipIfExists")) {
		    if (is("appendtoresultsfile"))
			maybeReplicateHtml(theSpecies, getTrueBaseFeatures(baseFeatures));
		    continue;
		}
		if (is("askoverwrite") && Utils.topLevelFrame!=null) {
		    Object[] options = {"Skip", "Skip all", "Redo", "Redo all"};
		    int val = JOptionPane.showOptionDialog(Utils.topLevelFrame, "Output file exists for " + theSpecies, "File already exists", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		    switch(val) {
		    case 1: params.setValue("skipIfExists", true);
		    case 0: 
			if (is("appendtoresultsfile"))
			    maybeReplicateHtml(theSpecies, getTrueBaseFeatures(baseFeatures));
			continue;
		    case 3: params.setValue("askoverwrite", false);
		    case 2: break;
		    }
		}
	    }

	    Feature[] baseFeaturesWithSamples = baseFeatures;
	    if (addSamplesToFeatures) {
		features = null;  // free up memory before makeFeatures
		baseFeaturesWithSamples = featuresWithSamples(baseFeatures, ss);
		if (baseFeaturesWithSamples==null) continue;
		features = makeFeatures(baseFeaturesWithSamples);
	    }
	    Feature[] baseFeaturesNoBias = getTrueBaseFeatures(baseFeaturesWithSamples);
	    if (Utils.interrupt) return;

	    Utils.reportDoing(theSpecies + ": ");

	    contributions = null;
	    MaxentRunResults res = maxentRun(features, ss,
					     testSampleSet!=null ? testSampleSet.getSamples(theSpecies) : null);
	    if (res==null) return;
	    Utils.echoln("Resulting gain: " + res.gain);
	    final FeaturedSpace X = res.X;

	    //	    if (X.biasDist != null)
	    //		X.setBiasDist(null); // remove biasDist
	    res.removeBiasDistribution();

	    boolean gsfromfile = (gs instanceof GridSetFromFile);
	    boolean writeRaw2cumfile = true;

	    //	    boolean saveInterpolate = Grid.interpolateSamples;
	    //	    Grid.interpolateSamples = false;
	    DoubleIterator backgroundIterator = null;
	    /*
	    if (addSamplesToFeatures)
		backgroundIterator=new DoubleIterator(baseFeatures[0].n) {
			double getNext() { return X.newDensity(i++); }
		    };
	    */
	    double auc = X.getAUC(backgroundIterator, X.testSamples);
	    double aucSD = X.aucSD;
	    double trainauc = X.getAUC(backgroundIterator, X.samples);
	    aucmax = X.aucmax;
	    //	    Grid.interpolateSamples = saveInterpolate;
	    if (backgroundIterator!=null)
		X.setDensityNormalizer(backgroundIterator);

	    double entropy = X.getEntropy(); // needs to be after setDensityNormalizer
	    double prevalence = X.getPrevalence(params);
	    try { X.writeFeatureWeights(lambdafile); }
	    catch (IOException e) {
		popupError("Error writing feature weights file", e);
		return;
	    }

	    double testGain = (testSampleSet==null) ? 0 : getTestGain(X);
	    startHtmlPage();

	    if (writeRaw2cumfile) {
		raw2cumfile = raw2cumfile(lambdafile);
		try {
		    double[] weights = (backgroundIterator==null) ? X.getWeights() :
			backgroundIterator.getvals(X.densityNormalizer);
		    writeCumulativeIndex(weights, raw2cumfile, X, auc, trainauc, baseFeaturesNoBias, entropy);
		} catch (IOException e) {
		    popupError("Error writing raw-to-cumulative index file", e);
		    return;
		}
	    }
		    
	    writtenGrid = null;
	    if (Utils.interrupt) return;

	    boolean explain = true;
	    for (String s: res.featureTypes)
		if (s.equals("product"))
		    explain = false;
	    startedPictureHtmlSection = false;
	    if (is("outputGrids")) {
		String filename = gsfromfile ? 
		    new File(outDir(), theSpecies + ".csv").getPath() :
		    f.getPath();
		try {
		    Project proj = new Project(params);
		    //	if (params.biasIsBayesianPrior) {
		    //	  String name = new File(params.biasFile).getName();
		    //	  for (int i=0; i<Utils.inputFileTypes.length; i++)
		    //	     name = name.replaceAll(Utils.inputFileTypes[i]+"$", "");
		    //	  proj.priorDistribution = gs.getGrid(name);
		    //  }
		    proj.entropy = entropy;
		    if (gsfromfile)
			proj.doProject(lambdafile, (GridSetFromFile) gs, filename);
		    else
			proj.doProject(lambdafile, environmentalLayers(), filename, null);
		    proj.entropy = -1.0;
		    if (Utils.interrupt) return;
		    writtenGrid = filename;
		    if (is("pictures") && !gsfromfile) 
			makePicture(f.getPath(), ss, X.testSamples, null);
		    makeExplain(explain, f, lambdafile, theSpecies + "_explain.bat", new File(environmentalLayers()).getAbsolutePath());
		    if (applyThresholdValue != -1 && !gsfromfile)
			new Threshold().applyThreshold(f.getPath(), applyThresholdValue);
		} catch (IOException e) {
		    popupError("Error writing output file " + new File(filename).getName(), e);
		    return;
		}
	    }
		    
	    if (Utils.interrupt) return;
	    projectedGrids = new ArrayList();
	    if (projectPrefix!=null && is("outputGrids"))
		for (int i=0; i<projectPrefix.length; i++) {
		    if (Utils.interrupt) return;
		    String prefix = new File(projectPrefix[i]).getName();
		    if (prefix.endsWith(".csv"))
			prefix = prefix.substring(0, prefix.length()-4);
		    boolean isFile = (new File(projectPrefix[i]).isFile());
		    File ff = new File(outDir(), theSpecies + "_" + prefix + (isFile ? ".csv" : suffix));
		    File ffclamp = new File(outDir(), theSpecies + "_" + prefix + "_clamping" + (isFile ? ".csv" : suffix));
		    try {
			Project proj = new Project(params);
			//			if (params.biasIsBayesianPrior)
			//			    proj.priorDistribution = gs.getGrid(new File(params.biasFile).getName());
			proj.needLayers = allLayers;
			proj.doProject(lambdafile, projectPrefix[i], ff.getPath(), is("writeClampGrid") ? ffclamp.getPath() : (String) null);
			if (Utils.interrupt) return;
			projectedGrids.add("<a href = \"" + ff.getName() + "\">The model applied to the environmental layers in " + projectPrefix[i] + "</a>");
			if (is("pictures") && !isFile) {
			    makePicture(ff.getPath(), ss, X.testSamples, projectPrefix[i]);
			    makeExplain(explain, ff, lambdafile, theSpecies + "_" + prefix + "_explain.bat", new File(projectPrefix[i]).getAbsolutePath());
			    if (is("writeClampGrid")) 
				makePicture(ffclamp.getPath(), new Sample[0], new Sample[0], projectPrefix[i], true);
			    makeNovel(baseFeaturesNoBias, projectPrefix[i], new File(outDir(), theSpecies+"_"+prefix+"_novel"+suffix).getPath());
			}
			if (applyThresholdValue != -1 && !isFile)
			    new Threshold().applyThreshold(ff.getPath(), applyThresholdValue);
		    } catch (IOException e) {
			popupError("Error projecting", e);
			return;
		    }
		}
	    if (Utils.interrupt) return;
	    try {
		writeSampleAverages(baseFeaturesWithSamples, ss);
	    } catch (IOException e) {
		popupError("Error writing file", e);
		return;
	    }
	    if (is("responsecurves")) {
		try {
		    createProfiles(baseFeaturesWithSamples, lambdafile, ss);
		}
		catch (IOException e) {
		    popupError("Error writing response curves for " + theSpecies, e);
		    return;
		}
	    }

	    if (Utils.interrupt) return;
	    double[] permcontribs=null;
	    try {
		permcontribs = new PermutationImportance().go(baseFeatures, ss, lambdafile);
	    } catch (IOException e) {
		popupError("Error computing permutation importance for " + theSpecies, e);
		return;
	    }
	    writeContributions(new double[][] { contributions, permcontribs }, "");
	    double[][] jackknifeGain = (is("jackknife")  && (baseFeaturesNoBias.length > 1)) ? 
		jackknifeGain(baseFeaturesWithSamples, ss, X.testSamples, res.gain, testGain, auc) :
		null;

	    writeSummary(res, testGain, auc, aucSD, trainauc, results, baseFeaturesNoBias, jackknifeGain, entropy, prevalence, permcontribs);

	    writeHtmlDetails(res, testGain, auc, aucSD, trainauc);
	    htmlout.close();
	    // if gsfromfile, we'll need to do something different, 
	    // using (GridSetFromFile) gs instead of params.environmentallayers.
	    maybeReplicateHtml(theSpecies, baseFeaturesNoBias);
	}
	if (threads()>1)
	    parallelRunner.close();
    }

    void makeNovel(Feature[] basefeatures, String projdir, String outfile) throws IOException {
	if (!params.isWritemess()) return;
	density.tools.Novel novel = new density.tools.Novel();
	novel.setWhiteNonNovel();
	novel.go(basefeatures, projdir, outfile);
	String projname = new File(projdir).getName();
	String layers = new File(environmentalLayers()).getName();
	htmlout.println("<br>The following two pictures compare the environmental similarity of variables in " + projname + " to the environmental data used for training the model.  In the first picture (MESS), areas in red have one or more environmental variables outside the range present in the training data, so predictions in those areas should be treated with strong caution.  The second picture (MoD) shows the most dissimilar variable, i.e., the one that is furthest outside its training range.  For details, see Elith et al., Methods in Ecology and Evolution, 2010");
	GridDimension dim = new LazyGrid(outfile).getDimension();
	htmlout.println("<br>" + htmlLink(Utils.pngname(outfile), null, dim.getnrows(), dim.getncols()) + "<br>");
	htmlout.println("<br>" + htmlLink(Utils.pngname(outfile).replaceAll(".png$", "_limiting.png"), null, dim.getnrows(), dim.getncols()) + "<br>");

    }

    void makeExplain(boolean makeIt, File predfile, String lambdafilename, String batfilename, String predvarsdir) throws IOException {
	if (!makeIt) {
	    htmlout.println("<br>(A link to the Explain tool was not made for this model.  The model uses product features, while the Explain tool can only be used for additive models.)<br><br>");
	    return;
	}
	boolean windows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	File batfile = new File(outDir(), batfilename);
	PrintWriter out = new PrintWriter(batfile);
	String jarfile = (Utils.getJarfileLocation()==null) ? "" :
	    (Utils.getJarfileLocation().replaceAll("%20"," ") + System.getProperty("path.separator"));
	int mem = (int) (Runtime.getRuntime().maxMemory()/1024/1024);
	if (mem<500) mem=500;
	out.println("java -mx" + mem + "m -cp \"" + jarfile + System.getProperty("java.class.path") + "\" density.Explain -l " + Utils.protectFileName(lambdafilename) + (params.cloglog()?" -c ":" ") + Utils.protectFileName(predfile.getAbsolutePath()) + " " + Utils.protectFileName(predvarsdir));
	if (windows)
	    out.println("@if errorlevel 1 pause");
	out.close();
	htmlout.println("<br>Click <a href="+batfile.getName()+" type=application/bat>here<a> to interactively explore this prediction using the Explain tool.  If clicking from your browser does not succeed in starting the tool, try running the script in " + batfile.getAbsolutePath() + " directly.  This tool requires the environmental grids to be small enough that they all fit in memory.<br><br>");
    }


    void maybeReplicateHtml(String theSpecies, Feature[] baseFeatures) {
	String species = theSpecies.replaceAll("_[0-9]+$","");
	if (replicates()>1 && theSpecies.endsWith("_" + (replicates(species)-1))) {
	    try { 
		replicateHtmlPage(species, baseFeatures);
	    }
	    catch (IOException e) { 
		Utils.warn2("Error making replicate summary for " + species + ": check maxent.log file for details", "replicateError");
		//		popupError("Error processing replicated species outputs", e); 
		Utils.logError("Error processing replicated species outputs", e); 
		return;
	    }
	}
    }

    void makeReplicateGridsAndPics(final String species, final String project, String projdir) throws IOException {
	final String[] suffix = new String[] {"avg", "stddev", "min", "max", "median", "lowerci"};
	final String[] link = new String[suffix.length];
	final String outprefix = species+(project==null?"":"_"+project);
	AvgStderr avgstderr = new AvgStderr(params, allLayers);
	avgstderr.process(outDir(), species, replicates(species), projdir, outprefix, new File(projdir).isFile()?".csv":outputFileType());
	if (!is("pictures") || Utils.interrupt || new File(projdir).isFile()) return;
	if (threads()>1)
	    parallelRunner.clear();
	for (int i=0; i<suffix.length; i++) {
	    final int me = i;
	    final String fn = new File(outDir(), outprefix+"_"+suffix[me]+outputFileType()).getPath();
	    Runnable task = new Runnable() {
		    public void run() {
			try {
			    link[me] = makePNG(fn, (me<2) ? null : suffix[me]);
			} catch (IOException e) {
			    popupError("Error writing file " + fn, e);
			}
		    }
		};
	    if (threads()<=1) task.run();
	    else parallelRunner.add(task, "Make PNG for " + fn);
	}
	if (threads()>1)
	    parallelRunner.runall("Replicate summary pictures", is("verbose"));
	htmlout.println("The following two pictures show the point-wise mean and standard deviation of the " + replicates(species) + (project==null?" output grids" : " models applied to the environmental layers in " + project) + ".  Other available summary grids are " + link[2] + ", " + link[3] + (link[5]==null?" and ":", ") + link[4] + (!avgstderr.wroteLowerci()?"":" and 95% confidence level ("+link[5]+")") + ".<br><br>");
	htmlout.println(link[0]);
	htmlout.println(link[1]);
	htmlout.println("<br>");
    }
	
	
    void replicateHtmlPage(String species, Feature[] baseFeatures) throws IOException {
	if (Utils.interrupt) return;
	results.print("Species", species + " (average)");
	htmlout = Utils.writer(outDir(), species + ".html");
	htmlout.println("<title>Replicated maxent model for " + species + "</title>");
	htmlout.println("<CENTER><H1>Replicated maxent model for " + species + "</H1></CENTER>");
	htmlout.print("<br> This page summarizes the results of " + replicates(species));
	if (cv())
	    htmlout.print("-fold cross-validation");
	else if (bootstrap()) 
	    htmlout.print(" bootstrap models");
	else htmlout.print(" split-sample models");
	htmlout.print(" for " + species + ", created " + new Date().toString() + " using Maxent version " + Utils.version + ".  The individual models are here:");
	for (int i=0; i<replicates(species); i++)
	    htmlout.print(" <a href = \"" + species+"_"+i+".html\">["+i+"]</a>");
	htmlout.println("<br>");
	if (is("plots"))
	    replicatedROCplot(species);
	if (is("pictures"))
	    htmlout.println("<br><HR><H2>Pictures of the model</H2>");
	makeReplicateGridsAndPics(species, null, environmentalLayers());
	if (Utils.interrupt) { htmlout.close(); return; }
	if (projectPrefix!=null)
	    for (int i=0; i<projectPrefix.length; i++) {
		String prefix = new File(projectPrefix[i]).getName();
		makeReplicateGridsAndPics(species, prefix, projectPrefix[i]);
	    }
	if (is("responsecurves")) 
	    replicatedProfiles(species, baseFeatures);
	writeContributions(replicatedContributions(species), "  Values shown are averages over replicate runs.");
	if (is("jackknife")) 
	    replicatedJackknife(htmlout, species, baseFeatures);
	htmlputs("<br><HR><br>Command line to repeat this species model: " + params.commandLine(species));
	htmlout.close();
	String[] fields = results.getColumnNames();
	for (int i=0; i<fields.length; i++) {
	    try {
		double val = getJackMean(results.filename(), fields[i], species);
		results.print(fields[i], val);
	    } catch (Exception e) {};
	}
	results.println();
    }

    double[][] replicatedContributions(String species) throws IOException {
	results.close();
	String res = results.filename();
	double[] rcont = new double[params.layers.length];
	double[] permcont = new double[params.layers.length];
	for (int i=0; i<rcont.length; i++) {
	    rcont[i] = getJackMean(res, params.layers[i] + " contribution", species);
	    permcont[i] = getJackMean(res, params.layers[i] + " permutation importance", species);
	}
	results.reopen();
	return new double[][] { rcont, permcont };
    }

    double getJackMean(String file, String field, String species) throws IOException {
	int num = replicates(species);
	String errstart = "Error calculating replicate summary: ";
	double[] vals = getDoubleCol(file, field);
	String[] sp = Csv.getCol(file, "Species");
	if (sp.length<1) {
	    Utils.warn2(errstart+"file " + file + " missing field \"Species\"", "jackMeanMissingSpecies");
	    return 0.0;
	}
	if (vals.length < sp.length) {
	    Utils.warn2(errstart+"file " + file + " missing values for field \"" + field + "\"", "jackMeanMissingField");
	    return 0.0;
	}
	double sum=0.0;
	int cnt=0;
	for (int i=0; i<sp.length; i++)
	    if (sp[i].startsWith(species+"_")) {
		sum += vals[i];
		cnt++;
	    }
	if (cnt!=num) {
	    Utils.warn2(errstart+"wrong number of values for \"" + field + "\" for replicates of " + species + " in " + file + ": found " + cnt + ", needed " + num, "jackmeannumvalues");
	    return 0.0;
	}
	return sum/num;
    }

    void replicatedJackknife(PrintWriter htmlout, String species, Feature[] baseFeatures) throws IOException {
	int nf = baseFeatures.length;
	results.close();
	String res = results.filename();
	double[] gain = new double[nf*2];
	double[] testgain = new double[nf*2];
	double[] auc = new double[nf*2];
	boolean hastest = new Csv(res).hasField("Test gain without " + baseFeatures[0].name);
	for (int f=0; f<nf; f++) {
	    String v = baseFeatures[f].name;
	    gain[f] = getJackMean(res, "Training gain without " + v, species);
	    gain[f+nf] = getJackMean(res, "Training gain with only " + v, species);
	    if (hastest) {
		testgain[f] = getJackMean(res, "Test gain without " + v, species);
		testgain[f+nf] = getJackMean(res, "Test gain with only " + v, species);
		auc[f] = getJackMean(res, "AUC without " + v, species);
		auc[f+nf] = getJackMean(res, "AUC with only " + v, species);
	    }
	}
	double allGain = getJackMean(res, "Regularized training gain", species);
	double allTestGain=0.0, allauc=0.0;
	if (hastest) {
	    allTestGain = getJackMean(res, "Test gain", species);
	    allauc = getJackMean(res, "Test AUC", species);
	}
	makeJackknifePlots(htmlout, species, gain, testgain, auc, baseFeatures, allGain, allTestGain, allauc, hastest, "  Values shown are averages over replicate runs.");
	results.reopen();
    }


    void replicatedProfiles(String species, Feature[] baseFeatures) throws IOException {
	int nr = replicates(species), nf = baseFeatures.length;
	int ni = 1001;  // number of interpolated points
	double[][] x = new double[nr][];
	double[][] y = new double[nr][];
	String plotdir = new File(outDir(), "plots").getPath();
	boolean exponent = is("responseCurvesExponent");
	Utils.reportDoing(species + " average response curves");
	htmlout.println("<br><HR><H2>Response curves</H2>");
	htmlout.println("<br>These curves show how each environmental variable affects the Maxent prediction.");
	if (exponent)
	    htmlout.println("The (raw) Maxent model has the form exp(...)/constant, and the");
	else htmlout.println("The ");
	htmlout.println("curves show how the " + (exponent?"exponent":"predicted probability of presence") + " changes as each environmental variable is varied, keeping all other environmental variables at their average sample value. Click on a response curve to see a larger version.  Note that the curves can be hard to interpret if you have strongly correlated variables, as the model may depend on the correlations in ways that are not evident in the curves.  In other words, the curves show the marginal effect of changing exactly one variable, whereas the model may take advantage of sets of variables changing together.  The curves show the mean response of the " + nr + " replicate Maxent runs (red) and and the mean +/- one standard deviation (blue, two shades for categorical variables).<br><br>");
	for (int version=0; version<2; version++) {
	    if (version==1)
		htmlout.println("<br><br>In contrast to the above marginal response curves, each of the following curves represents a different model, namely, a Maxent model created using only the corresponding variable.  These plots reflect the dependence of predicted suitability both on the selected variable and on dependencies induced by correlations between the selected variable and other variables.  They may be easier to interpret if there are strong correlations between variables.<br><br>");
	    for (int f=0; f<nf; f++) {
		Utils.reportProgress((f+version*nf)*100/(double) (2*nf));
		String varname = baseFeatures[f].name;
		for (int r=0; r<nr; r++) {
		    String filename = new File(plotdir, species+"_"+r+"_"+varname+(version==1?"_only":"")+".dat").getPath();
		    x[r] = getDoubleCol(filename, "x");
		    y[r] = getDoubleCol(filename, "y");
		}
		double min = x[0][0], max=x[0][x[0].length-1];
		for (int r=1; r<nr; r++) {
		    if (x[r][0] < min) min = x[r][0];
		    if (x[r][x[r].length-1] > max) max = x[r][x[r].length-1];
		}
		boolean iscategorical = baseFeatures[f].type()==Feature.L_CAT;
		double[] xx = iscategorical ?
		    unionCategories(x) :
		    interpolate(min, max, ni);
		double[][] vals = iscategorical ? 
		    interpolateCatValues(x, y, xx) :
		    interpolateValues(x, y, ni, min, max);
		double[] mean = new double[xx.length], stderr = new double[xx.length];
		for (int i=0; i<xx.length; i++) {
		    mean[i] = AvgStderr.mean(vals[i]);
		    stderr[i] = AvgStderr.stderr(vals[i]);
		}
		String plotfilename = new File(plotdir, species + "_" + varname + (version==1?"_only":"")).getPath();
		new ResponsePlot().makeplot(xx, mean, stderr, iscategorical, varname,
					    (exponent?"Log response":"Response") + " of " + species + " to " + varname,
					    exponent?"Log Contribution to Raw Prediction":(params.getString("outputformat")+" output"),
					    plotfilename, min+(max-min)/12, max-(max-min)/12, params, exponent, is("writePlotData"));
		String fname = new File(plotfilename).getName();
		htmlout.println("<a href = \"plots/" + fname + ".png\"> <img src=\"plots/" + fname + "_thumb.png\"></a>");
	    }
	}
    }

    double[] interpolate(double min, double max, int ni) {
	double[] result = new double[ni];
	for (int i=0; i<ni; i++)
	    result[i] = min + i*(max-min)/(ni-1);
	return result;
    }


    double[] getDoubleCol(String filename, String field) throws IOException {
	return Csv.getDoubleCol(filename, field);
    }

    double[] getDoubleCol(String filename, String field, double start, double end) throws IOException {
	ArrayList a = new ArrayList();
	a.add(start+"");
	Csv.getCol(filename, field, a);
	a.add(end+"");
	return Csv.aToDoubles(a);
    }

    // plots evenly spaced interpolations for calculating mean, stddev
    void interpolateColsPlot(double[][] indices, double[][] values, String title, String xlab, String ylab, File outFile, String[] legend) throws IOException {
	interpolateColsPlot(indices, new double[][][] { values }, title, xlab, ylab, outFile, legend);
    }
    void interpolateColsPlot(double[][] indices, double[][][] values, String title, String xlab, String ylab, File outFile, String[] legend) throws IOException {
	int ni = 1001;  // number of interpolated points
	double min = indices[0][0], max = indices[0][indices[0].length-1];
	MyPlot plot = new MyPlot();
	plot.setSize(700,450);
	plot.setTitle(title);
	plot.setXLabel(xlab);
	plot.setYLabel(ylab);
	int nv = values.length;
	Color[] colors = plot.getColors();
	if (nv==2)
	    plot.setColors(new Color[] {colors[0], colors[1], colors[2], colors[4], Color.black});
	else if (nv==1)
	    plot.setColors(new Color[] {colors[0], colors[1], Color.black});
	for (int var=0; var<nv; var++) {
	    double[][] vals = interpolateValues(indices, values[var], ni, min, max);
	    double[] x = interpolate(min, max, ni);
	    for (int i=0; i<ni; i++) {
		double mean = AvgStderr.mean(vals[i]);
		double stderr = AvgStderr.stderr(vals[i]);
		plot.addPoint(var*2, x[i], mean, true);
		plot.addPoint(var*2+1, x[i], (mean<stderr) ? 0 : mean-stderr, true);
		plot.addPoint(var*2+1, x[i], (mean>1-stderr) ? 1 : mean+stderr, true);
	    }
	}
	plot.addPoint(nv*2, min, values[nv-1][0][0], true);
	plot.addPoint(nv*2, max, values[nv-1][0][values[nv-1][0].length-1], true);
	for (int i=0; i<legend.length; i++)
	    if (legend[i]!=null)
		plot.addLegend(i, legend[i]);
	ImageIO.write(plot.exportImage(), "png", outFile);
	if (nv==2 || nv==1)
	    plot.setColors(colors);  // need to reset, as ptplot has static colors
    }

    double[] unionCategories(double[][] indices) {
	int nr = indices.length;
	HashSet s = new HashSet();
	for (int r=0; r<nr; r++)
	    for (int c=0; c<indices[r].length; c++)
		s.add(new Double(indices[r][c]));
	Double[] cats = (Double[]) s.toArray(new Double[0]);
	double[] categories = new double[cats.length];
	for (int c=0; c<cats.length; c++)
	    categories[c] = cats[c].doubleValue();
	Arrays.sort(categories);
	return categories;
    }

    double[][] interpolateCatValues(double[][] indices, double[][] values, double[] categories) {
	int nr = indices.length, nc = categories.length;
	double[][] result = new double[nc][nr];
	for (int r=0; r<nr; r++) {
	    int cnt = 0;
	    for (int c=0; c<nc; c++) {
		while (indices[r][cnt] < categories[c]) cnt++;
		result[c][r] = values[r][cnt];
	    }
	}
	return result;
    }
	
    double[][] interpolateValues(double[][] indices, double[][] values, int ni, double min, double max) {
	int nr = indices.length;
	int[] current = new int[nr];
	double[][] result = new double[ni][nr];
	for (int i=0; i<ni; i++) {
	    double x = min + i * (max - min)/(ni-1);
	    for (int r = 0; r<nr; r++) {
		for ( ; current[r] < indices[r].length-1; ) {
		    if ((min<max && indices[r][current[r]] < x) ||
			(min>max && indices[r][current[r]] > x))
			current[r]++;
		    else break;
		}
		if (current[r]==0)
		    result[i][r] = values[r][0];
		else if (i==ni-1 || current[r] >= indices[r].length)
		    result[i][r] = values[r][indices[r].length-1];
		else if (indices[r][current[r]-1] == x)
		    result[i][r] = values[r][current[r]-1];
		else 
		    result[i][r] = values[r][current[r]-1] + (x-indices[r][current[r]-1]) / (indices[r][current[r]] - indices[r][current[r]-1]) * (values[r][current[r]] - values[r][current[r]-1]);
	    }
	}
	return result;
    }

    void replicatedROCplot(String species) throws IOException {
	int nr = replicates(species);
	double[][] cumulative = new double[nr][];
	double[][] area = new double[nr][];
	double[][] omission = new double[nr][];
	double[][] sensitivity = new double[nr][];
	String omissionType = (bootstrap() || (!cv() && params.getint("randomTestPoints")==0)) ? "Training" : "Test";
	for (int i=0; i<nr; i++) {
	    String filename = new File(outDir(), species+"_"+i+"_omission.csv").getPath();
	    cumulative[i] = getDoubleCol(filename, "Corresponding cumulative value", 0, 100);
	    area[i] = getDoubleCol(filename, "Fractional area", 1, 0);
	    omission[i] = getDoubleCol(filename, omissionType + " omission", 0, 1);
	    sensitivity[i] = new double[omission[i].length];
	    for (int j=0; j<omission[i].length; j++)
		sensitivity[i][j] = 1 - omission[i][j];
	}
	// now calculate the average and SE of AUC
	double[] aucs = new double[nr];
	for (int i=0; i<nr; i++) {
	    File file = new File(outDir(), species+"_"+i+".html");
	    BufferedReader in = new BufferedReader(new FileReader(file));
	    while (true) {
		String line = in.readLine();
		if (line==null) break;
		if (omissionType.equals("Test")) {
		    if (line.startsWith("Test AUC is ")) {
			aucs[i] = Double.parseDouble(line.replaceAll("Test AUC is ","").replaceAll(",.*",""));
			break;
		    }
		} else {
		    if (line.indexOf(", training AUC is") != -1) {
			aucs[i] = Double.parseDouble(line.replaceAll(".*, training AUC is ", "").replaceAll(",.*",""));
		    }
		}
	    }
	    in.close();
	}
	double meanauc = AvgStderr.mean(aucs);
	String plotdir = new File(outDir(), "plots").getPath();
	File outFile = new File(plotdir, species + "_roc.png");
	interpolateColsPlot(area, sensitivity, "Average Sensitivity vs. 1 - Specificity for " + species, "1 - Specificity (Fractional Predicted Area)", "Sensitivity (1 - Omission Rate)", outFile, new String[] {"Mean (AUC = " + nf.format(meanauc) + ")","Mean +/- one stddev", "Random Prediction"});
	outFile = new File(plotdir, species + "_omission.png");
	interpolateColsPlot(cumulative, new double[][][] {area, omission}, "Average Omission and Predicted Area for " + species, "Cumulative threshold", "Fractional value", outFile, new String[] {"Mean area", "Mean area +/- one stddev", "Mean omission on " + omissionType.toLowerCase() + " data", "Mean omission +- one stddev", "Predicted omission"});
	htmlout.println("<br><HR><H2>Analysis of omission/commission</H2>");
	htmlout.print("The following picture shows the " + omissionType.toLowerCase() + " omission rate and predicted area as a function of the cumulative threshold, averaged over the replicate runs.");
	if (omissionType.equals("Test"))
	    htmlout.println("  The omission rate should be close to the predicted omission, because of the definition of the cumulative threshold.");
	else htmlout.println("");
	htmlout.println("<br><img src=\"" + (new File("plots", species+"_omission.png").getPath()) + "\"><br>");
	htmlout.print("<br> The next picture is the receiver operating characteristic (ROC) curve for the same data, again averaged over the replicate runs.  Note that the specificity is defined using predicted area, rather than true commission (see the paper by Phillips, Anderson and Schapire cited on the help page for discussion of what this means).  ");
	htmlout.println("The average " + omissionType.toLowerCase() + " AUC for the replicate runs is " + nf.format(meanauc) + ", and the standard deviation is " + nf.format(AvgStderr.stderr(aucs)) + ".");
	htmlout.println("<br><img src=\"" + (new File("plots", species+"_roc.png").getPath()) + "\"><br>");
    }


    PrintWriter htmlout;
    void startHtmlPage() {
	try { htmlout = Utils.writer(outDir(), theSpecies + ".html"); }
	catch (IOException e) { 
	    popupError("Can't save html file for " + theSpecies + " in " + outDir(), e);
	    return;
	}
	
	htmlout.println("<title>Maxent model for " + theSpecies + "</title>");
	htmlout.println("<CENTER><H1>Maxent model for " + theSpecies + "</H1></CENTER>");
	htmlout.println("<br> This page contains some analysis of the Maxent model for " + theSpecies + ", created " + new Date().toString() + " using Maxent version " + Utils.version + ".  If you would like to do further analyses, the raw data used here is linked to at the end of this page.<br>");
    }

    void htmlputs() { htmlputs(""); }
    void htmlputs(String s) { htmlout.println(s + "<br>"); }
    void htmlputsn(String s) { htmlout.print(s); }
    void htmlputs(String head, String[] s) {
	htmlputsn(head + ":");
	for (int i=0; i<s.length; i++) htmlputsn(" " + s[i]);
	htmlputs();
    }

    void writeHtmlDetails(MaxentRunResults res, double testGain, double auc, double aucSD, double trainauc) {
	double gain = res.gain;
	int iters = res.iterations;
	FeaturedSpace X = res.X;
	int time = (int) Math.round(res.time);
        htmlputs("<br><HR><H2>Raw data outputs and control parameters</H2>");
	htmlputs("The data used in the above analysis is contained in the next links.  Please see the Help button for more information on these.");
	if (writtenGrid!=null) {
	    String name = new File(writtenGrid).getName();
	    htmlputs("<a href = \"" + name + "\">The model applied to the training environmental layers</a>");
	}
	if (projectedGrids.size() > 0)
	    for (int i=0; i<projectedGrids.size(); i++)
		htmlputs((String) projectedGrids.get(i));
  	htmlputs("<a href = \"" + theSpecies + ".lambdas\">The coefficients of the model</a>");
  	htmlputs("<a href = \"" + theSpecies + "_omission.csv\">The omission and predicted area for varying cumulative and raw thresholds</a>");
  	htmlputs("<a href = \"" + theSpecies + "_samplePredictions.csv\">The prediction strength at the training and (optionally) test presence sites</a>");
  	htmlputs("<a href = \"maxentResults.csv\">Results for all species modeled in the same Maxent run, with summary statistics and (optionally) jackknife results</a>");
	htmlputs("<br>");

	htmlputsn("Regularized training gain is " + nf.format(gain));
	htmlputsn(", training AUC is " + nf.format(trainauc));
	htmlputs(", unregularized training gain is " + nf.format((gain+X.getL1reg())) + ".");
	if (X.numTestSamples!=0) {
	    htmlputs("Unregularized test gain is " + nf.format(testGain) +".");
	    htmlputs("Test AUC is " + nf.format(auc) + ", standard deviation is " + nf.format(aucSD) + " (calculated as in DeLong, DeLong & Clarke-Pearson 1988, equation 2" + ((aucSD==-1)?"; a value of -1 indicates that only one test point was used":"")+").");
	}
	String termination = (iters < params.getint("maximumiterations")) ? "converged" : "terminated";
	htmlputs("Algorithm " + termination + " after " + iters + " iterations (" + time + " seconds).");
	htmlputs();

	htmlputs("The follow settings were used during the run:");
	htmlputsn(X.numSamples + " presence records used for training");
	if (X.numTestSamples!=0)
	    htmlputsn(", " + X.numTestSamples + " for testing");
	htmlputs(".");
	htmlputs(X.numPoints + " points used to determine the Maxent distribution (background points" + (samplesAddedToFeatures? " and presence points":"") + ").");
	boolean allcont = true;
	for (int i=0; i<params.layers.length; i++) 
	    if (params.layerTypes[i].equals("Categorical")) allcont = false;
	htmlputsn("Environmental layers used" + (allcont ? " (all continuous):" : ":"));
	for (int i=0; i<params.layers.length; i++)
	    htmlputsn(" " + params.layers[i] + (params.layerTypes[i].equals("Continuous") ? "" : "(categorical)"));
	htmlputs();
	htmlputs("Regularization values: " + regularizationConstants());
	int numSamples = X.numSamples;
	htmlputs("Feature types used", res.featureTypes);
	for (Parameter param: params.allParams())
	    if (param.changed())
		htmlputs(param.toString());
	htmlputs("Command line used: " + params.commandLine());
	htmlputs();
	if (replicates()==1)
	    htmlputs("Command line to repeat this species model: " + params.commandLine(theSpecies));
    }

    String regularizationConstants() {
	return "linear/quadratic/product: " + nf.format(beta_lqp) + ", categorical: " + nf.format(beta_cat) + ", threshold: " + nf.format(beta_thr) + ", hinge: " + nf.format(beta_hge);
    }

    void makePicture(String fileName, Sample[] ss, Sample[] ts, String dir) throws IOException {
	makePicture(fileName, ss, ts, dir, false);
    }
    void makePicture(String fileName, Sample[] ss, Sample[] ts, String dir, boolean isClampPicture) throws IOException {
	Grid g = new ShrunkGrid(new LazyGrid(fileName), 2000);
	/*
	GridIO.maxRowsAndCols = 2000;
	Grid g = GridIO.readGrid(fileName);
	int[] rsave=null, csave=null;
	if (GridIO.keepEvery!=1) {
	    rsave = new int[ss.length];
	    csave = new int[ss.length];
	    for (int i=0; i<ss.length; i++) {
		rsave[i] = ss[i].row;
		ss[i].row /= GridIO.keepEvery;
		csave[i] = ss[i].col;
		ss[i].col /= GridIO.keepEvery;
	    }
	}
	GridIO.maxRowsAndCols = -1;
	GridIO.keepEvery = 1;
	*/
	makePicture(g, fileName, ss, ts, dir, isClampPicture);
	/*
	if (GridIO.keepEvery!=1) {
	    for (int i=0; i<ss.length; i++) {
		ss[i].row = rsave[i];
		ss[i].col = csave[i];
	    }
	}
	*/
    }

    String makePNG(String fileName, String tag) throws IOException {
	if (!(new File(fileName).exists())) return null;
	Grid g = new ShrunkGrid(new LazyGrid(fileName), 2000);
	return makePNG(g, fileName, null, null, false, tag);
    }

    // returns html link
    String makePNG(Grid g, String fileName, Sample[] ss, Sample[] testSamples, boolean forcePlain, String tag) {
	String newFileName = new File(new File(outDir(), "plots"), Utils.pngname(fileName)).getPath();
	Display d = new Display(g);
	if (!is("logScale") || occurrenceProbability() || forcePlain)
	    d.setMode(Display.PLAIN);
	else if (cumulative()) {
	    d.minval = .00001;
	    d.maxval = 100.0;
	}
	if (occurrenceProbability() || forcePlain) {
	    d.minval = 0.0;
	    d.maxval = 1.0;
	}
	d.visible = false;
	d.setSamples(ss);
	d.setTestSamples(testSamples);
	d.makeLegend = true;
	Utils.reportDoing("Writing " + newFileName);
	d.makeImage();
	d.writeImage(newFileName, 1);
	int nc = d.getCols(), nr = d.getRows();
	return htmlLink("plots/"  + Utils.pngname(fileName), tag, nr, nc);
    }

    String htmlLink(String filename, String tag, int nr, int nc) {
	String restrict=null;
	if (nc > 600)
	    restrict = " width=600";
	if (nr > 600 && nr > nc)
	    restrict = " height=600";
	return("<a href = \"" + filename + "\">" + ((tag==null) ? " <img src=\"" + filename + "\"" + (restrict==null?"":restrict) + ">" : tag) + "</a>");
    }

    boolean startedPictureHtmlSection = false;
    void makePicture(Grid g, String fileName, Sample[] ss, Sample[] testSamples, String dir, boolean isClampPicture) {
	String link = 
	    makePNG(g, fileName, ss, testSamples, isClampPicture, null);
	if (!startedPictureHtmlSection) {
	    startedPictureHtmlSection = true;
	    htmlout.println("<br><HR><H2>Pictures of the model</H2>");
	}
	if (dir == null)
	    htmlout.print("This is a representation of the Maxent model for " + theSpecies + ".");
	else {
	    if (isClampPicture)
		htmlout.println("The following picture shows where the prediction is most affected by variables being outside their training range, while projecting the Maxent model onto the environmental variables in " + new File(dir).getAbsolutePath() + ".  The values shown in the picture give the absolute difference in predictions when using vs not using clamping.  (Clamping means that environmental variables and features are restricted to the range of values encountered during training.)  Warmer colors show areas where the treatment of variable values outside their training ranges is likely to have a large effect on predicted suitability.");
	    else
		htmlout.print("This is the projection of the Maxent model for " + theSpecies + " onto the environmental variables in " + dir + ".");
	}
	if (!isClampPicture)
	    htmlout.println("  Warmer colors show areas with better predicted conditions.  White dots show the presence locations used for training, while violet dots show test locations.  Click on the image for a full-size version.<br>");
	htmlout.println("<br>" + link+"<br>");
	
    }

    boolean hasAllData(Sample s, Feature[] f) {
	for (int j=0; j<f.length; j++)
	    if (!f[j].hasData(s)) return false;
	return true;
    }
	
    Sample[] withAllData(Feature[] f, Sample[] ss) {
	ArrayList result = new ArrayList();
	for (int i=0; i<ss.length; i++)
	    if (hasAllData(ss[i], f)) result.add(ss[i]);
	return (Sample[]) result.toArray(new Sample[0]);
    }
	
    Feature[] featuresWithSamples(Feature[] f, Sample[] ss) {
	/*
	ArrayList withAllData = new ArrayList();
	for (int i=0; i<ss.length; i++)
	    if (hasAllData(ss[i], f)) withAllData.add(ss[i]);
	Sample[] wad = (Sample[]) withAllData.toArray(new Sample[0]);
	//	Sample[] toAdd = samplesToAddToFeatures(f, ss);
	Feature[] result = new Feature[f.length];
	for (int j=0; j<f.length; j++)
	    result[j] = new FeatureWithSamplesAsPoints(f[j], wad);
	//	    result[j] = new FeatureWithSamplesAsPoints(f[j], toAdd);
	return result;
	*/

	Feature[] result = new Feature[f.length];
	double[] rnd = new double[f.length];
	for (int j=0; j<rnd.length; j++)
	    rnd[j] = Utils.generator.nextDouble();
	double[] backgroundHash = new double[f[0].n];
	for (int i=0; i<f[0].n; i++)
	    for (int j=0; j<f.length; j++) 
		backgroundHash[i] += rnd[j] * f[j].eval(i);
	Arrays.sort(backgroundHash);
	ArrayList samplesToAdda = new ArrayList();
	for (int i=0; i<ss.length; i++) {
	    double r = 0.0;
	    for (int j=0; j<f.length; j++)
		if (f[j].hasData(ss[i]))
		    r += rnd[j] * f[j].eval(ss[i]);
	    if (is("addAllSamplesToBackground") || Arrays.binarySearch(backgroundHash, r) < 0)  // not found
		samplesToAdda.add(ss[i]);
	}
	if (samplesToAdda.size() == 0) return f;
	Sample[] sss = (Sample[]) samplesToAdda.toArray(new Sample[0]);
	for (int j=0; j<f.length; j++) {
	    ArrayList a = new ArrayList();
	    for (int i=0; i<sss.length; i++)
		if (f[j].hasData(sss[i])) a.add(new Double(f[j].eval(sss[i])));
	    if (a.size()==0) {
		Utils.warn2("Species " + theSpecies + " missing all data for " + f[j].name + ", skipping", "skippingBecauseNoData");
		return null;
	    }
	    for (int i=0; i<sss.length; i++) {
		if (!f[j].hasData(sss[i])) {
		    int rndi = Utils.generator.nextInt(a.size());
		    sss[i].featureMap.put(f[j].name, a.get(rndi));
		}
	    }
	    result[j] = new FeatureWithSamplesAsPoints(f[j], sss);
	}
	return result;
    }

    void createProfiles(final Feature[] baseFeatures, String lambdafile, final Sample[] ss) throws IOException {
	createProfiles(lambdafile, baseFeatures, ss, null);
	if (!(is("linear") || is("quadratic") || is("threshold") || is("hinge"))) {
	    Utils.echoln("Skipping 1-var response curves, as only product features are in use");
	    return;
	}
	htmlout.println("<br>In contrast to the above marginal response curves, each of the following curves represents a different model, namely, a Maxent model created using only the corresponding variable.  These plots reflect the dependence of predicted suitability both on the selected variable and on dependencies induced by correlations between the selected variable and other variables.  They may be easier to interpret if there are strong correlations between variables.<br><br>");
	if (threads()>1)
	    parallelRunner.clear();
	for (int i=0; i<baseFeatures.length; i++) {
	    if (!isTrueBaseFeature(baseFeatures[i])) continue;
	    final Feature f = baseFeatures[i];
	    String myname = "Response curve: only " + f.name;
	    Utils.echoln(myname);
	    Runnable task = new Runnable() {
		    public void run() {
			oneVarResponseRun(baseFeatures, ss, f);
		    }
		};
	    String fname = theSpecies + "_" + f.name + "_only";
	    htmlout.println("<a href = \"plots/" + fname + ".png\"> <img src=\"plots/" + fname + "_thumb.png\"></a>");
	    if (threads()<=1) task.run();
	    else parallelRunner.add(task, myname);
	}
	if (threads()>1)
	    parallelRunner.runall("1-var response curves", is("verbose"));
	htmlout.println("<br>");
    }

    Feature[] onlyOneFeature(Feature[] baseFeatures, Feature feature) {
	ArrayList<Feature> onlya = new ArrayList();
	onlya.add(feature);
	for (int i=0; i<baseFeatures.length; i++)
	    if (!isTrueBaseFeature(baseFeatures[i])) 
		onlya.add(baseFeatures[i]);
	return onlya.toArray(new Feature[0]);
    }
	
    void oneVarResponseRun(Feature[] baseFeatures, Sample[] ss, Feature f) {
	Feature[] only = onlyOneFeature(baseFeatures, f);
	Feature[] features = makeFeatures(only);
	if (Utils.interrupt) return;
	Utils.reportDoing(theSpecies + " " + f.name + ": ");
	final MaxentRunResults res = maxentRun(features, ss, new Sample[0]);
	if (res!=null) {
	    Utils.echoln("Resulting gain: " + res.gain);
	    res.removeBiasDistribution();
	    try {
		String lambdas = res.X.writeFeatureWeights();
		//		Feature[] baseFeature = new Feature[] { f };
		double[][] raw2cum = cumulative() ? 
		    writeCumulativeIndex(res.X.getWeights(), null, res.X, -1, -1, features, -1) : null;
		createProfiles(lambdas, only, null, raw2cum);
	    } catch (IOException e) {
		popupError("Error writing response curve for " + theSpecies + " " + f.name, e);
		return;
	    }
	}
    }	

    double[] sampleAverages(Feature[] baseFeatures, Sample[] samples, double[][] categories, boolean[] isCategorical) {
	double[] result = new double[baseFeatures.length];
	for (int i=0; i<baseFeatures.length; i++) {
	    boolean iscat = baseFeatures[i].type()==Feature.L_CAT;
	    if (isCategorical!=null) isCategorical[i] = iscat;
	    if (iscat) {
		HashSet s = new HashSet();
		for (int j=0; j<baseFeatures[i].n; j++)
		    s.add(new Double(baseFeatures[i].eval(j)));
		Double[] cats = (Double[]) s.toArray(new Double[0]);
		double[] allcats = new double[cats.length];
		for (int j=0; j<cats.length; j++)
		    allcats[j] = cats[j].doubleValue();
		Arrays.sort(allcats);
		if (categories!=null)
		    categories[i] = allcats;
		int[] cnt = new int[allcats.length];
		for (int j=0; j<samples.length; j++)
		    if (baseFeatures[i].hasData(samples[j])) {
			double val = baseFeatures[i].eval(samples[j]);
			int k = Arrays.binarySearch(allcats, val);
			if (k>=0)
			    cnt[k]++;
		    }
		int max = 0, maxj=0;
		for (int j=0; j<cnt.length; j++)
		    if (cnt[j] > max) {
			max = cnt[j];
			maxj = j;
		    }
		result[i] = allcats[maxj];
	    }
	    else {
		double sum=0;
		int cnt=0;
		for (int j=0; j<samples.length; j++)
		    if (baseFeatures[i].hasData(samples[j])) {
			sum += baseFeatures[i].eval(samples[j]);
			cnt++;
		    }
		result[i] = (cnt==0) ? 0.0 : sum/cnt;
	    }
	}
	return result;
    }

    void writeSampleAverages(Feature[] baseFeatures, Sample[] samples) throws IOException {
	boolean[] isCategorical = new boolean[baseFeatures.length];
	double[] averages = sampleAverages(baseFeatures, samples, null, isCategorical);
	PrintWriter avgout = Utils.writer(outDir(), theSpecies + "_sampleAverages.csv");
	avgout.println("Predictor variable,Categorical,Sample average");
	for (int i=0; i<baseFeatures.length; i++)
	    avgout.println(baseFeatures[i].name + "," + isCategorical[i] + "," + averages[i]);
	avgout.close();
    }


    void createProfiles(String lambdafile, Feature[] baseFeatures, Sample[] samples, double[][] raw2cum) throws IOException {
	boolean oneVarProfile = (samples==null);
	boolean exponent = is("responseCurvesExponent");
	if (oneVarProfile) samples = new Sample[0];
	Utils.reportDoing(theSpecies + " response curves");
	final HashMap map = new HashMap();
	boolean[] isCategorical = new boolean[baseFeatures.length];
	double[][] categories = new double[baseFeatures.length][];
	double[] averages = sampleAverages(baseFeatures, samples, categories, isCategorical);
	for (int i=0; i<baseFeatures.length; i++)
	    map.put(baseFeatures[i].name, new Double(averages[i]));

	//	final Sample sample = new Sample(0,0,0,0,0,"",map);
	Project proj = new Project(params);
	proj.mapping = true;
	proj.varmap = map;
	proj.exponent = exponent;
	proj.raw2cum = raw2cum;
	Grid projgrid = proj.projectGrid(lambdafile, null)[0];
	final String plotdir = new File(outDir(), "plots").getPath();
	try { new File(plotdir).mkdir(); }
	catch (SecurityException e) {
	    popupError("Can't create directory in " + outDir() + " for plots", e);
	    return;
	}
	PrintWriter out = htmlout;
	if (!oneVarProfile) {
	    out.println("<br><HR><H2>Response curves</H2>");
	    out.println("<br>These curves show how each environmental variable affects the Maxent prediction.");
	    if (exponent)
		out.println("The (raw) Maxent model has the form exp(...)/constant, and the");
	    else out.println("The ");
	    out.println("curves show how the " + (exponent?"exponent":"predicted probability of presence") + " changes as each environmental variable is varied, keeping all other environmental variables at their average sample value. Click on a response curve to see a larger version.  Note that the curves can be hard to interpret if you have strongly correlated variables, as the model may depend on the correlations in ways that are not evident in the curves.  In other words, the curves show the marginal effect of changing exactly one variable, whereas the model may take advantage of sets of variables changing together.<br><br>");
	}
	for (int i=0; i<baseFeatures.length; i++) {
	    if (!isTrueBaseFeature(baseFeatures[i])) 
		continue;
	    Utils.reportProgress(i*100/(double) baseFeatures.length);
	    String name = baseFeatures[i].name;
	    double min=0, max=0;
	    if (!isCategorical[i])
		for (int j=0; j<baseFeatures[i].n; j++) {
		    if (j==0 || baseFeatures[i].eval(j) < min) min = baseFeatures[i].eval(j);
		    if (j==0 || baseFeatures[i].eval(j) > max) max = baseFeatures[i].eval(j);
		}
	    double[][] rpd = responsePlotData(projgrid, map, name, min, max, isCategorical[i] ? categories[i] : null, exponent);

	    String plotfilename = new File(plotdir, theSpecies + "_" + name + (oneVarProfile?"_only":"")).getPath();
	    try {
		new ResponsePlot().makeplot(rpd[0], rpd[1], null, isCategorical[i], name,
					    (exponent?"Log response":"Response") + " of " + theSpecies + " to " + name,
					    exponent?"Log Contribution to Raw Prediction":(params.getString("outputformat")+" output"),
					    plotfilename, min, max, params, exponent,
					    is("writePlotData") || (replicates()>1));
	    } catch (IOException e) {
		popupError("Error plotting response curve", e);
	    }

	    String fname = new File(plotfilename).getName();
	    if (!oneVarProfile)
		out.println("<a href = \"plots/" + fname + ".png\"> <img src=\"plots/" + fname + "_thumb.png\"></a>");
	}
	if (!oneVarProfile) 
	    out.println("<br>");
    }

    static double[][] responsePlotData(Grid projgrid, HashMap map, String var, double min, double max, double[] categories, boolean exponent) {
	Object savedMean = map.get(var);
	double[] x;
	double minx = min - (max-min)/10, maxx = max + (max-min)/10;
	if (categories!=null)
	    x = categories;
	else {
	    if (min==max) { minx = min-0.1; maxx = max+0.1; }
	    int num=501;
	    x = new double[num];
	    for (int j=0; j<num; j++)
		x[j] = minx+j*(maxx-minx)/(num-1);
	}
	double[] y = new double[x.length];
	map.put(var, new Double(categories!=null ? categories[0]-1 // less than all
				 : minx));
	double zeropoint = projgrid.eval(0,0);
	for (int j=0; j<x.length; j++) {
	    map.put(var, new Double(x[j]));
	    y[j] = projgrid.eval(0,0) - (exponent?zeropoint:0.0);
	}
	map.put(var, savedMean);
	return new double[][] { x, y };
    }

    static void dump(HashMap map) {
	for (Object o: map.keySet())
	    System.out.println("Map " + o + ":" + map.get(o));
    }

    void leaveOneOutRun(Feature[] baseFeatures, Sample[] ss, Sample[] testSamples, int me, double[] gain, double[] testgain, double[] auc, Feature toLeaveOut) {
	boolean hastest = testSamples!=null && testSamples.length>0;
	Feature[] leaveOneOut = new Feature[baseFeatures.length-1];
	int cnt=0;
	for (int j=0; j<baseFeatures.length; j++)
	    if (baseFeatures[j] != toLeaveOut)
		leaveOneOut[cnt++] = baseFeatures[j];
	Feature[] features = //(addSamplesToFeatures) ?
	    //	    makeFeatures(featuresWithSamples(leaveOneOut, ss)) :
	    makeFeatures(leaveOneOut);
	if (features==null) return;
	if (Utils.interrupt) return;
	Utils.reportDoing(theSpecies + " " + toLeaveOut.name + ": ");
	final MaxentRunResults res = maxentRun(features, ss, testSamples);
	if (res==null) return;
	res.removeBiasDistribution();
	gain[me] = res.gain;
	if (hastest) {
	    DoubleIterator backgroundIterator = null;
	    /*
	    if (addSamplesToFeatures)
		backgroundIterator=new DoubleIterator(baseFeatures[0].n) {
			double getNext() { return res.X.getDensity(i++); }
		    };
	    */
	    auc[me] = res.X.getAUC(backgroundIterator, testSamples);
	    if (backgroundIterator!=null)
		res.X.setDensityNormalizer(backgroundIterator);
	    testgain[me] = getTestGain(res.X);
	}
    }

    void onlyOneRun(Feature[] baseFeatures, Sample[] ss, Sample[] testSamples, int me, double[] gain, double[] testgain, double[] auc, Feature onlyfeature) {
	int num = getTrueBaseFeatures(baseFeatures).length;
	boolean hastest = testSamples!=null && testSamples.length>0;
	Feature[] only = onlyOneFeature(baseFeatures, onlyfeature);
	Feature[] features = //(addSamplesToFeatures) ?
	    //	    makeFeatures(featuresWithSamples(only, ss)) :
	    makeFeatures(only);
	if (features==null) return;
	if (Utils.interrupt) return;
	Utils.reportDoing(theSpecies + " " + onlyfeature.name + ": ");
	final MaxentRunResults res = maxentRun(features, ss, testSamples);
	if (res==null) return;
	Utils.echoln("Res.gain: " + res.gain);
	res.removeBiasDistribution();
	gain[num+me] = res.gain;
	if (hastest) {
	    DoubleIterator backgroundIterator = null;
	    /*
	    if (addSamplesToFeatures)
		backgroundIterator=new DoubleIterator(baseFeatures[0].n) {
			double getNext() { return res.X.getDensity(i++); }
		    };
	    */
	    auc[num+me] = res.X.getAUC(backgroundIterator, testSamples);
	    if (backgroundIterator!=null)
		res.X.setDensityNormalizer(backgroundIterator);
	    testgain[num+me] = getTestGain(res.X);
	}
    }

    double[][] jackknifeGain(final Feature[] baseFeatures, final Sample[] ss, final Sample[] testSamples, double allGain, double allTestGain, double allauc) {
	final Feature[] features = getTrueBaseFeatures(baseFeatures);
	int num = features.length;
	final double[] gain = new double[num*2];
	final double[] testgain = new double[num*2];
	final double[] auc = new double[num*2];
	final boolean hastest = testSamples!=null && testSamples.length>0;
	if (threads()>1)
	    parallelRunner.clear();
	for (int i=0; i<num; i++) {
	    if (Utils.interrupt) return null;
	    String myname = "Jackknife: leave " + features[i].name + " out";
	    Utils.echoln(myname);
	    final int me = i;
	    Runnable task = new Runnable() {
		    public void run() {
			leaveOneOutRun(baseFeatures, ss, testSamples, me, gain, testgain, auc, features[me]);
		    }
		};
	    if (threads()<=1) task.run();
	    else parallelRunner.add(task, myname);
	}
	for (int i=0; i<num; i++) {
	    if (Utils.interrupt) return null;
	    String myname = "Jackknife: only " + features[i].name;
	    Utils.echoln(myname);
	    final int me = i;
	    Runnable task = new Runnable() {
		    public void run() {
			onlyOneRun(baseFeatures, ss, testSamples, me, gain, testgain, auc, features[me]);
		    }
		};
	    if (threads()<=1) task.run();
	    else parallelRunner.add(task, myname);
	}
	if (threads()>1)
	    parallelRunner.runall("jackknife", is("verbose"));
	if (is("plots")) 
	    makeJackknifePlots(htmlout, theSpecies, gain, testgain, auc, features, allGain, allTestGain, allauc, hastest, "");
	if (!hastest) return new double[][] { gain };
	return new double[][] { gain, testgain, auc };
    }

    void makeJackknifePlots(PrintWriter htmlout, String theSpecies, double[] gain, double[] testgain, double[] auc, Feature[] baseFeatures, double allGain, double allTestGain, double allauc, boolean hastest, String msg) {
	int num = baseFeatures.length;
	File outfile = new File("plots", theSpecies + "_jacknife.png");
	makeJackknifePlot(gain, baseFeatures, "regularized training gain", allGain, new File(outDir(), outfile.getPath()), true, theSpecies);
	int bestonlyfeature=0, bestomitfeature=0;
	double bestonlygain = 0.0, bestomitgain = 0.0;
	for (int i=0; i<baseFeatures.length; i++) {
	    //	    if (!isTrueBaseFeature(baseFeatures[i])) continue;
	    if (i==0 || gain[i] < bestomitgain) { 
		bestomitgain = gain[i]; 
		bestomitfeature = i; 
	    }
	    if (i==0 || gain[num+i] > bestonlygain) { 
		bestonlygain = gain[num+i]; 
		bestonlyfeature = i; 
	    }
	}

	htmlout.println("The following picture shows the results of the jackknife test of variable importance.  The environmental variable with highest gain when used in isolation is " + baseFeatures[bestonlyfeature].name + ", which therefore appears to have the most useful information by itself.  The environmental variable that decreases the gain the most when it is omitted is " + baseFeatures[bestomitfeature].name + ", which therefore appears to have the most information that isn't present in the other variables." + msg + "<br>");
	htmlout.println("<br><img src=\"" + outfile.getPath() + "\"><br>");
	if (hastest) {
	    outfile = new File("plots", theSpecies + "_jacknife_test.png");
	    makeJackknifePlot(testgain, baseFeatures, "test gain", allTestGain, new File(outDir(), outfile.getPath()), true, theSpecies);
	    htmlout.println("<br>The next picture shows the same jackknife test, using test gain instead of training gain.  Note that conclusions about which variables are most important can change, now that we're looking at test data.");
	    htmlout.println("<br><img src=\"" + outfile.getPath() + "\"><br>");
	    outfile = new File("plots", theSpecies + "_jacknife_auc.png");
	    makeJackknifePlot(auc, baseFeatures, "AUC", allauc, new File(outDir(), outfile.getPath()), true, theSpecies);
	    htmlout.println("<br>Lastly, we have the same jackknife test, using AUC on test data.");
	    //For this plot, the color of \"without\" and \"only\" versions is reversed for better visibility");
	    htmlout.println("<br><img src=\"" + outfile.getPath() + "\"><br>");
	}
    }

    void makeJackknifePlot(double[] gain, Feature[] features, String what, double allGain, File outfile, boolean reverse, String theSpecies) {
	int num=0;
	for (int i=0; i<features.length; i++)
	    if (isTrueBaseFeature(features[i])) 
		num++;
	MyPlot plot = new MyPlot();
	plot.horizontal = true;
	plot.setSize(700,24*(features.length)+114);
	plot.setTitle("Jackknife of " + what + " for " + theSpecies);
	plot.setYLabel("Environmental Variable");
	plot.setXLabel(what);
	int bestonlyfeature=0, bestomitfeature=0;
	double bestonlygain = 0.0, bestomitgain = 0.0;
	int cnt=0;
	for (int i=0; i<features.length; i++) {
	    //	    System.out.println(i + " " + features[i].name + " " + gain[i] + " " + gain[num+i]);
	    if (!isTrueBaseFeature(features[i])) continue;
	    plot.addPoint(reverse?2:1,gain[i], num-cnt, false);
	    plot.addYTick(features[i].name, num-cnt);
	    plot.addPoint(reverse?1:2,gain[num+i], num-cnt, false);
	    cnt++;
	}
	plot.addPoint(0, allGain, 0, false);
	plot.setBars(0.5, 0.1);
	plot.addLegend(reverse?2:1, "Without variable");
	plot.addLegend(reverse?1:2, "With only variable");
	plot.addLegend(0, "With all variables");
	BufferedImage bi = plot.exportImage();
	try {
	    ImageIO.write(bi, "png", outfile);
	} catch (IOException e) {
	    popupError("Error writing jackknife picture", e);
	}
    }

    String recordTypeName(int t) {
	String[] typenames = new String[] { "linear", "quadratic", "product", "threshold", "hinge" };
	int[] types = new int[] { Feature.LINEAR, Feature.SQUARE, Feature.PRODUCT, Feature.THR_GEN, Feature.HINGE_GEN };
	for (int i=0; i<types.length; i++)
	    if (types[i]==t) return typenames[i];
	return null;
    }
    MaxentRunResults maxentRun(Feature[] features, Sample[] ss) {
	return maxentRun(features, ss, null);
    }
    MaxentRunResults maxentRun(Feature[] features, Sample[] ss, Sample[] testss) {
	autoSetBeta(features, ss.length);
	for (int j=0; j<features.length; j++)
	    features[j].setLambda(0.0);
	
	for (int j=0; j<features.length; j++)
	    features[j].setActive(true);
	if (is("autofeature"))
	    autoSetActive(features, ss.length);

	HashSet<String> types = new HashSet();

	for (int j=0; j<features.length; j++) {
	    int type = features[j].type();
	    // if linear features only added for clamping, deactivate them.
	    if (type == Feature.LINEAR && !is("linear"))
		features[j].setActive(false);
	    if (features[j].isActive() && recordTypeName(type)!=null)
		types.add(recordTypeName(type));
	}
	
	int cnt=0;
	for (int i=0; i<features.length; i++)
	    if (features[i].isActive())
		cnt++;
	if (cnt==0 || cnt==1 && !biasFile().equals("")) {
	    popupError("No features available: select more feature types or deselect auto features", null);
	    Utils.interrupt = true;
	    return null;
	}

	FeaturedSpace X = new FeaturedSpace(ss, features, params); //is("doClamp"));
	X.setXY(coords);
	if (is("biasIsBayesianPrior")) X.biasIsBayesianPrior=true;
	if (testss!=null)
	    X.recordTestSamples(testss);
	for (int j=0; j<features.length; j++)
	    if ((features[j].isBinary() && features[j].sampleExpectation == 0)) {
		Utils.echoln("Deactivating " + features[j].name);
		features[j].setActive(false);
	    }
	Utils.reportMemory("FeaturedSpace");
	
	Sequential alg = new Sequential(X, params);
	alg.setParallelUpdateFrequency(params.getint("parallelUpdateFrequency"));
	if (Utils.interrupt) return null;

	double gain=Math.log(X.bNumPoints()) - alg.run();
	determineContributions(X);
	if (Utils.interrupt) return null;
	return new MaxentRunResults(gain,alg.iteration,X,alg.getTime(),types.toArray(new String[0]));
    }

    void writeContributions(double[][] contributions, String msg) {
	int[] index = DoubleIndexSort.sort(contributions[0]);

	htmlputs("<br><HR><H2>Analysis of variable contributions</H2>");
	htmlputs("The following table gives estimates of relative contributions of the environmental variables to the Maxent model.  To determine the first estimate, in each iteration of the training algorithm, the increase in regularized gain is added to the contribution of the corresponding variable, or subtracted from it if the change to the absolute value of lambda is negative.  For the second estimate, for each environmental variable in turn, the values of that variable on training presence and background data are randomly permuted.  The model is reevaluated on the permuted data, and the resulting drop in training AUC is shown in the table, normalized to percentages.  As with the variable jackknife, variable contributions should be interpreted with caution when the predictor variables are correlated." + msg);
	htmlputsn("<br><table border cols=3>");
	htmlputsn("<tr><th>Variable</th><th>Percent contribution</th><th>Permutation importance</th>");
	NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
	nf.setMaximumFractionDigits(1);
	for (int i=contributions[0].length-1; i>=0; i--)
	    htmlputsn("<tr align=right><td>" + params.layers[index[i]] + "</td><td>" + nf.format(contributions[0][index[i]]) + "</td><td>" + nf.format(contributions[1][index[i]]) + "</td></tr>");
	htmlputs("</table><br>");
    }
    
    double[] contributions;
    void determineContributions(FeaturedSpace X) {
	if (contributions!=null) return;
	String[] names = params.layers;
	contributions = new double[names.length];
	for (int i=0; i<X.features.length; i++) {
	    double contrib = X.features[i].contribution;
	    if (contrib <= 0.0) continue;
	    String featureName = X.features[i].name.replaceFirst("\\(","").replaceFirst("=.*\\)","").replaceFirst("\\^2","").replaceFirst("'","").replaceFirst("`","").replaceFirst(".*<","").replaceFirst("\\)","").replaceFirst("__rev","");
	    String[] ff = featureName.split("\\*");
	    for (int k=0; k<ff.length; k++) {
		int j;
		for (j=0; j<names.length; j++)
		    if (names[j].equals(ff[k])) {
			contributions[j] += contrib / ff.length;
			break;
		    }
		if (j==names.length) 
		    Utils.echoln("Contribution not found: " + featureName);
	    }
	}
	double sum=0.0;
	for (int j=0; j<names.length; j++)
	    sum += contributions[j];
	for (int j=0; j<names.length; j++)
	    contributions[j] = (sum==0) ? 0 : contributions[j] * 100.0 / sum;
    }

    void writeLog(String head, String[] s) {
	Utils.echo(head + ":");
	for (int i=0; i<s.length; i++) Utils.echo(" " + s[i]);
	Utils.echoln();
    }
    void writeLog() {
	Utils.echoln("Command line used: " + params.commandLine());
	Utils.echoln("Command line to repeat: " + params.commandLine(null));
	writeLog("Species", params.species);
	writeLog("Layers", params.layers);
	writeLog("Layertypes", params.layerTypes);
	for (Parameter param: params.allParams())
	    if (param.changed())
		Utils.echoln(param.toString());
	//	Utils.echoln("Command line: " + params.commandLine());
	Utils.echoln();
    }

    double[][] writeCumulativeIndex(double[] weights, String raw2cumfile, FeaturedSpace X, double auc, double trainauc, Feature[] baseFeatures, double entropy) throws IOException {
	double[] origweights = (double[]) weights.clone();
	// initialize weights and indices
	Arrays.sort(weights);
	    
	double[] testvals = new double[X.numTestSamples];
	for (int i=0; i<testvals.length; i++) {
	    testvals[i] = X.getDensity(X.testSamples[i]) / X.densityNormalizer;
	    if (Double.isNaN(testvals[i]) || Double.isInfinite(testvals[i]))
		testvals[i] = 1;
	    if (testvals[i] > 1) testvals[i] = 1;
	}
	boolean hastest = (testvals.length > 0);
	ArrayList a = new ArrayList();
	for (int i=0; i<X.numSamples; i++)
	    if (hasAllData(X.samples[i], baseFeatures))
		a.add(new Double(X.getDensity(X.samples[i]) / X.densityNormalizer));
	double[] trainvals = new double[a.size()];
	for (int i=0; i<trainvals.length; i++) {
	    trainvals[i] = ((Double) a.get(i)).doubleValue();
	    if (Double.isNaN(trainvals[i]) || Double.isInfinite(trainvals[i]))
		trainvals[i] = 1;
	    if (trainvals[i] > 1) trainvals[i] = 1;
	}
	Arrays.sort(trainvals);
	int num = weights.length + trainvals.length + X.numTestSamples;
	double[] allweights = new double[num];
	int cnt=0;
	for (int i=0; i<weights.length; i++)
	    allweights[cnt++] = weights[i];
	for (int i=0; i<trainvals.length; i++)
	    allweights[cnt++] = trainvals[i];
	for (int i=0; i<testvals.length; i++)
	    allweights[cnt++] = testvals[i];
	int[] idx = DoubleIndexSort.sort(allweights);
	double[] sweights = new double[num];
	byte[] source = new byte[num];
	double sum=0;
	for (int i=0; i<num; i++) {
	    if (i<weights.length) sum+=weights[i];
	    sweights[i] = allweights[idx[i]];
	    source[i] = (idx[i] < weights.length) ? (byte) 0 : 
		(idx[i] < weights.length+a.size()) ? (byte) 1 : (byte) 2;
	}
	double[] cweights = new double[weights.length];
	double tmp=0;
	for (int i=0; i<weights.length; i++) {
	    tmp += weights[i];
	    cweights[i] = 100.0*tmp/sum;
	}

	// initialize thresholds for html
	Thresholdinfo[] thresholdinfo = new Thresholdinfo[hastest?11:9];
	cnt=0;
	int[] fixed = new int[] {1, 5, 10};
	for (int i=0; i<fixed.length; i++) {
	    initThreshold(thresholdinfo, cnt, "Fixed cumulative value " + fixed[i]);
	    thresholdinfo[cnt++].cumulative = fixed[i];
	}
	initThreshold(thresholdinfo, cnt, "Minimum training presence");
	thresholdinfo[cnt++].threshold = trainvals[0];
	initThreshold(thresholdinfo, cnt, "10 percentile training presence");
	thresholdinfo[cnt++].threshold = trainvals[trainvals.length/10];
        initThreshold(thresholdinfo, cnt++, "Equal training sensitivity and specificity");
	initThreshold(thresholdinfo, cnt++, "Maximum training sensitivity plus specificity");
	if (hastest) {
	    initThreshold(thresholdinfo, cnt++, "Equal test sensitivity and specificity");
	    initThreshold(thresholdinfo, cnt++, "Maximum test sensitivity plus specificity");
	}
	initThreshold(thresholdinfo, cnt++, "Balance training omission, predicted area and threshold value");
	initThreshold(thresholdinfo, cnt++, "Equate entropy of thresholded and original distributions");

	//initialize for ROC plots
	if (is("plots")) initPlots(hastest, X, auc, trainauc);

	// initialize thresholds for _omission.csv file
	StringWriter outstring = raw2cumfile==null ? new StringWriter() : null;
	PrintWriter out = new PrintWriter(raw2cumfile==null ?
					  outstring :
					  new FileWriter(raw2cumfile));
	out.println("Raw value,Corresponding cumulative value,Corresponding "+params.occurrenceProbabilityTransform()+" value,Fractional area,Training omission,Test omission");
	double[] thresh1 = new double[] {.000000001, .0000000025, .00000001, .000000025, .0000001, .00000025, .000001, .0000025, .00001, .000025, .00005, .0001, .00025, .0005, .001, .0025, .005, .01, .025, .05, 0.75, .1, .2, .3, .4, .5, .6, .7, .8, .9, 1, 1.1, 1.2, 1.3, 1.4, 1.5, 1.75}; // 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5};
	double[] thresh = new double[thresh1.length + 393];
	for (int i=0; i<thresh1.length; i++) thresh[i] = thresh1[i];
	cnt = thresh1.length;
	for (int i=8; i<=400; i++) thresh[cnt++] = i/4.0;
	Arrays.sort(thresh); // just in case
	int current = 0;

	// run through all weights
	int[] count = new int[3];
	double threshold = sweights[0]-1, cumulative=0.0, occurrenceProbability=0.0;
	double expent = Math.exp(entropy);
	NumberFormat tnf = NumberFormat.getNumberInstance(Locale.US);
	DecimalFormat df = (DecimalFormat)tnf;
	df.applyPattern("#.#####E0");
	NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
	nf.setGroupingUsed(false);
	nf.setMaximumFractionDigits(9);
	ArrayList<Double> rr = new ArrayList(), cc = new ArrayList();
	Project proj = new Project(params);
	for (int i=0; i<sweights.length; i++) {
	    if (sweights[i] != threshold) {
		threshold = sweights[i];
		double area = 1.0 - (count[0])/(double)weights.length;
		double trainomission = count[1]/(double) a.size();
		double testomission = hastest ? count[2]/(double) testvals.length : 0;
		cumulative = interpolate(sweights[i], count[0], weights, cweights);
		occurrenceProbability = proj.occurrenceProbability(threshold, entropy);
		double[] attrs = new double[] { area, trainomission, testomission, cumulative, occurrenceProbability, threshold };
		for (int j=0; j<fixed.length; j++)
		    recordThreshold(thresholdinfo[j], (cumulative >= thresholdinfo[j].cumulative) ? 0 : 1, attrs, false);
		for (int j=3; j<5; j++)
		    recordThreshold(thresholdinfo[j], (threshold == thresholdinfo[j].threshold) ? 0 : 1, attrs, false);
		recordThreshold(thresholdinfo[5], Math.abs(trainomission - area), attrs);
		recordThreshold(thresholdinfo[6], trainomission + area, attrs);
		recordThreshold(thresholdinfo[hastest?9:7], 6*trainomission+cumulative/25.0+area*1.6, attrs);
		recordThreshold(thresholdinfo[hastest?10:8], area < expent / weights.length ? 0 : 1, attrs, false);
		if (hastest) {
		    recordThreshold(thresholdinfo[7], Math.abs(testomission - area), attrs);
		    recordThreshold(thresholdinfo[8], testomission + area, attrs);
		}

		if (is("plots")) 
		    addPlotPoints(cumulative, area, trainomission, testomission, hastest);

		if (current < thresh.length && cumulative >= thresh[current]) {
		    out.println(quotedCsv(new String[] { df.format(sweights[i]).replaceAll(",","."), nf.format(cumulative), nf.format(occurrenceProbability), nf.format(area), nf.format(trainomission), nf.format(testomission) }));
		    rr.add(sweights[i]);
		    cc.add(cumulative);
		    while (current < thresh.length && cumulative > thresh[current])
			current++;
		}

	    }
	    count[source[i]]++;
	    while (i<sweights.length-1 && sweights[i+1] == sweights[i])
		count[source[++i]]++;
	}
	out.close();
	if (raw2cumfile==null) {
	    double[][] res = new double[2][rr.size()];
	    for (int i=0; i<rr.size(); i++) {
		res[0][i] = rr.get(i);
		res[1][i] = cc.get(i);
	    }
	    return res;
	}

	if (is("writeBackgroundPredictions")) {
	    PrintWriter bgout = Utils.writer(raw2cumfile.replaceAll("omission.csv$", "backgroundPredictions.csv"));
	    bgout.println("x,y,raw,cumulative,"+params.occurrenceProbabilityTransform());
	    for (int i=0; i<origweights.length; i++) {
		// there may be presences added at end of background, 
		// but their coords are not currently recorded in X
		if (i>=X.numCoords()) break;
		cumulative = interpolate(origweights[i], -1, weights, cweights);
		occurrenceProbability = proj.occurrenceProbability(origweights[i], entropy);
		bgout.println(quotedCsv(new String[] { ""+X.getX(i), ""+X.getY(i), df.format(origweights[i]).replaceAll(",","."), nf.format(cumulative), nf.format(occurrenceProbability) }));
	    }
	    bgout.close();
	}

	if (is("plots")) {
	    addPlotPoints(100, 0.0, 1.0, 1.0, hastest);
	    writePlots();
	}

	for (int j=0; j<fixed.length; j++)
	    thresholdinfo[j].cumulative = fixed[j];
	writeThresholds(thresholdinfo, testvals.length);

	// write the sample predictions file.  Use the cumulative index, as background does too
	File file = new File(outDir(), theSpecies + "_samplePredictions.csv");
	try {
	    double[][] raw2cum = Project.readCumulativeIndex(raw2cumfile);
	    PrintWriter sout= Utils.writer(file);
	    sout.println("X,Y,Test or train,Raw prediction,Cumulative prediction,"+params.occurrenceProbabilityTransform()+" prediction");
	    for (int i=0; i<X.numSamples; i++) {
		if (!hasAllData(X.samples[i], baseFeatures)) continue;
		// can't use trainvals[i] as array is sorted
		double raw = X.getDensity(X.samples[i]) / X.densityNormalizer;
		sout.println(X.samples[i].lon + "," + X.samples[i].lat + ",train," + raw + "," + Project.interpolateCumulative(raw2cum, raw) + "," + proj.occurrenceProbability(raw, entropy));
	    }
	    for (int i=0; i<testvals.length; i++)
		sout.println(X.testSamples[i].lon + "," + X.testSamples[i].lat + ",test," + testvals[i] + "," + Project.interpolateCumulative(raw2cum, testvals[i]) + "," + proj.occurrenceProbability(testvals[i], entropy));
	    sout.close();
	} catch (IOException e) {
	    popupError("Error writing sample predictions file", e);
	}
	return null;
    }
	
    String quote(String s) {
	if (s.indexOf(",")==-1) return s;
	return "\"" + s + "\"";
    }
    String quotedCsv(String[] s) {
	if (s==null || s.length==0) return "";
	String result = quote(s[0]);
	for (int i=1; i<s.length; i++)
	    result += "," + quote(s[i]);
	return result;
    }

    // given: x > raw[i-1], i in range 0..raw.length
    double interpolate(double x, int i, double[] raw, double[] cum) {
	if (i==-1) {
	    i = Arrays.binarySearch(raw, x);
	    if (i>=0) 
		while (i<raw.length && raw[i]==x) i++;
	    if (i<0) i = -i - 1;
	}
	if (i<raw.length && raw[i] == x) return cum[i]; // x is a raw value
	if (i==0) return 0;  // x is < all raw values
	if (i==raw.length) return cum[cum.length-1];  // x is > all raw values
	return (cum[i-1] + (x-raw[i-1]) / (raw[i] - raw[i-1]) * (cum[i] - cum[i-1]));
    }


    // Assumes a is sorted in increasing order
    double omissionrate(double[] a, double t) {
	for (int i=0; i<a.length; i++)
	    if (a[i] >= t) return i / (double) a.length;
	return 1.0;
    }

    class Thresholdinfo {
	String meaning;
	double threshold=-1, value=1, area=-1, trainomission=-1;
	double testomission=-1, cumulative=-1, occurrenceProbability = -1;
	boolean started=false;
	public Thresholdinfo(String m) { meaning = m; }
	public Thresholdinfo(String m, double t, double v, double a, double traino, double testo, double c) {
	    meaning = m; threshold=t; value=v; area=0; trainomission=traino;
	    testomission=testo; cumulative=c;
	}
    }

    void initThreshold(Thresholdinfo[] thresholdinfo, int i, String meaning) { 
	thresholdinfo[i] = new Thresholdinfo(meaning);
    }

    void recordThreshold(Thresholdinfo thr, double val, double[] attrs) { 
	recordThreshold(thr, val, attrs, true); 
    }

    int TAREA=0, TTRAINO=1, TTESTO=2, TCUM=3, TOCCPROB=4, TTHRESH=5;
    void recordThreshold(Thresholdinfo thr, double val, double[] attrs, boolean init) {
	if ((init && !thr.started) || val < thr.value) {
	    thr.value = val;
	    thr.threshold = attrs[TTHRESH];
	    thr.area = attrs[TAREA];
	    thr.trainomission = attrs[TTRAINO];
	    thr.testomission = attrs[TTESTO];
	    thr.cumulative = attrs[TCUM];
	    thr.occurrenceProbability = attrs[TOCCPROB];
	    thr.started = true;
	}
    }

    /*
    double getThreshold(Thresholdinfo[] thresholdinfo, String s) {
	for (int i=0; i<thresholdinfo.length; i++)
	    if (thresholdinfo[i].meaning.equals(s))
		return thresholdinfo[i].threshold;
	return 0.0;
    }
    */

    void writeThresholds(Thresholdinfo[] thresholdinfo, int numTestSamples) {
	boolean hastest = (numTestSamples > 0);
	htmlputs("Some common thresholds and corresponding omission rates are as follows.  If test data are available, binomial probabilities are calculated exactly if the number of test samples is at most 25, otherwise using a normal approximation to the binomial.  These are 1-sided p-values for the null hypothesis that test points are predicted no better than by a random prediction with the same fractional predicted area.  The \"Balance\" threshold minimizes 6 * training omission rate + .04 * cumulative threshold + 1.6 * fractional predicted area.");
	htmlputsn("<br><table border cols=" + (hastest?6:4) + " cellpadding=3>");
	htmlputsn("<tr><th>Cumulative threshold</th><th>"+params.occurrenceProbabilityTransform()+" threshold</th><th>Description</th><th>Fractional predicted area</th><th>Training omission rate</th>" + (hastest?"<th>Test omission rate</th><th>P-value</th>" : ""));
    
	NumberFormat tnf = NumberFormat.getNumberInstance(Locale.US);
	DecimalFormat df = (DecimalFormat)tnf;
	df.applyPattern("#.###E0");
//	DecimalFormat df = new DecimalFormat("#.###E0");
	applyThresholdValue = -1;
	for (int i=0; i<thresholdinfo.length; i++) {
	    Thresholdinfo t = thresholdinfo[i];
	    String name = t.meaning;
	    if (name.equals("Fixed cumulative value")) 
		name += " " + t.cumulative;
	    if (t.threshold == -1) {
		Utils.echoln("Leaving out " + t.meaning + " from threshold table, as uninitialized");
		for (String s: new String[] {"cumulative threshold", params.occurrenceProbabilityTransform()+" threshold", "area", "training omission"} )
		    results.print(name + " " + s, "na");
		if (hastest) 
		    for (String s: new String[] {"test omission", "binomial probability"})
		    results.print(name + " " + s, "na");
		continue;
	    }
	    double testbinomial=0.0;
	    if (hastest) {
		testbinomial = (numTestSamples > 25) ?
		    binomial(numTestSamples, t.area, 1-t.testomission) :
		    exactBinomial((int) Math.round((1-t.testomission) * numTestSamples), numTestSamples, t.area);
	    }
	    htmlputsn("<tr align=center><td>" + nf.format(t.cumulative) + "</td><td>" + nf.format(t.occurrenceProbability) + "</td><td>" + t.meaning + "</td><td>" + nf.format(t.area) + "</td><td>" + nf.format(t.trainomission) + "</td>" + (hastest?"<td>" + nf.format(t.testomission) + "</td><td>" + df.format(testbinomial) + "</td>" : ""));
	    results.print(name + " cumulative threshold", t.cumulative);
	    results.print(name + " " + params.occurrenceProbabilityTransform() + " threshold", t.occurrenceProbability);
	    results.print(name + " area", t.area);
	    results.print(name + " training omission", t.trainomission);
	    if (hastest) {
		results.print(name + " test omission", t.testomission);
		results.print(name + " binomial probability", df.format(testbinomial));
	    }
	    if (t.meaning.toLowerCase().equals(params.getString("applyThresholdRule")))
		applyThresholdValue = occurrenceProbability() ? t.occurrenceProbability : 
		    cumulative() ? t.cumulative : -1;
	}
	if (applyThresholdValue==-1 && !params.getString("applyThresholdRule").equals(""))
	    popupError("Threshold rule " + params.getString("applyThresholdRule") + " not recognized", null);
	results.resetInsertionIndex();
	htmlputs("</table>");
    }

    double exactBinomial(int success, int n, double p) { // one-sided
	double prob = 0.0;
	for (int i=success; i<=n; i++) {
	    long fac = 1;
	    int ii = (i>=n/2) ? i : n-i;
	    for (int j=ii+1; j<=n; j++) fac*=j;
	    for (int j=1; j<=n-ii; j++) fac/=j;
	    prob += fac*Math.pow(p, i)*Math.pow(1-p, n-i);
	}
	return prob;
    }

    double binomial(int n, double p, double successrate) {
	double mean = n*p, sd = Math.sqrt(n*p*(1-p));
	if (sd==0) return (n > 0 && (p!=successrate)) ? 0 : 1;
	double z = (n*successrate - mean) / sd;
	return cPhi(z);
    }
    // from http://www.jstatsoft.org/v11/i04/v11i04.pdf
    // saved in Papers/Density/otherspapers/calculatingNormalDistribution.pdf
    double cPhi(double x) {
	int i,j=(int) (.5*(Math.abs(x)+1)); 
	double R[] = new double[]
	    {1.25331413731550025, .421369229288054473, .236652382913560671,
	     .162377660896867462, .123131963257932296,.0990285964717319214,
	     .0827662865013691773,.0710695805388521071,.0622586659950261958};
	if (j>=R.length) return 0;
	double pwr=1,a=R[j],z=2*j,b=a*z-1,h=Math.abs(x)-z,s=a+h*b,t=a,q=h*h;
	for(i=2;s!=t;i+=2)
	    {
		a=(a+z*b)/i; 
		b=(b+z*a)/(i+1); 
		pwr*=q; 
		s=(t=s)+pwr*(a+h*b);
	    }
	s=s*Math.exp(-.5*x*x-.91893853320467274178);
	if(x>=0) return (double) s; 
	return (double) (1.-s);
    }

    Feature[] makeFeatures(Feature[] f1) {
	return makeFeatures(f1, is("cacheFeatures"), true);
    }
    Feature[] makeFeatures(Feature[] f1, boolean doCache, boolean doReport) { 
	int i, j, cnt=0, len=f1.length;
	ArrayList<Feature> features = new ArrayList();
	ArrayList contList = new ArrayList();
	if (doReport)
	    Utils.reportDoing("Making features");
	String[] names = new String[len];
	int[] types = new int[len];
	for (i=0; i<len; i++) {
	    names[i] = f1[i].name;
	    types[i] = f1[i].type();
	}
	for (i=0; i<len; i++)
	    switch (types[i]) {
	    case Feature.L_F_BIAS_OUT:
	    case Feature.L_DEBIAS_AVG:
		features.add(f1[i]); 
		break;
	    case Feature.L_CAT:
		Feature[] tmp = BinaryFeature.makeAll(f1[i], names[i]);
		for (j=0; j<tmp.length; j++)
		    features.add(tmp[j]);
		break;
	    case Feature.L_CONT:
		contList.add(is("doClamp") ? naturallyClamped(f1[i], names[i]) : f1[i]);
		break;
	    default:
		Utils.fatalException("makeFeatures: Cannot process feature of type "+types[i], null);
	    }
	Feature[] cont = (Feature[]) contList.toArray(new Feature[0]);
	//	if (is("linear"))   // always added, as needed for clamping
	for (i=0; i<cont.length; i++)
	    features.add(new LinearFeature(cont[i], cont[i].name)); 
	if (is("quadratic"))
	    for (i=0; i<cont.length; i++) {
		features.add(new SquareFeature(cont[i], cont[i].name)); 
		if (is("polyhedral"))
		    features.add(new PolyhedralFeature(cont[i], cont[i].name)); 
	    }
	if (is("product"))
	    for (i=0; i<cont.length; i++)
		for (j=i+1; j<cont.length; j++)
		    if (!cont[i].isMask() && !cont[j].isMask())
			features.add(new ProductFeature(cont[i], cont[i].name, cont[j], cont[j].name));
	if (is("threshold"))
	    for (i=0; i<cont.length; i++)
		features.add(new ThrGeneratorFeature(cont[i], cont[i].name)); 
	if (is("hinge"))
	    for (i=0; i<cont.length; i++) {
		features.add(new HingeGeneratorFeature(cont[i], cont[i].name)); 
		features.add(new HingeGeneratorFeature(revFeature(cont[i]), cont[i].name+"__rev")); 
	    }
	int n = features.size();
	Feature[] result = new Feature[n];
	for (i=0; i<n; i++) {
	    if (doReport)
		Utils.reportProgress(i*100/(double) n);		    
	    if (Utils.interrupt) return null;
	    Feature ff = features.get(i);
	    if (!(ff instanceof LayerFeature ||
		  ff.type() == Feature.L_F_BIAS_OUT ||
		  ff.type() == Feature.L_DEBIAS_AVG ||
		  ff instanceof BinaryFeature ||
		  ff instanceof ThrGeneratorFeature ||
		  ff instanceof HingeGeneratorFeature)) {
		ff = (is("doClamp")) ? naturallyClamped(ff, ff.name) : ff;
		ff = (doCache) ? new CachedScaledFeature(ff) : new ScaledFeature(ff);
	    }
	    result[i] = ff;
	}
	if (doReport)
	    Utils.reportMemory("makeFeatures");
	return result;
    }

    static Feature revFeature(final Feature f) {
	return new Feature(f.n, f.name+"__rev") {
		public double eval(Sample s) { return -f.eval(s); }
		public double eval(int p) { return -f.eval(p); }
		public boolean hasData(Sample s) { return f.hasData(s); }
	    };
    }

    Feature naturallyClamped(Feature f, String s) {
	double min=f.eval(0), max=f.eval(0);
	for (int i=1; i<f.n; i++) {
	    double val=f.eval(i);
	    if (val>max) max=val;
	    else if (val<min) min=val;
	}
	Feature result = new ClampedFeature(f,min,max);
	result.name = s;
	return result;
    }

    static double interpolate(int[] x, double[] y, int xx) {
	int i;
	for (i=0; i<x.length; i++)
	    if (xx <= x[i]) break;
	if (i==0)
	    return y[0];
	else if (i==x.length)
	    return y[x.length-1];
	else
	    return y[i-1] + (y[i]-y[i-1]) * (xx-x[i-1])/(x[i]-x[i-1]);
    }

    double beta_lqp, beta_thr, beta_hge, beta_cat;
    void autoSetBeta(Feature[] features, int numSamples) {
	int[]    thresholds = null;
	double[] betas = null;
	if (is("product") && (!is("autofeature") || numSamples >= params.getint("lq2lqptThreshold"))) {
	    thresholds = new int[] {   0,  10,  17,   30,  100 };
	    betas = new double[]   { 2.6, 1.6, 0.9, 0.55, 0.05 };
	}
	else if (is("quadratic") && (!is("autofeature") || numSamples >= params.getint("l2lqThreshold"))) {
	    thresholds = new int[] {   0,  10,  17,   30,  100 };
	    betas = new double[]   { 1.3, 0.8, 0.5, 0.25, 0.05 };
	}
	else { // linear
	    thresholds = new int[] {  10,  30,  100 };
	    betas = new double[]   { 1.0, 0.2, 0.05 };
	}
	beta_lqp = interpolate(thresholds, betas, numSamples);
	beta_thr = interpolate( new int[]    {  0, 100},
				new double[] {2.0, 1.0},
				numSamples);
	beta_hge = 0.5; 
	if (is("doSqrtCat")) {
	    beta_cat = interpolate( new int[]    { 10,  17,  30},
				    new double[] {.20, .10, .05},
				    numSamples);
	    beta_cat = Math.sqrt(beta_lqp*beta_cat);
	}
	else
	    beta_cat = interpolate( new int[]    {  0,  10, 17},
				    new double[] {.65, .5, .25},
				    numSamples);
	//	    beta_cat = interpolate( new int[]    {  0,  10,  17,  30, 100},
	//				    new double[] {.65, .45, .25, .15, .05},

	if (params.getdouble("beta_categorical") >= 0) beta_cat = params.getdouble("beta_categorical");
	if (params.getdouble("beta_threshold") >= 0) beta_thr = params.getdouble("beta_threshold");
	if (params.getdouble("beta_hinge") >= 0) beta_hge = params.getdouble("beta_hinge");
	if (params.getdouble("beta_lqp") >= 0) beta_lqp = params.getdouble("beta_lqp");

	for (int i=0; i<features.length; i++) {
	    if (features[i] instanceof BinaryFeature)
		features[i].setBeta(beta_cat * betaMultiplier());
	    else if (features[i] instanceof ThrGeneratorFeature)
		features[i].setBeta(beta_thr * betaMultiplier());
	    else if (features[i] instanceof HingeGeneratorFeature)
		features[i].setBeta(beta_hge * betaMultiplier());
	    else {
		features[i].setBeta(beta_lqp * betaMultiplier());
		if (params.betaMap!=null) {
		    String sval = (String) params.betaMap.get(features[i].name);
		    if (sval!=null) {
			features[i].setBeta(Double.parseDouble(sval));
			Utils.echoln("Setting beta for " + features[i].name + " to " + sval);
		    }
		}
	    }
	}
	Utils.echoln("Regularization values: " + regularizationConstants());
    }

    void autoSetActive(Feature[] features, int numSamples) {
	for (int i=0; i<features.length; i++) {
	    switch (features[i].type()) {
	    case Feature.THR_GEN:
		if (numSamples < params.getint("lq2lqptThreshold"))
		    features[i].setActive(false);
		break;
	    case Feature.HINGE_GEN:
		if (numSamples < params.getint("hingeThreshold"))
		    features[i].setActive(false);
		break;
	    case Feature.PRODUCT:
		if (numSamples < params.getint("lq2lqptThreshold"))
		    features[i].setActive(false);
		break;
	    case Feature.SQUARE:
		if (numSamples < params.getint("l2lqThreshold"))
		    features[i].setActive(false);
		break;
	    case Feature.LINEAR:
		break;
	    }
	}
    }

    void writeSummary(MaxentRunResults res, double testGain, double auc, double aucSD, double trainauc, CsvWriter writer, Feature[] baseFeatures, double[][] jackknifeGain, double entropy, double prevalence, double[] permcontribs) {
	double gain = res.gain;
	int iters = res.iterations;
	FeaturedSpace X = res.X;
	writer.print("Species", theSpecies);
        writer.print("#Training samples", X.numSamples);
	writer.print("Regularized training gain", gain);
	writer.print("Unregularized training gain", gain+X.getL1reg());
	writer.print("Iterations", iters);
	writer.print("Training AUC", trainauc);
	if (X.numTestSamples!=0) {
	    writer.print("#Test samples", X.numTestSamples);
	    writer.print("Test gain", testGain);
	    writer.print("Test AUC", auc);
	    writer.print("AUC Standard Deviation", aucSD);
	}
	writer.print("#Background points", X.numPoints);
	/*
	  if (X.testProb!=null)
	  writer.print("test RE", testRE);
	*/
	for(int i=0; i<params.layers.length; i++)
	    writer.print(params.layers[i] + " contribution", contributions[i]);
	for(int i=0; i<params.layers.length; i++)
	    writer.print(params.layers[i] + " permutation importance", permcontribs[i]);
	if (jackknifeGain!=null) {
	    String[] labels = new String[] {"Training gain", "Test gain", "AUC"};
	    for (int j=0; j<jackknifeGain.length; j++)
		if (jackknifeGain[j] != null) {
		    int ii=0;
		    for (int i=0; i<baseFeatures.length; ii++, i++)
			writer.print(labels[j] + " without " + baseFeatures[i].name, jackknifeGain[j][ii]);
		    for (int i=0; i<baseFeatures.length; ii++, i++)
			writer.print(labels[j] + " with only " + baseFeatures[i].name, jackknifeGain[j][ii]);
		}
	}
	writer.print("Entropy", entropy);
	writer.print("Prevalence (average probability of presence over background sites)", prevalence);
	writer.println();
    }

    double getTestGain(FeaturedSpace X) {
	return Math.log(X.numPointsForNormalizer) - X.getTestLoss();
	//System.out.println("X.numPointsForNormalizer " + X.numPointsForNormalizer + " X.densityNormalizer " + X.densityNormalizer + " X.linearPredictorNormalizer " + X.linearPredictorNormalizer + " X.getTestLoss() " + X.getTestLoss());
	//return Math.log(X.densityNormalizer) + X.linearPredictorNormalizer - X.getTestLoss();
    }


    /*  Make the ROC and omission / predicted area plots */

    MyPlot plot=null, plot2=null;
    void initPlots(boolean hastest, FeaturedSpace X, double auc, double trainauc) {
	plot = new MyPlot();
	plot.setSize(700,450);
	plot.setTitle("Omission and Predicted Area for " + theSpecies);
	plot.setYLabel("Fractional value");
	plot.setXLabel("Cumulative threshold");
	plot2 = new MyPlot();
	plot2.setSize(700,450);
	plot2.setTitle("Sensitivity vs. 1 - Specificity for " + theSpecies);
	plot2.setYLabel("Sensitivity  (1 - Omission Rate)");
	plot2.setXLabel("1 - Specificity  (Fractional Predicted Area)");
	plot.addPoint(0, 0.0, 1.0, true);
	plot.addPoint(1, 0.0, 0.0, true);
	if (hastest) plot.addPoint(2, 0.0, 0.0, true);
	plot2.addPoint(0, 1.0, 1.0, true);
	if (hastest) plot2.addPoint(1, 1.0, 1.0, true);

	for (int i=0; i<=100; i+=10) plot.addXTick(i+"", i);

	plot.addLegend(0, "Fraction of background predicted");
	plot.addLegend(1, "Omission on training samples");
	plot2.addLegend(0, "Training data (AUC = " + nf.format(trainauc) + ")");
	if (hastest) {
	    plot.addLegend(2, "Omission on test samples");
	    plot2.addLegend(1, "Test data (AUC = " + nf.format(auc) + ")");
	}
	int rndplt = 3;
	plot.addPoint(rndplt, 0, 0, true);
	plot.addPoint(rndplt, 100, 1.0, true);
	plot.addLegend(rndplt, "Predicted omission");
	plot2.addPoint(rndplt, 0, 0, true);
	plot2.addPoint(rndplt, 1, 1, true);
	plot2.addLegend(rndplt, "Random Prediction (AUC = 0.5)");

    }

    void addPlotPoints(double cumulative, double predarea, double trainomission, double testomission, boolean hastest) {
	plot.addPoint(0, cumulative, predarea, true);
	plot.addPoint(1, cumulative, trainomission, true);
	plot2.addPoint(0, predarea, 1-trainomission, true);
	if (hastest) {
	    plot.addPoint(2, cumulative, testomission, true);
	    plot2.addPoint(1, predarea, 1-testomission, true);
	}
    }

    void writePlots() {
	File dir = new File(outDir(), "plots");
	try { dir.mkdir(); }
	catch (SecurityException e) {
	    popupError("Can't create directory in " + outDir() + " for plots", e);
	    return;
	}
	File file1 = new File(dir, theSpecies + "_omission.png");
	File file2 = new File(dir, theSpecies + "_roc.png");
	try {
	    ImageIO.write(plot.exportImage(), "png", file1);
	} catch (IOException e) {
	    popupError("Error writing omission picture", e);
	}
	try {
	    ImageIO.write(plot2.exportImage(), "png", file2);
	} catch (IOException e) {
	    popupError("Error writing ROC picture", e);
	}
	htmlout.println("<br><HR><H2>Analysis of omission/commission</H2>");
	htmlout.println("The following picture shows the omission rate and predicted area as a function of the cumulative threshold.  The omission rate is is calculated both on the training presence records, and (if test data are used) on the test records.  The omission rate should be close to the predicted omission, because of the definition of the cumulative threshold.");
	htmlout.println("<br><img src=\"" + (new File("plots", file1.getName()).getPath()) + "\"><br>");
	htmlout.print("<br> The next picture is the receiver operating characteristic (ROC) curve for the same data.  Note that the specificity is defined using predicted area, rather than true commission (see the paper by Phillips, Anderson and Schapire cited on the help page for discussion of what this means).");
	if (is("giveMaxAUCEstimate"))
	    htmlout.print("  This implies that the maximum achievable AUC is less than 1.  If test data is drawn from the Maxent distribution itself, then the maximum possible test AUC would be " + nf.format(aucmax) + " rather than 1; in practice the test AUC may exceed this bound.");
	htmlout.println();
	htmlout.println("<br><img src=\"" + (new File("plots", file2.getName()).getPath()) + "\"><br>");
	htmlputs();
	htmlputs();
    }

}
