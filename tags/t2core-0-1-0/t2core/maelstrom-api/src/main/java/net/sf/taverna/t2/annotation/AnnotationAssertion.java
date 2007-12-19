package net.sf.taverna.t2.annotation;

/**
 * Represents a single assertion of information, providing access to a bean
 * containing the information in the assertion and one specifying the source of
 * the information contained.
 * 
 * @author Tom Oinn
 * 
 */
public interface AnnotationAssertion<AnnotationBeanType extends AnnotationBeanSPI>
		extends Curateable {

	/**
	 * Each annotation assertion contains a bean specifying the actual
	 * annotation, varying from a simple string for a free text description to
	 * more sophisticated semantic annotations or controlled vocabularies.
	 * 
	 * @return the annotation bean specifying this annotation assertion
	 */
	public AnnotationBeanType getDetail();

	/**
	 * The annotation assertion plays one of several roles within the annotation
	 * chain, either an initial assertion, a refinement of a previous assertion
	 * or a replacement of a previous assertion.
	 * 
	 * @return the annotation role of this annotation
	 */
	public AnnotationRole getRole();

}
