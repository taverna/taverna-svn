/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;

public class ProcessFailureEvent extends WorkflowInstanceEvent {

    private Exception cause;
    private Processor processor;
    
    public ProcessFailureEvent(WorkflowInstance workflow,
			       Processor processor,
			       Exception cause) {
	super(workflow);
	this.processor = processor;
	this.cause = cause;
    }

    public Exception getCause() {
	return this.cause;
    }
    
    public Processor getProcessor() {
	return this.processor;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Processor '"+processor.getName()+"' failed, cause :\n  ");
	sb.append(cause.toString());
	sb.append("\n");
	return sb.toString();
    }

}
