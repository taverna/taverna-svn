package org.embl.ebi.escience.scufl.enactor.event;

import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;

/**
 * This event is sent right before workflowInstance.destroy() is to be called.
 * <p>
 * This is your last chance to access the workflow instance before it will
 * become unusable. The event WorkflowDestroyedEvent will be sent after 
 * destroy() has been called, but you can no longer access the instance then. 
 * 
 * @see WorkflowDestroyedEvent
 * @author Stian Soiland
 *
 */
public class WorkflowToBeDestroyedEvent extends WorkflowInstanceEvent {

	public WorkflowToBeDestroyedEvent(WorkflowInstance workflow) {
		super(workflow);
	}
	
	public String toString() {
		return "Workflow '" + workflowInstance.getID()
				+ "' is to be destructed\n";
	}

}
