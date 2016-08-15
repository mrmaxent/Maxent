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

import java.io.*;
import density.*;
import java.util.*;

public class LimitingFactor {

    // orange, sky blue, bluish green, yellow, blue, vermilion, reddish purple, ivory, lightcoral, black; see jfly.iam.u-tokyo.ac.jp/html/color_blind, and I've added two.
    String colors="230|159|0 86|180|233 0|158|115 240|228|66 0|114|178 213|94|0 204|121|167 255|255|240 240|128|128 0|0|0";
    boolean debug = false; 

    public static void main(String args[]) {
	try {
	    new LimitingFactor().go(args);
	} catch (IOException e) { 
	    System.err.println(e.toString());
	    e.printStackTrace();
	}
    }

    void go(String[] args) throws IOException {
	String usage = "Usage: density.tools.LimitingFactor lambdafile projectiondirectory outfile ";
	if (args.length<3) { System.out.println(usage); return; }
	String lambdafile = args[0], samplefile = args[1];
	String projdir = args[1], outfile = args[2];
	String root = lambdafile.replaceAll(".lambdas$","");
	Csv meancsv = new Csv(root+"_sampleAverages.csv");
	meancsv.indexAll();
	for (int i=0; i<2; i++) 
	    density.tools.Utils.checkExists(args[i]);
	final HashMap<String,Double> map = new HashMap();
	Project proj = new Project(new Params());
	proj.mapping = true;
	proj.varmap = map;
	final Grid projgrid = proj.projectGrid(lambdafile, null)[0];
	HashSet<String> usedGrids = proj.gridNames();
	String[] gridfilename = density.Utils.gridFileNames(projdir, usedGrids);
	int ng = gridfilename.length;
	final LazyGrid[] grid = new LazyGrid[ng];
	final double[] mean = new double[ng];
	final String[] gridname = new String[ng];
	for (int i=0; i<ng; i++) {
	    grid[i] = new LazyGrid(gridfilename[i]);
	    gridname[i] = density.Utils.fileToLayer(gridfilename[i]);
	    mean[i] = meancsv.getDoubleVal(gridname[i], "Sample average");
	}
	Grid result = new Grid(grid[0].getDimension(), "limiting") {
		public boolean hasData(int r, int c) {
		    for (int i=0; i<grid.length; i++)
			if (!grid[i].hasData(r,c)) return false;
		    return true;
		}
		public float eval(int r, int c) {
		    for (int i=0; i<grid.length; i++)
			map.put(gridname[i], (double) grid[i].eval(r,c));
		    double max = -1;
		    int var = -1;
		    double v = projgrid.eval(0,0);
		    for (int i=0; i<grid.length; i++) {
			map.put(gridname[i], mean[i]);
			double diff = projgrid.eval(0,0) - v;
			if (diff>max) {
			    max = diff;
			    var = i;
			}
			map.put(gridname[i], (double) grid[i].eval(r,c));
		    }
		    if (max==0 && debug) {
			System.out.println("Row " + r + " column " + c + ": prediction is " + v + ", change when setting each variable to its sample mean is: ");
			for (int i=0; i<grid.length; i++) {
			    map.put(gridname[i], mean[i]);
			    System.out.println("  " + gridname[i] + " " + (projgrid.eval(0,0) - v));
			    map.put(gridname[i], (double) grid[i].eval(r,c));
			}
			System.out.println();
		    }
		    return var;
		}};
	new GridWriter(result, outfile).writeAll();
	System.out.println("Index to output grid: ");
	for (int i=0; i<gridname.length; i++)
	    System.out.println(i + " " + gridname[i]);
	Display d = new Display(new LazyGrid(outfile));
	d.setMode(Display.CLASS);
	d.setClassNames(gridname);
	density.Utils.generator = new Random(System.currentTimeMillis());
	while (colors.split(" ").length < gridname.length)
	    colors += " " + density.Utils.generator.nextInt(256) + "|" + density.Utils.generator.nextInt(256) + "|" + density.Utils.generator.nextInt(256);
	d.setColorClasses(colors);
	d.visible = false;
	d.makeLegend = true;
	d.makeImage();
	d.writeImage(density.Utils.pngname(outfile, true));
    }
}
