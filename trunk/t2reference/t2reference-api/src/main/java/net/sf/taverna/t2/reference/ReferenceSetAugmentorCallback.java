package net.sf.taverna.t2.reference;

import java.util.Set;

/**
 * Callback interface used when augmenting a ReferenceSet in an asynchronous
 * fashion through
 * {@link ReferenceSetAugmentor#augmentReferenceSetAsynch(ReferenceSet, Set, ReferenceContext, ReferenceSetAugmentorCallback) augmentReferenceSetAsynch}
 * in {@link ReferenceSetAugmentor}.
 * 
 * @author Tom Oinn
 * 
 */
public interface ReferenceSetAugmentorCallback {

	/**
	 * Called when the augmentation has succeeded
	 * 
	 * @param newReferences
	 *            a set of ExternalReferenceSPI instances created during the
	 *            augmentation process. It is the responsibility of the caller
	 *            to re-integrate these back into the ReferenceSet used in the
	 *            translation
	 */
	public void augmentationCompleted(Set<ExternalReferenceSPI> newReferences);

	/**
	 * Called when the augmentation has failed for some reason
	 * 
	 * @param cause
	 *            a {@link ReferenceSetAugmentationException} object describing
	 *            the failure.
	 */
	public void augmentationFailed(ReferenceSetAugmentationException cause);

}
