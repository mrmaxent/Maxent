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

public class ScaledFeature extends Feature {
    public double min, max, scale;
    Feature f;

    ScaledFeature(Feature f, double min, double max) { 
	super(f.n, f.name);
	this.f = f; 
	this.min = min; 
	this.max = max;
	scale = max - min;
    }

    public ScaledFeature(Feature f) {
	super(f.n, f.name);
	this.f = f;
	min = max = f.eval(0);
	for (int i=1; i<f.n; i++) {
	    double val = f.eval(i);
	    if (val < min)
		min = val;
	    if (val > max)
		max = val;
	}
	scale = max - min;
	if (scale==0) {
	    scale = 1;
//	    min = min-1;
	}
    }

    public double eval(int p) { return (f.eval(p) - min) / scale; }
    public double eval(Sample s) { return (f.eval(s) - min) / scale; }
    public boolean hasData(Sample s) { return f.hasData(s); }

    String description() {
	java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance();
	nf.setMinimumFractionDigits(4);
	return (name + ": lambda = " + nf.format(lambda) + " min = " + nf.format(min) + " max = " + nf.format(max));
    }
}
