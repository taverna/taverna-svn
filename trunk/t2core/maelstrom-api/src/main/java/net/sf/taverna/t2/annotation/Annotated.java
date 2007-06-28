package net.sf.taverna.t2.annotation;

import java.util.Set;

/**
 * Denotes that the object carries workflow object level annotation. Rather than
 * defining specific annotation types for each workflow entity we work on the
 * basis that multiple annotations of different types may apply, so free text
 * description is one example, semantic annotation of the internal function of a
 * processor might be another.
 * 
 * @author Tom Oinn
 * 
 */
public interface Annotated {

	Set<WorkflowAnnotation> getAnnotations();

}
