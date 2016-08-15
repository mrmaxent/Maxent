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

import gnu.getopt.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.event.*;
// import com.sun.image.codec.jpeg.*;

public class Show {
    Display theDisplay;
    boolean blackandwhite = false, visible = true, makeLegend = true, dichromatic = false;
    String sampleFile=null, testSampleFile=null, sampleName=null, initialFile = null, outputDir = null;
    String classColors = null, classNames = null;
    int initialMode = Display.PLAIN, breakpoint = 50;
    String suffix="";
    NumberFormat nf = NumberFormat.getNumberInstance();
    JLabel loc = new JLabel("                                          ");
    File showingFile=null;
    Grid showingGrid=null;
    FileEntry fileEntry, samplesEntry;
    int sampleRadius = 7, minrows = 1200, mincols = 1600, maxRowsAndCols=1000;
    JFrame frame;
    double minval=-1, maxval=-1;

    public Show(String[] args) {
	String usage = "Usage: density.Show [-B breakpoint] [-b] [-l] [-s samplefile] [-e testSampleFile] [-S samplename] [-t] [-o outfile] datafile [datafile ...]";
	boolean outputToFile = false;
	int c;
	Getopt g = new Getopt("Show", args, "A:e:F:B:blos:S:r:v:fO:VxR:C:Lm:M:c:aX:Y:d:Nk:Z:t:n:DpT2");

	while ((c=g.getopt()) != -1) {
	    switch(c) {
		//	    case 'p': Display.xOffset = Integer.parseInt(g.getOptarg()); break;
		//	    case 'P': Display.yOffset = Integer.parseInt(g.getOptarg()); break;
	    case '2': dichromatic = true; break;
	    case 'p': GridIO.compressGrids = false; break;
	    case 'd': Display.divisor = Double.parseDouble(g.getOptarg()); break;
	    case 'X': Display.xline = Double.parseDouble(g.getOptarg()); break;
	    case 'Y': Display.yline = Double.parseDouble(g.getOptarg()); break;
	    case 'F': Display.maxFracDigits = Integer.parseInt(g.getOptarg()); break;
	    case 'k': maxRowsAndCols = Integer.parseInt(g.getOptarg()); break;
	    case 'c': Display.numCategories = Integer.parseInt(g.getOptarg()); break;
	    case 't': suffix="_thumb"; GridIO.maxRowsAndCols = Integer.parseInt(g.getOptarg()); break;
	    case 'A': Display.setCategories(g.getOptarg()); break;
	    case 'N': Display.makeNorth = true; break;
	    case 'a': Display.addTinyVals = false; break;
	    case 'l': initialMode = Display.LOG; break;
	    case 'e': testSampleFile = g.getOptarg(); break;
	    case 's': sampleFile = g.getOptarg(); break;
	    case 'S': sampleName = g.getOptarg(); break;
	    case 'b': blackandwhite = true; break;
	    case 'B': breakpoint = Integer.parseInt(g.getOptarg()); break;
	    case 'o': outputToFile = true; break;
	    case 'O': outputDir = g.getOptarg(); break;
	    case 'r': sampleRadius = Integer.parseInt(g.getOptarg()); break;
	    case 'f' : SampleSet.setNCEAS_FORMAT(); break;
	    case 'x' : visible = false; break;
	    case 'R' : minrows = Integer.parseInt(g.getOptarg()); break;
	    case 'C' : mincols = Integer.parseInt(g.getOptarg()); break;
	    case 'L' : makeLegend = false; break;
	    case 'm' : minval = Double.parseDouble(g.getOptarg()); break;
	    case 'M' : maxval = Double.parseDouble(g.getOptarg()); break;
	    case 'Z' : classColors = g.getOptarg(); break;
	    case 'n' : classNames = g.getOptarg(); break;
	    case 'D' : Display.setNumCategoriesByMax = true; break;
	    case 'T' : Display.toggleSampleColor = false; break;
	    default: System.out.println(usage); System.exit(0);
	    }
	}
	if (outputToFile) {
	    visible = false;
	    Grid grid = Grid.vals2Grid(new float[600][600], "");
	    theDisplay = new Display(grid, minrows, mincols);
	    theDisplay.visible = false;
	    if (blackandwhite) theDisplay.setColorScheme(0); // ugly
	    if (dichromatic) {
		theDisplay.dichromatic = true;
		if (classColors!=null) {
		    int[] colors = theDisplay.stringToColors(classColors);
		    theDisplay.dichromaticColors = new Color[] { new Color(colors[0]), new Color(colors[1]) };
		}
	    }
	    theDisplay.setBreakpoint(breakpoint);
	    if (!makeLegend) theDisplay.makeLegend=false;
	    if (minval!=-1 || maxval!=-1) {
		theDisplay.setMinval(minval);
		theDisplay.setMaxval(maxval);
	    }
	    theDisplay.setMode(initialMode);
	    if (classNames!=null) theDisplay.setClassNames(classNames);
	    if (classColors!=null && !dichromatic)
		theDisplay.setColorClasses(classColors);
	    for (int i=g.getOptind(); i<args.length; i++) {
		showFile(new File(args[i]));
		if (sampleFile!=null && sampleSet==null) setSamples(new File(sampleFile));
		if (testSampleFile!=null) setSamples(new File(testSampleFile), true);
		theDisplay.writeImage(toPngName(true));
	    }
	    return;
	}
	if (args.length > g.getOptind()) {
	    initialFile = args[g.getOptind()];
	    buildShow();
	    if (outputToFile)
		theDisplay.writeImage(toPngName(true));
	    for (int i=g.getOptind()+1; i<args.length; i++) {
		fileEntry.setText(args[i]);
		if (outputToFile)
		    theDisplay.writeImage(toPngName(true));
	    }
	}
	else buildShow();
    }

