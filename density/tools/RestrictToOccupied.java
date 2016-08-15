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

import density.*;
import gnu.getopt.*;
import java.io.*;
import java.util.*;

public class RestrictToOccupied {
    double mindist, maxdist, threshold=-1;
    String sampleFile, species;
    boolean debug = false, doHull = false;

    public static void main(String[] args) {
	try {
	    new RestrictToOccupied().go(args);
	}
	catch (IOException e) { 
	    System.out.println("Error: " + e.toString());
	    System.exit(1);
	}

    }

    void go(String[] args) throws IOException {
	int gc;
	Getopt g = new Getopt("RestrictToOccupied", args, "s:S:p:P:t:fh");
	String sampleFile = null;

	while ((gc=g.getopt()) != -1) {
	    switch(gc) {
 	    case 'f': SampleSet.setNCEAS_FORMAT(); break;
	    case 's': sampleFile = g.getOptarg(); break;
	    case 'S': species = g.getOptarg(); break;
	    case 'p': mindist = Double.parseDouble(g.getOptarg()); break;
	    case 'P': maxdist = Double.parseDouble(g.getOptarg()); break;
	    case 't': threshold = Double.parseDouble(g.getOptarg()); break;
	    case 'h': doHull = true; break;
	    }
	}

	if (args.length < 2 + g.getOptind() || sampleFile==null) {
	    System.out.println("Usage: RestrictToOccupied [-s sampleFile (required)] [-S species] [-p mindist (in pixels)] [-P maxdist] [-t threshold] ingrid outfile");
	    System.exit(1);
	}
	String gridfile = args[g.getOptind()];
	String outfile = args[g.getOptind()+1];

	final Grid grid = GridIO.readGrid(gridfile);
	GridDimension dim = grid.getDimension();
	SampleSet sampleSet = new SampleSet2(sampleFile, grid.getDimension(), null);
	final Sample[] samples = (species==null) ? sampleSet.getSamples() :
	    sampleSet.getSamples(species);
	double[][] dist;
	int nr = dim.getnrows(), nc = dim.getncols();
	if (threshold!=-1) {  
	    boolean[][] connected = connected(grid, samples, threshold, 1);
	    if (doHull) connected = computeHull(connected);
	    dist = distanceToConn(connected, maxdist, grid);
	    
	    if (debug)
		for (int r=0; r<nr; r++) {
		    for (int c=0; c<nc; c++)
			System.out.print((int) dist[r][c]);
		    System.out.println();
		}
	    
	}
	else {
	    dist = new double[nr][nc];
	    for (int r=0; r<nr; r++)
		for (int c=0; c<nc; c++) {
		    if (!grid.hasData(r,c)) continue;
		    double mind=0.0;
		    for (int i=0; i<samples.length; i++) {
			Sample s = samples[i];
			int rr=s.getRow(), cc = s.getCol();
			double d = (r-rr)*(r-rr) + (c-cc)*(c-cc);
			if (i==0 || d<mind)
			    mind = d;
		    }
		    dist[r][c] = Math.sqrt(mind);
		}
	}
	final double[][] ddist = dist;
	Grid newgrid = new Grid(dim, grid.getName()) {
		public boolean hasData(int r, int c) { 
		    return grid.hasData(r,c); 
		}
		public float eval(int r, int c) { 
		    //		    return (float) ddist[r][c];
		    float val = grid.eval(r,c);
		    double d = ddist[r][c];
		    if (d <= mindist) return val;
		    if (d >= maxdist) return (float) 0.0;
		    return (float) (val * (maxdist - d) / (maxdist - mindist));
		}
	    };
	new GridWriter(newgrid, outfile).writeAll();
    }

