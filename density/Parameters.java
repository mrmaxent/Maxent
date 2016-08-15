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

import java.util.*;
import java.io.*;

public class Parameters {
    ArrayList<Parameter> orderedParams = new ArrayList();
    HashMap<String,Parameter> params = new HashMap();
    HashMap<String,Parameter> flags = new HashMap();
    HashMap<String,Parameter> selections = new HashMap();

    ArrayList<Parameter> allParams() { return orderedParams; }

    String initializationError = null;
    String[] args=null;
	
    String commandLine() { // as given
	String result = "";
	if (args!=null)
	    for (String s: args)
		result += " " + s;
	return result;
    }

    boolean isKey(String s) { return params.containsKey(s) || selections.containsKey(s); }

    void addParam(Parameter p) { 
	String key = p.getName().toLowerCase();
	if (isKey(key)) 
	    System.out.println("Warning: duplicate parameter " + key);
	params.put(key, p);
	orderedParams.add(p);
	String a = p.getAbbreviations();
	if (!a.equals(""))
	    for (int i=0; i<a.length(); i++) {
		String ch = a.substring(i,i+1);
		if (!ch.equals("!")) {
		    if (flags.containsKey(ch)) 
			System.out.println("Warning: duplicate flag " + ch);
		    flags.put(ch, p);
		}
	    }
	if (p.isSelection())
	    for (String s: p.allowedValues()) {
		if (isKey(s.toLowerCase()))
		    System.out.println("Warning: duplicate flag " + s.toLowerCase());
		selections.put(s.toLowerCase(), p);
	    }
    }

    Parameter getParameter(String p) { return params.get(p.toLowerCase()); }
    // private to force outside calls to use type-specific versions
    private Object getValue(String p) { return getParameter(p).getValue(); }
    String getString(String p) { return (String) (getValue(p)); }
    Boolean getBoolean(String p) { return (Boolean) (getValue(p)); }
    boolean getboolean(String p) { return getBoolean(p).booleanValue(); }
    Double getDouble(String p) { return (Double) (getValue(p)); }
    double getdouble(String p) { return getDouble(p).doubleValue(); }
    Integer getInteger(String p) { return (Integer) (getValue(p)); }
    int getint(String p) { return getInteger(p).intValue(); }
    boolean changed(String p) { return getParameter(p).changed(); }

    boolean isParam(String p) { return params.containsKey(p); }
    boolean isBooleanParam(String p) { return isParam(p) && getParameter(p).isBoolean(); }
    void setValue(String p, String s) { getParameter(p).setValue(s); }
    void setValue(String p, int i) { setValue(p, Integer.toString(i)); }
    void setValue(String p, double d) { setValue(p, Double.toString(d)); }
    void setValue(String p, boolean b) { setValue(p, Boolean.toString(b)); }

    void checkParseParam(String s) {
	if (!parseParam(s)) addInitializationError(s);
    }
    String readFromArgs(String[] args) {
	this.args = args;
	for (int i=0; i<args.length; i++) {
	    String arg = args[i];
	    if (arg.startsWith("-")) {
		if (arg.length() != 2) {
		    addInitializationError(arg);
		    continue;
		}
		String flag = arg.substring(1,2);
		if (flags.containsKey(flag)) {
		    Parameter param = flags.get(flag);
		    if (param.isBoolean())
			//			checkParseParam((param.isNegatedFlag(flag)?"no":"") + param.getName());
			checkParseParam((getboolean(param.getName())?"no":"") + param.getName());
		    else {
			if (i==args.length-1 ||
			    !parseParam(param.getName() + "=" + args[++i]))
			    addInitializationError(arg);
		    }
		}
		else addInitializationError(arg);
	    }
	    else 
		checkParseParam(arg);
	}
	return initializationError;
    }

    String getParamKey(String s) { return s.replaceAll("=.*", "").toLowerCase(); }
    String getParamVal(String s) { 
	int eq = s.indexOf("=");
	if (eq==-1) return null;
	return s.substring(eq+1, s.length());
    }

    boolean parseParam(String p) {
	String key = getParamKey(p), value=getParamVal(p);
	//	System.out.println(key + " " + value + " " + getParameter(key) + " " + selections.get(key));
        if (isBooleanParam(key) && value==null) 
	    setValue(key, true);
	else if (key.startsWith("no") && isBooleanParam(key.substring(2)) && value==null)
	    setValue(key.substring(2), false);
	else if (key.startsWith("dont") && isBooleanParam(key.substring(4)) && value==null)
	    setValue(key.substring(4), false);
	else if (isParam(key) && value != null) {
	    try {
		setValue(key, value);
	    } catch (java.lang.IllegalArgumentException e) { 
		return false; 
	    }
	}
	else if (selections.containsKey(key))
	    setValue(selections.get(key).getName(), key);
	else 
	    return false;
	return true;
    }

    void addInitializationError(String key) {
	//	System.out.println("Unrecognized flag: " + key);
	if (initializationError==null)
	    initializationError = key;
	else 
	    initializationError = initializationError + ", " + key;
    }

}
