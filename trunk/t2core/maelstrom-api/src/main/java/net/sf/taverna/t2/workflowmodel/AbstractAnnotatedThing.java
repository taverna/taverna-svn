package net.sf.taverna.t2.workflowmodel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;

/**
 * Convenient abstract superclass for annotated things
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractAnnotatedThing implements Annotated {

	protected Set<WorkflowAnnotation> annotations = new HashSet<WorkflowAnnotation>();
	
	public Set<WorkflowAnnotation> getAnnotations() {
		return Collections.unmodifiableSet(annotations);
	}

}
