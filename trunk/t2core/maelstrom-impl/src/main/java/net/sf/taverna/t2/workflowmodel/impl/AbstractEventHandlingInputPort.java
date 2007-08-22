package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.annotation.impl.MutableAnnotated;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

/**
 * Extends AbstractPort with the getIncomingLinks method and an additional
 * implementation method to set the incoming data link
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractEventHandlingInputPort extends AbstractPort
		implements EventHandlingInputPort, MutableAnnotated {

	private Datalink incomingLink = null;

	protected AbstractEventHandlingInputPort(String name, int depth) {
		super(name, depth);
	}

	public Datalink getIncomingLink() {
		return this.incomingLink;
	}

	protected void setIncomingLink(Datalink newLink) {
		this.incomingLink = newLink;
	}
	
	public void addAnnotation(WorkflowAnnotation newAnnotation) {
		annotations.add(newAnnotation);
	}

	public void removeAnnotation(WorkflowAnnotation annotationToRemove) {
		annotations.remove(annotationToRemove);
	}

}
