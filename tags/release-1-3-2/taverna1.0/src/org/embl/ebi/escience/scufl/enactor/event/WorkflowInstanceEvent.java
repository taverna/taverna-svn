/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;

import org.embl.ebi.escience.scufl.enactor.*;

public class WorkflowInstanceEvent {

    protected WorkflowInstanceEvent(WorkflowInstance workflow) {
	this.workflowInstance = workflow;
    }

    protected WorkflowInstance workflowInstance;

    public WorkflowInstance getWorkflowInstance() {
	return this.workflowInstance;
    }

}
