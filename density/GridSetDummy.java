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
import java.util.ArrayList;

// Assumes each sample maps feature names to doubles
public class GridSetDummy extends GridSet {
    ArrayList featureNames = new ArrayList();
    ArrayList features = new ArrayList();

    public int getNumPoints() { return 0; }
    Feature getFeature(final String s) {
	if (!featureNames.contains(s)) {
	    featureNames.add(s);
	    features.add(new Feature(0, s) {
		    public double eval(int p) { return 0.0; }
		    public double eval(Sample ss) { 
			if (ss.featureMap.containsKey(s)) {
			    Object o = ss.featureMap.get(s);
			    return ((Double) o).doubleValue();
			}
			throw new RuntimeException("Didn't find " + s + " in GridSetDummy");
		    }
		});
	}
	return (Feature) features.get(featureNames.indexOf(s));
    }
}
