package density;

import java.io.*;
import java.util.*;

public class RandomSample2 {

    public static void main(String[] args) {
	if (args.length < 3) {
	    System.out.println("Usage: RandomSample distributionFile biasFile numSamples");
	    System.exit(1);
	}
	String distr = args[0];
	String bias = args[1];
	int numSamples = Integer.parseInt(args[2]);
	Grid grid=null, biasGrid=null;
	try { 
	    grid = GridIO.readGrid(distr);
	    biasGrid = GridIO.readGrid(bias);
	}
	    catch (IOException e) { 
		System.out.println("Error reading files: " + e.toString());
		System.exit(1);
	    }
	GridDimension dim = grid.getDimension();
	System.out.println("Species,long,lat");
	Random generator = new Random(System.currentTimeMillis());
	double[] closestDist = new double[numSamples];
	double[] cumulative = new double[numSamples];
	for (int i=0; i<numSamples; i++)
	    closestDist[i] = 101;
	for (int i=0; i<numSamples; i++)
	    cumulative[i] = generator.nextDouble() * 100.0;
	Arrays.sort(cumulative);
	for (int c=0; c<dim.ncols; c++) {
	    for (int r=0; r<dim.nrows; r++)
		if (grid.hasData(r, c)) {
		    double val = grid.eval(r,c);
		    int index = Arrays.binarySearch(cumulative, val);
		    if (index<0) index = -index - 1;
		    for (int i=index; i<numSamples; i++) {
			if (Math.abs(val-cumulative[i]) < closestDist[i])
			    closestDist[i] = Math.abs(val-cumulative[i]);
			else break;
		    }
		    for (int i=index-1; i>=0; i--) {
			if (Math.abs(val-cumulative[i]) < closestDist[i])
			    closestDist[i] = Math.abs(val-cumulative[i]);
			else break;
		    }
		}
	}
	int[] cnt = new int[numSamples], select = new int[numSamples];
	for (int c=0; c<dim.ncols; c++)
	    for (int r=0; r<dim.nrows; r++)
		if (grid.hasData(r, c)) {
		    double val = grid.eval(r,c);
		    int index = Arrays.binarySearch(cumulative, val);
		    if (index<0) index = -index - 1;
		    for (int i=index; i<numSamples; i++) {
			if (Math.abs(val-cumulative[i]) == closestDist[i])
			    cnt[i]++;
			else break;
		    }
		    for (int i=index-1; i>=0; i--) {
			if (Math.abs(val-cumulative[i]) == closestDist[i])
			    cnt[i]++;
			else break;
		    }
		}
	for (int i=0; i<numSamples; i++)
	    select[i] = (int) (generator.nextDouble() * cnt[i]);
	for (int c=0; c<dim.ncols; c++)
	    for (int r=0; r<dim.nrows; r++)
		if (grid.hasData(r, c)) {
		    double val = grid.eval(r,c);
		    int index = Arrays.binarySearch(cumulative, val);
		    if (index<0) index = -index - 1;
		    for (int i=index; i<numSamples; i++) {
			if (Math.abs(val-cumulative[i]) == closestDist[i]) {
			    if (select[i]==0 && generator.nextDouble() <= biasGrid.eval(r,c))
				System.out.println("Rnd," + dim.toX(c) + "," + dim.toY(r));
			    select[i]--;
			}
			else break;
		    }
		    for (int i=index-1; i>=0; i--) {
			if (Math.abs(val-cumulative[i]) == closestDist[i]) {
			    if (select[i]==0 && generator.nextDouble() <= biasGrid.eval(r,c))
				System.out.println("Rnd," + dim.toX(c) + "," + dim.toY(r));
			    select[i]--;
			}
			else break;
		    }
		}
    }
}
