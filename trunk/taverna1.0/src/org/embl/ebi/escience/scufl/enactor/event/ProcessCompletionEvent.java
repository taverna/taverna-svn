/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;

import org.embl.ebi.escience.scufl.Processor;
import java.util.Map;

public class ProcessCompletionEvent extends WorkflowInstanceEvent {

    private boolean isIterating;
    private Map inputMap, outputMap;
    private Processor processor;
    
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
