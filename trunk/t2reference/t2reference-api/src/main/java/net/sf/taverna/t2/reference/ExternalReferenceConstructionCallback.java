package net.sf.taverna.t2.reference;

/**
 * Callback inteface used by {@link ExternalReferenceTranslatorSPI} and
 * {@link ExternalReferenceBuilderSPI} instances when asynchronous reference
 * construction is used.
 * 
 * @author Tom Oinn
 */
public interface ExternalReferenceConstructionCallback {

	/**
	 * Called when the new ExternalReferenceSPI implementation has been
	 * successfuly constructed from whatever source was used.
	 * 
	 * @param newReference
	 *            the new ExternalReferenceSPI
	 */
	public void ExternalReferenceCreated(ExternalReferenceSPI newReference);

	/**
	 * Called when the reference construction process failed.
	 * 
	 * @param cause
	 *            an ExternalReferenceConstructionException describing the
	 *            failure
	 */
	public void ExternalReferenceConstructionFailed(
			ExternalReferenceConstructionException cause);

}