    SampleSet sampleSet = null, testSampleSet = null;
    void setSamples(File f) { setSamples(f, false); }
    void setSamples(File f, boolean isTest) {
	try {
	    if (isTest)
		testSampleSet = new SampleSet2(f.getPath(), showingGrid.getDimension(), null);
	    else
		sampleSet = new SampleSet2(f.getPath(), showingGrid.getDimension(), null);
	    theDisplay.sampleRadius = sampleRadius;
	    showSamples();
	    theDisplay.makeImage();
	}
	catch (IOException e) { 
	    System.out.println("Error reading samples from " + sampleFile);
	}
    }
    void showSamples() {
	for (SampleSet ss: new SampleSet[] { sampleSet, testSampleSet }) {
	    if (ss!=null) {
		String species;
		if (sampleName!=null) 
		    species = sampleName;
		else {
		    String inFile = showingFile.getName();
		    species = (inFile.endsWith(".asc")||inFile.endsWith(".mxe")) ? inFile.substring(0,inFile.length()-4) : inFile;
		}
		Sample[] samples = ss.getSamples(species);
		if (samples.length==0) {
		    String[] names = ss.getNames();
		    for (int i=0; i<names.length; i++)
			if (showingFile.getName().startsWith(names[i]))
			    species = names[i];
		    samples = ss.getSamples(species);
		}
		if (ss==sampleSet)
		    theDisplay.setSamples(samples);
		else
		    theDisplay.setTestSamples(samples);
		theDisplay.sampleRadius = sampleRadius;
	    }
	}
    }

