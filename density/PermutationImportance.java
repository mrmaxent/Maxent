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

// ref:  http://journal.r-project.org/archive/2009-2/RJournal_2009-2_Strobl~et~al.pdf, and Breiman 2001 cited therein;  also Jane email 2/1/2010

public class PermutationImportance {
    int np, nb, nv;
    String[] vars;
    Evaluate eval;

    public static void main(String args[]) {
	try { 
	    new PermutationImportance().go(args);
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }

    void error(String s) {
	System.out.println(s);
	System.exit(1);
    }

    void go(String[] args) throws IOException {
	String usage = "Usage: PermutationImportance swdPresence swdBackground lmbdafile";
	if (args.length < 3) {
	    System.out.println(usage);
	    System.exit(0);
	}
	String pres = args[0], back = args[1], lambdas = args[2];
	Csv prescsv = new Csv(pres), backcsv = new Csv(back);
	nv = prescsv.headers().length-3;
	vars = new String[nv];
	if (backcsv.headers().length-3 != nv)
	    error(back + " and " + pres + " have different numbers of fields");
	for (int i=0; i<nv; i++) {
	    vars[i] = prescsv.headers()[i+3];
	    if (!backcsv.headers()[i+3].equals(vars[i]))
		error("Variable names in " + back + " and " + pres + " differ:" + backcsv.headers()[i+3] + " " + vars[i]);
	}
	double[][] pvals = prescsv.getDoubleAllCols(3);
	double[][] bvals = backcsv.getDoubleAllCols(3);
	double[] aucdiffs = importance(pvals, bvals, lambdas);
	for (int i=0; i<nv; i++)
	    System.out.println(vars[i] + " " + aucdiffs[i]);
    }


    public double[] go(Feature[] features, Sample[] samples, String lambdafile) throws IOException {
	nv = features.length;
	np = samples.length;
	nb = features[0].getN();
	double[][] pvals = new double[np][nv];
	double[][] bvals = new double[nb][nv];
	vars = new String[nv];
	for (int v=0; v<nv; v++) {
	    vars[v] = features[v].name;
	    for (int p=0; p<np; p++)
		pvals[p][v] = features[v].eval(samples[p]);
	    for (int b=0; b<nb; b++)
		bvals[b][v] = features[v].eval(b);

	}
	return importance(pvals, bvals, lambdafile);
    }
	    
    double[] importance(double[][] pvals, double[][] bvals, String lambdas) throws IOException {
	eval = new Evaluate(lambdas);
	np = pvals.length;
	nb = bvals.length;
	double[][] allvals = new double[np+nb][nv];
	for (int i=0; i<np; i++)
	    for (int j=0; j<nv; j++)
		allvals[i][j] = pvals[i][j];
	for (int i=0; i<nb; i++)
	    for (int j=0; j<nv; j++)
		allvals[i+np][j] = bvals[i][j];
	double originalAUC = auc(allvals);
	double[] aucdiffs = new double[nv];
	for (int i=0; i<nv; i++) {
	    aucdiffs[i] = originalAUC - auc(allvals, i, permutation(np+nb));
	    if (aucdiffs[i]<0) aucdiffs[i]=0;
	}
	double tot=0.0;
	for (int i=0; i<nv; i++)
	    tot += aucdiffs[i];
	for (int i=0; i<nv; i++)
	    aucdiffs[i] *= (tot<=0 ? 0 : 100.0/tot);
	return aucdiffs;
    }

    void dump(double[] x) {
	for (double xx: x) System.out.print(xx + "  ");
	System.out.println();
    }

    Random generator = new Random(11111);
    int[] permutation(int n) {
	double[] x = new double[n];
	for (int i=0; i<n; i++)
	    x[i] = generator.nextDouble();
	return DoubleIndexSort.sort(x);
    }
    /*
    double[][] permute(double[][] a, int col) {
	int n = a.length;
	double[][] result = new double[n][];
	for (int i=0; i<n; i++)
	    result[i] = (double[]) a[i].clone();
	double[] x = new double[n];
	for (int i=0; i<n; i++)
	    x[i] = generator.nextDouble();
	int[] ord = DoubleIndexSort.sort(x);
	for (int i=0; i<n; i++)
	    result[i][col] = a[ord[i]][col];
	return result;
    }
    */
    double eval(double[] v) {
	for (int i=0; i<nv; i++)
	    eval.setValue(vars[i], v[i]);
	return eval.evaluate();
    }

    double[] eval(double[][] v, int index, int[] permutation) {
	double[] result = new double[v.length];
	for (int i=0; i<v.length; i++) {
	    double[] vv = v[i].clone();
	    if (permutation!=null) vv[index] = v[permutation[i]][index];
	    result[i] = eval(vv);
	}
	return result;
    }

    double auc(double[][] a) { return auc(a, 0, null); }
    double auc(double[][] a, int index, int[] permutation) {
	double[] pred = eval(a, index, permutation);
	double[] ppred = new double[np], bpred = new double[nb];
	for (int i=0; i<np; i++)
	    ppred[i] = pred[i];
	for (int i=0; i<nb; i++)
	    bpred[i] = pred[i+np];
	return density.tools.Stats.auc(ppred, bpred);
    }

}
