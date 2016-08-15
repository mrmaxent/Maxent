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
import java.util.*;
import java.awt.*;
import java.text.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class Display extends Canvas {
    BufferedImage img;
    int scale, mode;
    public static final int LOG = 0, PLAIN = 1, CLASS = 2;
    int[] classColor;
    String[] className;
    Grid grid;
    public boolean blackandwhite = false, redandyellow = false, dichromatic = false;
    Color[] dichromaticColors = new Color[] { Color.red, Color.blue };
    static boolean toggleSampleColor=true;
    int background;
    public double breakpoint = -9999;
    int[] pixels;
    int minx = -1, maxx = -1, miny = -1, maxy = -1;
    Sample[] samples = null, testsamples = null;
    public boolean visible = true, makeLegend = true;
    public static boolean setNumCategoriesByMax=false;
    static boolean makeNorth = false;
    static int defaultSampleRadius = 7;
    static int adjustSampleRadius = 0;
    static int numCategories = 14;
    static double[] categories = null;
    static int maxFracDigits = -1;
    static double divisor = 2.0;
    double minval=-1, maxval=-1;
    int xOffset = -1, yOffset = -1;

    double aspect(int x1, int x2, int y1, int y2) {
	return (y2-y1) / (double) (x2-x1);
    }
    void setZoom(int x1, int x2, int y1, int y2) {
	minx = (x1<x2) ? x1 : x2;
	maxx = (x1>x2) ? x1 : x2;
	miny = (y1<y2) ? y1 : y2;
	maxy = (y1>y2) ? y1 : y2;
	if (nozoom()) return;
	double target = aspect(0,getCols(),0,getRows());
	if (aspect(minx,maxx,miny,maxy) < target)
	    maxy = miny + (int) (target * (maxx-minx));
	else
	    maxx = minx + (int) ((maxy-miny) / target);
    }

    void zoomOut() { 
	if (nozoom()) return;
	if (minx==0 && maxx==getCols() && miny==0 && maxy==getRows()) return;
	minx = 0; maxx = getCols(); miny = 0; maxy = getRows();
	makeImage();
    }
    public void setClassNames(String s) { setClassNames(s.split(":")); }
    public void setClassNames(String[] s) { 
	className = s;
	if (minval == -1) {
	    minval = 0;
	    maxval = className.length-1;
	    numCategories = className.length;
	}
	else numCategories = (int) (maxval-minval+1);
	mode = PLAIN;
    }
    int[] stringToColors(String s) {
	String[] colors = s.split(" ");
	int[] result = new int[colors.length];
	for (int i=0; i<colors.length; i++) {
	    if (colors[i].indexOf("|") != -1) {
		String[] rgb = colors[i].split("\\|");
		int[] rgbi = new int[3];
		for (int j=0; j<3; j++) rgbi[j] = Integer.parseInt(rgb[j]);
		result[i] = new Color(rgbi[0], rgbi[1], rgbi[2]).getRGB();
	    } else try {
		result[i] = Integer.decode(colors[i]).intValue();
	    } catch (NumberFormatException e) {
		try {
		    result[i] = ((Color) Color.class.getField(colors[i]).get(null)).getRGB();
		} catch (Exception ee) { throw new NumberFormatException("Invalid color: " + colors[i]); }
	    }
	}
	return result;
    }
    public void setColorClasses(String s) {
	classColor = stringToColors(s);
	//	minval=0;
	//	maxval = classColor.length-1;
	mode = CLASS;
	//	numCategories = classColor.length;
    }

    static void setCategories(String s) {
	String[] cats = s.split(" ");
	categories = new double[cats.length];
	for (int i=0; i<cats.length; i++)
	    categories[i] = Double.parseDouble(cats[i]);
	numCategories = cats.length;
    }

    static double xline = -1.0, yline = -1.0;
    int linerow, linecol;
    void setline() { 
	linerow = linecol = -1;
	if (xline!=-1.0) 
	    linerow = grid.getDimension().toRow(xline)*scale;
	if (yline!=-1.0) 
	    linecol = grid.getDimension().toCol(yline)*scale;
    }

    public void setMinval(double m) { minval = m; }
    public void setMaxval(double m) { maxval = m; }
    public void setMode(int i) { mode = i; }
    void setBreakpoint(double x) { breakpoint = x; }
    void setColorScheme(int i) { blackandwhite = (i==0); }
    //    void setGrid(Grid grid) { setGrid(grid, 1200, 1600); }
    public void setGrid(Grid grid, int minrows, int mincols) { 
	this.grid = grid; 
	scale = 1;
	double xscale = Math.floor(mincols/(double)getCols());
	double yscale = Math.floor(minrows/(double)getRows());
	scale = (int) ((xscale < yscale) ? xscale : yscale);
	if (scale < 1) scale = 1;
	img = new BufferedImage(getCols(), getRows(), BufferedImage.TYPE_INT_RGB);
	pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
	setZoom(-1,-1,-1,-1);
	setline();
	if (blackandwhite) newborder();
    }
    void setSamples(Sample[] s) { samples = s; }
    void setTestSamples(Sample[] s) { testsamples = s; }

    double max(double x, double y) { return (x>y) ? x : y; }
    GridDimension viewDimension() { 
	GridDimension dim = grid.getDimension();
	double cs = dim.getcellsize()/scale;
	return new GridDimension(dim.getxllcorner()+max(minx,0)*cs, 
				 dim.getyllcorner()+(maxy==-1 ? 0 : (getRows()-maxy))*cs, 
				 cs * (maxx==-1 ? 1 : (maxx-minx)/(double)getCols()), getRows(), getCols());
    }
    int getRows() { return scale * grid.getDimension().nrows; }
    int getCols() { return scale * grid.getDimension().ncols; }
    boolean inBounds(int r, int c) { 
	return r>=0 && r < getRows() && c>=0 && c<getCols(); 
    }

    public Display(Grid grid, int minrows, int mincols) { 
	setGrid(grid, minrows, mincols); 
    }

    public Display(Grid grid) { setGrid(grid, 1200, 1600); }

    int white = (255<<24) | (255<<16) | (255<<8) | 255;
    int black = Color.black.getRGB();
    int sampleColor = white;
    int testSampleColor = (255<<24) | (138<<16) | (43<<8) | 226;  // violet
    int sampleRadius = defaultSampleRadius;
    double min=0, max=0;

    boolean nozoom() { return minx==-1 || maxx==-1 || miny==-1 || maxy==-1; }

    int windowx2imgx(int x) { 
	return (int) (getCols()/(double)getSize().width * x);
    }
    int windowy2imgy(int y) {
	return (int) (getRows()/(double)getSize().height * y);
    }
    int gridrow(int r) {
	if (nozoom()) return r/scale;
	int rr = (int) (miny + r/(double)getRows() * (maxy-miny));
	return rr/scale;
    }
    int gridcol(int c) {
	if (nozoom()) return c/scale;
	int cc = (int) (minx + c/(double)getCols() * (maxx-minx));
	return cc/scale;
    }
    boolean hasData(int r, int c) {
	return grid.hasData(gridrow(r), gridcol(c));
    }
    float eval(int r, int c) {
	return grid.eval(gridrow(r), gridcol(c));
    }

    public void makeImage() {
	boolean start=true;
	background = redandyellow?(255|(128<<16)|(128<<8)):
	    blackandwhite?white:255<<24;
	//	    255<<24;
	if (minval != -1 || maxval != -1) {
	    min = minval;
	    max = maxval;
	}
	else {
	    for (int i=0; i<getRows(); i++)
		for (int j=0; j<getCols(); j++) {
		    if (!hasData(i,j)) continue;
		    if (start) {
			min=max=eval(i,j);
			start = false;
		    }
		    else {
			float val = eval(i,j);
			if (((val < min) && (mode!=LOG || val>0)) 
			    || (min<=0 && mode==LOG))
			    min = val;
			if (val > max) max = val;
		    }
		}
	    if (grid instanceof LazyGrid) {
		try { 
		    ((LazyGrid) grid).initialize();
		} catch (IOException e) {
		    Utils.fatalException("Error initializing file " + grid.name, null);
		}
	    }
	}
	// makes cumulative distributions look good
	if (max==100.0 && min >= 0 && min < .00001) 
	    min=.00001;
	if (max==100.0 && min >= 0 && min < .001 && blackandwhite) 
	    min=.01;
	// makes plain distributions look good
	if (min > 0 && max/min > 1e15) min = max/1e15;
	// makes logistic distributions look good
	if (min >= 0 && min <= 0.1 && max >= 0.7 && max <= 1.0 && mode!=LOG && maxval==-1) {
	    min = 0;
	    max = 1.0;
	    numCategories = 11;
	}

	for (int i=0; i<getRows(); i++) {
	    Utils.reportProgress((i*100)/(double)getRows());
	    for (int j=0; j<getCols(); j++)
		pixels[i*getCols()+j] = (i==linerow) ? white :
		    (j==linecol) ? white :
		    hasData(i,j) ? 
		    showColor(eval(i,j), min, max) : 
		    blackandwhite && isborder(i,j) ? black : background;
	
	}
	int sr = sampleRadius;
	int rr = getRows() * scale, cc = getCols() * scale;
	if (scale > 1) sr = (int) Math.ceil(sr / (double) scale);
	if (rr < 900 && cc < 900 && sr*scale >= 5 && sr > 2)
	    sr -= 2;
	if (rr < 600 && cc < 600 && sr*scale >= 5 && sr > 2)
	    sr -= 2;
	if (rr < 300 && cc < 300 && sr*scale >= 5 && sr > 2)
	    sr -= 2;
	if (rr > 2000 || cc > 2000)
	    sr += 1/scale;
	if (rr > 4000 || cc > 4000)
	    sr += 1/scale;
	sr += adjustSampleRadius;
	if (samples!=null) 
	    showSamples(samples, sampleColor, sr);
	if (testsamples!=null)
	    showSamples(testsamples, testSampleColor, sr);
	if (makeLegend) makeLegend();
	if (makeNorth) makeNorth();
	if (visible) repaint();
    }

    /*
    boolean isborder(int i, int j) {
	for (int id = -4; id <= 4; id++)
	    for (int jd = -4; jd <= 4; jd++)
		if (id!=0 && jd!=0 && grid.hasData(i/scale+id, j/scale+jd))
		    return true;
	return false;
    }
    */

    int maxborder=5;
    int[][] bdist;
    void newborder() { 
	int nr = getRows(), nc = getCols();
	bdist = new int[nr][nc]; 
	for (int i=0; i<getRows(); i++)
	    for (int j=0; j<getCols(); j++)
		bdist[i][j] = hasData(i, j) ? 0 : 100000;
	for (int iter=0; iter<maxborder; iter++)
	    for (int i=0; i<nr; i++)
		for (int j=0; j<nc; j++)
		    for (int id=-1; id<=1; id+=2)
			for (int jd=-1; jd<=1; jd+=2) {
			    int ii = i+id, jj = j+jd;
			    if (ii<0 || jj<0 || ii>=nr || jj>=nc) continue;
			    if (bdist[ii][jj] < bdist[i][j]-1)
				bdist[i][j] = bdist[ii][jj]+1;
			}
    }
    boolean isborder(int i, int j) { return (bdist[i][j] < maxborder); }
	
    void showSamples(Sample[] samples, int color, int sr) {
	GridDimension dim = viewDimension();
	int[] under = null;
	if (blackandwhite && toggleSampleColor)
	    under = (int[]) pixels.clone();
	for (int i=0; i<samples.length; i++) {
	    int r = samples[i].getRow(dim), c = samples[i].getCol(dim);
	    int color2 = color;
	    if (blackandwhite && toggleSampleColor) {
		int cnt=0;
		int tot=0;
		for (int j=(-sr+1)*scale; j<sr*scale; j++)
		    for (int k=(-sr+1)*scale; k<sr*scale; k++)
			if (inBounds(r+j, c+k)) {
			    cnt++;
			    tot += new Color(under[(r+j)*getCols()+(c+k)]).getBlue();
			}
		if (cnt>0 && (tot / cnt) > 128) color2 = black;
	    }
	    else if (blackandwhite) color2 = black;
			
	    for (int j=(-sr+1)*scale; j<sr*scale; j++)
		for (int k=(-sr+1)*scale; k<sr*scale; k++)
		    if (inBounds(r+j, c+k))
			pixels[(r+j)*getCols()+(c+k)] = color2;
	}
    }

    static boolean addTinyVals = true;
    void makeLegend() {
	int num = setNumCategoriesByMax ? (int) max+1 : numCategories;
	double[] vals = (categories == null) ? new double[num] : categories;
	int fontSize = (getRows() > 2000 || getCols() > 2000) ? 24 :
	    (getRows() > 1000 || getCols() > 1000) ? 18 : 11;
	if (categories == null) {
	    if (mode==LOG) {
		double x=max;
		if (max > 50 && max <= 100) {
		    for (int i=0; i<num; i++) {
			vals[i] = x;
			x/=divisor;
		    }
		    if (addTinyVals) {
			vals[num-3] = 0.01; 
			vals[num-2] = 0.001;
			vals[num-1] = (min > .0001) ? .0001 : min;
		    }
		    else
			vals[num-1] = 0.0;
		}
		else {
		    double div = Math.exp(Math.log(min<max/1e15?1e15:max/min) / (num-1));
		    for (int i=0; i<num; i++) {
			vals[i] = x;
			x /= div;
		    }
		    //		    vals[num-1] = (min < vals[num-2]) ? min : vals[num-2];
		}
		
	    }
	    else {
		for (int i=0; i<num; i++)
		    vals[i] = max - i*(max-min)/(num-1);
		if (vals[num-1] < .01 && vals[num-2] > 1)
		    vals[num-1] = 0;
	    }
	    if (min==max) { 
		num = 1; 
		vals = new double[] { min }; 
	    }
	}
	Graphics2D g = (Graphics2D) img.getGraphics();
	Font font = new Font("Dialog", 1, fontSize);
	g.setFont(font);
	FontMetrics fm = g.getFontMetrics(font);
	int height = fm.getHeight()+2;
	NumberFormat nf = (max <= 1 && mode!=LOG) ? new DecimalFormat() : 
	    NumberFormat.getNumberInstance();
	nf.setGroupingUsed(false);
	String[] labels = new String[num];
	int legendWidth=0;
	for (int i=0; i<num; i++) {
	    if (max < 0.5 || max<2 && mode==LOG)
		((DecimalFormat) nf).applyPattern("0.#E0");
	    else
		nf.setMaximumFractionDigits(maxFracDigits!=-1 ? maxFracDigits : vals[i]>=1 ? 1 : vals[i] >= .01 ? 2 : vals[i] >= .001 ? 3 : vals[i] > 0.0001 ? 4 : vals[i] < 0 ? 1 : 5);
	    labels[i] = (className==null || className.length<=vals[i]) ? nf.format(vals[i]) : className[(int) vals[i]];
	    int w = fm.stringWidth(labels[i]);
	    if (w>legendWidth) legendWidth = w;
	}
	computeOffsets(legendWidth+6+2*height, (num+2)*height);
	for (int i=0; i<num; i++) {
	    int y = getRows() - (num-i+1) * height - yOffset;
	    g.setColor(new Color(showColor(vals[i], min, max)));
	    g.fill(new Rectangle(xOffset + 4, y, height, height));
	    g.setColor(blackandwhite?Color.black:Color.white);
	    //	    g.setColor(Color.white);
	    g.drawString(labels[i], xOffset + 6+height, y+height-2);
	}
    }

    void computeOffsets(int w, int h) {
	xOffset = 0;  // lower left
	yOffset = 0;
	int overlap = computeOverlap(0,0,w,h);
	if (overlap==0) return;
	int overlap2 = computeOverlap(getCols()-w,0,w,h);  // lower right
	if (overlap2 < overlap) {
	    overlap = overlap2;
	    xOffset = getCols() - w;
	    yOffset = 0;
	}
	if (overlap==0) return;
	overlap2 = computeOverlap(getCols()-w,getRows()-h,w,h); // top right
	if (overlap2 < overlap) {
	    overlap = overlap2;
	    xOffset = getCols() - w;
	    yOffset = getRows() - h;
	}
	if (overlap==0) return;
	overlap2 = computeOverlap(0,getRows()-h,w,h); // top left
	if (overlap2 < overlap) {
	    overlap = overlap2;
	    xOffset = 0;
	    yOffset = getRows() - h;
	}
    }

    int computeOverlap(int llx, int lly, int w, int h) {
	int cnt=0;
	for (int y=lly; y<lly+h; y++)
	    for (int x=llx; x<llx+w; x++)
		if (nonBackground(getRows()-y,x)) cnt++;
	return cnt;
    }
    boolean nonBackground(int r, int c) { 
	int i = r*getCols()+c;
	return (i>=0 && i < pixels.length && 
		pixels[i] != background);
    }

    void makeNorth() {
	Graphics2D g = (Graphics2D) img.getGraphics();
	g.setColor(blackandwhite? Color.black : Color.white);
	Font font = new Font("Dialog", 1, 48);
	g.setFont(font);
	FontMetrics fm = g.getFontMetrics(font);
	int height = fm.getHeight()+2;
	int nx = (int) (getCols() * .95);
	g.drawString("N", nx, (int) (getRows() *.1) + height);
	int x = nx + fm.stringWidth("N")/2;
	int yb = (int) (getRows() *.1 - height*1.25);
	int yheight = (int) (getRows() *.05);
	int w = 6;
	g.fillRect(x-w/2, yb, w, yheight);
	g.fillPolygon(new int[] { x, x+3*w, x-3*w }, new int[] { yb-w/2, yb+3*w, yb+3*w }, 3);
    }



    public void writeImage(String outFile) { writeImage(outFile, 1); }
    void writeImage(String outFile, int magstep) {
	int ncols=getCols(), nrows=getRows();
	BufferedImage toWrite = (magstep==1) ? img :
	    new BufferedImage(ncols*magstep, nrows*magstep, BufferedImage.TYPE_INT_RGB);
	if (magstep>1) {
	    int[] p = ((DataBufferInt)toWrite.getRaster().getDataBuffer()).getData();
	    for (int r=0; r<nrows*magstep; r++)
		for (int c=0; c<ncols*magstep; c++)
		    p[r*ncols*magstep+c] = pixels[(r/magstep)*ncols + c/magstep];
	}
	try {
	    ImageIO.write(toWrite, "png", new File(outFile));
	} catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	}
    }

    /*
    void setAspectRatio() {
	if (getHeight() < getRows() && getWidth() == getCols()) 
	    setSize(new Dimension((int) (getCols()*getHeight() / (double) getRows()), getHeight()));
	if (getWidth() < getCols() && getHeight() == getRows()) 
	    setSize(new Dimension(getWidth(), (int) (getRows()*getWidth() / (double) getCols())));
    }
    */

    int showColor(double val, double min, double max) {
	if (mode == CLASS) return classColor[(int) val];
	if (mode == LOG) {
	    val = (val<=min)?Math.log(min):Math.log(val);
	    min = (min<=0)?0:Math.log(min);
	    max = (max<=0)?0:Math.log(max);
	}
	int red, green, blue;
	if (val < min) val = min;
	if (val > max) val = max;
	if (dichromatic) {
	    if (breakpoint==-9999) breakpoint = (max-min)/2;
	    double frac = (val < breakpoint) ?
		(breakpoint - val) / (breakpoint-min) :
		(val - breakpoint) / (max - breakpoint);
	    int end = (val < breakpoint) ? 0 : 1;
	    return fadedColor(dichromaticColors[end], frac);
	} else if (redandyellow) {
	    if (breakpoint==-9999) breakpoint = 50;
	    int index = (int) ((max - val) * 100.0 / (max-min));
	    red = 255;
	    green = (int) ((index<breakpoint)?(index*255/breakpoint):255);
	    blue = (int) ((index<breakpoint)?0:(index-breakpoint)*255/(511-breakpoint));
	}
	else if (blackandwhite) {
	    //	    double scale = Math.sqrt(max) - (min<0?-Math.sqrt(-min):Math.sqrt(min));
	    //	    double index = (Math.sqrt(max) - (val<0?-Math.sqrt(-val):Math.sqrt(val))) / scale;
	    double index = (max - val) / (max - min);
	    //	    red = green = blue = (int) (220*index) + 20;
	    red = green = blue = (int) (220*index) + 30;
	}
	else {
	    int i = (int) ((max - val) * 1020.0 / (max-min));
	    red = (i<256)?255:(i>510) ? 0 : 510-i;
	    green = (i<256)?i:(i<765)?255:1020-i;
	    blue = (i<510)?0:(i<765)?i-510:255;
	}
	return ((255<<24) | (red<<16) | (green<<8) | blue);
    }

    int fadedColor(Color c, double frac) {
	int[] rgb = new int[] { c.getRed(), c.getGreen(), c.getBlue() };
	for (int i=0; i<3; i++)
	    rgb[i] = rgb[i] + (int) ((1-frac) * (255-rgb[i]));
	return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
    }

    public void paint(Graphics g) {
	int w = getSize().width;
	int h = getSize().height;
	if (img != null) {
	    g.drawImage(img, 0, 0, w, h, this);
	}
    }

    // don't blank canvas first
    public void update(Graphics g) {paint(g);}

    public Dimension getPreferredSize() {
	return new Dimension(getCols(), getRows());
    }
}
