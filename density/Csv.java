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

public class Csv {
    String[] headers;
    HashMap headerMap = new HashMap();
    String fileName;
    String[] currentRecord;
    BufferedReader in;

    String separator = ",";
    static boolean allowQuote = true;
    boolean frenchMode = false;

    String lastLine=null;
    int lineNum=0;
    boolean initialized = false;

    void checkEuropean(String line) {
	if (line!=null && (line.indexOf(separator) == -1) && (line.indexOf(";") != -1)) {
	    frenchMode = true;
	    separator = ";";
	    Utils.warn("Detected \";\" as separator in " + fileName);
	}
    }

    String[] getTokens(BufferedReader in, String where) throws IOException {
	ArrayList records = new ArrayList();
	String token = "";
	boolean inQuote = false;
	//, lastTokWasQuote = false;
	ArrayList tokens = new ArrayList();
	char quote = '\"';
	char sepchar = separator.charAt(0);
	while (true) {
	    String s = lastLine = in.readLine();
	    if (!initialized) { checkEuropean(s); initialized = true; }
	    lineNum++;
	    if (s==null && tokens.size()==0) {
		return null;
	    }
	    if (s==null)
		throw new IOException("End of file reached while parsing " + where);
	    for (int i=0; i<s.length(); i++) {
		char tok = s.charAt(i);
		if ((tok==quote) && allowQuote) {
		    inQuote = !inQuote;
		}
		else if ((tok==sepchar) && !inQuote) {
		    tokens.add(token.trim());
		    token = "";
		}
		else {
		    token += tok;
		}
	    }
	    if (inQuote)
		token += System.getProperty("line.separator");
	    else {
		tokens.add(token.trim());
		if (tokens.size()==0 || (tokens.size()==1 && tokens.get(0).equals(""))) return null;
		return (String[]) tokens.toArray(new String[0]);
	    }
	}
    }

    public void close() throws IOException { in.close(); } 

    public Csv(String fileName) throws IOException { this(fileName, true); }
    public Csv(String fileName, boolean firstLineFieldNames) throws IOException {
	this.fileName = fileName;
	if (!(fileName.endsWith(".csv") || fileName.endsWith(".CSV")) && !(new File(fileName).exists()))
	    this.fileName = fileName + ".csv";

	initialize(new FileReader(this.fileName), firstLineFieldNames);
    }
    public Csv(InputStream inputstream) throws IOException {
	initialize(new InputStreamReader(inputstream), true);
    }

    void initialize(InputStreamReader reader, boolean firstLineFieldNames) throws IOException {
	initialized = false;
	in = new BufferedReader(reader);
	if (firstLineFieldNames)
	    processHeader(getTokens(in, fileName));
    }
    
    void processHeader(String[] headers) { 
	if (headers==null) headers = new String[0];
	this.headers = headers;
	for (int i=0; i<headers.length; i++) {
	    headers[i] = headers[i];
	    headerMap.put(headers[i], new Integer(i));
	}
    }

    public String[] getRecord() throws IOException {
	while (true) {
	    currentRecord = getTokens(in, fileName);
	    if (frenchMode && currentRecord!=null)
		for (int i=0; i<currentRecord.length; i++)
		    currentRecord[i] = currentRecord[i].replaceAll(",",".");
	    if (currentRecord==null || currentRecord.length == headers.length)
		return currentRecord;
	    Utils.warn2((currentRecord.length>headers.length ? "Extra" : "Missing") + " fields in " + fileName + " line " + lineNum + ": skipping... ", "csvmissingfields");	    
	}
    }

    public String[] getCurrentRecord() { return currentRecord; }

    boolean hasField(String s) {
	return headerMap.containsKey(s);
    }

    int fieldIndex(String s) {
	Integer i = (Integer) headerMap.get(s);
	if (i==null) 
	    System.err.println("Field " + s + " not found in " + fileName);
	return (i==null) ? -1 : i.intValue();
    }

