/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;

import org.embl.ebi.escience.scufl.Processor;
import java.util.*;
import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.baclava.*;

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

    /**
     * Print a summary of the event details
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Single process '"+processor.getName()+"' complete\n");
	sb.append("  inputs\n");
	for (Iterator i = inputMap.keySet().iterator(); i.hasNext(); ) {
	    String inputKey = (String)i.next();
	    DataThing inputThing = (DataThing)inputMap.get(inputKey);
	    String mainLSID = inputThing.getLSID(inputThing.getDataObject());
	    sb.append("    "+inputKey+"->"+mainLSID+"\n");
	}
	sb.append("  outputs\n");
	for (Iterator i = outputMap.keySet().iterator(); i.hasNext(); ) {
	    String outputKey = (String)i.next();
	    DataThing outputThing = (DataThing)outputMap.get(outputKey);
	    String mainLSID = outputThing.getLSID(outputThing.getDataObject());
	    sb.append("    "+outputKey+"->"+mainLSID+"\n");
	}
	return sb.toString();
    }
		

	    

}
