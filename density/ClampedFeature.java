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

public class ClampedFeature extends Feature {
    double min, max;
    Feature f;

    public ClampedFeature(Feature f) { this(f, 0, 1); }
    public ClampedFeature(Feature f, double min, double max) {
	super(f.n, f.name);
        this.f = f;
	this.min = min;
	this.max = max;
    }

    public double eval(int p) {
	double val = f.eval(p);
	if (val<min) return min;
	if (val>max) return max;
	return val;
    }

    public double eval(Sample s) {
	double val = f.eval(s);
	if (val<min) return min;
	if (val>max) return max;
	return val;
    }

    public boolean hasData(Sample s) { return f.hasData(s); }
}
