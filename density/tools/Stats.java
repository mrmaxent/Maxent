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

import java.util.*;
import java.io.*;

public class Stats {
    static double maxKappa, maxKappaThresh;
    // tp total positive examples, np classified as negative
    // tn total negative examples, nn classified as negative
    static double kappa(int np, int nn, int tp, int tn) {
	double observed = nn + tp-np, all = tn+tp;
	double expected = (nn+np)*tn/all + (all-nn-np)*tp/all;
	return (observed - expected) / (all - expected);
    }
    // computes maxKappa at the same time
    public static double auc(double[] presence, double[] absence) {
	Arrays.sort(presence);
	Arrays.sort(absence);
	maxKappa = 0.0; // == kappa(0, 0, presence.length, absence.length);
	maxKappaThresh = absence[0] -1;
	long auc = 0;
	for (int i=0, j=0; i<presence.length; i++) {
	    for (; j<absence.length && absence[j] < presence[i]; j++);
	    double kappa = kappa(i, j, presence.length, absence.length);
	    int less = j, icnt=1, jcnt=0;
	    for (; j<absence.length && absence[j] == presence[i]; j++)
		jcnt++;
	    for (; i<presence.length-1 && presence[i+1]==presence[i]; i++)
		icnt++;
	    auc += (2*less + jcnt) * icnt;
	    if (kappa > maxKappa) {
		maxKappa = kappa;
		maxKappaThresh = presence[i];
	    }
	}
	return auc / (2.0*(double)presence.length*(double)absence.length);
    }
    static public double mean(double[] x) {
	double sum=0.0;
	for (int i=0; i<x.length; i++) sum += x[i];
	return sum/x.length;
    }
    static public double variance(double[] x) {
	double sum=0.0, mean=mean(x);
	for (int i=0; i<x.length; i++) sum += x[i]*x[i];
	double var=sum/x.length - mean*mean;
	return (var < 0.0 ? 0 : var);
    }
    static double stddev(double[] x) { return Math.sqrt(variance(x)); }
    static double correlation(double[] x, double[] y) {
	double sum=0.0;
	checkLength(x, y, "Correlation");
	for (int i=0; i<x.length; i++) sum += x[i]*y[i];
	if (stddev(x)==0 || stddev(y)==0) return 0;
	double sumx2=0.0; for (int i=0; i<x.length; i++) sumx2 += x[i]*x[i];
	return (sum/x.length - mean(x)*mean(y)) / (stddev(x) * stddev(y));
    }
    static void checkLength(double[] x, double[] y, String where) {
	if (x.length != y.length)
	    throw new RuntimeException(where + ": vectors have different lengths");
    }

    // Find a monotonic map of x that (tries to) maximize correlation
    static double maxCorrelation(double[] x, double[] y) {
	int n = x.length;
	int[] idx = density.DoubleIndexSort.sort(x);
	double[] xs = new double[n], ys = new double[n], map = new double[n];
	for (int i=0; i<n; i++) {
	    xs[i] = map[i] = x[idx[i]];
	    ys[i] = y[idx[i]];
	}
	double muy = mean(y), correlation = correlation(x, y);
	double stdx = stddev(x), stdy = stddev(y);
	double prodsum = 0.0, sumx = 0.0, sumx2 = 0.0;
	for (int i=0; i<n; i++) {
	    prodsum += xs[i]*ys[i];
	    sumx += xs[i];
	    sumx2 += xs[i] * xs[i];
	}
	double epsilon = (xs[n-1] - xs[0]) / 1000;
	for (int iter=0; iter<10000; iter++) {
	    boolean changed = false;
	    for (int i=0; i<n; i++) {
		double ysum=0, xsum=0;
		int j;
		for (j=i; j<n; j++) {
		    if (map[j] != map[i]) break;
		    ysum += ys[j];
		    xsum += map[j];
		}
		if (j<n) {
		    double eps = (map[j]-map[i] < epsilon) ? 
			(map[j]-map[i]) : epsilon;
		    // try increase i..j-1 by eps
		    double p = prodsum + ysum*eps;
		    double mx = (sumx + (j-i)*eps) / n;
		    double sx = Math.sqrt((sumx2+(j-i)*(2*eps*map[i]+eps*eps))/n - mx*mx);
		    double cor = (p/n - mx*muy) / (sx*stdy);
		    if (cor > correlation) {
			sumx2 += (j-i)*(2*eps*map[i]+eps*eps);
			for (int k=i; k<j; k++)
			    map[k] += eps;
			changed = true;
			prodsum += ysum*eps;
			sumx += (j-i) * eps;
			correlation = cor;
		    }
		}
		ysum=0; xsum=0;
		for (j=i; j>=0; j--) {
		    if (map[j] != map[i]) break;
		    ysum += ys[j];
		    xsum += map[j];
		}
		if (j>=0) {
		    double eps = (map[j]-map[i] < -epsilon) ? 
			(map[j]-map[i]) : -epsilon;
		    // try increase i..j+1 by eps
		    double p = prodsum + ysum*eps;
		    double mx = (sumx + (i-j)*eps) / n;
		    double sx = Math.sqrt((sumx2+(i-j)*(2*eps*map[i]+eps*eps))/n - mx*mx);
		    double cor = (p/n - mx*muy) / (sx*stdy);
		    if (cor > correlation) {
			sumx2 += (i-j)*(2*eps*map[i]+eps*eps);
			for (int k=i; k>j; k--)
			    map[k] += eps;
			changed = true;
			prodsum += ysum*eps;
			sumx += (i-j) * eps;
			correlation = cor;
		    }
		}
	    }
	    if (!changed) break;
	    prodsum = 0.0; sumx = 0.0; sumx2 = 0.0;
	    for (int i=0; i<n; i++) {
		prodsum += xs[i]*ys[i];
		sumx += xs[i];
		sumx2 += xs[i] * xs[i];
	    }
	}	
	System.out.println("Final correlation: " + correlation(map, ys));
	try {
	    PrintWriter out = new PrintWriter(new FileOutputStream("maps/" + Eval.species));
	    for (int i=0; i<n; i++)
		out.println(xs[i] + " " + map[i]);
	    out.close();
	} catch (IOException e) {}
	return correlation;
    }
}
