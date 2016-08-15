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
import java.util.*;
import java.util.zip.*;

public class MyScanner {
    BufferedReader in;
    int posn, max, buflen=10000;
    char[] buffer = new char[buflen];

    public MyScanner(BufferedReader in) throws IOException { 
	this.in = in; 
	refresh();
    }

    public float nextFloat() throws IOException {
	String s = next();
	try {
	    return Float.parseFloat(s);
	} catch (NumberFormatException e) {
	    // deal with decimal commas, without localization headaches
	    return Float.parseFloat(s.replace(',', '.'));
	}
    }

    void step() throws IOException { 
	posn++; 
	if (posn==max) 
	    refresh();
    }

    void refresh() throws IOException {
	max = in.read(buffer, 0, buflen);
	if (max==-1)
	    throw new IOException("Unexpected end of file");
	posn = 0;
    }

    boolean isSpace() {
	return (buffer[posn] == ' ' || buffer[posn] == '\n' || buffer[posn] == '\r' || buffer[posn] == '\t');
    }

    public String next() throws IOException {
	while (isSpace()) step();
	String result = "";
	int start = posn;
	while (!isSpace()) {
	    posn++;
	    if (posn==max) {
		result += new String(buffer,start,max-start);
		refresh();
		start = 0;
	    }
	}
	result += new String(buffer,start,posn-start);
	return result;
    }
}
    
