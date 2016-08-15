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

// works with a collection of Grids, e.g. makes them into a collection of features.

import java.io.*;
import java.util.*;

public class GridSet {
    boolean[][] hasData;
    int numPoints;
    Grid[] grids;
    Layer[] layers;
    Feature[] features=null;
    HashMap featureNameMap = new HashMap();
    GridDimension dimension;

    public SampleSet2 train=null, test=null;

    public GridSet() {}  // GridSetDummy implicitly calls this

    public Layer[] getLayers() { return layers; }

    public Feature[] toFeatures() { return features; }

    Feature getFeature(String s) {
	toFeatures();
	if (!featureNameMap.containsKey(s))
	    Utils.fatalException("Error", new RuntimeException("Feature not found: " + s));
	return (Feature) featureNameMap.get(s);
    }

    public int getNumPoints() { return numPoints; }
    public GridDimension getDimension() { return dimension; }
}

