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

import java.io.File;

class Parameter {
    public static enum Level { SPECIAL, CORE, BASIC, ADVANCED, EXPERIMENTAL, HIDDEN, STARTUP }
    public static enum Type { BOOLEAN, INTEGER, DOUBLE, FILE, FILEDIRECTORY, DIRECTORY, STRING, HIDDEN }

    String name, displaytext, abbreviations="", tooltip="", filetype=null;
    Level level;
    Object value, defaultValue;
    String[] allowedValues;
    Type type;
    int minint=0, maxint=1000000;

    public String toString() { return name + ": " + getValue(); }
    public String getName() { return name; }    
    public String getDisplayText() { return displaytext; }    
    public String getAbbreviations() { return abbreviations; }    
    public String getToolTip() { return tooltip; }    
    public Level  getLevel() { return level; }    
    public Type   getType() { return type; }    
    public Object getValue() { return (value==null) ? defaultValue : value; }
    public Object getDefaultValue() { return defaultValue; }
    public boolean changed() { return !getValue().equals(defaultValue); }

    public boolean isBoolean() { return type == Type.BOOLEAN; }
    public boolean isFile() { return type == Type.FILE; }
    public boolean isDirectory() { return type == Type.DIRECTORY; }
    public boolean isFileOrDirectory() { 
	return type == Type.FILE || type == Type.DIRECTORY || type == Type.FILEDIRECTORY; }
    public boolean isInteger() { return type == Type.INTEGER; }
    public boolean isDouble() { return type == Type.DOUBLE; }
    public boolean isString() { 
	return type == Type.STRING || type == Type.FILE || type == Type.DIRECTORY || type == Type.FILEDIRECTORY;
    }
    public boolean isSelection() { return allowedValues != null; }

    public String[] allowedValues() { return allowedValues; }
    public boolean isAllowedValue(String s) {
	for (String a: allowedValues)
	    if (a.toLowerCase().equals(s.toLowerCase())) return true;
	return false;
    }

    /*
      public boolean isNegatedFlag(String flag) {
      boolean negated = false;
      for (int i=0; i<abbreviations.length(); i++) {
      String ch = abbreviations.substring(i,i+1);
      if (ch.equals("!")) { negated = true; continue; }
      if (ch.equals(flag)) return negated;
      negated = false;
      }
      return false;
      }
    */

    public void setValue(String s) { 
	if (isBoolean()) {
	    if (s.toLowerCase().equals("true")) value = true;
	    else if (s.toLowerCase().equals("false")) value = false;
	    else throw new java.lang.IllegalArgumentException();
	}
	else if (isInteger()) value = Integer.parseInt(s);
	else if (isDouble()) value = Double.parseDouble(s);
	else if (isSelection()) {
	    if (!isAllowedValue(s))
		throw new java.lang.IllegalArgumentException();
	    value = s.toLowerCase();
	}
	else value = s;
    }

    // need to allow for blank defaults?
    public Parameter(String s, String abbreviations, String display, String values, String deflt, String level, String tooltip) { 
	name = s.toLowerCase();
	displaytext = display;
	defaultValue = null;
	this.tooltip = tooltip;
	this.level = Enum.valueOf(Level.class, level);
	this.abbreviations = abbreviations;
	type = Enum.valueOf(Type.class, values.replaceAll("/.*", "").toUpperCase(java.util.Locale.US));
	if (values.equals("boolean")) defaultValue = Boolean.parseBoolean(deflt);
	else if (values.startsWith("integer")) {
	    defaultValue = Integer.parseInt(deflt);
	    String[] vminmax = values.split("/");
	    if (vminmax.length>1) minint = Integer.parseInt(vminmax[1]);
	    if (vminmax.length>2) maxint = Integer.parseInt(vminmax[2]);
	}
	else if (values.equals("double")) defaultValue = Double.parseDouble(deflt);
	else defaultValue = deflt.toLowerCase();
	if (values.startsWith("file/"))
	    filetype = values.split("/")[1];
	if (values.startsWith("string/")) {
	    String[] options = values.split("/");
	    allowedValues = new String[options.length-1];
	    for (int i=1; i<options.length; i++) 
		allowedValues[i-1] = options[i];
	}
    }

    boolean isHidden() { return (getLevel() == Level.HIDDEN); }
    String capname() { return isToggle() ? name : Utils.capitalize(name); }
    boolean isToggle() { return name.startsWith("toggle"); }

    String typename() {
	if (isBoolean()) return "boolean";
	if (isInteger()) return "int";
	if (isDouble()) return "double";
	return "String";
    }

    String getdoc() {
	return "   /**\n   * Get value of <i>" + name + "</i> parameter: " + tooltip.replaceAll("<html>", "").replaceAll("> 1", "is greater than 1") + "\n   * @return The value <i>" + name + "</i> parameter" + "\n   */";
    }
    String getfn() { 
	return "public " + typename() + (isBoolean()?" is":" get") + capname() + "() { return get" + typename() + "(\"" + name + "\"); }"; 
    }

    String setdoc() {
	return "   /**\n   * " + (isToggle() ? "" : "Set value of <i>" + name + "</i> parameter: ") + tooltip.replaceAll("<html>", "").replaceAll("> 1", "is greater than 1") + (defaultValue.toString().equals("") ? "" : "\n   * <p>\n   * Default value is " + defaultValue + ".") + "\n   * @param value the " + (isToggle() ? "prefix" : "new value") + "\n   */";
    }
    String setfn() {
	return (isHidden()?"":"public ") + "void " + (isToggle() ? "" : "set") + capname() + "(" + typename() + " value) { " + (isToggle() ? ("parseParam(\""+name+"=\"+value)") : "setValue(\"" + name + "\", value)") + "; }"; 
    }

}
