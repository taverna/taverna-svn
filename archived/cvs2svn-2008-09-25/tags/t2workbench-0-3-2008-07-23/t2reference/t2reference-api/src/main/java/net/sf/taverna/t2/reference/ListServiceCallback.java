package net.sf.taverna.t2.reference;

/**
 * Callback interface used by asynchronous methods in the
 * {@link ListService} interface
 * 
 * @author Tom Oinn
 * 
 */
public interface ListServiceCallback {

	/**
	 * Called when the requested {@link ReferenceSet} has been successfully
	 * retrieved.
	 * 
	 * @param references
	 *            the ReferenceSet requested
	 */
	public void listRetrieved(IdentifiedList<T2Reference> references);

	/**
	 * Called if the retrieval failed for some reason
	 * 
	 * @param cause
	 *            a ListServiceException explaining the retrieval
	 *            failure
	 */
	public void listRetrievalFailed(ListServiceException cause);

}
