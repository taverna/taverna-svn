package net.sf.taverna.t2.facade;

/**
 * Used to communicate a failure of the overall workflow to interested parties.
 * 
 * @author Tom Oinn
 */
public interface FailureListener {

	/**
	 * Called if the workflow fails in a critical and fundamental way. Most
	 * internal failures of individual process instances will not trigger this,
	 * being handled either by the per processor dispatch stack through retry,
	 * failover etc or by being converted into error tokens and injected
	 * directly into the data stream. This therefore denotes a catastrophic and
	 * unrecoverable problem.
	 * 
	 * @param message
	 * @param t
	 */
	public void workflowFailed(String message, Throwable t);

}
