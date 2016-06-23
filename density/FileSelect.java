package density;

import java.util.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;

class FileSelect extends DirectorySelect {

    public FileSelect(String content, String suffix, ActionListener listener) {
	super(content, suffix, null, listener);
	final String suff = suffix;
	dirLine.label.setText("File");
	dirLine.fileSelectionMode = JFileChooser.FILES_ONLY;
	dirLine.filter = new javax.swing.filechooser.FileFilter() {
		public boolean accept(File f) {
		    if (f.isDirectory()) return true;
		    return f.getName().toLowerCase().endsWith(suff);
		}
		public String getDescription() { return (suff + " files"); }
	    };
    }

    static String[] getNames(File f, final FileSelect fs) throws IOException {
	final HashSet set = new HashSet();
	final Csv csv = new Csv(f.getPath());
	if (csv.headers().length <= SampleSet.speciesIndex)
	    return new String[0];
	csv.apply(csv.new Applier() {
		public void process() {
		    String name = SampleSet.sanitizeSpeciesName(csv.get(SampleSet.speciesIndex));
		    if (name.equals("")) return;
		    if (!set.contains(name))
			set.add(name);
		}});
	String[] result = (String[]) set.toArray(new String[0]);
	Arrays.sort(result);
	return result;
    }
	

    String[] getFiles(File f) {
	BufferedReader in;
	HashSet set = new HashSet();
	String line;
	String[] result;
	try {
	    result = getNames(f, this);
	}
	catch (IOException e) { return new String[0]; }
	return result;
    }
}
