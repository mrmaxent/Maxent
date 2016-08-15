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

