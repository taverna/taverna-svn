package net.sf.taverna.t2.annotation;

/**
 * Specifies the role of an AnnotationAssertion within an AnnotationChain
 * 
 * @author Tom Oinn
 */
public enum AnnotationRole {

	/**
	 * The information assertion is the first in the chain (if this is applied
	 * to an annotation that isn't the earliest in its chain it should be
	 * treated as a validation failure)
	 */
	INITIAL_ASSERTION,

	/**
	 * The information assertion was added to the chain to refine the existing
	 * annotation assertion or assertions, such as cases where a generic
	 * description exists which can be specialized in a particular instance but
	 * where the original more generic form is still correct
	 */
	REFINEMENT,

	/**
	 * The information assertion was added to the chain in order to override an
	 * earlier information assertion which was regarded as incorrect.
	 */
	REPLACEMENT;

}