    // traversal
    int qcurrent, qtail;
    void addToQueue(boolean[][] conn, int[] row, int[] col, int r, int c) {
	row[qtail] = r;
	col[qtail++] = c;
	conn[r][c] = true;
    }
    boolean[][] connected(Grid grid, Sample[] samples, double threshold, int dist) {
	int nr = grid.getDimension().getnrows(), nc = grid.getDimension().getncols();
	boolean[][] connected = new boolean[nr][nc];
	int[] row = new int[nr*nc], col = new int[nr*nc];
	qcurrent = 0;
	qtail = 0;
	for (int i=0; i<samples.length; i++) {
	    //	    System.out.println("Sample " + i + " " + samples[i].getRow() + " " + samples[i].getCol());
	    addToQueue(connected, row, col, samples[i].getRow(), samples[i].getCol());
	}
	//	System.out.println(rr + " " + cc + " " + grid.eval(rr, cc));
	while (qcurrent < qtail) {
	    int rr = row[qcurrent], cc = col[qcurrent++];
	    for (int r=rr-dist; r<=rr+dist; r++) {
		if (r<0 || r>=nr) continue;
		for (int c=cc-dist; c<=cc+dist; c++) {
		    if (c<0 || c>=nc) continue;
		    if (connected[r][c]) continue;
		    if (dist>1 && ((r-rr)*(r-rr)+(c-cc)*(c-cc) > dist*dist))
			continue;
		    if (!grid.hasData(r,c)) continue;
		    if (grid.eval(r,c) >= threshold) {
			//			System.out.println(r + " " + c + " " + grid.eval(r,c) + " " + threshold);
			addToQueue(connected, row, col, r, c);
		    }
		}
	    }
	}
	return connected;
    }
	

    double[][] distanceToConn(boolean[][] connected, double maxdist, Grid grid) {
	int nr = connected.length, nc = connected[0].length;
	Node[] nds = new Node[nr*nc];
	NodePriorityQueue queue = new NodePriorityQueue(nds);
	for (int r=0; r<nr; r++)
	    for (int c=0; c<nc; c++)
		nds[r*nc+c] = new Node(r, c);
	queue.initialize();
	for (int r=0; r<nr; r++)
	    for (int c=0; c<nc; c++) {
		if (connected[r][c]) {
		    nds[r*nc+c].qweight = 0;
		    queue.insert(nds[r*nc+c]);
		}
		else 
		    nds[r*nc+c].qweight = maxdist;
	    }
	while (!queue.isEmpty()) {
	    Node min = queue.deleteMin();
	    int rr=min.r, cc=min.c;
	    double dist=1.5;
	    int idist = (int) Math.floor(dist);
	    if (debug) System.out.println("Deleted " + rr + " " + cc + " " + min.qweight);
	    for (int r=rr-idist; r<=rr+idist; r++) {
		if (r<0 || r>=connected.length) continue;
		for (int c=cc-idist; c<=cc+idist; c++) {
		    if (debug) System.out.println("Considering " + r + " " + c);
		    if (c<0 || c>=connected[r].length) continue;
		    Node nd = nds[r*nc + c];
		    if (queue.alreadyDeleted(nd)) continue;
		    int sqd = (r-rr)*(r-rr)+(c-cc)*(c-cc);
		    if (dist>1 && sqd > dist*dist)
			continue;
		    if (!grid.hasData(r,c)) continue;
		    double newweight = min.qweight + Math.sqrt(sqd);
		    if (debug) System.out.println("Newweight " + r + " " + c + " " + newweight);
		    if (newweight >= maxdist) continue;
		    if (queue.contains(nd)) {
			if (nd.qweight > newweight) {
			    nd.qweight = newweight;
			    queue.reducedWeight(nd);
			    if (debug) System.out.println("Reduce " + r + " " + c);
			}
		    }
		    else {
			nd.qweight = newweight;
			queue.insert(nd);
			if (debug) System.out.println("Add " + r + " " + c);
		    }
		}
	    }
	}
	double[][] result = new double[nr][nc];
	int cnt=0;
	for (int r=0; r<nr; r++)
	    for (int c=0; c<nc; c++)
		result[r][c] = nds[cnt++].qweight;
	return result;
    }


    // computing and filling in the convex hull

    class Stack { 
	int[] stack;
	int n;
	public Stack(int max) { 
	    stack = new int[max];
	    n = 0;
	}
	void push(int x) { stack[n++] = x; }
	void dump(int[] y) { 
	    for (int i=0; i<n; i++) 
		System.out.print(" " + stack[i] + ":" + y[stack[i]]);
	    System.out.println();
	}
	void pop() { n--; }
	int top() { return stack[n-1]; }
	int second() { return stack[n-2]; }
	int size() { return n; }
    }
    
