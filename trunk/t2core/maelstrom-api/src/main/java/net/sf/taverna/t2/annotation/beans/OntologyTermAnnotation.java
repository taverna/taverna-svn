/**
 * 
 */
package net.sf.taverna.t2.annotation.beans;

import net.sf.taverna.t2.annotation.WorkflowAnnotation;

/**
 * @author alanrw
 *
 */
public interface OntologyTermAnnotation extends WorkflowAnnotation {
	/**
	 * @return
	 */
	OntologyTerm getTerm();
}
