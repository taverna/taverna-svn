/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

import org.embl.ebi.escience.scufl.enactor.event.CollectionConstructionEvent;
import org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowDestroyedEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowToBeDestroyedEvent;

/**
 * Implementations express an interest in events produced during the lifecycle
 * of an enactor instance including but not limited to workflow creation and
 * process reports
 * 
 * @see WorkflowEventAdapter
 * 
 * @author Tom Oinn
 */
public interface WorkflowEventListener {

	/**
	 * Called when a workflow instance has been submitted along with associated
	 * input data to an enactor instance. Methods on the WorkflowCreationEvent
	 * allow access to the underlying workflow instance, the user context and
	 * the enactor.
	 */
	public void workflowCreated(WorkflowCreationEvent e);

	/**
	 * Called when a workflow instance fails for some reason
	 */
	public void workflowFailed(WorkflowFailureEvent e);

	/**
	 * Called when a previously scheduled workflow completes successfuly. This
	 * is called after results are available, so storage plugins may rely on the
	 * getResults method on the workflow instance references within the event
	 * being valid.
	 */
	public void workflowCompleted(WorkflowCompletionEvent e);
	
	/**
	 * Called when a nested workflow fails. The event contains an referenece to the 
	 * failed workflow instance.
	 */
	public void nestedWorkflowFailed(NestedWorkflowFailureEvent e);
	
	
	/**
	 * Called when a nested workflow instance is created and about to be invoked
	 * by the enactor instance. Where a nested workflow exists within an iteration, this
	 * will be called for each iteration.The event carries details of the workflow instance created.
	 */
	public void nestedWorkflowCreated(NestedWorkflowCreationEvent e);
	
	/**
	 * Called when a nested workflow instance has completed its invocation 
	 * successfully. The event carries with it details of the workflow instance invoked.
	 * 
	 * @param e
	 */
	public void nestedWorkflowCompleted(NestedWorkflowCompletionEvent e);
	
	/**
	 * Called when an individual processor within a workflow completes its
	 * invocation successfuly. For cases where iteration is involved this is
	 * called once for each invocation of the processor task within the
	 * iteration.	
	 */
	public void processCompleted(ProcessCompletionEvent e);

	/**
	 * Called when the iteration stage of the processor is completed the event
	 * carries details of the LSIDs of the component results which are now
	 * integrated into the result of the process
	 */
	public void processCompletedWithIteration(IterationCompletionEvent e);

	/**
	 * Called when a process fails - typically this will be followed by a
	 * WorkflowFailed event.
	 */
	public void processFailed(ProcessFailureEvent e);

	/**
	 * Called when a user changes intemediate data (output).
	 */
	public void dataChanged(UserChangedDataEvent e);

	/**
	 * Called when a data item is wrapped up inside a default collection prior
	 * to being passed to a service expecting a higher cardinality version of
	 * the same input type
	 */
	public void collectionConstructed(CollectionConstructionEvent e);

	/**
	 * Called right before workflowInstance.destroy() is to be called.
	 * (Usually this has been triggered by the user clicking a 
	 * "Close" button in the result window)
	 * <p>
	 * This is your last chance to access the workflow instance before it 
	 * becomes unusable. workflowDestroyed(WorkflowDestroyedEvent) 
	 * will be called after destroy() has been invoked, but at that
	 * point it will be too late to access the instance.
	 * <p>
	 * <strong>Note</strong>: This is the last chance to access
	 * workflowInstance before it is destroyed. If you have your own 
	 * references to the instance or any of the data of workflowInstance
	 * (such as the input map), this is the time to remove such references.
	 * 
	 */
	public void workflowToBeDestroyed(WorkflowToBeDestroyedEvent event);

	/**
	 * This event is sent after workflowInstance.destroy() has been called.
	 * <p>
	 * This is the last message you receive about this workflow instance, 
	 * which by now should not be accessed anymore.
	 * <p>
	 * event.getWorkflowInstance() on this event will therefore always 
	 * return null, but you can access what would have been the result 
	 * of workflowInstance.getID() by calling 
	 * event.getWorkflowInstanceID().
	 * <p>
	 * If you would like to access the instance before it has been destroyed, 
	 * do so from workflowToBeDestroyed(WorkflowToBeDestroyedEvent)
	 */
	public void workflowDestroyed(WorkflowDestroyedEvent event);

}
