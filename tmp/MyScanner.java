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
    
