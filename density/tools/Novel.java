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

// to do:  apply mask to current layers - write a density.tools.Mask() to transform grids

import java.io.*;
import density.*;
import gnu.getopt.*;

public class Novel {
    String[] gridnames;
    float[][] minmax;
    double[][] histogram;
    double[][] centrality;  // 0 at edge to 0.5 at median
    Grid[] proj;
    int nf, nbins=100000;
    boolean makeNovelPicture = true, whiteNonNovel=false;
    String maskfile;
    // orange, sky blue, bluish green, yellow, blue, vermilion, reddish purple, lightcoral, black; see jfly.iam.u-tokyo.ac.jp/html/color_blind, and I've added two.
    // Jun 4 2010: removed ivory (255|255|240) from before lightcoral
    String colors="230|159|0 86|180|233 0|158|115 240|228|66 0|114|178 213|94|0 204|121|167 240|128|128 0|0|0";

    public void setWhiteNonNovel() { whiteNonNovel = true; }

    int limitingLayer;
    double similarity(int r, int c) {
	double minc = 200.0;
	int mini = -1;
	for (int i=0; i<nf; i++) {
	    double val = proj[i].eval(r,c), min=minmax[i][0], max=minmax[i][1];
	    double cent = 0.0;
	    if (val < min)
		cent = 100*(val-min)/(max-min);
	    else if (val > max) 
		cent = 100*(max-val)/(max-min);
	    else {
		int bin = Grid.getbin(val, min, max, nbins);
		cent = centrality[i][bin] * 200;
	    }
	    if (cent < minc) {
		mini = i;
		minc = cent;
	    }
	}
	limitingLayer = mini;
	if (minc >= 0 && whiteNonNovel) limitingLayer = nf;
	return minc;
    }

    public void go(String args[]) throws IOException {
	int c;
	Getopt g = new Getopt("Novel", args, "c:m:w");
	if (args.length<3) {
	    System.out.println("Usage: Novel [-m basemask] [-c colors] [-w] basedir projdir outfile");
	    System.exit(0);
	}
	while ((c=g.getopt()) != -1) {
	    switch(c) {
	    case 'c': colors = g.getOptarg(); break;
	    case 'm': maskfile = g.getOptarg(); break;
	    case 'w': whiteNonNovel = true; break;
	    }
	}
	String basedir = args[g.getOptind()];
	String projdir = args[g.getOptind()+1];
	String outfile = args[g.getOptind()+2];
	density.tools.Utils.checkExists(basedir);
	if (!outfile.matches(".*\\....$"))
	    outfile = outfile + ".asc";
	String[] filenames = density.Utils.gridFileNames(basedir);
	makeHistograms(filenames);
	makeNovel(projdir, outfile);
    }

    public void go(Feature[] basefeatures, String projdir, String outfile) throws IOException {
	makeHistograms(basefeatures);
	makeNovel(projdir, outfile);
    }

    void makeNovel(String projdir, String outfile) throws IOException {
	density.tools.Utils.checkExists(projdir);
	for (int i=0; i<nf; i++)
	    proj[i] = new LazyGrid(Extractor.CachedGridFilename(density.Utils.getGridAbsolutePath(projdir, gridnames[i]), true));
	if (whiteNonNovel) gridnames[nf] = "none";
	Grid result = new Grid(proj[0].getDimension(), "Novel") {
		public boolean hasData(int r, int c) {
		    for (int i=0; i<nf; i++)
			if (!proj[i].hasData(r,c)) return false;
		    return true;
		}
		public float eval(int r, int c) {
		    return (float) similarity(r, c);
		}};
	Grid limiting = new Grid(proj[0].getDimension(), "Limiting") {
		public boolean hasData(int r, int c) {
		    for (int i=0; i<nf; i++)
			if (!proj[i].hasData(r,c)) return false;
		    return true;
		}
		public float eval(int r, int c) {
		    return (float) limitingLayer;
		}};
	int index = outfile.lastIndexOf('.');
	String limitingfile = outfile.substring(0,index) + "_limiting" + outfile.substring(index);
//outfile.replaceAll("\\.", "_limiting.");
	GridWriter.writeGrids(new Grid[] {result, limiting}, new String[] {outfile, limitingfile}, false);
	if (makeNovelPicture) {
	    Display d = new Display(new LazyGrid(outfile));
	    d.visible = false;
	    d.makeLegend = true;
	    d.dichromatic = true;
	    float[] minmax = new LazyGrid(outfile).minmax();
	    d.breakpoint = 0;
	    d.setMinval(minmax[0]);
	    d.setMaxval(minmax[1]);
	    d.setMode(Display.PLAIN);
	    d.makeImage();
	    d.writeImage(density.Utils.pngname(outfile, true));
	}
	if (colors != null) {
	    Display d = new Display(new LazyGrid(limitingfile));
	    d.setMode(Display.CLASS);
	    d.setClassNames(gridnames);
	    density.Utils.generator = new java.util.Random(System.currentTimeMillis());
	    while (colors.split(" ").length < nf)
		colors += " " + density.Utils.generator.nextInt(256) + "|" + density.Utils.generator.nextInt(256) + "|" + density.Utils.generator.nextInt(256);
	    if (whiteNonNovel) 
		colors += " 255|255|255";
	    d.setColorClasses(colors);
	    d.visible = false;
	    d.makeLegend = true;
	    d.makeImage();
	    d.writeImage(density.Utils.pngname(limitingfile, true));
	}
    }

    void initializeHistograms(int num) {
	nf = num;
	minmax = new float[nf][];
	histogram = new double[nf][];
	centrality = new double[nf][];
	proj = new Grid[nf];
	gridnames = new String[whiteNonNovel?nf+1:nf];
    }

    void makeHistograms(Feature[] features) {
	initializeHistograms(features.length);
	for (int i=0; i<nf; i++) {
	    gridnames[i] = features[i].name;
	    Grid g = features[i].toGrid();
	    makeHistogram(g, i);
	}
    }
	
    void makeHistograms(String[] filenames) throws IOException {
	initializeHistograms(filenames.length);
	for (int i=0; i<nf; i++)
	    gridnames[i] = density.Utils.fileToLayer(filenames[i]);
	for (int i=0; i<nf; i++) {
	    final Grid grid = GridIO.readGrid(filenames[i]);
	    Grid gg = grid;
	    final Grid mask = maskfile==null ? null : GridIO.readGrid(maskfile);
	    if (mask!=null)
		gg = new Grid(grid.getDimension(), "masked") {
			public boolean hasData(int r, int c) {
			    return grid.hasData(r,c) && mask.hasData(r,c);
			}
			public float eval(int r, int c) {
			    return grid.eval(r,c);
			}};
	    makeHistogram(gg, i);
	}
    }
    
    void makeHistogram(Grid g, int i) {
	minmax[i] = g.minmax();
	histogram[i] = g.histogram(minmax[i][0], minmax[i][1], nbins);
	centrality[i] = new double[nbins];
	double sum=0.0;
	for (int j=0; j<nbins; j++) {
	    sum += histogram[i][j];
	    centrality[i][j] = sum;
	}
	sum = 0.0;
	for (int j=nbins-1; j>=0; j--) {
	    sum += histogram[i][j];
	    if (sum < centrality[i][j])
		centrality[i][j] = sum;
	}
    }

    public static void main(String args[]) {
	try {
	    new Novel().go(args);
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }

}
