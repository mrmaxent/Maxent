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

class DeBiasedFeature extends Feature {
    double sampleMean;
    Feature bias, f;

    public DeBiasedFeature(Feature f, Feature bias, Sample[] ss) {
	this.f = f;
	this.bias = bias;
	n=f.n; 
	name=f.name;
	sampleMean = 0.0;
	for (int i=0; i<ss.length; i++)
	    sampleMean += f.eval(ss[i]);
	sampleMean /= ss.length;
    }

    public double eval(int p) { 
	//	return bias.eval(p) * f.eval(p) + (1-bias.eval(p)) * sampleMean;
	return sampleMean + bias.eval(p) * (f.eval(p) - sampleMean);
    }
    public double eval(Sample s) { 
	return f.eval(s);
	//	return sampleMean + bias.eval(s) * (f.eval(s) - sampleMean);
    }
}
