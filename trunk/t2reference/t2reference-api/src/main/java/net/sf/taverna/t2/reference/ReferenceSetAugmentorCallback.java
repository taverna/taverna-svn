package net.sf.taverna.t2.reference;

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
	 * @param result
	 *            the augmented reference set, which will now contain the
	 *            required ExternalReferenceSPI implementation amongst its
	 *            external references.
	 */
	public void augmentationCompleted(ReferenceSet result);

	/**
	 * Called when the augmentation has failed for some reason
	 * 
	 * @param cause
	 *            an exception object describing the failure.
	 */
	public void augmentationFailed(Exception cause);

}
