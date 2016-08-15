package density.tools;

import java.io.*;
import density.*;

// perhaps add option to specify origin, and to swap test/train
public class Checkerboard {
    int blocksperrow, blockspercol, maxtest=100000, maxtrain=100000;
    double offset=0.0, excludeEdgeWidth=0.15;

    public static void main(String args[]) {
	try { new Checkerboard().go(args);
	} catch (IOException e) { 
	    System.out.println(e.toString());
	    e.printStackTrace();
	}
    }

    boolean isTrain(double x, double y, GridDimension dim) {
	int r = dim.toRow(y), c = dim.toCol(x);
	int rb = (int) (r * blockspercol / (double) dim.getnrows() + offset);
	int cb = (int) (c * blocksperrow / (double) dim.getncols() + offset);
	return (((rb + cb) % 2) == 0);
    }

    boolean isExcludedEdge(double x, double y, GridDimension dim) {
	int r = dim.toRow(y), c = dim.toCol(x);
	double rb = r * blockspercol / (double) dim.getnrows() + offset;
	double cb = c * blocksperrow / (double) dim.getncols() + offset;
	return ((rb-(int)rb>1-excludeEdgeWidth) || 
		(cb-(int)cb>1-excludeEdgeWidth));
    }

    void go(String[] args) throws IOException {
	String usage = "Usage: density.Checkerboard samplesFile blocksPerRow blocksPerColumn offset outSuffix envlayer";
	if (args.length<6) {
	    System.out.println(usage); 
	    System.exit(0); 
	}

	final String samplesFile = args[0], outsuffix = args[4], grid = args[5];
	blocksperrow = Integer.parseInt(args[1]);
	blockspercol = Integer.parseInt(args[2]); 
	offset = Double.parseDouble(args[3]);

	final PrintWriter train = density.Utils.writer(outsuffix + "_train.csv");
	final PrintWriter test = density.Utils.writer(outsuffix + "_test.csv");

	final Csv csv = new Csv(samplesFile);
	final GridDimension dim = new LazyGrid(grid).getDimension();
	train.println(csv.headerString());
	test.println(csv.headerString());
	csv.apply(csv.new Applier() {
		double ntest=0, ntrain=0;
		public void process() {
		    double x = getDouble(1), y = getDouble(2);
		    if (isExcludedEdge(x,y,dim)) return;
		    PrintWriter out = isTrain(x, y, dim) ? train : test;
		    if (isTrain(x,y,dim)) {
			if (ntrain++ < maxtrain)
			    train.println(Csv.toCsvFormat(csv.getCurrentRecord()));
		    } else {
			if (ntest++ < maxtest)
			    test.println(Csv.toCsvFormat(csv.getCurrentRecord()));
		    }
		}
	    });
	train.close();
	test.close();
    }
}
