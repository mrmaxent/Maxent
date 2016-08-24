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

/* To do, perhaps:
     Entries for arguments
     Drag on response curve: highlight areas in map with those values
     Drag on map: highlight range of vars in response curves
     Or drag on map drags map
     Color schemes
     Prettier layout
*/

import gnu.getopt.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.text.*;

public class Explain extends JFrame implements ActionListener {
    GridZoom zoom;
    Grid[] predictors = new Grid[0];
    Grid pred, projgrid, proj2grid;
    String predfile, responsedir, species;
    ResponsePlot[] plots;
    ResponsePlot logitplot;
    double[] mean, contribs;
    float[][] minmax;
    boolean[] isCategorical;
    boolean cloglog;
    HashMap map;
    int nvars=0;
    NumberFormat nf;
    JLabel pointval;
    Project proj, proj2;
    Csv meancsv;
    JDialog helpFrame = null;

    static final private String HELP = "help";
    static final private String LEFT = "left";
    static final private String RIGHT = "right";
    static final private String UP = "up";
    static final private String DOWN = "down";
    static final private String IN = "in";
    static final private String OUT = "out";
    static final private String CENTER = "center";
    static final private String MODEL = "model";

    public Explain(String[] args) throws IOException {
	String usage = "Usage: density.Explain [-c] [-l lambdafile] predictionFile predictorsDirectory";
	Getopt g = new Getopt("Show", args, "cl:");
	String lambdafile=null;

	int gg;
	while ((gg=g.getopt()) != -1) {
	    switch(gg) {
	    case 'l': lambdafile = g.getOptarg(); break;
	    case 'c': cloglog = true; break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	if (args.length -g.getOptind() < 2) {
	    System.out.println(usage);
	    System.exit(0);
	}
	predfile = args[g.getOptind()];
	String griddir = args[g.getOptind()+1];
	Utils.topLevelFrame = this;
	nf = NumberFormat.getNumberInstance(Locale.US);
	nf.setGroupingUsed(false);
	try {
	    pred = GridIO.readGrid(predfile);
	    String root = predfile.substring(0, predfile.length()-4);
	    if (lambdafile==null)
		lambdafile = root+".lambdas";
	    else
		root = lambdafile.replaceAll(".lambdas$","");
	    meancsv = new Csv(root+"_sampleAverages.csv");
	    meancsv.indexAll();
	    if (args.length>1) {
		System.out.println("Loading " + lambdafile);
		map = new HashMap();
	        Params params = new Params();
	        if (cloglog) params.setOutputformat("Cloglog");
		proj = new Project(params);
		proj.mapping = true;
		proj.varmap = map;
		projgrid = proj.projectGrid(lambdafile, null)[0];
		String[] files = Utils.gridFileNames(griddir, proj.gridNames());
		nvars = files.length;
		predictors = new Grid[nvars];
		minmax = new float[nvars][];
		for (int i=0; i<nvars; i++) {
		    String f = Extractor.CachedGridFilename(files[i], false);
		    System.out.println("Loading " + f);
		    predictors[i] = GridIO.readGrid(f);
		    minmax[i] = predictors[i].minmax();
		}
		initIsCategorical();
		proj2 = new Project(params);
		proj2.mapping = true;
		proj2.varmap = map;
		proj2grid = proj2.projectGrid(lambdafile, null)[0];
		for (Project.Pfeature pf: proj.allFeatures)
		    if (pf instanceof Project.ScaledGrid && ((Project.ScaledGrid) pf).g0 instanceof Project.ProductGrid)
			Utils.warn2("Model in " + lambdafile + " is not additive.  This tool requires a model made without product features", "notadditive");
		responsedir = new File(new File(root).getParent(), "plots").getPath(); 
		species = new File(root).getName();
		setTitle("Explain predictions for " + species);
	    }
	} catch(IOException e) { 
	    System.out.println(e.toString());
	    System.exit(1); 
	}
	zoom = new GridZoom(pred) {
		public void mouseClicked(int r, int c) {
		    showClicked(r, c);
		}
		public void mousePointAt(int r, int c) {
		    GridDimension dim = zoom.grid.getDimension();
		    nf.setMaximumFractionDigits(8);
		    pointval.setText("(" + nf.format(dim.toX(c)) + ", " + nf.format(dim.toY(r)) + "): " + (zoom.grid.hasData(r,c) ? nf.format(zoom.grid.eval(r,c)) : "<no data>"));
		}
	    };
	JToolBar toolBar = makeToolBar();
		
	JPanel displayPane = new JPanel();
	displayPane.setPreferredSize(new Dimension(800, 800));
	displayPane.setLayout(new BorderLayout());
	displayPane.add(toolBar, BorderLayout.PAGE_START);
	displayPane.add(zoom, BorderLayout.CENTER);
	displayPane.add(pointval=new JLabel(" "), BorderLayout.PAGE_END);

	final Container contentPane = getContentPane();
	contentPane.add(displayPane, BorderLayout.CENTER);

	if (responsedir!=null) {
	    JPanel responsePane = new JPanel();
	    responsePane.setLayout(new GridLayout(nvars,1));
	    plots = new ResponsePlot[nvars+1];
	    for (int i=0; i<nvars; i++)
		responsePane.add(plots[i] = response(i));
	    JScrollPane scrollPane = new JScrollPane(responsePane);
	    scrollPane.setPreferredSize(plots[0].getPreferredSize());
	    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    JPanel responsePane2 = new JPanel();
	    responsePane2.setLayout(new BorderLayout());
	    responsePane2.add(logitplot(), BorderLayout.PAGE_START);
	    responsePane2.add(scrollPane, BorderLayout.CENTER);
	    contentPane.add(responsePane2, BorderLayout.EAST);
	}

	pack();
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) { System.exit(0); }
	    });
	setVisible(true);
    }
    
    void initIsCategorical() {
	isCategorical = new boolean[nvars];
	for (int j=0; j<nvars; j++)
	    isCategorical[j] =
		"true".equals(meancsv.getVal(predictors[j].getName(), "Categorical"));
    }

    void initProjGrid() {
	for (int j=0; j<nvars; j++) {
	    String var = predictors[j].getName();
	    map.put(var, meancsv.getDoubleVal(var, "Sample average"));
	}
    }

    double contribution(int i, double v) {
	initProjGrid();
	String varname = predictors[i].getName();
	proj.exponent=true;
	double before = projgrid.eval(0,0);
	map.put(varname, v);
	double after = projgrid.eval(0,0);
	// subtract off prorated constant term, so constant in logit is 0
	double offset = (before - Math.log(proj.densityNormalizer) + proj.entropy)/nvars;

	return after - before + offset;
    }

    ResponsePlot logitplot() {
	logitplot = new ResponsePlot();
	logitplot.setSize(300,100);
	logitplot._topPadding = 1;
	logitplot._bottomPadding = 4;
	logitplot._rightPadding = 2;
	double[] x = new double[nvars];
	contribs = new double[nvars];
	for (int i=0; i<nvars; i++) x[i] = i+1;
	logitplot.makeplot(x, new double[nvars], null, true, true, "Contribution to "+(cloglog?"linear predictor":"logit"));
	logitplot.addMouseMotionListener(new MouseMotionListener() {
		public void mouseMoved(MouseEvent e) {
		    double x = logitplot.plotXtoX(e.getX());
		    int ix = (int) Math.round(x);
		    nf.setMaximumFractionDigits(3);
		    if (Math.abs(x - ix) < 0.25 && ix>=1 && ix<=nvars)
			logitplot.setToolTipText(predictors[ix-1].getName() + ": " + nf.format(contribs[ix-1]));
		    else
			logitplot.setToolTipText(null);
		}
		public void mouseDragged(MouseEvent e) {}
	    });
	return logitplot;
    }
	
    ResponsePlot response(int i) throws IOException {
	String varname = predictors[i].getName();
	initProjGrid();
	proj.exponent = false;
	double[][] rpd = Runner.responsePlotData(projgrid, map, varname, minmax[i][0], minmax[i][1], isCategorical[i] ? categories(i) : null, false);

	ResponsePlot plot = new ResponsePlot();
	plot.makeplot(rpd[0],rpd[1],null,true,isCategorical[i],(i+1) + ": " + varname);
	plot.setSize(300,100);
	plot.setYRange(0,1);
	plot.setImpulses(true, 1);
	plot._topPadding = 1;
	plot._bottomPadding = 4;
	plot._rightPadding = 2;
	return plot;
    }

    double[] categories(int i) { 
	Grid g = predictors[i];
	final HashSet<Double> set = new HashSet();
	g.apply(g.new Applier() {
		public void process(int r, int c, float v) { 
		    set.add((double) v); 
		}});
	double[] result = new double[set.size()];
	int cnt=0;
	for (Double d: set) 
	    result[cnt++] = d;
	return result;
    }

    JToolBar makeToolBar() {
	JToolBar toolBar = new JToolBar("Navigation buttons");
	addButton(toolBar, "Help", HELP, "Help", "Help");
	addButton(toolBar, "Back24", LEFT, "Shift left", "<");
	addButton(toolBar, "Up24", UP, "Shift up", "^");
	addButton(toolBar, "Down24", DOWN, "Shift down", "v");
	addButton(toolBar, "Forward24", RIGHT, "Shift right", ">");
	addButton(toolBar, "Plus", IN, "Zoom in", "+");
	addButton(toolBar, "Minus", OUT, "Zoom out", "-");
	addButton(toolBar, "Dot", CENTER, "Recenter", "@");
	if (nvars>0) {
	    String[] varnames = new String[nvars+1];
	    varnames[0] = "Model for " + species;
	    for (int i=0; i<nvars; i++)
		varnames[i+1] = predictors[i].getName();
	    final JComboBox showList = new JComboBox(varnames);
	    showList.setMaximumRowCount(25);
	    showList.setSelectedIndex(0);
	    showList.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			int sel = showList.getSelectedIndex();
			if (sel==0) 
			    zoom.setGrid(pred);
			else 
			    zoom.setGrid(predictors[sel-1]);
			zoom.makeImage();
		    }});
	    toolBar.addSeparator();
	    toolBar.add(showList);
	}
	return toolBar;
    }

    protected void addButton(JToolBar toolBar, String imageName, String actionCommand, String toolTipText, String altText) {
	JButton button = new JButton();
	button.setActionCommand(actionCommand);
	button.setToolTipText(toolTipText);
	button.addActionListener(this);
	button.setText(altText);
	toolBar.add(button);
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (LEFT.equals(cmd)) zoom.right(0.5);
	else if (RIGHT.equals(cmd)) zoom.right(-0.5);
	else if (UP.equals(cmd)) zoom.down(0.5);
	else if (DOWN.equals(cmd)) zoom.down(-0.5);
	else if (IN.equals(cmd)) zoom.adjustZoom(2.0);
	else if (OUT.equals(cmd)) zoom.adjustZoom(0.5);
	else if (CENTER.equals(cmd)) zoom.recenter(true);
	else if (HELP.equals(cmd)) {
	    if (helpFrame==null) {
		createHelpPane();
	    } else {
		helpFrame.setVisible(true);
		helpFrame.toFront();
	    }
	}
    }

    private void createHelpPane() {
	helpFrame = new JDialog(this, "Help for Explain tool for Maxent");
	JEditorPane text = new JEditorPane();
	try { 
	    text.setPage(GUI.class.getResource("helpExplain.html"));
	} catch (Exception e) { 
	    Utils.popupError("Error opening help window", e);
	}
	JScrollPane pane = new JScrollPane(text);
	pane.setPreferredSize(new Dimension(600,400));
	helpFrame.getContentPane().add(pane);
	helpFrame.pack();
	helpFrame.setVisible(true);
    }

    public void showClicked(int r, int c) {
	if (!zoom.grid.getDimension().inBounds(r,c)) return;
	zoom.showDot();
	if (plots==null) {
	    for (int i=0; i<nvars; i++)
		System.out.print(predictors[i].getName() + ": " + (predictors[i].hasData(r,c) ? predictors[i].eval(r,c) : "NA") + "  ");
	    if (nvars>0) System.out.println();
	    return;
	}
	logitplot.clear(0);

	for (int i=0; i<nvars; i++)
	    map.put(predictors[i].getName(), new Double(predictors[i].eval(r,c)));
	proj.exponent = true;
	double logit = projgrid.eval(0,0) - Math.log(proj.densityNormalizer) + proj.entropy;
	double sum=0.0;
	for (int i=0; i<nvars; i++) {
	    if (predictors[i].hasData(r,c)) {
		double v = predictors[i].eval(r,c);
		contribs[i] = contribution(i, v);
		sum += contribs[i];
		plots[i].clear(1);
		plots[i].addPoint(1, v, 1.0, false);
		plots[i].repaint();
		logitplot.addPoint(0, i+1, contribs[i], false);
	    }
	    else {
		plots[i].clear(1);
		logitplot.addPoint(0, i+1, 0, false);
	    }
	}
	nf.setMaximumFractionDigits(3);
	logitplot.setTitle("Contribution to "+(cloglog?"linear predictor":"logit") + (pred.hasData(r,c) ? " (sum=" + nf.format(logit) + ")": ""));
	logitplot.fillPlot(); // causes y-range to be recalculated
    }

    public static void main(String args[]) {
	try {
	    Explain view = new Explain(args);
	} catch (IOException e) { 
	    System.out.println(e.toString()); 
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
