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

//  Parameters with extra maxent-specific bits 

import java.util.*;
import java.io.*;

public class ParamsPre extends Parameters {

    String[] species=null, layers=null, layerTypes=null, unusedLayers=null;
    ArrayList toggleType = new ArrayList();
    ArrayList toggleSelectedSamples = new ArrayList();
    ArrayList toggleSelectedLayers = new ArrayList();
    HashMap betaMap = new HashMap();

    String commandLine(String species) { // to repeat
	String result = "java density.MaxEnt nowarnings noprefixes";
	if (species!=null) result += " -E \"\" -E " + species;
	for (Parameter param: allParams())
	    if (param.changed()) {
		String flag = param.isBoolean() ? (param.getValue().equals(true) ? "" : "no") + param.getName() :
		    param.getName() + "=" + param.getValue();
		if (flag.indexOf(" ")!=-1) flag = "\"" + flag + "\"";
		result += " " + flag;
	    }
	for (String s: unusedLayers) result += " -N " + s;
	for (int i=0; i<layers.length; i++)
	    if (layerTypes[i].equals("Categorical")) result += " -t " + layers[i];
	return result;
    }

    boolean logistic() { return getString("outputformat").toLowerCase().equals("logistic"); }
    boolean cloglog() { return getString("outputformat").toLowerCase().equals("cloglog"); }
    String occurrenceProbabilityTransform() { return logistic() ? "Logistic" : "Cloglog"; }
    boolean occurrenceProbability() { return logistic() || cloglog(); }
    boolean cumulative() { return getString("outputformat").toLowerCase().equals("cumulative"); }
    boolean allowpartialdata() { return getboolean("allowpartialdata"); }

    public ParamsPre() { init(); }

    public void init() {
	try {
	    Csv csv = new Csv(ParamsPre.class.getResourceAsStream("parameters.csv"));
	    csv.apply(csv.new Applier() {
		    public void process() {
			addParam(new Parameter(get("Parameter"), get("Abbreviations"), get("Display text"), get("Values"), get("Default"), get("Level"), get("Tooltip")));
		    }});
	} catch (IOException e) {
	    Utils.fatalException("Can't find default parameters file", null);
	}
    }

    /**
     * Parse an array of arguments as if they were command-line arguments
     * <p>
     * Returns a string containing any arguments not understood
     * Returns null if all arguments are understood
     */
    public String readFromArgs(String[] args) {
	super.readFromArgs(args);
	if (getboolean("printversion")) {
	   System.out.println("MaxEnt version "+Utils.version); System.exit(0);
        }
	if (changed("factorbiasout")) {
	    setValue("biasFile",getString("factorbiasout")); 
	    setValue("biasType", Layer.F_BIAS_OUT); 
	}
	if (changed("priordistribution")) { 
	    setValue("biasFile",getString("priordistribution")); 
	    setValue("biasType", Layer.F_BIAS_OUT); 
	    setValue("biasIsBayesianPrior", true); 
	}
	if (changed("debiasaverages")) { 
	    setValue("biasFile",getString("debiasaverages")); 
	    setValue("biasType", Layer.DEBIAS_AVG);
	}
	String toReturn = null;
	if (initializationError!=null) {
	    Utils.visible = getBoolean("visible");
	    Utils.popupError("Initialization flags not understood: " + initializationError, null);
	    toReturn = initializationError;
	    initializationError = null;
	}	
	return toReturn;
    }

    /**
     * Parse a single parameter
     *
     * @param param string describing the parameter setting.  Examples:  "nopictures", "betamultiplier=2.0"; see help button for more details
     */
    public boolean parseParam(String param) {
	String key = getParamKey(param), value=getParamVal(param);
	try {
	    if (key.equals("togglelayertype")) toggleType.add(value); 
	    else if (key.equals("togglespeciesselected")) toggleSelectedSamples.add(value); 
	    else if (key.equals("togglelayerselected")) toggleSelectedLayers.add(value); 
	    else if (key.equals("setfeaturebeta")) {
		String[] fields = value.split(":");
		if (fields.length<2) 
		    return false;
		else
		    betaMap.put(fields[0], fields[1]);
	    }
	    else if (key.equals("redoifexists"))
		super.parseParam("noaskoverwrite");

	    else return super.parseParam(param);  // comes last to avoid specials
	    return true;
	} catch (java.lang.IllegalArgumentException e) {
	    return false;
	}
    }

    public void setSelections() {
	String[] tmplayers=null;
	try {
	    tmplayers = (getString("environmentalLayers").equals("")) ?
		SampleSet2.featureNames(getString("samplesFile")) :
		DirectorySelect.getFiles(new File(getString("environmentalLayers")), Utils.inputFileTypes);
	} catch (IOException e) { 
	    Utils.popupError("Error reading layer names", e);
	    System.exit(1); 
	}
	layers = getSelected(tmplayers, toggleSelectedLayers);
	unusedLayers = getSelected(tmplayers, toggleSelectedLayers, false);
	boolean[] continuous = getSelectedBits(layers, toggleType, true);
	layerTypes = new String[layers.length];
	for (int i=0; i<layers.length; i++)
	    layerTypes[i] = continuous[i] ? "Continuous" : "Categorical";
	String[] tmpspecies=null;
	try {
	    tmpspecies = FileSelect.getNames(new File(getString("samplesFile")), null);
	} catch (IOException e) { 
	    Utils.popupError("Error reading species file", e);
	    System.exit(1);
	}
	species = getSelected(tmpspecies, toggleSelectedSamples);
    }