    boolean[][] computeHull(boolean[][] conn) {
	// Treating row as y, col as x (i.e. upside down)
	int nr = conn.length, nc = conn[0].length, startrow=-1;
	int[] rowmin = new int[nr], rowmax = new int[nr];
	for (int r=0; r<nr; r++)
	    rowmin[r] = rowmax[r] = -1;
	for (int r=0; r<nr; r++)
	    for (int c=0; c<nc; c++)
		if (conn[r][c]) {
		    if (startrow==-1) 
			startrow = r;
		    if (rowmin[r] == -1) 
			rowmin[r] = c;
		    rowmax[r] = c;
		}
	
	Stack[] stack = new Stack[2];
	int[] top = new int[2];
	// 0 is increasing r using rowmax, 1 is descending r using rowmin
	stack[0] = new Stack(nr);
	stack[0].push(startrow);
	stack[0].push(startrow);
	for (int r = startrow + 1; r < nr; r++) {
	    if (rowmax[r] == -1) continue;
	    int o = crossProduct(stack[0].second(), stack[0].top(), r, rowmax);
	    while (o<=0 && stack[0].size() > 2) {
		stack[0].pop();
		o = crossProduct(stack[0].second(), stack[0].top(), r, rowmax);
	    }
	    stack[0].push(r);
	}
	//	stack[0].dump(rowmax);
	stack[1] = new Stack(nr);
	startrow = stack[0].top();
	stack[1].push(startrow);
	stack[1].push(startrow);
	for (int r = startrow - 1; r >= 0; r--) {
	    if (rowmin[r] == -1) continue;
	    int o = crossProduct(stack[1].second(), stack[1].top(), r, rowmin);
	    while (o<=0 && stack[1].size() > 2) {
		stack[1].pop();
		o = crossProduct(stack[1].second(), stack[1].top(), r, rowmin);
	    }
	    stack[1].push(r);
	}
	//	stack[1].dump(rowmin);
	int[] nrowmax = new int[nr], nrowmin = new int[nr];
	int low = stack[1].top(), high = stack[0].top();
	for (int r=0; r<low; r++) 
	    nrowmax[r] = nrowmin[r] = -1;
	for (int r=high+1; r<nr; r++)
	    nrowmax[r] = nrowmin[r] = -1;
	// compute max for each row
	int cnt=0;
	for (int r=low; r<=high; r++) {
	    for ( ; stack[0].stack[cnt] <= r && cnt < stack[0].n; cnt++);
	    if (cnt == stack[0].n) 
		nrowmax[r] = rowmax[r];
	    else {
		int rl = stack[0].stack[cnt-1], rh = stack[0].stack[cnt];
		nrowmax[r] = (int) Math.ceil( rowmax[rl] + (r-rl) / (double) (rh-rl) * (rowmax[rh] - rowmax[rl]));
	    }
	    //	    System.out.print(" " + nrowmax[r]);
	}
	//	System.out.println();
	// compute min for each row
	cnt=0;
	for (int r=high; r>=low; r--) {
	    for ( ; stack[1].stack[cnt] >= r && cnt < stack[1].n; cnt++);
	    if (cnt == stack[1].n) 
		nrowmin[r] = rowmin[r];
	    else {
		int rh = stack[1].stack[cnt-1], rl = stack[1].stack[cnt];
		nrowmin[r] = (int) Math.floor( rowmin[rh] + (rh-r) / (double) (rh-rl) * (rowmin[rl] - rowmin[rh]));
	    }
	    //	    System.out.print(" " + nrowmin[r]);
	}
	//	System.out.println();
	boolean[][] result = new boolean[nr][nc];
	for (int r=0; r<nr; r++)
	    for (int c=0; c<nc; c++)
		result[r][c] = (c >= nrowmin[r] && c <= nrowmax[r]);
	return result;
    }
    
    int crossProduct(int p1, int p2, int p3, int[] x) {  // y's are the pi's
	return (x[p2] - x[p1])*(p3 - p1) - (x[p3] - x[p1])*(p2 - p1);
    }
}
