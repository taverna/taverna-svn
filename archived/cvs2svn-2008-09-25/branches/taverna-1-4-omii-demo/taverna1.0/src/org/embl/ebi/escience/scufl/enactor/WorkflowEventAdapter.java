/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

import org.embl.ebi.escience.scufl.enactor.event.*;

/**
 * An (abstract) adapter around the workflow event listener interface allowing for
 * convenient implementation of a subset of the available event handlers.
 * 
 * @author Tom Oinn
 */
// Note how it is NOT abstract to discover unimplemented methods
public class WorkflowEventAdapter implements WorkflowEventListener {

	/**
	 * Called when a workflow instance has been submitted along with associated
	 * input data to an enactor instance. Methods on the WorkflowCreationEvent
	 * allow access to the underlying workflow instance, the user context and
	 * the enactor.
	 */
	public void workflowCreated(WorkflowCreationEvent e) {	
	}
	public void workflowFailed(WorkflowFailureEvent e) {
	}
	public void workflowCompleted(WorkflowCompletionEvent e) {
	}
	public void processCompleted(ProcessCompletionEvent e) {
	}
	public void processCompletedWithIteration(IterationCompletionEvent e) {
	}
	public void processFailed(ProcessFailureEvent e) {
	}
	public void collectionConstructed(CollectionConstructionEvent e) {
	}
	public void dataChanged(UserChangedDataEvent e) {		
	}

}
