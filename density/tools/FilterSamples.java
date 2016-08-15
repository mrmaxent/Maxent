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

package density.tools;

import java.io.IOException;
import density.*;
import java.util.Random;

public class FilterSamples {

    public static void main(String args[]) {
	String usage = "Usage: density.tools.FilterSamples samplesfile mindist outfile";
	if (args.length<3) { System.out.println(usage); return; }
	String samplefile = args[0], outfile = args[2];
	double mindist = Double.parseDouble(args[1]);
	boolean zeroseed = false;
	density.Utils.generator = new Random(zeroseed ? 0 : System.currentTimeMillis());
	try {
	    SampleSet ss = new SampleSet2(samplefile, null, null);
	    SampleSet ss2 = ss.spatialFilter(mindist);
	    ss2.write(outfile);
	}
	catch (IOException e) { 
	    System.err.println(e.toString());
	}
    }
}
