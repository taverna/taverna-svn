package net.sf.taverna.t2.annotation;

import java.util.Set;

/**
 * Denotes that the object carries workflow object level annotation. Rather than
 * defining specific annotation types for each workflow entity we work on the
 * basis that multiple annotations of different types may apply, so free text
 * description is one example, semantic annotation of the internal function of a
 * processor might be another.
 * <p>
 * Where annotations are conceptually editable such as free text descriptions
 * the editing framework should internally remove the original annotation and
 * add the replacement rather than modifying the previous annotation in place.
 * 
 * @author Tom Oinn
 * 
 */
public interface Annotated {

	/**
	 * Each annotated object contains a bag of metadata object instances
	 * 
	 * @return set of metadata objects that apply to the annotated object
	 */
	Set<WorkflowAnnotation> getAnnotations();

	

}
