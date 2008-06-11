package net.sf.taverna.t2.reference;

/**
 * Callback inteface used by {@link ExternalReferenceTranslatorSPI} and
 * {@link ExternalReferenceBuilderSPI} instances when asynchronous reference
 * construction is used.
 * 
 * @author Tom Oinn
 */
public interface ExternalReferenceConstructionCallback<TargetType extends ExternalReferenceSPI> {

	/**
	 * Called when the new ExternalReferenceSPI implementation has been
	 * successfuly constructed from whatever source was used.
	 * 
	 * @param newReference
	 *            the new ExternalReferenceSPI
	 */
	public void ExternalReferenceCreated(TargetType newReference);

	/**
	 * Called when the reference construction process failed.
	 * 
	 * @param cause
	 *            an Exception describing the failure
	 */
	public void ExternalReferenceConstructionFailed(Exception cause);

}
