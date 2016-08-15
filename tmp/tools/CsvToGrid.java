package density.tools;

import java.io.*;
import density.*;

public class CsvToGrid {

    public static void main(String args[]) {
	String usage = "Usage: CsvToGrid csvFile xcol ycol gridFileForDimension outFile";
	if (args.length < 5) {
	    System.out.println(usage);
	    System.exit(0);
	}
	String csvFile = args[0], gridFile = args[3], outfile = args[4];
	final String xcol = args[1], ycol = args[2];
	try {
	    final GridDimension dim = GridIO.readGrid(gridFile).getDimension();
	    Csv csv = new Csv(csvFile);
	    int nr = dim.getnrows(), nc = dim.getncols();
	    final boolean[][] vals = new boolean[nr][nc];
	    csv.apply(csv.new Applier() {
		    public void process() {
			double x = Double.parseDouble(get(xcol));
			double y = Double.parseDouble(get(ycol));
			if (dim.inBounds(x,y))
			    vals[dim.toRow(y)][dim.toCol(x)] = true;
		    }});
	    Grid out = new Grid(dim, "grid") {
		    public float eval(int r, int c) {
			return vals[r][c] ? 1 : 0;
		    }
		    public boolean hasData(int r, int c) {
			return true;
		    }
		};
	    new GridWriter(out, outfile).writeAll();
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e);
	    System.exit(0);
	}
    }
}
