package net.sf.taverna.t2.reference.impl;

/**
 * Abstract superclass for all service implementation objects, will be used to
 * allow injection of thread pooling logic as and when we implement it.
 * 
 * @author Tom Oinn
 */
public class AbstractServiceImpl {

	/**
	 * Schedule a runnable for execution - current naive implementation uses a
	 * new thread and executes immediately, but this is where any thread pool
	 * logic would go if we wanted to add that.
	 * 
	 * @param r
	 */
	protected void executeRunnable(Runnable r) {
		new Thread(r).start();
	}

}
