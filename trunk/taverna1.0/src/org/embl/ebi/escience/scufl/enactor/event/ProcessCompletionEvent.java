/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;

import org.embl.ebi.escience.scufl.Processor;
import java.util.Map;
import org.embl.ebi.escience.scufl.enactor.*;

public class ProcessCompletionEvent extends WorkflowInstanceEvent {

    private boolean isIterating;
    private Map inputMap, outputMap;
    private Processor processor;
    
    public ProcessCompletionEvent(boolean isIterating,
				  Map inputs,
				  Map outputs,
				  Processor proc,
				  WorkflowInstance workflow) {
	this.isIterating = isIterating;
	this.inputMap = inputs;
	this.outputMap = outputs;
	this.processor = proc;
	this.workflowInstance = workflow;
    }

    public boolean isIterating() {
	return isIterating;
    }
    
    public Map getInputMap() {
	return this.inputMap;
    }

    public Map getOutputMap() {
	return this.outputMap;
    }

    public Processor getProcessor() {
	return this.processor;
    }

}
