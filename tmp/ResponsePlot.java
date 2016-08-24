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

import ptolemy.plot.*;
import javax.imageio.*;
import java.text.*;
import java.io.*;
import java.util.*;

public class ResponsePlot extends MyPlot {

    void makeplot(double[] x, double[] y, double[] stddev, boolean capstddev, boolean iscategorical, String title) {
	if (title!=null) setTitle(title);
	if (iscategorical)
	    setBars(0.5, 0.1);
	for (int i=0; i<x.length; i++) {
	    double xloc = // iscategorical ? i : 
		x[i];
	    addPoint(0, xloc, y[i], !iscategorical);
	    if (stddev!=null) {
		addPoint(1, xloc, (y[i]<1-stddev[i] || !capstddev) ? y[i]+stddev[i] : 1, !iscategorical);
		addPoint(iscategorical?2:1, xloc, (y[i]>stddev[i] || !capstddev) ? y[i]-stddev[i] : 0, !iscategorical);
	    }
	}
	if (iscategorical) {
	    for (int i=0; i<x.length; i++) {
		double c = x[i];
		if (c==(int) c) 
		    addXTick(((int) c)+"", c);
		else
		    addXTick(c+"", c);
	    }
	}
    }	

    void makeplot(double[] x, double[] y, double[] stddev, boolean iscategorical, String var, String title, String ylabel, String outfile, double mmin, double mmax, Params params, boolean exponent, boolean writePlotData) throws IOException {
	setSize(600,400);
	setXLabel(var);
	setYLabel(ylabel);
	makeplot(x,y,stddev,!exponent,iscategorical,title);
	if (writePlotData) {
	    PrintWriter plotDataOut = new PrintWriter(new FileOutputStream(outfile+".dat"));
	    plotDataOut.println("variable,x,y");
	    for (int i=0; i<x.length; i++)
		plotDataOut.println(var + "," + x[i] + "," + y[i]);
	    plotDataOut.close();
	}
	String filename = outfile + ".png";
	String filename2 = outfile + "_thumb.png";
	ImageIO.write(exportImage(), "png", new File(filename));
	setSize(210, 140);
	setTitle(var);
	setXLabel("");
	setYLabel("");
	_topPadding = 5;
	_bottomPadding = 0;
	_rightPadding = 0;
	_leftPadding = 0;
	if (params.occurrenceProbability() && !exponent) setYRange(0,1);
	if (params.cumulative() && !exponent) setYRange(0,100);
	setTitleFont("Helvetica bold 12");
	setLabelFont("Helvetica plain 9");
	setSuperscriptFont("Helvetica plain 6");
    
	if (!iscategorical) {
	    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
	    nf.setGroupingUsed(false);
	    nf.setMaximumFractionDigits(3);
	    addXTick(nf.format(mmin), mmin);
	    addXTick(nf.format(mmax), mmax);
	}
	repaint();
	ImageIO.write(exportImage(), "png", new File(filename2));
    }
}
