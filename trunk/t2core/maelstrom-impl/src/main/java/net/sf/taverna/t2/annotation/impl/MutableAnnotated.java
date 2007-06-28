package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;

/**
 * Internally implementation classes implement this so edits can operate over
 * them in a generic way. Technically there may be better ways of doing this but
 * it's not a security issue - if people really want to cast to implementation
 * packages or use reflection to break the edit model that's their problem.
 * 
 * @author Tom Oinn
 * 
 */
public interface MutableAnnotated extends Annotated {

	/**
	 * Add a new annotation to the annotated object
	 * 
	 * @param newAnnotation
	 *            the metadata object to add
	 */
	void addAnnotation(WorkflowAnnotation newAnnotation);

	/**
	 * Remove an existing piece of metadata from the annotated object
	 * 
	 * @param annotationToRemove
	 *            the metadata object to remove
	 */
	void removeAnnotation(WorkflowAnnotation annotationToRemove);

}
