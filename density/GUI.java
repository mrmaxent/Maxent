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
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;

import java.io.*;
import java.util.*;

public class GUI {
    FileSelect samplesSelect;
    DirectorySelect layersSelect;
    String[] featureTypes = new String[] {"Linear", "Quadratic", "Product", "Threshold", "Hinge", "Auto"};
    FileEntry outputDirEntry, projectDirEntry;
    JCheckBox[] featureButtons;
    JButton runButton;
    JDialog helpFrame = null;
    JFrame paramsFrame = null;
    static JFrame topLevelFrame = null;
    static ActionListener listener;
    Params params;
    Runner runner;

    public GUI(Params p) { params = p; }

    void myAdd(JPanel p, GridBagLayout gridbag, GridBagConstraints c, Component o) {
	gridbag.setConstraints(o, c);
	p.add(o);
    }

    HashMap tooltips = new HashMap();
    void setToolTipText(JComponent c, String s) { tooltips.put(c, s); }
    void setToolTipText(JComponent c, Parameter p) { tooltips.put(c, p.getToolTip()); }
    void showToolTips(boolean show) {
	JComponent[] c = (JComponent[]) tooltips.keySet().toArray(new JComponent[0]);
	for (int i=0; i<c.length; i++)
	    c[i].setToolTipText(show ? (String) tooltips.get(c[i]) : null); 
    }

    Container createContentPane() {
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
        JPanel p = new JPanel(gridbag);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	c.weightx = 1.0;
	c.weighty = 1.0;
	c.fill = GridBagConstraints.BOTH;
	myAdd(p, gridbag, c, samplesSelect = new FileSelect("Samples", ".csv", null));
	String s = "Please enter the name of a file containing presence locations for one or more species.";
	setToolTipText(samplesSelect, s);
	setToolTipText(samplesSelect.dirLine.text, s);

	c.gridwidth = GridBagConstraints.REMAINDER;
	String[] types = new String[] {"Continuous", "Categorical"};
	myAdd(p, gridbag, c, layersSelect = new DirectorySelect("Environmental layers", Utils.inputFileTypes, types));
	s = "<html>Environmental variables can be in a directory containing one file per variable, <br>or all together in a .csv file in SWD format.  Please enter a directory name or file name.";
	setToolTipText(layersSelect, s);
	setToolTipText(layersSelect.dirLine.text, s);
	c.weighty = 0.0;
	myAdd(p, gridbag, c, controls());
	paneParams.put("samplesfile", samplesSelect);
	paneParams.put("environmentallayers", layersSelect);
        return p;
    }

