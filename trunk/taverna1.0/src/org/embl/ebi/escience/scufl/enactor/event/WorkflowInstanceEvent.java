/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;

import org.embl.ebi.escience.scufl.enactor.*;

public class WorkflowInstanceEvent {

    private EnactorProxy enactor;
    private UserContext userContext;
    private WorkflowInstance workflowInstance;

    public WorkflowInstance getWorkflowInstance() {
	return this.workflowInstance;
    }

    public EnactorProxy getEnactor() {
	return this.enactor;
    }

    public UserContext getUserContext() {
	return this.userContext;
    }

}
