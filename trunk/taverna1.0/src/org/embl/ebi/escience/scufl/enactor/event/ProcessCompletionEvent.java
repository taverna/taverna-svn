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
	super(workflow);
	this.isIterating = isIterating;
	this.inputMap = inputs;
	this.outputMap = outputs;
	this.processor = proc;
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
	sb.append("Process '"+processor.getName()+"' complete ");
	if (isIterating) {
	    sb.append("(iterating)");
	}
	else {
	    sb.append("(simple)");
	}
	sb.append("\n");
	String prefix = "in  ";
	for (Iterator i = inputMap.keySet().iterator(); i.hasNext(); ) {
	    String inputKey = (String)i.next();
	    DataThing inputThing = (DataThing)inputMap.get(inputKey);
	    String mainLSID = inputThing.getLSID(inputThing.getDataObject());
	    sb.append(prefix+"'"+inputKey+"'->"+mainLSID+"\n");
	    prefix = "    ";
	}
	prefix = "out ";
	for (Iterator i = outputMap.keySet().iterator(); i.hasNext(); ) {
	    String outputKey = (String)i.next();
	    DataThing outputThing = (DataThing)outputMap.get(outputKey);
	    String mainLSID = outputThing.getLSID(outputThing.getDataObject());
	    sb.append(prefix+""+mainLSID+"->'"+outputKey+"'\n");
	    prefix = "    ";
	}
	return sb.toString();
    }
		

	    

}
