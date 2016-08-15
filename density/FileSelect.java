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
