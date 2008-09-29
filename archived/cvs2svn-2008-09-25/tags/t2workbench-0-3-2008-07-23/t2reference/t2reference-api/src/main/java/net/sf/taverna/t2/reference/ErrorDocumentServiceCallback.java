package net.sf.taverna.t2.reference;

/**
 * Callback interface used by asynchronous methods in the
 * {@link ErrorDocumentService} interface
 * 
 * @author Tom Oinn
 * 
 */
public interface ErrorDocumentServiceCallback {

	/**
	 * Called when the requested {@link ReferenceSet} has been successfully
	 * retrieved.
	 * 
	 * @param errorDoc
	 *            the ErrorDocument requested
	 */
	public void errorRetrieved(ErrorDocument errorDoc);

	/**
	 * Called if the retrieval failed for some reason
	 * 
	 * @param cause
	 *            an ErrorDocumentServiceException explaining the retrieval
	 *            failure
	 */
	public void errorRetrievalFailed(ErrorDocumentServiceException cause);

}
