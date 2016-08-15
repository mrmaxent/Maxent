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

class CsvOnePass {
    String[] headers;
    HashMap headerMap = new HashMap();
    String fileName;
    String[] currentRecord;
    BufferedReader in;

    String myseparator = ",";
    static String separator = ",";
    static boolean allowQuote = true;

    static String[] getTokens(BufferedReader in, String where) throws IOException {
	return getTokens(in, where, separator);
    }

    static String[] getTokens(BufferedReader in, String where, String separator) throws IOException {
	ArrayList records = new ArrayList();
	String token = "";
	boolean inQuote = false;
	//, lastTokWasQuote = false;
	ArrayList tokens = new ArrayList();
	while (true) {
	    String s = in.readLine();
	    //	    System.out.println("Inquote = " + inQuote + ", Allowquote = " + allowQuote + ", Read line: " + s);
	    if (s==null && tokens.size()==0) {
		return null;
	    }
	    if (s==null)
		throw new IOException("End of file reached while parsing " + where);
	    StringTokenizer st = new StringTokenizer(s, separator+"\"", true);
	    while (st.hasMoreTokens()) {
		String tok = st.nextToken();
		//		System.out.println(tok + " " + inQuote);
		if (tok.equals("\"") && allowQuote) {
		    inQuote = !inQuote;
		    //		    if (lastTokWasQuote) 
		    //			token += "\"";
		    //		    else 
		    //			lastTokWasQuote = true;
		}
		else if (tok.equals(separator) && !inQuote) {
		    //		    lastTokWasQuote = false;
		    tokens.add(token);
		    token = "";
		}
		else {
		    token += tok;
		    //		    lastTokWasQuote = false;
		}
	    }
	    if (inQuote)
		token += System.getProperty("line.separator");
	    else {
		tokens.add(token);
		if (tokens.size()==0 || (tokens.size()==1 && tokens.get(0).equals(""))) return null;
		return (String[]) tokens.toArray(new String[0]);
	    }
	}
    }

    public void close() { 
	try { in.close(); } 
	catch (IOException e) { 
	    System.out.println("Error closing " + fileName + ": " + e.toString());
	    System.exit(1);
	}
    }
    public CsvOnePass(String fileName) { this(fileName, true, separator); }
    public CsvOnePass(String fileName, String mysep) { 
	this(fileName, true, mysep); 
    }
    public CsvOnePass(String fileName, boolean firstLineFieldNames) {
	this(fileName, firstLineFieldNames, separator);
    }
    public CsvOnePass(String fileName, boolean firstLineFieldNames, String mysep) {
	myseparator = mysep;
	String line;

	this.fileName = fileName;
	if (!(fileName.endsWith(".csv") || fileName.endsWith(".CSV")) && !(new File(fileName).exists()))
	    this.fileName = fileName + ".csv";
	if (fileName.endsWith(".tab") || fileName.endsWith(".TAB"))
	    myseparator = "\t";
	try {
	    in = new BufferedReader(new FileReader(this.fileName));
	    if (firstLineFieldNames)
		processHeader(getTokens(in, fileName, myseparator));
	}
	catch (IOException e) {
	    System.out.println(e.toString()); 
	    System.exit(1); 
	}
    }
    
    void processHeader(String[] headers) { 
	this.headers = headers;
	for (int i=0; i<headers.length; i++) {
	    headers[i] = headers[i].toLowerCase().trim();
	    headerMap.put(headers[i], new Integer(i));
	}
    }

    String[] getRecord() {
	try {
	    currentRecord = getTokens(in, fileName, myseparator);
	} 
	catch (IOException e) {
	    System.out.println("Error reading from " + fileName + ": " + e.toString()); 
	    System.exit(1); 
	}
	return currentRecord;
    }

    String[] getCurrentRecord() { return currentRecord; }

    boolean hasField(String s) {
	return headerMap.containsKey(s.toLowerCase());
    }

    int fieldIndex(String s) {
	Integer i = (Integer) headerMap.get(s.toLowerCase());
	if (i==null) 
	    System.err.println("Field " + s + " not found in " + fileName);
	return (i==null) ? -1 : i.intValue();
    }

    public double getDouble(String field) { return Double.parseDouble(get(field).replaceAll(",","")); }
    public double getDouble(int i) { return Double.parseDouble(get(i).replaceAll(",","")); }

    String get(int i) { return (currentRecord[i]); }
    String get(String field) { 
	int i = fieldIndex(field);
	return (i==-1 || i>=currentRecord.length) ? null : currentRecord[i]; 
    }
    String[] headers() { return headers; }
    String headerString() { 
	String result = "";
	for (int i=0; i<headers.length; result += headers[i++]);
	return result;
    }
    public abstract class Applier {
	String get(String field) { return CsvOnePass.this.get(field); }
	String get(int i) { return CsvOnePass.this.get(i); }
	public abstract void process();
    }
    void apply(Applier applier) {
	while (getRecord() != null) {
	    applier.process();
	}
    }
}

