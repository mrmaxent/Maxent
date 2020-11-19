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

// this class has static stuff used by a variety of classes

import java.io.*;
import javax.swing.*;
import java.util.*;
import java.util.prefs.Preferences;

public class Utils {

    static String version = "3.4.4";
    static boolean interrupt=false, warnings=true;
    public static boolean verbose=false, visible=true;
    static JFrame topLevelFrame = null;
    public static Random generator;
    private static Preferences userPrefs = Preferences.userNodeForPackage(Utils.class);

    static String[] inputFileTypes = new String[] {".mxe", ".asc", ".grd", ".bil"};
    
    public static String getVersion() { return version; }

    // useful for getting non-flag args from Getopt
    public static String[] getArgs(String[] allargs, int optind) {
	String[] args = new String[allargs.length-optind];
	for (int i=0; i<args.length; i++)
	    args[i] = allargs[i+optind];
	return args;
    }

    public static String getJarfileLocation() {
	String classLocation = Utils.class.getName().replace('.', '/') 
	    + ".class";
	ClassLoader loader = Utils.class.getClassLoader();
	if (loader == null)  return null;
	String location = loader.getResource(classLocation).toString();
	String lookFor = "jar:file:";
	if (location.indexOf(lookFor) == -1) return null;
	return location.substring(location.indexOf(lookFor)+9, location.indexOf("Utils.class")-10);
    }
  
    public static FilenameFilter filenameFilter = new FilenameFilter() {
		    public boolean accept(File f, String name) {
			for (int j=0; j<inputFileTypes.length; j++)
			    if (name.toLowerCase().endsWith(inputFileTypes[j]))
				return true;
			return false;
		    }};
    public static javax.swing.filechooser.FileFilter fileFilter = new javax.swing.filechooser.FileFilter() {
	    public boolean accept(File f) {
		if (f.isDirectory()) return true;
		for (int j=0; j<inputFileTypes.length; j++)
		    if (f.getName().toLowerCase().endsWith(inputFileTypes[j]))
			return true;
		return false;
	    }
	    public String getDescription() { 
		String types = "";
		for (int i=0; i<inputFileTypes.length; i++)
		    types += (i==0?"":"/") + inputFileTypes[i];
		return types + " files"; 
	    }
	};
    
    static void applyStaticParams(Params params) {
	GridWriter.scientificPattern = params.getString("scientificPattern").toUpperCase(Locale.US);
	verbose = params.getboolean("verbose");
	warnings = params.getboolean("warnings");
	visible = params.getboolean("visible");
	DirectorySelect.prefixes = params.getboolean("prefixes");
	Grid.defaultNODATA_value = SampleSet.NODATA_value = params.getint("nodata");
	if (params.getBoolean("nceas")) SampleSet.setNCEAS_FORMAT(); 
	Display.adjustSampleRadius = params.getint("adjustsampleradius");
    }
	
    public static String getGridAbsolutePath(String dir, String gridName) {
	for (int j=0; j<inputFileTypes.length; j++) {
	    File f = new File(dir, gridName+inputFileTypes[j]);
	    if (f.exists())
		return f.getAbsolutePath();
	    f = new File(dir, gridName+inputFileTypes[j].toUpperCase(Locale.US));
	    if (f.exists())
		return f.getAbsolutePath();
	}
	popupError("Layer " + gridName + " is missing from " + dir, null);
	return null;
    }
	
    public static String fileToLayer(String s) { return fileToLayer(new File(s)); }
    public static String fileToLayer(File f) {
	String name = f.getName();
	for (int j=0; j<inputFileTypes.length; j++)
	    if (name.toLowerCase().endsWith(inputFileTypes[j]))
		return name.substring(0, name.length() - inputFileTypes[j].length());
	return name;
    }
    public static String[] gridFileNames(String dir) throws IOException {
	return gridFileNames(dir, null);
    }
    public static String[] gridFileNames(String dir, HashSet<String> keep) throws IOException {
	File[] files = new File(dir).listFiles(filenameFilter);
	ArrayList<String> a = new ArrayList();
	HashSet used = new HashSet();
	for (int i=0; i<files.length; i++) {
	    String name = fileToLayer(files[i].getName());
	    if (keep==null || keep.contains(name)) {
		a.add(files[i].getPath());
		used.add(name);
	    }
	}
	if (keep!=null && a.size() != keep.size())
	    for (String s: keep)
		if (!used.contains(s))
		    throw new IOException("Directory " + dir + " is missing environmental layer " + s);
	String[] names = (String[]) a.toArray(new String[0]);
	Arrays.sort(names);
	return names;
    }

    static PrintStream logOut = null;
    static void openLog(String dir, String logfile) throws IOException {
	if (logOut==null && logfile!=null && dir!=null)
	    logOut = new PrintStream(new FileOutputStream(new File(dir,logfile), false));
    }
    static void closeLog() { if (logOut!=null) logOut.close(); logOut=null; }
    static void echo(String s) {
	if (logOut!=null) 
	    logOut.print(s);
    }
    static long startTime, lastReportedTime;
    static void startTimer() { 
	lastReportedTime = startTime = System.currentTimeMillis();
    }

    static void echoln() { echoln(""); }
    static void echoln(String s) {
	if (logOut!=null) {
	    long time = System.currentTimeMillis();
	    if (time>lastReportedTime+1000) {
		logOut.println("Time since start: " + (time-startTime)/1000.0);
		lastReportedTime = time;
	    }
	    logOut.println(s);
	    logOut.flush();
	}
    }


