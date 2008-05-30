package net.sf.taverna.t2.reference;

/**
 * Callback interface used by asynchronous methods in the
 * {@link ReferenceSetService} interface
 * 
 * @author Tom Oinn
 * 
 */
public interface ReferenceSetServiceCallback {

	/**
	 * Called when the requested {@link ReferenceSet} has been successfully
	 * retrieved.
	 * 
	 * @param references
	 *            the ReferenceSet requested
	 */
	public void referenceSetRetrieved(ReferenceSet references);

	/**
	 * Called if the retrieval failed for some reason
	 * 
	 * @param cause
	 *            an Exception explaining the retrieval failure
	 */
	public void referenceSetRetrievalFailed(Exception cause);

}
