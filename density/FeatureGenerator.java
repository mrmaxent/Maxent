package density;

import java.io.*;

interface FeatureGenerator {
    void setSampleExpectations(FeaturedSpace X);
    void updateFeatureExpectations(FeaturedSpace X);
    int getFirstIndex();
    int getLastIndex();
    Feature exportFeature(int index);
    Feature getFeature(int index);
    Feature toFeature(int index);
    void outputDescription(PrintWriter out);
}