    boolean[] getSelectedBits(String[] names, ArrayList toggle, boolean verbose) {
	boolean[] choose = new boolean[names.length];
	Arrays.fill(choose, true);
	for (int i=0; i<toggle.size(); i++) {
	    String prefix = (String) toggle.get(i);
	    boolean hadEffect = false;
	    for (int j=0; j<choose.length; j++)
		if (names[j].equals(prefix) || getboolean("prefixes") && names[j].startsWith(prefix)) {
		    choose[j] = !choose[j];
		    hadEffect = true;
		}
	    if (!hadEffect && verbose)
		Utils.warn2("Warning: toggle \"" + prefix + "\" had no effect", "toggleNoEffect");
	}
	return choose;
    }

    String[] getSelected(String[] names, ArrayList toggle) { return getSelected(names, toggle, true); }
    String[] getSelected(String[] names, ArrayList toggle, boolean selected) {
	boolean[] choose = getSelectedBits(names, toggle, selected);
	ArrayList a = new ArrayList();
	for (int i=0; i<choose.length; i++)
	    if (choose[i]==selected) a.add(names[i]);
	String[] result = (String[]) a.toArray(new String[0]);
	Arrays.sort(result);
	return result;
    }

    void write() {
	System.out.println("<table border=\"1\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\">");
	System.out.println("<tr><th>Flag</th><th>Abbrv</th><th>Type</th><th>Default</th><th>Meaning</th></tr>");
	for (Parameter param: orderedParams) {
	    if (param.isHidden()) continue;
	    System.out.println("<tr><td>" + param.getName() + "</td><td>" + param.getAbbreviations() + "</td><td>" + param.getType().toString().toLowerCase().replaceAll("filedirectory", "file/directory") + "</td><td>" + param.getDefaultValue() + "</td><td>" + param.getToolTip());
	}
	System.out.println("</table>");
    }


    void check(String[] args) {
	for (String s: args) {
	    try {
		BufferedReader in = new BufferedReader(new FileReader(s));
		int linenum=0;
		while (true) {
		    String line = in.readLine();
		    if (line==null) break;
		    line = line.toLowerCase();
		    linenum++;
		    String[] fns = new String[] { "is", "params.getdouble", "params.getint", "params.getinteger", "params.getboolean", "params.getstring" };
		    for (String fn: fns) {
			int start = -1;
			while (true) {
			    start = line.indexOf(fn+"(\"", start+1);
			    if (start==-1) break;
			    String flag = line.substring(start+fn.length()+2).replaceAll("\".*", "").toLowerCase();
			    if (!isParam(flag) ||
				(fn.equals("is") && !getParameter(flag).isBoolean()) ||
				fn.endsWith("double") && !getParameter(flag).isDouble() ||
				fn.endsWith("int") && !getParameter(flag).isInteger() ||
				fn.endsWith("integer") && !getParameter(flag).isInteger() ||
				fn.endsWith("boolean") && !getParameter(flag).isBoolean() ||
				fn.endsWith("string") && !getParameter(flag).isString())
				checkError(s + " line " + linenum + ": " + flag + " isn't " + fn.replaceAll("params.get", "").replaceAll("is", "boolean"));
			}
		    }
		}
	    } catch (IOException e) { checkError(e.toString()); }
	}
    }
    void checkError(String s) { System.out.println(s); System.exit(1); }
		    
    String[] paramsJavadoc = new String[] {
	"Params is the main class for querying and adjusting parameters for MaxEnt.",
	"<p>",
        "Each parameter can be queried or set with its own typesafe methods.",
	"More general non-typesafe methods for setting one or more parameters",
	"are available in the parent class.",
	"<p>",
	"Typical usage to set up some parameters and make a Maxent model is:",
	"<ul>",
	"<li><code> Params params = new Params();</code>",
	"<li><i>  ... set some parameters using the methods in this package </i>",
	"<li><code> params.setSelections();</code>",
	"<li><code> Runner runner = new Runner(params);</code>",
	"<li><code> runner.start();</code>",
	"<li><code> runner.end();</code>",
	"</ul>",
	"<p>",
	"The <code>params.setSelections()</code> method is needed only if one or more of the toggle parameters have been used (toggle species selected, toggle layer type, toggle layer selected).",
    };

    void typesafe() {
       System.out.println("package density;");
       System.out.println("\n// Automatically created\n");
       System.out.println("/**");
       for (String s: paramsJavadoc) System.out.println(" * " + s);
       System.out.println(" */");
       System.out.println("public class Params extends ParamsPre {");
       System.out.println("  /**\n   * Get the type of a parameter, or <code>null</code> if the parameter doesn't exist\n   * @param param the parameter\n   * @return The parameter type\n   */");
       System.out.println("  public String getType(String param) { if (!isParam(param)) return null; return getParameter(param).typename(); }");
       System.out.println("  /**\n   * Get a list of all Maxent parameters\n   * @return The list of parameters\n   */");
       System.out.print("   public String[] getParameters() { return new String[] { ");
       boolean started = false;
       for (Parameter param: orderedParams) {
          System.out.print((started?",":"") + "\"" + param.getName() + "\" ");
	  started = true;
       }
       System.out.println("}; }");
       for (Parameter param: orderedParams) {
	   //	  if (param.isHidden()) continue;
	  System.out.println(param.setdoc());
	  System.out.println("   " + param.setfn());
	  if (param.name.startsWith("toggle"))
	      continue;
	  System.out.println(param.getdoc());
	  System.out.println("   " + param.getfn());
       }
       System.out.println("}");
    }


    public static void main(String[] args) {
	if (args[0].equals("write"))
	    new ParamsPre().write();
	else if (args[0].equals("typesafe"))
	    new ParamsPre().typesafe();
	else
	    new ParamsPre().check(args);
    }
}