    JPanel controls() {
	JPanel checks = new JPanel();
	checks.setLayout(new GridLayout(0, 1));
	ButtonGroup group = new ButtonGroup();
	featureButtons = new JCheckBox[featureTypes.length];
	for (int i=0; i<featureTypes.length; i++) {
	    JCheckBox button = new JCheckBox(featureTypes[i] + " features");
	    featureButtons[i] = button;
	    setToolTipText(featureButtons[i], "Feature types to be used during training.  See Help for details.");
	    checks.add(button);
	    if (featureTypes[i].equals("Auto")) {
	        setToolTipText(featureButtons[i], "<html>Allow automatic limiting of feature types for small sample sizes,<br>with feature types being a subset of those selected above.");
		button.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    boolean sel = (e.getStateChange() == ItemEvent.SELECTED);
			    for (int i=0; i<featureButtons.length-1; i++) {
//				if (sel)
//				    featureButtons[i].setSelected(true);
				featureButtons[i].setEnabled(!sel);
			    }
			}});
		button.setSelected(params.getboolean("autofeature"));
	    }
	    else button.setSelected(params.getboolean(featureTypes[i]));

	}

	checks.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));
	runButton = new JButton("Run");
	runButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    doRun();
		}});
	JButton paramsButton = new JButton("Settings");
	paramsButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    paramsFrame.setVisible(true);
		    paramsFrame.toFront();
		}});
	JButton helpButton = new JButton("Help");
	helpButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (helpFrame==null) {
			createHelpPane();
		    } else {
			helpFrame.setVisible(true);
			helpFrame.toFront();
		    }
		}});
	
	JPanel buttons = new JPanel();
	buttons.setLayout(new GridLayout(1,0));
	buttons.add(runButton);
	buttons.add(paramsButton);
	buttons.add(helpButton);
	
	JPanel all = new JPanel();
	all.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	all.setLayout(new BorderLayout());
	all.add(createParamsPanes(), BorderLayout.EAST);
	all.add(checks, BorderLayout.WEST);
	all.add(buttons, BorderLayout.SOUTH);

	((JCheckBox) paneParams.get("tooltips")).addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    showToolTips(e.getStateChange()==ItemEvent.SELECTED);
		}});
	return all;
    }

    void setFeatureTypes(String s) {
	for (int i=0; i<featureTypes.length; i++) {
	    if (s.equals(featureTypes[i]))
		featureButtons[i].setSelected(true);
	    if (!s.equals("Auto") && featureTypes[i].equals("Auto"))
		featureButtons[i].setSelected(false);
	}
    }

    boolean getBooleanPaneParam(String name) {
	JCheckBox b = (JCheckBox) paneParams.get(name);
	return b.isSelected();
    }

    int getIntPaneParam(String name) {
	JFormattedTextField f = (JFormattedTextField) paneParams.get(name);
	return ((Integer) f.getValue()).intValue();
    }

    double getDoublePaneParam(String name) {
	JFormattedTextField f = (JFormattedTextField) paneParams.get(name);
	return ((Number) f.getValue()).doubleValue();
    }

    String getStringPaneParam(String name) {
	Object pp = paneParams.get(name);
	if (pp instanceof JFormattedTextField) {
	    JFormattedTextField f = (JFormattedTextField) pp;
	    return ((String) f.getValue()).trim();
	}
	else { // File Entry
	    FileEntry fe = (FileEntry) pp;
	    return fe.getText();
	}
    }

    void dumpPaneParams() {
	for (String s: paneParams.keySet())
	    System.out.println(s + " " + paneParams.get(s));
    }	

    HashMap<String,JComponent> paneParams = new HashMap<String,JComponent>();
    void addPaneParam(JComponent c, Parameter p) { addPaneParam(new JComponent[] {c}, p); }
    void addPaneParam(JComponent[] c, Parameter p) {
	paneParams.put(p.getName(), c[0]);
	for (JComponent cc: c)
	    setToolTipText(cc, p.getToolTip());
    }
	
    JPanel createParamsPanes() { // returns core panel 
	paramsFrame = new JFrame("Maximum Entropy Parameters");
	JTabbedPane tabs = new JTabbedPane();
	paramsFrame.setContentPane(tabs);
	for (Parameter.Level level: new Parameter.Level[] { Parameter.Level.BASIC, Parameter.Level.ADVANCED, Parameter.Level.EXPERIMENTAL, Parameter.Level.CORE }) {  // core must come last
	    JPanel checks = new JPanel(new GridLayout(0,1));
	    JPanel labels = new JPanel(new GridLayout(0,1));
	    JPanel entries = new JPanel(new GridLayout(0,1));
	    JPanel browsers = new JPanel(new GridLayout(0,1));
	    if (level == Parameter.Level.CORE)
		labels.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
	    for (Parameter param: params.allParams()) {
		if (param.getLevel() != level) continue;
		if (param.isBoolean()) {
		    JCheckBox c = new JCheckBox(param.getDisplayText());
		    if (level == Parameter.Level.CORE) {
			((JCheckBox) c).setHorizontalAlignment(SwingConstants.RIGHT);
			((JCheckBox) c).setHorizontalTextPosition(SwingConstants.LEFT);
		    }
		    checks.add(c);
		    addPaneParam(c, param);
		}
		else if (param.isFileOrDirectory()) {
		    FileEntry fe = (param.filetype==null) ? 
			new FileEntry(param.getDisplayText(), null,  Utils.fileFilter) :
			new FileEntry(param.getDisplayText(), null, "." + param.filetype);
		    if (param.isFile()) 
			fe.setFilesOnly();
		    if (param.isDirectory())
			fe.setDirOnly();
		    browsers.add(fe);
		    addPaneParam(new JComponent[] { fe, fe.text }, param);
		}
		else {  // an entry
		    DefaultFormatter df = new DefaultFormatter();
		    df.setOverwriteMode(false);
		    JLabel l = new JLabel(param.getDisplayText());
		    if (level == Parameter.Level.CORE)
			l.setHorizontalAlignment(SwingConstants.RIGHT);
		    JComponent c = 
			param.isInteger() ? 
			new WholeNumberField(((Integer) param.getDefaultValue()).intValue(), param.minint, param.maxint) :
			param.isDouble() ? 
			new DoubleNumberField(((Double) param.getDefaultValue()).doubleValue()) :
			param.isSelection() ?
			new JComboBox(param.allowedValues()) :
			new JFormattedTextField(df);  // string
		    //		    if (c instanceof JComboBox) {
		    //			ListCellRenderer renderer = new DefaultListCellRenderer();
		    //			( (JLabel) renderer ).setHorizontalAlignment( SwingConstants.RIGHT );
		    //			((JComboBox) c).setRenderer(renderer);
		    //		    }
		    if (c instanceof JFormattedTextField)
			((JFormattedTextField) c).setHorizontalAlignment(JTextField.RIGHT);
		    labels.add(l);
		    entries.add(c);
		    addPaneParam(new JComponent[] { c, l }, param);
		}
	    }

	    JPanel paramspanel = new JPanel();
	    boolean isCore = (level == Parameter.Level.CORE);
	    int b = isCore ? 2 : 5;
	    paramspanel.setBorder(BorderFactory.createEmptyBorder(b, b, b, b));
	    GridBagLayout gbl = new GridBagLayout();
	    GridBagConstraints constraints = new GridBagConstraints();
	    constraints.gridwidth = GridBagConstraints.REMAINDER;
	    constraints.fill = GridBagConstraints.BOTH;
	    paramspanel.setLayout(gbl);
	    myAdd(paramspanel, gbl, constraints, checks);
	    constraints.gridwidth = GridBagConstraints.RELATIVE;
	    constraints.weightx = (isCore ? 1.0 : 0.0);
	    myAdd(paramspanel, gbl, constraints, labels);
	    constraints.gridwidth = GridBagConstraints.REMAINDER;
	    constraints.weightx = (isCore ? 0.0 : 1.0);
	    myAdd(paramspanel, gbl, constraints, entries);
	    myAdd(paramspanel, gbl, constraints, browsers);

	    if (level == Parameter.Level.CORE) {
		paramsFrame.pack();
		return paramspanel;
	    }
	    else tabs.add(Utils.capitalize(level.toString()), paramspanel);
	}
	return null;
    }

    private void createHelpPane() {
	helpFrame = new JDialog(topLevelFrame, "Help for Maximum Entropy Species Distribution Modeling");
	JEditorPane text = new JEditorPane();
	try { 
	    text.setPage(GUI.class.getResource("help.html"));
	} catch (Exception e) { 
	    popupError("Error opening help window", e);
	}
	JScrollPane pane = new JScrollPane(text);
	pane.setPreferredSize(new Dimension(1000,700));
	helpFrame.getContentPane().add(pane);
	helpFrame.pack();
	helpFrame.setVisible(true);
    }

    static MyProgressMonitor progressMonitor = null;
    void mkProgressMonitor() {
	progressMonitor = new MyProgressMonitor(topLevelFrame, "Maximum Entropy Species Distribution Modeling", "________________________________________________________________", 100);
	progressMonitor.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    runner.stop();
		}});
	if (params.getBoolean("visible")) {
	    progressMonitor.pack();
	    progressMonitor.setLocationRelativeTo(topLevelFrame);
	    progressMonitor.setVisible(true);
	}
	progressMonitor.setNote("Reading files");
    }

    void doRun() { doRun(false); }
    void doRun(final boolean terminate) {
	if (paramsFromInterface()) {
	    runButton.setEnabled(false);
	    mkProgressMonitor();
	    while (progressMonitor==null) {
		try { wait(100); }
		catch (InterruptedException e) {}
	    }
	    Utils.progressMonitor = progressMonitor;
	    Utils.topLevelFrame = topLevelFrame;
	    final SwingWorker worker = new SwingWorker() {
		    public Object construct() { 
			runner = new Runner(params); 
			try {
			    runner.start(); 
			} catch (OutOfMemoryError e) {
			    Utils.fatalException("Please refer to help button", e);
			} catch (Exception e) {
			    //			    if (e instanceof TerminalException)
			    //				Utils.popupError(((TerminalException) e).msg, e);
			    //			    else
			    Utils.fatalException("Fatal Error", e);
			}
			return null;
		    }
		    public void finished() { 
			runner.end();
			if (terminate) System.exit(0);
			runButton.setEnabled(true); 
		    }
		};
	    worker.start();
	}
	else if (terminate) System.exit(0);
    }

    void setCombo(JComboBox c, String s) {
	for (int i=0; i<c.getItemCount(); i++)
	    if (((String)c.getItemAt(i)).toLowerCase().equals(s))
		c.setSelectedIndex(i);
    }

    void applyParams() {
	for (String s: paneParams.keySet()) {
	    JComponent comp = paneParams.get(s);
	    if (comp instanceof JCheckBox) 
		((JCheckBox) comp).setSelected(params.getBoolean(s));
	    else if (comp instanceof DirectorySelect)
		((DirectorySelect) comp).setText(params.getString(s));
	    else if (comp instanceof WholeNumberField)
		((WholeNumberField) comp).setValue(params.getInteger(s));
	    else if (comp instanceof DoubleNumberField)
		((DoubleNumberField) comp).setValue(params.getDouble(s));
	    else if (comp instanceof JFormattedTextField)
		((JFormattedTextField) comp).setValue(params.getString(s));
	    else if (comp instanceof FileEntry)
		((FileEntry) comp).setText(params.getString(s));
	    else if (comp instanceof JComboBox)
		setCombo((JComboBox) comp, params.getString(s));
	    else System.out.println("Error: unknown component " + s + " " + comp);
	}
	DirectorySelect.prefixes = params.getboolean("prefixes");
	for (int i=0; i<params.toggleType.size(); i++)
	    layersSelect.toggleType((String) params.toggleType.get(i));
	for (int i=0; i<params.toggleSelectedSamples.size(); i++)
	    samplesSelect.toggleSelected((String) params.toggleSelectedSamples.get(i));
	for (int i=0; i<params.toggleSelectedLayers.size(); i++)
	    layersSelect.toggleSelected((String) params.toggleSelectedLayers.get(i));
    }

    boolean paramsFromInterface() {
	for (JComponent c: paneParams.values())
	    if (c instanceof JFormattedTextField) {
		try {
		    ((JFormattedTextField) c).commitEdit();
		} catch (java.text.ParseException e) {
		    Utils.echoln(e.toString());
		}
	    }

	for (String s: paneParams.keySet()) {
	    Parameter param = params.getParameter(s);
	    JComponent comp = paneParams.get(s);
	    if (comp instanceof JCheckBox) 
		param.setValue(Boolean.toString(((JCheckBox) paneParams.get(s)).isSelected()));
	    else if (comp instanceof WholeNumberField)
		param.setValue(((WholeNumberField) comp).getValue().toString());
	    else if (comp instanceof DoubleNumberField)
		param.setValue(((DoubleNumberField) comp).getValue().toString());
	    else if (comp instanceof JFormattedTextField)
		param.setValue((String) ((JFormattedTextField) comp).getValue());
	    else if (comp instanceof FileEntry)
		param.setValue(((FileEntry) comp).getText().trim());
	    else if (comp instanceof DirectorySelect)
		param.setValue(((DirectorySelect) comp).getText().trim());
	    else if (comp instanceof JComboBox)
		param.setValue(((String) (((JComboBox) comp).getSelectedItem())));
	    else System.out.println("Error: unknown component");

	}
	params.species = samplesSelect.getSelected();
	params.layers = layersSelect.getSelected();
	params.unusedLayers = layersSelect.getSelected(false);
	params.layerTypes = layersSelect.getTypes();
	getFeatureTypes();
	if (!params.getString("biasFile").equals("")) params.setValue("biasType", Layer.F_BIAS_OUT);
	else params.setValue("biasType", 0);
	return true;
    }

    void getFeatureTypes() {
	for (int i=0; i<featureTypes.length; i++) {
	    String type = featureTypes[i];
	    boolean selected = featureButtons[i].isSelected();
	    if (type.equals("Auto"))
		params.setValue("autofeature", selected);
	    else params.setValue(type, selected);
	}
    }

    void popupError(String s, Throwable e) { Utils.popupError(s, e); }
}
