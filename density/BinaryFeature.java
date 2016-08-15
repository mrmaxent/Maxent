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

public class BinaryFeature extends Feature {
    Feature f;
    float target;
    boolean isFirstCategory=false;

    public BinaryFeature(Feature f, float target, String s) {
	super(f.n, "(" + s + "=" + target + ")");
	this.f = f;
	this.target = target;
    }

    public double eval(int i) { 
	if (f.eval(i) == target)
	    return 1.0;
	else
	    return 0.0;
    }

    public double eval(Sample s) { return f.eval(s, target); }
    public boolean hasData(Sample s) { return f.hasData(s); }
    boolean isBinary() { return true; }

    static public BinaryFeature[] makeAll (Feature f, String s) {
	double[] allvals = f.distinctVals();
	BinaryFeature[] result = new BinaryFeature[allvals.length];
	
	for (int i=0; i<allvals.length; i++)
	    result[i] = new BinaryFeature(f, (float) allvals[i], s);
	if (allvals.length>0) result[0].isFirstCategory=true;
	return result;
    }
}
