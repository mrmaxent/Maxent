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

import java.util.Arrays;

public class RunLengthEncoding {
    private int countsUsed, valsUsed;
    double compressionRatio;
    boolean verify = false;

    void decompress (byte[] counts, float[] vals, float[] into) {
	int tcnt=0, vcnt=0;
	for (int i=0; i<counts.length; i++) {
	    byte b = counts[i];
	    if (b<0)
		for (; b<0; b++)
		    into[tcnt++] = vals[vcnt++];
	    else {
		for (; b>=0; b--) // if non-negative, it's 1 less than number of copies
		    into[tcnt++] = vals[vcnt];
		vcnt++;
	    }
	}
    }

    void decompress (byte[] counts, short[] vals, short[] into) {
	int tcnt=0, vcnt=0;
	for (int i=0; i<counts.length; i++) {
	    byte b = counts[i];
	    if (b<0)
		for (; b<0; b++)
		    into[tcnt++] = vals[vcnt++];
	    else {
		for (; b>=0; b--) // if non-negative, it's 1 less than number of copies
		    into[tcnt++] = vals[vcnt];
		vcnt++;
	    }
	}
    }

    void decompress (byte[] counts, byte[] vals, byte[] into) {
	int tcnt=0, vcnt=0;
	for (int i=0; i<counts.length; i++) {
	    byte b = counts[i];
	    if (b<0)
		for (; b<0; b++)
		    into[tcnt++] = vals[vcnt++];
	    else {
		for (; b>=0; b--) // if non-negative, it's 1 less than number of copies
		    into[tcnt++] = vals[vcnt];
		vcnt++;
	    }
	}
    }

    void decompress (byte[] counts, int[] vals, int[] into) {
	int tcnt=0, vcnt=0;
	for (int i=0; i<counts.length; i++) {
	    byte b = counts[i];
	    if (b<0)
		for (; b<0; b++)
		    into[tcnt++] = vals[vcnt++];
	    else {
		for (; b>=0; b--) // if non-negative, it's 1 less than number of copies
		    into[tcnt++] = vals[vcnt];
		vcnt++;
	    }
	}
    }

    void compress(short[] from, byte[] counts, short[] vals) {
	int i, start=0;
	countsUsed = valsUsed = 0;
	while (start < from.length) {
	    short current = from[start];
	    for (i=start+1; i<from.length && from[i] == current; i++);
	    while (i>start+128) {
		counts[countsUsed++] = (byte) 127;
		vals[valsUsed++] = current;
		start += 128;
	    }
	    if (i==start+1) {
		for ( ; i <= from.length && (i==from.length || from[i] != from[i-1]); i++)
		    vals[valsUsed++] = from[i-1];
		int cnt = i-start-1;
		while (cnt > 128) {
		    counts[countsUsed++] = (byte) -128;
		    cnt -= 128;
		}
		if (cnt>0)
		    counts[countsUsed++] = (byte) -cnt;
		start = i-1;
	    }
	    else {
		counts[countsUsed++] = (byte) (i-start-1);
		vals[valsUsed++] = current;
		start = i;
	    }		
	}
	compressionRatio = (countsUsed + 2 * valsUsed) / (2 * from.length);
	if (verify) {
	    byte[] cnts = copyCounts(counts);
	    short[] vls = copyVals(vals);
	    short[] into = new short[from.length];
	    decompress(cnts, vls, into);
	    if (!Arrays.equals(into, from))
		System.out.println("!Verify");
	}
    }

    void compress(byte[] from, byte[] counts, byte[] vals) {
	int i, start=0;
	countsUsed = valsUsed = 0;
	while (start < from.length) {
	    byte current = from[start];
	    for (i=start+1; i<from.length && from[i] == current; i++);
	    while (i>start+128) {
		counts[countsUsed++] = (byte) 127;
		vals[valsUsed++] = current;
		start += 128;
	    }
	    if (i==start+1) {
		for ( ; i <= from.length && (i==from.length || from[i] != from[i-1]); i++)
		    vals[valsUsed++] = from[i-1];
		int cnt = i-start-1;
		while (cnt > 128) {
		    counts[countsUsed++] = (byte) -128;
		    cnt -= 128;
		}
		if (cnt>0)
		    counts[countsUsed++] = (byte) -cnt;
		start = i-1;
	    }
	    else {
		counts[countsUsed++] = (byte) (i-start-1);
		vals[valsUsed++] = current;
		start = i;
	    }		
	}
	compressionRatio = (countsUsed + 2 * valsUsed) / (2 * from.length);
	if (verify) {
	    byte[] cnts = copyCounts(counts);
	    byte[] vls = copyVals(vals);
	    byte[] into = new byte[from.length];
	    decompress(cnts, vls, into);
	    if (!Arrays.equals(into, from))
		System.out.println("!Verify");
	}
    }

