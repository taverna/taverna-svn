/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;

import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.baclava.DataThing;
import java.util.Map;
import java.util.Iterator;

public class WorkflowCompletionEvent extends WorkflowInstanceEvent {

	public WorkflowCompletionEvent(WorkflowInstance workflow) {
		super(workflow);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Workflow '" + workflowInstance.getID()
				+ "' completed with outputs\n");
		Map outputs = workflowInstance.getOutput();
		for (Iterator i = outputs.keySet().iterator(); i.hasNext();) {
			String outputName = (String) i.next();
			DataThing outputValue = (DataThing) outputs.get(outputName);
			String outputLSID = outputValue
					.getLSID(outputValue.getDataObject());
			sb.append("  " + outputLSID + "->'" + outputName + "'\n");
		}
		return sb.toString();
	}
}
