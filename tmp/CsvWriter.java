package density;

import java.util.*;
import java.io.*;

public class CsvWriter {
    PrintWriter csvWriter;
    int n, insertionIndex = 0;
    boolean isFirstRow=true, isEmptyRow=true;
    ArrayList<String> columnNames;
    //    String[] columnNames;
    HashMap columnMap;
    java.text.NumberFormat nf;
    File csvFile;
    
    String[] getColumnNames() { return (String[]) columnNames.toArray(new String[0]); } 

    CsvWriter(File csvFile) throws IOException {
	this(csvFile, false);
    }

    CsvWriter(File csvFile, boolean append) throws IOException {
	this.csvFile = csvFile;
	columnNames = new ArrayList();
	columnMap = new HashMap();
	if (append && csvFile.exists()) 
	    readColumnNames(csvFile);
	csvWriter = Utils.writer(csvFile, append);

	nf = java.text.NumberFormat.getNumberInstance(Locale.US);
	nf.setMinimumFractionDigits(4);
	nf.setGroupingUsed(false);
    }

    void readColumnNames(File csvFile) throws IOException {
	Csv csv = new Csv(csvFile.getPath());
	String[] h = csv.headers();
	for (int i=0; i<h.length; i++) {
	    columnNames.add(h[i]);
	    columnMap.put(h[i], null);
	}
	csv.close();
	isFirstRow = false;
	n = columnNames.size();
    }

    void print(String column, int value) {
	print(column, Integer.toString(value));
    }
    
    void print(String column, double f) {
	print(column, nf==null ? Double.toString(f) : nf.format(f));
    }

    boolean warnRewrite=false, addedColumns=false;
    void print(String column, String value) {
	if (columnMap.containsKey(column)) {
	    if (columnMap.get(column)!=null && warnRewrite)
		Utils.warn("CsvWriter: Replacing previous value in column \"" + column + "\"");
	}
	else {
	    if (!isFirstRow) 
		Utils.warn2("CsvWriter: printing into a non-existent column \"" + column + "\" in file " + csvFile.getName(), "nonexistentcolumn");
	    columnNames.add(insertionIndex++, column);
	    addedColumns = true;
	}
	columnMap.put(column,value);
	isEmptyRow = false;
    }

    void resetInsertionIndex() { insertionIndex = 0; }

    String protect(String s) {
	if (s.indexOf(",") != -1) 
	    return "\"" + s + "\"";
	return s;
    }

    void println() {
	if (isEmptyRow) return;
	if (isFirstRow || addedColumns) {
	    //	    columnNames = (String[]) columnNamesList.toArray(new String[0]);
	    n = columnNames.size();
	    for (int i=0; i<n-1; i++)
		csvWriter.print(protect(columnNames.get(i))+",");
	    csvWriter.println(protect(columnNames.get(n-1)));
	    isFirstRow = false;
	    addedColumns = false;
	}
	for (int i=0; i<n; i++) {
	    String value = (String) columnMap.get(columnNames.get(i));
	    csvWriter.print( protect((value==null ? "" : value)) +
			     (i<n-1 ? "," : "\n") );
	    columnMap.put(columnNames.get(i), null);
	}
	csvWriter.flush();
	isEmptyRow = true;
    }

    void close() { csvWriter.close(); }

    void reopen() throws IOException { 
	csvWriter = Utils.writer(csvFile, true); 
    }

    String filename() { return csvFile.getPath(); }

}
