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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class DirectorySelect extends JPanel {
    final JPanel filePane = new JPanel();
    FileEntry dirLine;
    String titleString = "Directory/File";
    String[] suffix;
    String[] types;
    GridBagLayout gridbag;
    GridBagConstraints c;
    JButton deselectAll, selectAll;
    static boolean prefixes = true;

    String getText() { return dirLine.getText(); }
    void setText(String s) { dirLine.setText(s); }
    String[] getSelected() { return getSelected(true); }
    String[] getSelected(boolean sel) {
	Component[] contents = filePane.getComponents();
	ArrayList tmp = new ArrayList();

	for (int i=0; i<contents.length; i++)
	    if (contents[i] instanceof JCheckBox) {
		JCheckBox c = (JCheckBox) contents[i];
		if (c.isSelected() == sel)
		    tmp.add(c.getText());
	    }
	return (String[]) tmp.toArray(new String[0]);
    }

    void warnToggleNoEffect(String s) {
	Utils.warn2("Warning: toggle \"" + s + "\" had no effect", "toggleNoEffect");
    }
	
    void toggleType(String fileName) {
	Component[] contents = filePane.getComponents();
	boolean hadEffect = false;
	for (int i=0; i<contents.length; i+=2) {
	    String text = ((JCheckBox) contents[i]).getText().toLowerCase();
	    String flc = fileName.toLowerCase();
	    if (text.equals(flc) || prefixes && text.startsWith(flc)) {
		JComboBox c = (JComboBox) contents[i+1];
		c.setSelectedIndex(1-c.getSelectedIndex());
		hadEffect = true;
	    }
	}
	if (!hadEffect) warnToggleNoEffect(fileName);
    }
    
    void toggleSelected(String name) {
	Component[] contents = filePane.getComponents();
	boolean hadEffect = false;
	for (int i=0; i<contents.length; i++)
	    if (contents[i] instanceof JCheckBox) {
		JCheckBox c = (JCheckBox) contents[i];
		String text = c.getText();
		if (name.equals("") || text.equals(name) || prefixes && text.startsWith(name)) {
		    c.setSelected(!(c.isSelected()));
		    hadEffect = true;
		}
	    }
	if (!hadEffect) warnToggleNoEffect(name);
    }
    
    String[] getTypes() {
	Component[] contents = filePane.getComponents();
	ArrayList tmp = new ArrayList();

	for (int i=0; i<contents.length; i+=2) {
	    JCheckBox c = (JCheckBox) contents[i];
	    if (c.isSelected()) 
		tmp.add((String) (((JComboBox) contents[i+1]).getSelectedItem()));
	}
	return (String[]) tmp.toArray(new String[0]);
    }

    public DirectorySelect(String content, String[] suff, String[] types) {
	this(content, suff, types, null);
    }

    void myAdd(GridBagLayout gridbag, GridBagConstraints c, Component o) {
	gridbag.setConstraints(o, c);
	add(o);
    }

    public DirectorySelect(String content, String suff, String[] types, ActionListener listener) {
	this(content, new String[] { suff }, types, listener);
    }
    public DirectorySelect(String content, String[] suff, String[] types, ActionListener listener) {
	suffix = suff;
	this.types = types;
	final ActionListener l = listener;
	dirLine = new FileEntry(titleString, new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    File f = new File(e.getActionCommand());
		    if (f.isFile())
			for (int i=0; i<Utils.inputFileTypes.length; i++)
			    if (f.getPath().endsWith(Utils.inputFileTypes[i])) {
				f = f.getParentFile();
				dirLine.text.setText(f.getPath());
			    }
		    showFiles(f);
		    if (l!=null) l.actionPerformed(e);
		}}, ".csv");

	filePane.setLayout(new GridLayout(0, 1));
	
	JScrollPane fileChoice = new JScrollPane(filePane);
	fileChoice.setPreferredSize(new Dimension(300,300));

	gridbag = new GridBagLayout();
	c = new GridBagConstraints();
	setLayout(gridbag);
	c.fill = GridBagConstraints.BOTH;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 1.0;
	myAdd(gridbag, c, new JLabel(content, SwingConstants.CENTER));
	myAdd(gridbag, c, dirLine);
	c.weighty = 1.0;
	myAdd(gridbag, c, fileChoice);
	selectAll = new JButton("Select all");
	deselectAll = new JButton("Deselect all");
	selectAll.addActionListener(new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    Component[] contents = filePane.getComponents();
		    for (int i=0; i<contents.length; i++)
			if (contents[i] instanceof JCheckBox)
			    ((JCheckBox) contents[i]).setSelected(true);
		}});
	deselectAll.addActionListener(new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    Component[] contents = filePane.getComponents();
		    for (int i=0; i<contents.length; i++)
			if (contents[i] instanceof JCheckBox)
			    ((JCheckBox) contents[i]).setSelected(false);
		}});
	setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
    }


    String[] getFiles(File dir) throws IOException { return getFiles(dir, suffix); }

    static String[] getFiles(File dir, final String[] suffix) throws IOException {
	if (dir.isFile()) {
	    Csv csv = new Csv(dir.getPath());
	    return csv.getAll(csv.headers(), SampleSet.firstEnvVar);
	}
	String[] result = dir.list(new FilenameFilter() {
		public boolean accept(File d, String name) { 
		    for (int i=0; i<suffix.length; i++)
			if (name.toLowerCase().endsWith(suffix[i])) return true;
		    return false;
		}});
	if (result==null) return new String[0];
	for (int i=0; i<result.length; i++)
	    for (int j=0; j<suffix.length; j++)
		if (result[i].toLowerCase().endsWith(suffix[j])) {
		    result[i] = result[i].substring(0, result[i].length() - suffix[j].length());
		    break;
		}
	Arrays.sort(result);
	return result;
    }

    void showFiles(File dir ) { 
	try { showLayers(getFiles(dir)); }
	catch (IOException e) {
	    showLayers(new String[0]);
	}
    }
    
    boolean buttonsAdded = false;
    void showLayers(String[] layers) {
	Arrays.sort(layers);
	filePane.removeAll();
	if (!buttonsAdded && layers.length>10) {
	    c.gridwidth = 1;
	    c.weighty = 0.0;
	    myAdd(gridbag, c, selectAll);
	    myAdd(gridbag, c, deselectAll);
	    buttonsAdded = true;
	    revalidate();
	}
	if (types!=null)
	    filePane.setLayout(new GridLayout(0,2));
	if (layers != null)
	    for (int i=0; i<layers.length; i++) {
		filePane.add(new JCheckBox(layers[i], true));
		if (types!=null)
		    filePane.add(new JComboBox(types));
	    }
	filePane.revalidate();
    }
}