    public String get(int i) { return (currentRecord[i]); }
    public String get(String field) { 
	int i = fieldIndex(field);
	return (i==-1) ? null : currentRecord[i]; 
    }

    double[] getDoubles(int start) {
	double[] vals = new double[headers.length-start];
	for (int i=start; i<headers.length; i++)
	    vals[i-start] = Double.parseDouble(currentRecord[i]);
	return vals;
    }
    String[] getAll(int start) { return getAll(currentRecord, start); }
    String[] getAll(String[] s, int start) {
	if (start>=headers.length) return new String[0];
	String[] vals = new String[headers.length-start];
	for (int i=start; i<headers.length; i++)
	    vals[i-start] = s[i];
	return vals;
    }

    public String[] headers() { return headers; }
    public String headerString() { 
	return toCsvFormat(headers);
    }
    public static String toCsvFormat(String[] s) {
	if (s.length == 0) return "";
	String result = s[0];
	for (int i=1; i<s.length; i++) {
	    boolean quote = (s[i].indexOf(',') != -1 && !s[i].startsWith("\"") && !s[i].endsWith("\""));
	    result += "," + (quote?"\"":"") + s[i] + (quote?"\"":"");
	}
	return result;
    }

    public abstract class Applier {
	public String get(String field) { return Csv.this.get(field); }
	public String get(int i) { return Csv.this.get(i); }
	public double getDouble(int i) { return Double.parseDouble(Csv.this.get(i)); }
	public abstract void process();
    }
    public void apply(Applier applier) throws IOException {
	while (getRecord() != null) {
	    applier.process();
	}
	close();
    }


    HashMap<String,String> indexmap;
    String mapsep = "__||__";
    public void indexAll() throws IOException { indexAll(headers[0]); }
    public void indexAll(final String indexVar) throws IOException {
	indexmap = new HashMap();
	apply(new Applier() {
		public void process() {
		    for (String f: headers)
			indexmap.put(get(indexVar)+mapsep+f, get(f));
		}});
    }
    public String getVal(String key, String var) {
	return indexmap.get(key+mapsep+var);
    }
    public double getDoubleVal(String key, String var) { 
	return Double.parseDouble(getVal(key, var));
    }


    public static void getCol(String filename, String field, final ArrayList a) throws IOException {
	Csv csv = new Csv(filename);
	final int index = csv.fieldIndex(field);
	if (index==-1)
	    return;
	csv.apply(csv.new Applier() {
		public void process() { a.add(get(index)); }});
    }
	
    public static String[] getCol(String filename, String field) throws IOException {
	ArrayList a = new ArrayList();
	getCol(filename, field, a);
	return (String[]) a.toArray(new String[0]);
    }
				    
    public static double[] getDoubleCol(String filename, String field) throws IOException {
	ArrayList a = new ArrayList();
	getCol(filename, field, a);
	return aToDoubles(a);
    }
	
    static double[] aToDoubles(ArrayList a) {
	double[] result = new double[a.size()];
	for (int i=0; i<a.size(); i++)
	    result[i] = Double.parseDouble((String) a.get(i));
	return result;
    }
	
    public double[][] getDoubleAllCols() throws IOException {
	return getDoubleAllCols(0);
    }
    public double[][] getDoubleAllCols(final int start) throws IOException {
	final int nh = headers.length;
	final ArrayList<Double>[] a = new ArrayList[nh-start];
	for (int i=start; i<nh; i++)
	    a[i-start] = new ArrayList();
	apply(new Applier() {
		public void process() {
		    for (int i=start; i<nh; i++) {
			double v = -1;
			try { v = Double.parseDouble(get(i)); }
			catch (NumberFormatException e) {}
			a[i-start].add(v);
		    }
		}});
	double[][] result = new double[a[0].size()][nh-start];
	for (int i=0; i<result.length; i++)
	    for (int j=start; j<nh; j++)
		result[i][j-start] = a[j-start].get(i);
	return result;
    }
	
}

