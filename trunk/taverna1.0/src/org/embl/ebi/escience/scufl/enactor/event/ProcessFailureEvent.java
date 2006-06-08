/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;

import java.util.Map;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;

public class ProcessFailureEvent extends WorkflowInstanceEvent {

	private Exception cause;

	private Processor processor;

	private Map inputs;

	public ProcessFailureEvent(WorkflowInstance workflow, Processor processor,
			Exception cause, Map inputs) {
		super(workflow);
		this.processor = processor;
		this.cause = cause;
		this.inputs = inputs;
	}

	public Exception getCause() {
		return this.cause;
	}

	public Processor getProcessor() {
		return this.processor;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb
				.append("Processor '" + processor.getName()
						+ "' failed, cause :\n  ");
		sb.append(cause.toString());
		sb.append("\n");
		return sb.toString();
	}

	public Map getInputMap() {
		return this.inputs;
	}

}
