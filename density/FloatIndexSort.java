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

/* the guts of the quicksort used here were borrowed from 
   http://www.flex-compiler.lcs.mit.edu/Harpoon/srcdoc/java/util/Arrays.html, 
   and follow Jon L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 49 1993). 
   It was trimmed and modified to sort indices. */

/* avoids the memory wastage of having index as Integer[], and 
   using Arrays.sort(.., Comparator). */

package density;

public class FloatIndexSort {

    // return the indices of vals array, sorted by increasing val
    // Throws RuntimeException if NaN or -0.0 in vals
    public static int[] sort(float[] vals) {
	int[] a = new int[vals.length];
	for (int i=0; i<a.length; i++) {
	    a[i] = i;
	    if (Float.isNaN(vals[i]))
		throw new RuntimeException("NaN encountered in FloatIndexSort");
	    if (vals[i]==Float.NEGATIVE_INFINITY)
		throw new RuntimeException("-0.0 encountered in FloatIndexSort");
	}
	sort1(a, 0, a.length, vals);
	return a;
    }


    /**
     * Sorts the specified sub-array of integers into ascending order.
     */
    static void sort1(int x[], int off, int len, float[] vals) {
	// Insertion sort on smallest arrays
	if (len < 7) {
	    for (int i=off; i<len+off; i++)
		for (int j=i; j>off && vals[x[j-1]]>vals[x[j]]; j--)
		    swap(x, j, j-1);
	    return;
	}

	// Choose a partition element, v
	int m = off + len/2;       // Small arrays, middle element
	if (len > 7) {
	    int l = off;
	    int n = off + len - 1;
	    if (len > 40) {        // Big arrays, pseudomedian of 9
		int s = len/8;
		l = med3(x, l,     l+s, l+2*s, vals);
		m = med3(x, m-s,   m,   m+s, vals);
		n = med3(x, n-2*s, n-s, n, vals);
	    }
	    m = med3(x, l, m, n, vals); // Mid-size, med of 3
	}
	float v = vals[x[m]];

	// Establish Invariant: v* (<v)* (>v)* v*
	int a = off, b = a, c = off + len - 1, d = c;
	while(true) {
	    while (b <= c && vals[x[b]] <= v) {
		if (vals[x[b]] == v)
		    swap(x, a++, b);
		b++;
	    }
	    while (c >= b && vals[x[c]] >= v) {
		if (vals[x[c]] == v)
		    swap(x, c, d--);
		c--;
	    }
	    if (b > c)
		break;
	    swap(x, b++, c--);
	}

	// Swap partition elements back to middle
	int s, n = off + len;
	s = Math.min(a-off, b-a  );  vecswap(x, off, b-s, s);
	s = Math.min(d-c,   n-d-1);  vecswap(x, b,   n-s, s);

	// Recursively sort non-partition-elements
	if ((s = b-a) > 1)
	    sort1(x, off, s, vals);
	if ((s = d-c) > 1)
	    sort1(x, n-s, s, vals);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(int x[], int a, int b) {
	int t = x[a];
	x[a] = x[b];
	x[b] = t;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(int x[], int a, int b, int n) {
	for (int i=0; i<n; i++, a++, b++)
	    swap(x, a, b);
    }

    private static int med3(int x[], int a, int b, int c, float[] vals) {
	return (vals[x[a]] < vals[x[b]] ?
		(vals[x[b]] < vals[x[c]] ? b : vals[x[a]] < vals[x[c]] ? c : a) :
		(vals[x[b]] > vals[x[c]] ? b : vals[x[a]] > vals[x[c]] ? c : a));
    }
 }
