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
    

