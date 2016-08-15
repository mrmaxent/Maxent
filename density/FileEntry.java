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

class FileEntry extends JPanel {
    final JTextField text = new JTextField(20);
    static final JFileChooser fc = new JFileChooser();
    JLabel label;
    JButton browse;
    int fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES;
    javax.swing.filechooser.FileFilter filter;
    ActionListener listener;

    void setDirOnly() {
	fileSelectionMode = JFileChooser.DIRECTORIES_ONLY;
    }
    void setFilesOnly() {
	fileSelectionMode = JFileChooser.FILES_ONLY;
    }

    public FileEntry(String title) {
	this(title, (ActionListener) null);
    }
    public FileEntry(String title, String suffix) {
	this(title, null, suffix);
    }
    public FileEntry(String title, ActionListener ll) {
	this(title, ll, fc.getAcceptAllFileFilter());
    }
    public FileEntry(String title, ActionListener ll, final String suffix) {
	this(title, ll, (javax.swing.filechooser.FileFilter) null);
	if (suffix==null)
	    filter = new javax.swing.filechooser.FileFilter() {
		    public boolean accept(File f) { return f.isDirectory(); }
		    public String getDescription() { return "directories"; }
		};
	else
	    filter = new javax.swing.filechooser.FileFilter() {
		    public boolean accept(File f) {
			if (f.isDirectory()) return true;
			return f.getName().toLowerCase().endsWith(suffix);
		    }
		    public String getDescription() { 
			return (fileSelectionMode == JFileChooser.FILES_AND_DIRECTORIES ? "directories and " : "") 
			    + suffix + " files"; }
		};
    }
    public FileEntry(String title, ActionListener ll, javax.swing.filechooser.FileFilter ff) {
	browse = new JButton("Browse");
	listener = ll;
	filter = ff;
	browse.addActionListener(new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    fc.setFileSelectionMode(fileSelectionMode);
		    fc.resetChoosableFileFilters();
		    fc.setFileFilter(filter);
		    int returnVal = fc.showOpenDialog(FileEntry.this);
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.exists() && file.getParentFile().exists())
			    file = file.getParentFile();
			text.setText(file.getAbsolutePath());
			passFileName(listener);
		    }
		}});
	text.addActionListener(new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    passFileName(listener);
		}});
	
	setLayout(new BorderLayout());
	add(label = new JLabel(title+" "), BorderLayout.WEST);
	add(text, BorderLayout.CENTER);
	add(browse, BorderLayout.EAST);
    }

    void passFileName(ActionListener listener) {
	if (listener!=null)
	    listener.actionPerformed(new ActionEvent(this, 0, text.getText()));
    }

    String getText() { return text.getText().trim(); }
    void setText(String s) { text.setText(s); passFileName(listener); }
    void disable(String s) {
	text.setText(s);
	text.setEnabled(false);
	browse.setEnabled(false);
    }
    public void enable() { 
	text.setEnabled(true); text.setText(""); browse.setEnabled(true); 
    }
}
