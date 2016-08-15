package density;

import java.util.*;
import java.io.*; 

public class Layer {
    public static final int UNKNOWN = 0, CONT = 1, CAT = 2, F_BIAS_OUT = 3, MASK = 4, PROB = 5, CUM = 6, DEBIAS_AVG = 7;
    public static final String[] typeName = {"Unknown", "Continuous", "Categorical", "Bias", "Mask", "Probability", "Cumulative", "DebiasAvg"};
    public static String getTypeName(int type) { return typeName[type]; }

    String name;
    int    type = UNKNOWN;

    public Layer(String n, int t)    { name = n; type = t;   }
    public Layer(String n, String s) { name = n; setType(s); }
    public Layer(File f,   int t)    { name = Utils.fileToLayer(f); type = t; }
    public Layer(File f,   String s) { name = Utils.fileToLayer(f); setType(s); }
    
    public int getType() { return type; }
    public String getTypeName() { return typeName[type]; }

    public void setType(int t) { type = t; }
    public void setType(String s) {
	s = s.toLowerCase();
	for (int i=0; i<typeName.length; i++)
	    if (s.equals(typeName[i].toLowerCase())) {
		type = i;
		return;
	    }
    }

    public String getName() { return name; }
}
    

