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
public interface ExternalReferenceTranslatorSPI<SourceType extends ExternalReferenceSPI, TargetType extends ExternalReferenceSPI> {

	/**
	 * Given an existing ExternalReferenceSPI, build the appropriate target
	 * ExternalReferenceSPI implementation and return it.
	 * 
	 * @param sourceReference
	 *            the reference to be used as source for the translation.
	 * @param context
	 *            a reference resolution context, needed potentially to access
	 *            the existing external references or to construct the new one,
	 *            especially in cases where the context contains security agents
	 *            giving access to a remote data staging system
	 * @return the newly constructed ExternalReferenceSPI instance.
	 */
	public TargetType createReference(SourceType sourceReference,
			ReferenceContext context);

	/**
	 * As with {@link #createReference(ReferenceSet, ReferenceContext)} but
	 * handled in an asynchronous fashion through the specified callback
	 * 
	 * @param callback
	 *            a callback object used to notify the caller of reference
	 *            contruction or failure thereof.
	 */
	public void createReferenceAsynch(SourceType sourceReference,
			ReferenceContext context,
			ExternalReferenceConstructionCallback<TargetType> callback);

	/**
	 * Return the type of external reference that this translator consumes.
	 * 
	 * @return ExternalReferenceSPI class corresponding to the reference type
	 *         used as a source by this translator.
	 */
	public Class<SourceType> getSourceReferenceType();

	/**
	 * Return the type of external reference this translator constructs.
	 * 
	 * @return ExternalReferenceSPI class corresponding to the reference type
	 *         emitted by this translator.
	 */
	public Class<TargetType> getTargetReferenceType();

	/**
	 * Because the reference translator may rely on facilities provided to it
	 * through the context this method is available to check whether these
	 * facilities are sufficient.
	 * 
	 * @param context
	 *            the reference context that will be used to construct new
	 *            references during the translation process
	 * @return whether the context contains necessary resources for the
	 *         reference construction process
	 */
	public boolean isEnabled(ReferenceContext context);

	/**
	 * Return an approximate complexity cost of the translation. In general we
	 * can't make any guarantees about this because the complexity of the
	 * translation depends on more than just the types involved - it can depend
	 * on local configuration, network location relative to the data stores
	 * referenced and in some cases on the data themselves. For now though we
	 * assign an approximation, the default value is 1.0f and lower values
	 * represent less costly operations.
	 */
	public float getTranslationCost();

}