    void buildShow() {
	Grid grid = Grid.vals2Grid(new float[600][600], "");
	loc.setHorizontalAlignment(SwingConstants.RIGHT);
	theDisplay = new Display(grid, minrows, mincols);
	theDisplay.setMode(initialMode);
	if (blackandwhite) theDisplay.setColorScheme(0);
	if (dichromatic) theDisplay.dichromatic = true;
	if (classNames!=null) theDisplay.setClassNames(classNames);
	if (classColors!=null&&!dichromatic) theDisplay.setColorClasses(classColors);
	if (!makeLegend) theDisplay.makeLegend=false;
	theDisplay.setBreakpoint(breakpoint);
	if (minval!=-1 || maxval!=-1) {
	    theDisplay.setMinval(minval);
	    theDisplay.setMaxval(maxval);
	}
	nf.setMinimumFractionDigits(4);
	nf.setMaximumFractionDigits(4);
	frame = new JFrame();
	frame.setTitle("Show");
	final Container contentPane = frame.getContentPane();
	contentPane.setLayout(new BorderLayout());

	gui.layouts.PreferredSizeGridLayout psgl =
	    new gui.layouts.PreferredSizeGridLayout(1, 1);
	psgl.setBoundableInterface(new gui.layouts.AspectBoundable());
	JPanel displayPane = new JPanel();
	displayPane.setLayout(psgl);
	displayPane.add(theDisplay);

	contentPane.add(displayPane, BorderLayout.CENTER);

	JPanel controls = new JPanel();

	fileEntry = new FileEntry("Grid file:", new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    File f = new File(e.getActionCommand());
		    showFile(f);
		}}, Utils.fileFilter);

	samplesEntry = new FileEntry("Sample file:", new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    File f = new File(e.getActionCommand());
		    setSamples(f);
		}}, ".csv");
	if (initialFile!=null)
	    fileEntry.setText(initialFile);
	else 
	    theDisplay.makeImage();

	if (sampleFile!=null) 
	    samplesEntry.setText(sampleFile);
	if (testSampleFile!=null) setSamples(new File(testSampleFile), true);

	fileEntry.fileSelectionMode = JFileChooser.FILES_ONLY;
	samplesEntry.fileSelectionMode = JFileChooser.FILES_ONLY;
	if (initialFile!=null) 
	    fileEntry.fc.setCurrentDirectory(new File(initialFile).getParentFile());
	fileEntry.filter = new javax.swing.filechooser.FileFilter() {
		public boolean accept(File f) {
		    if (f.isDirectory()) return true;
		    return f.getName().endsWith(".asc") || f.getName().endsWith(".mxe");
		}
		public String getDescription() { return (".asc and .mxe files"); }
	    };
	samplesEntry.filter = new javax.swing.filechooser.FileFilter() {
		public boolean accept(File f) {
		    if (f.isDirectory()) return true;
		    return f.getName().endsWith(".csv");
		}
		public String getDescription() { return (".csv files"); }
	    };
	controls.add(fileEntry, BorderLayout.NORTH);
	controls.add(samplesEntry);

	JButton previous = new JButton("Previous");
	previous.addActionListener(nextActionListener(false));
	JButton next = new JButton("Next");
	next.addActionListener(nextActionListener(true));
	controls.add(previous);
	controls.add(next);
	final Choice choice = new Choice();
	choice.addItem("Log");
	choice.addItem("Plain");
	choice.addItemListener(new ItemListener () {
		public void itemStateChanged(ItemEvent e) {
		    theDisplay.setMode(choice.getSelectedIndex());
		    theDisplay.makeImage();
		}});
	choice.select(initialMode);
	controls.add(choice);

	final JButton save = new JButton("Save");
	final JFileChooser fc = new JFileChooser();
	fc.addChoosableFileFilter(myFileFilter("asc"));
	fc.addChoosableFileFilter(myFileFilter("mxe"));
	fc.addChoosableFileFilter(myFileFilter("png"));
	save.addActionListener(new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    if (showingFile!=null) {
			fc.setCurrentDirectory(showingFile);
			fc.setSelectedFile(new File(toPngName(false)));
			if (JFileChooser.APPROVE_OPTION==fc.showSaveDialog(contentPane)) {
			    String fileName = fc.getSelectedFile().getPath();
			    if (fileName.endsWith(".asc") || fileName.endsWith(".mxe")) {
				try {
				    new GridWriter(showingGrid, fileName).writeAll();
				}
				catch (IOException ee) { 
				    JOptionPane.showMessageDialog(contentPane, ee.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			    }
			    else
				theDisplay.writeImage(fileName);
			}
		    }
		}});
	controls.add(save);
	
	contentPane.add(controls, BorderLayout.NORTH);
	contentPane.add(loc, BorderLayout.SOUTH);
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e)
		{
		    System.exit(0);
		}
	    });
	if (visible) frame.pack();
	if (showingGrid!=null) {
	    GridDimension dim = showingGrid.getDimension();
	    double rs =  maxRowsAndCols / (double) dim.nrows;
	    double cs =  maxRowsAndCols / (double) dim.ncols;
	    double min = (rs<cs) ? rs : cs;
	    if (min<1.0)
		frame.setSize((int) (dim.ncols * min), 50 + (int) (dim.nrows * min));
	}
	frame.setVisible(visible);
	//	theDisplay.setAspectRatio();
    }

    javax.swing.filechooser.FileFilter myFileFilter(final String suffix) {
	return new javax.swing.filechooser.FileFilter() {
		public boolean accept(File f) {
		    if (f.isDirectory()) return true;
		    return f.getName().endsWith("."+suffix);
		}
		public String getDescription() { return ("."+suffix+" files"); }
	    };
    }

    String toPngName(boolean addDir) {
	String inFile = showingFile.getName();
	String out = (inFile.endsWith(".asc")||inFile.endsWith(".mxe")) ? inFile.substring(0,inFile.length()-4) + suffix + ".png" 
	    : inFile+suffix+".png";
	if (addDir)
	    out = (outputDir==null) ? 
		(new File(showingFile.getParent(), out)).getPath()
		: (new File(outputDir + out)).getPath();
	return out;
    }

    ActionListener nextActionListener(final boolean isNext) {
	return new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    if (showingFile!=null) {
			File[] files = showingFile.getAbsoluteFile().getParentFile().listFiles(Utils.filenameFilter);
			int i;
			for (i=0; i<files.length; i++) {
			    if (files[i].getName().equals(showingFile.getName())) break;
			}
			if (i<files.length-1 && isNext) {
			    fileEntry.setText(files[i+1].getAbsolutePath());
			    showSamples();
			    theDisplay.makeImage();
			}
			if (i>0 && !isNext) {
			    fileEntry.setText(files[i-1].getAbsolutePath());
			    showSamples();
			    theDisplay.makeImage();
			}
		    }}};
    }
    
    MouseInputAdapter mml = null;
    void showFile(File f) {
	if (!f.exists())
	    Utils.fatalException("Missing file " + f.getPath(), null);
	showingFile = f;
	try { 
	    showingGrid = GridIO.readGrid(f.getPath()); 
	}
	catch (IOException e) { Utils.fatalException("Error reading files",e); }
	theDisplay.setGrid(showingGrid, minrows, mincols);
	if (visible) {
	    theDisplay.setSize(theDisplay.getPreferredSize());
	    frame.getContentPane().setSize(frame.getContentPane().getPreferredSize());
	    theDisplay.removeMouseMotionListener(mml);
	    theDisplay.removeMouseListener(mml);
	    final GridDimension dim = showingGrid.getDimension();
	    final Grid grid2=showingGrid;
	    theDisplay.addMouseMotionListener(mml = new MouseInputAdapter() {
		    int pressedx = -1, pressedy = -1;
		    public void mouseMoved(MouseEvent e) {
			int mapx = getX(e);
			int mapy = getY(e);
			//			System.out.println(mapx + " " + mapy + " " + theDisplay.getSize().width + " " + theDisplay.getSize().height);
			double x = dim.toX(mapx);
			double y = dim.toY(mapy);
			boolean hasData = grid2.hasData(mapy,mapx);
			double val = hasData ? grid2.eval(mapy,mapx) : grid2.getNODATA_value();
			loc.setText("(" + nf.format(x) + "," + nf.format(y) + "): " + (hasData ? val+"" : " <no data> "));
		    }
		    public void mousePressed(MouseEvent e) {
			pressedx = getX(e); pressedy = getY(e);
		    }
		    public void mouseReleased(MouseEvent e) {
			int x = getX(e), y = getY(e);
			if (pressedx!=-1 && pressedy!=-1 && x!=pressedx && y!=pressedy) {
			    //			    System.out.println("Zoom " + x + " " + y + " " + pressedx + " " + pressedy);
			    theDisplay.setZoom(x, pressedx, y, pressedy);
			    theDisplay.makeImage();
			}
			pressedx = pressedy = -1;
		    }
		    public void mouseDragged(MouseEvent e) {}
		    public void mouseClicked(MouseEvent e) {
			theDisplay.zoomOut();
		    }
		});
	    theDisplay.addMouseListener(mml);
	}
	showSamples();
	theDisplay.makeImage();
    }
	
    int getX(MouseEvent e) { 
	return theDisplay.gridcol(theDisplay.windowx2imgx(e.getX()));
	//	return (int)((e.getX()/(double)theDisplay.getWidth())*(theDisplay.getCols() / theDisplay.scale));
    }
    int getY(MouseEvent e) { 
	return theDisplay.gridrow(theDisplay.windowy2imgy(e.getY()));
	//	return (int)((e.getY()/(double)theDisplay.getHeight())*(theDisplay.getRows() / theDisplay.scale));
    }

    public static void main(String args[]) {
	Show show = new Show(args);
    }
}
