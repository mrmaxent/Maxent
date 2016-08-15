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

import java.util.*;
import java.util.concurrent.*;

public class ParallelRun {
    ExecutorService threadPool;
    Collection<Callable<Object>> tasks;
    boolean verbose = false;

    public ParallelRun(int nthreads) {
	threadPool = Executors.newFixedThreadPool(nthreads);
	tasks = new ArrayList<Callable<Object>>();
    }

    public void clear() { tasks.clear(); }
    public void add(final Runnable task, final String name) { 
	Runnable vtask = new Runnable() {
		public void run() {
		    if (verbose) System.out.println("Starting " + name);
		    task.run();
		    if (verbose) System.out.println("Ending " + name);
		}};
	tasks.add(Executors.callable(vtask)); 
    }

    public void runall(String runtype, boolean verbose) {
	this.verbose = verbose;
	Collection<Future<Object>> futures = null;
	try { futures = threadPool.invokeAll(tasks); } 
	catch (InterruptedException e) {}
	if (futures != null) for (Future<Object> f : futures) {
	    try { f.get(); }
	    catch (InterruptedException ex) {}
	    catch (ExecutionException ex) {
		Utils.fatalException("Error in parallel " + runtype, ex);
	    }
	}
    }

    public void close() { threadPool.shutdown(); }
}

