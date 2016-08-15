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
import java.io.*;
import java.util.Random;

public class AUC {

    public static void main(String[] args) {
	if (args.length < 2) {
	    System.out.println("Usage: AUC testpoints prediction");
	    System.exit(1);
	}
	try { 
	    new AUC().go(args);
	}
	catch (IOException e) { 
	    System.out.println("Error in AUC: " + e.toString());
	    System.exit(1);
	}
    }

    void go(String[] args) throws IOException {
	boolean zeroseed = true;
	Utils.generator = new Random(zeroseed ? 0 : System.currentTimeMillis());
	String testpoints = args[0], predfile = args[1];
	String[] species = (args.length>2) ? new String[] {args[2]} : null;
	Extractor ext = new Extractor();
	ext.extractSamples(new String[] {predfile}, 10000, testpoints, null, species);
	double[] back = new double[ext.numBackground];
	for (int i=0; i<back.length; i++)
	    back[i] = ext.randextract[i].vals[0];
	double[] presence = new double[ext.sampleextract.length];
	for (int i=0; i<presence.length; i++)
	    presence[i] = ext.sampleextract[i].vals[0];
	System.out.println(density.tools.Stats.auc(presence, back));
    }
}
