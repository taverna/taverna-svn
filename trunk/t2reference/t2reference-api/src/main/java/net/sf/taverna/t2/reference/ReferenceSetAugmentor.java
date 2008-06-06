package net.sf.taverna.t2.reference;

import java.util.Set;

/**
 * Provides a framework to find and engage appropriate instances of
 * {@link ExternalReferenceTranslatorSPI} and
 * {@link ExternalReferenceBuilderSPI} to build external references from,
 * respectively, other external references and from streams. These are then used
 * to augment the contents of implementations of {@link ReferenceSet} with
 * additional {@link ExternalReferenceSPI} implementations.
 * <p>
 * Methods in this interface throw the runtime exception
 * {@link ReferenceSetAugmentationException} for all problems, other exceptions
 * are wrapped in this type and re-thrown.
 * 
 * @author Tom Oinn
 */
public interface ReferenceSetAugmentor {

	/**
	 * Attempts to modify the supplied ReferenceSet such that it contains an
	 * implementation of at least one of the ExternalReferenceSPI classes
	 * specified. Uses the supplied context if required to build or translate
	 * existing references within the reference set.
	 * 
	 * @param references
	 *            reference set object to augment
	 * @param targetReferenceTypes
	 *            a set of Class objects, this method succeeds if it can create
	 *            an instance of at least one of these pointing to the same data
	 *            as the other external references in the supplied reference set
	 * @param context
	 *            a reference resolution context, potentially required for
	 *            access to the existing references or for creation of the
	 *            augmentations
	 * @return augmented reference set object
	 * @throws ReferenceSetAugmentationException
	 *             if a problem occurs either in configuration of the
	 *             ReferenceSetAugmentor or in the augmentation process itself.
	 *             Any other exception types are wrapped in this and re-thrown.
	 */
	public ReferenceSet augmentReferenceSet(ReferenceSet references,
			Set<Class<? extends ExternalReferenceSPI>> targetReferenceTypes,
			ReferenceContext context) throws ReferenceSetAugmentationException;

	/**
	 * As with {@link #augmentReferenceSet(ReferenceSet, Set, ReferenceContext)}
	 * but called in an asynchronous fashion. Returns immediately and uses the
	 * supplied instance of {@link ReferenceSetAugmentorCallback} to provide
	 * either the augmented {@link ReferenceSet} or an exception indicating a
	 * failure in the augmentation process.
	 * 
	 * @param callback
	 *            callback object used to indicate failure or to return the
	 *            modified reference set
	 * @throws ReferenceSetAugmentationException
	 *             if the ReferenceSetAugmentor is missing critical
	 *             configuration. Exceptions that happen during augmentation or
	 *             as a result of a failure to find an appropriate augmentation
	 *             path are signalled by calls to the callback object, this
	 *             method only throws the exception if it can't even try to do
	 *             the augmentation for some reason.
	 */
	public void augmentReferenceSetAsynch(ReferenceSet references,
			Set<Class<? extends ExternalReferenceSPI>> targetReferenceTypes,
			ReferenceContext context, ReferenceSetAugmentorCallback callback)
			throws ReferenceSetAugmentationException;

}