    static long lastMem = 0;
    public static void reportMemory(String s) {
	//	long max = Utils.testMemory(10000000000L);  more reliable, but slow and causes thrashing
	java.lang.Runtime runtime = java.lang.Runtime.getRuntime();
	long tot = runtime.totalMemory();
	long free = runtime.freeMemory();
	long max = runtime.maxMemory();
	echoln(s + ": max memory " + max + ", total allocated " + tot + ", free " + free + ", used " + (tot-free) + ", increment " + (tot-free-lastMem));
	lastMem = tot-free;
    }

    static String protectFileName(String s) {
	return (s.indexOf(" ")==-1) ? s : ("\""+s+"\"");
    }

    static void reportDoing(String s) {
	echoln(s);
	progressMsg = s;
	lastProgress = -1.0;
	reportProgress(0.0);
	lastProgress = -1.0;
    }

    static MyProgressMonitor progressMonitor;
    static double lastProgress;
    static String progressMsg;
    static void reportProgress(double x, String suffix) { reportProgress(progressMsg+suffix, x); }
    static void reportProgress(double x) { reportProgress(progressMsg, x); }
    static void reportProgress(String s, double x) {
	if (progressMonitor!=null && (x-lastProgress) >= .005) {
	    progressMonitor.setProgress((int) x);
	    progressMonitor.setNote(s);
	    lastProgress = x;
	}
    }

    static String logError(String s, Throwable e) {
	String msg = s + (e==null ? "" : ": "+e.toString());
	echoln(msg);
	if (e!=null) {
	    if (verbose) e.printStackTrace(System.out);
	    if (logOut!=null) {
		e.printStackTrace(logOut);
		logOut.flush();
	    }
	}
	else if (logOut!=null) new Exception().printStackTrace(logOut);
	return msg;
    }

    static void popupError(String s, Throwable e) {
	System.err.println("Error: " + s); 
	String msg = logError(s, e);
	if (visible)
	    JOptionPane.showMessageDialog(topLevelFrame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void disposeProgressMonitor() {
	if (progressMonitor==null) return;
	progressMonitor.dispose(); 
	progressMonitor=null; 
    }

    static void warn(String s) { 
	System.err.println("Warning: " + s); 
	if (logOut!=null) logOut.println("Warning: " + s); 
    }

    static ArrayList suppressedWarnings = new ArrayList();
    static void warn2(String s, String warntype) {
	warn(s);
	boolean warn2doPopup = warnings && !suppressedWarnings.contains(warntype);
	if (warn2doPopup && topLevelFrame!=null) {
	    Object[] options = {"OK", "Suppress similar visual warnings"};
	    int val = JOptionPane.showOptionDialog(topLevelFrame, s, "", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
	    switch(val) {
	    case 1: suppressedWarnings.add(warntype); break;
	    }
	}
    }


    static HashMap chosenWarnOptions = new HashMap();

/*
    static void warn3(String s, String warntype, String[] options) {
	boolean warn2doPopup = warnings && !suppressedWarnings.contains(warntype);
	warn(s);
	if (warn2doPopup && topLevelFrame!=null) {
	    int val = JOptionPane.showOptionDialog(topLevelFrame, s, "", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
	    if (val<0) val=0;  // happens if user hits <esc>
	    chosenWarnOptions.put(warntype, options[val]);
	    suppressedWarnings.add(warntype);
	}
    }
*/

    public static void fatalException(String s, Throwable e) {
	System.err.println(s + (e==null ? "" : (": " + e.toString())));
	popupError(s, e);
	System.exit(1);
    }

    // got this from.  http://www.onjava.com/pub/a/onjava/2001/08/22/optimization.html
    // They don't say it, but this is a way to get around the fact that some
    // java implementations report maxMemory() incorrectly (too high)
    public static boolean testMemory(long bytesToTest)
    {
	System.gc();
	if (System.getProperties().getProperty("os.name").startsWith("Windows")) {
	    Runtime r = Runtime.getRuntime();
	    long avail = r.maxMemory() - r.totalMemory() + r.freeMemory();
	    return (avail >= bytesToTest);
	}
	int MEGABYTE = 1048576;
	int maximumMeg = (int) Math.ceil(bytesToTest / (double) MEGABYTE);
	Object[] memoryHolder = new Object[maximumMeg];
	int count = 0;
	boolean result = false;
	try {
		for (; count < memoryHolder.length; count++)
			memoryHolder[count] = new byte[MEGABYTE];
	}
	catch(OutOfMemoryError bounded){}
	result = (count == memoryHolder.length);
	memoryHolder = null;
	return result;
    }

    static public PrintWriter writer(File f, boolean append) throws IOException {
	return new PrintWriter(new FileOutputStream(f, append));
    }
    static public PrintWriter writer(File f) throws IOException {
	return writer(f, false);
    }
    static public PrintWriter writer(String file) throws IOException {
	return writer(new File(file));
    }
    static public PrintWriter writer(String dir, String file) throws IOException {
	return writer(new File(dir, file));
    }
    static double[] doubleArrayList2Array(ArrayList a) {
	int num = a.size();
	double[] result = new double[num];
	for (int i=0; i<num; i++)
	    result[i] = ((Double) a.get(i)).doubleValue();
	return result;
    }
    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1).toLowerCase();
    }

    public static String pngname(String fileName) { 
	return pngname(fileName, false);
    }
    public static String pngname(String fileName, boolean includePath) {
	File f = new File(fileName.substring(0, fileName.length()-4));
	return (includePath?f.getPath():f.getName()) + ".png";
    }

}
    
