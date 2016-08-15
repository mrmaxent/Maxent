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

public class Getval {

    public static void main(String args[]) {
	String usage = "Usage: density.Getval samplesFile coverageFile [coverageFile2...]";
	if (args.length<2) { System.out.println(usage); return; }
	String sampleFile = args[0];
	String[] fileNames = new String[args.length - 1];
	int ngrids = fileNames.length;
	for (int i=0; i<fileNames.length; i++)
	    fileNames[i] = args[i+1];
	Extractor extractor = new Extractor();
	extractor.params = new Params();
	extractor.params.setValue("allowpartialdata", true);
	Layer[] layers = new Layer[ngrids];
	for (int i=0; i<ngrids; i++)
	    layers[i] = new Layer(new File(fileNames[i]), "Continuous");
	try {
	    extractor.extractSamples(fileNames, 0, sampleFile, null, layers, null);
	}
	catch (IOException e) { Utils.fatalException("Error reading file " + extractor.readingFile,e); }
	Feature[] f = extractor.toFeatures();
	System.out.print(extractor.train.header);
	for (int i=0; i<ngrids; i++)
	    System.out.print("," + layers[i].getName());
	System.out.println();
	Sample[] samples = extractor.train.getSamples();
	for (int i=0; i<samples.length; i++) {
	    System.out.print(samples[i].name + "," + samples[i].getLon()+","+samples[i].getLat());
	    for (int j=0; j<ngrids; j++)
		System.out.print("," + (f[j].hasData(samples[i])?f[j].eval(samples[i]):SampleSet.NODATA_value));
	    System.out.println();
	}
    }
}
