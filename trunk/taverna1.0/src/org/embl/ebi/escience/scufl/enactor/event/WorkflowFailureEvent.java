/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;
import org.embl.ebi.escience.scufl.enactor.*;

public class WorkflowFailureEvent extends WorkflowInstanceEvent {

    public WorkflowFailureEvent(WorkflowInstance workflow) {
	super(workflow);
    }
    
    public String toString() {
	return "Workflow '"+workflowInstance.getID()+"' failed or cancelled\n";
    }
}
