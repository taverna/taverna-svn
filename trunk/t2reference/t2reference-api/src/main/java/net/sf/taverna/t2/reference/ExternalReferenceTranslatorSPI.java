package net.sf.taverna.t2.reference;

/**
 * Constructs an ExternalReference instance from an existing ExternalReference,
 * most usually of a different type. Used by the {@link ReferenceSetAugmentor}.
 * This SPI should not be used for cases where an ExternalReferenceSPI is
 * constructed from a stream of bytes, this is intended for direct reference to
 * reference translation with the assumption that this is more efficient for
 * whatever reason. For cases where the reference is constructed from a byte
 * stream you should implement {@link ExternalReferenceBuilder} instead.
 * <p>
 * For SPI purposes implementations should be java beans with default
 * constructors, any required state such as the location of remote repositories
 * to which data can be staged will be passed in in the ReferenceContext.
 * 
 * @author Tom Oinn
 */
public interface ExternalReferenceTranslatorSPI {

	/**
	 * Given an existing ReferenceSet, build the appropriate target
	 * ExternalReferenceSPI implementation and return it.
	 * 
	 * @param existingReferences
	 *            the references to be used as sources for the translation. In
	 *            general the implementation will use one of these references.
	 * @param context
	 *            a reference resolution context, needed potentially to access
	 *            the existing external references or to construct the new one,
	 *            especially in cases where the context contains security agents
	 *            giving access to a remote data staging system
	 * @return the newly constructed ExternalReferenceSPI instance.
	 */
	public ExternalReferenceSPI createReference(
			ReferenceSet existingReferences, ReferenceContext context);

	/**
	 * As with {@link #createReference(ReferenceSet, ReferenceContext)} but
	 * handled in an asynchronous fashion through the specified callback
	 * 
	 * @param callback
	 *            a callback object used to notify the caller of reference
	 *            contruction or failure thereof.
	 */
	public void createReferenceAsynch(ReferenceSet existingReferences,
			ReferenceContext context,
			ExternalReferenceConstructionCallback callback);

}
