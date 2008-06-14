package net.sf.taverna.t2.reference;

/**
 * Provides facilities to register list of T2References, register empty lists at
 * any given depth and to resolve appropriate T2Reference instances back to
 * these lists. Registration operations assign names and lock the list contents
 * as a result. This service operates strictly on T2References, it neither tries
 * to nor is capable of any form of reference resolution, so aspects such as
 * collection traversal are not handled here (these are performed by the top
 * level reference service)
 * 
 * @author Tom Oinn
 */
public interface ErrorDocumentService {

	/**
	 * Register a new error document
	 * 
	 * @param message
	 *            a free text message describing the error, if available. If
	 *            there is no message use the empty string here.
	 * @param t
	 *            a Throwable describing the underlying fault causing this error
	 *            document to be registered, if any. If there is no Throwable
	 *            associated use null.
	 * @param depth
	 *            depth of the error, used when returning an error document
	 *            instead of an identified list.
	 * @return a new ErrorDocument instance, constructed fully and stored in the
	 *         underlying storage system
	 */
	public ErrorDocument registerError(String message, Throwable t, int depth)
			throws ErrorDocumentServiceException;

	/**
	 * Equivalent to <code>registerError(message, null, depth)</code>
	 */
	public ErrorDocument registerError(String message, int depth)
			throws ErrorDocumentServiceException;

	/**
	 * Equivalent to <code>registerError("",t, depth)</code>
	 */
	public ErrorDocument registerError(Throwable t, int depth)
			throws ErrorDocumentServiceException;

	/**
	 * Retrieve a previously named and registered ErrorDocument from the backing
	 * store
	 * 
	 * @param id
	 *            identifier of the error document to retrieve
	 * @return an ErrorDocument
	 * @throws ErrorDocumentServiceException
	 *             if anything goes wrong with the retrieval process or if there
	 *             is something wrong with the reference (such as it being of
	 *             the wrong reference type).
	 */
	public ErrorDocument getError(T2Reference id)
			throws ErrorDocumentServiceException;

	/**
	 * Functionality the same as {@link #getError(T2Reference) getError} but in
	 * asynchronous mode, returning immediately and using the supplied callback
	 * to communicate its results.
	 * 
	 * @param id
	 *            a {@link T2Reference} identifying an {@link ErrorDocument} to
	 *            retrieve
	 * @param callback
	 *            a {@link ErrorDocumentServiceCallback} used to convey the
	 *            results of the asynchronous call
	 * @throws ErrorDocumentServiceException
	 *             if the reference set service is not correctly configured.
	 *             Exceptions encountered when performing the asynchronous call
	 *             are not returned here, for obvious reasons, and are instead
	 *             messaged through the callback interface.
	 */
	public void getErrorAsynch(T2Reference id,
			ErrorDocumentServiceCallback callback)
			throws ErrorDocumentServiceException;

}