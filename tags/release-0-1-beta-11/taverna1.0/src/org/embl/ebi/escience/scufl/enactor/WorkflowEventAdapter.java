/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

import org.embl.ebi.escience.scufl.enactor.event.*;

/**
 * An abstract adapter around the workflow event listener
 * interface allowing for convenient implementation of
 * a subset of the available event handlers.
 * @author Tom Oinn
 */
public abstract class WorkflowEventAdapter implements WorkflowEventListener {

    /**
     * Called when a workflow instance has been submitted
     * along with associated input data to an enactor instance.
     * Methods on the WorkflowCreationEvent allow access to
     * the underlying workflow instance, the user context
     * and the enactor.
     */
    public void workflowCreated(WorkflowCreationEvent e) {
	//
    }
    
    /**
     * Called when a workflow instance fails for some reason
     */
    public void workflowFailed(WorkflowFailureEvent e) {
	//
    }
    
    /**
     * Called when a previously scheduled workflow completes
     * successfuly. This is called after results are available,
     * so storage plugins may rely on the getResults method
     * on the workflow instance references within the event
     * being valid.
     */
    public void workflowCompleted(WorkflowCompletionEvent e) {
	//
    }
    
    /**
     * Called when an individual processor within a workflow
     * completes its invocation successfuly. For cases where
     * iteration is involved this is called once for each invocation
     * of the processor task within the iteration.
     */
    public void processCompleted(ProcessCompletionEvent e) {
	//
    }
    
    /**
     * Called when the iteration stage of the processor is completed
     * the event carries details of the LSIDs of the component results
     * which are now integrated into the result of the process
     */
    public void processCompletedWithIteration(IterationCompletionEvent e) {
	//
    }
    
    /**
     * Called when a process fails - typically this will be followed
     * by a WorkflowFailed event.
     */
    public void processFailed(ProcessFailureEvent e) {
	//
    }
    
    /**
     * Called when a data item is wrapped up inside a default collection
     * prior to being passed to a service expecting a higher cardinality
     * version of the same input type
     */
    public void collectionConstructed(CollectionConstructionEvent e) {
	//
    }
   

}