    void compress(int[] from, byte[] counts, int[] vals) {
	int i, start=0;
	countsUsed = valsUsed = 0;
	while (start < from.length) {
	    int current = from[start];
	    for (i=start+1; i<from.length && from[i] == current; i++);
	    while (i>start+128) {
		counts[countsUsed++] = (byte) 127;
		vals[valsUsed++] = current;
		start += 128;
	    }
	    if (i==start+1) {
		for ( ; i <= from.length && (i==from.length || from[i] != from[i-1]); i++)
		    vals[valsUsed++] = from[i-1];
		int cnt = i-start-1;
		while (cnt > 128) {
		    counts[countsUsed++] = (byte) -128;
		    cnt -= 128;
		}
		if (cnt>0)
		    counts[countsUsed++] = (byte) -cnt;
		start = i-1;
	    }
	    else {
		counts[countsUsed++] = (byte) (i-start-1);
		vals[valsUsed++] = current;
		start = i;
	    }		
	}
	compressionRatio = (countsUsed + 2 * valsUsed) / (2 * from.length);
	if (verify) {
	    byte[] cnts = copyCounts(counts);
	    int[] vls = copyVals(vals);
	    int[] into = new int[from.length];
	    decompress(cnts, vls, into);
	    if (!Arrays.equals(into, from))
		System.out.println("!Verify");
	}
    }

    void compress(float[] from, byte[] counts, float[] vals) {
	int i, start=0;
	countsUsed = valsUsed = 0;
	while (start < from.length) {
	    float current = from[start];
	    for (i=start+1; i<from.length && from[i] == current; i++);
	    while (i>start+128) {
		counts[countsUsed++] = (byte) 127;
		vals[valsUsed++] = current;
		start += 128;
	    }
	    if (i==start+1) {
		for ( ; i <= from.length && (i==from.length || from[i] != from[i-1]); i++)
		    vals[valsUsed++] = from[i-1];
		int cnt = i-start-1;
		while (cnt > 128) {
		    counts[countsUsed++] = (byte) -128;
		    cnt -= 128;
		}
		if (cnt>0)
		    counts[countsUsed++] = (byte) -cnt;
		start = i-1;
	    }
	    else {
		counts[countsUsed++] = (byte) (i-start-1);
		vals[valsUsed++] = current;
		start = i;
	    }		
	}
	compressionRatio = (countsUsed + 4 * valsUsed) / (4 * from.length);
	if (verify) {
	    byte[] cnts = copyCounts(counts);
	    float[] vls = copyVals(vals);
	    float[] into = new float[from.length];
	    decompress(cnts, vls, into);
	    if (!Arrays.equals(into, from))
		System.out.println("!Verify");
	}
    }

    byte[] copyCounts(byte[] counts) {
	byte[] cp = new byte[countsUsed];
	for (int i=0; i<cp.length; i++)
	    cp[i] = counts[i];
	return cp;
    }

    short[] copyVals(short[] vals) {
	short[] cp = new short[valsUsed];
	for (int i=0; i<cp.length; i++)
	    cp[i] = vals[i];
	return cp;
    }

    byte[] copyVals(byte[] vals) {
	byte[] cp = new byte[valsUsed];
	for (int i=0; i<cp.length; i++)
	    cp[i] = vals[i];
	return cp;
    }

    int[] copyVals(int[] vals) {
	int[] cp = new int[valsUsed];
	for (int i=0; i<cp.length; i++)
	    cp[i] = vals[i];
	return cp;
    }

    float[] copyVals(float[] vals) {
	float[] cp = new float[valsUsed];
	for (int i=0; i<cp.length; i++)
	    cp[i] = vals[i];
	return cp;
    }
}
