package net.sf.taverna.t2.reference;

/**
 * Used by the asynchronous form of the resolveIdentifier method in
 * ReferenceService
 * 
 * @author Tom Oinn
 * 
 */
public interface ReferenceServiceResolutionCallback {

	/**
	 * Called when the resolution process has completed
	 * 
	 * @param result
	 *            the Identified that corresponds to the T2Reference specified
	 *            in the call to resolveIdentifierAsynch in ReferenceService
	 */
	public void identifierResolved(Identified result);

	/**
	 * Called when the resolution process has failed
	 * 
	 * @param cause
	 *            a ReferenceServiceException describing the failure
	 */
	public void resolutionFailed(ReferenceServiceException cause);

}
