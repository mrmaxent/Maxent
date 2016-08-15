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

