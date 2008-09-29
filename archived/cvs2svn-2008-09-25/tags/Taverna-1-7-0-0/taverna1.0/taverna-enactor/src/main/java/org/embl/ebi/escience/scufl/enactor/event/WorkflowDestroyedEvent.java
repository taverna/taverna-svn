package org.embl.ebi.escience.scufl.enactor.event;


/**
 * This event is sent after workflowInstance.destroy() has been called.
 * <p>
 * This is the latest message you receive about this workflow instance, 
 * which by now should no longer be accessed. getWorkflowInstance() on 
 * this event will therefore always return null, but you can access 
 * what would have been the result of workflowInstance.getID() by
 * calling getWorkflowInstanceID().
 * <p>
 * If you would like to access the instance before it has been destroyed, 
 * look for the WorkflowDestructionEvent event instead.
 * 
 * @see WorkflowToBeDestroyedEvent
 * @author Stian Soiland
 *
 */
public class WorkflowDestroyedEvent extends WorkflowInstanceEvent {

	private String workflowInstanceID;

	public WorkflowDestroyedEvent(String instanceID) {
		super(null);
		workflowInstanceID = instanceID;
	}
	
	public String getWorkflowInstanceID() {
		return workflowInstanceID;
	}
	
	public String toString() {
		return "Workflow '" + getWorkflowInstanceID()
				+ "' was destroyed.\n";
	}
	

}
