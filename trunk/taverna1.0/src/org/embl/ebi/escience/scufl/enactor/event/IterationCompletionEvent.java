/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;
import java.util.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.enactor.*;

public class IterationCompletionEvent extends WorkflowInstanceEvent {
    
    private Processor processor;
    private Map structureMapping, inputShredding;

    public IterationCompletionEvent(Map structureMapping,
				    Map inputShredding,
				    WorkflowInstance wf,
				    Processor activeProcessor) {
	this.structureMapping = structureMapping;
	this.inputShredding = inputShredding;
	this.processor = activeProcessor;
	this.workflowInstance = wf;
    }
    
    /**
     * Return a map containing information about the collections built
     * by this iteration. The keys in the map are the top level LSID
     * values for the collections produced by the iterations (one collection
     * and LSID per output), the values for these keys are sets, each
     * set contains the LSID values of the top level individual result
     * objects. The sets and their keys can be interpreted as 'key is
     * constructed from the members of this set'. The LSIDs in the set
     * will also be refered to as the object of statements generated
     * from the ProcessCompletionEvent objects
     */
    public Map getStructureMapping() {
	return this.structureMapping;
    }
    
    /**
     * This is the equivalent for the inputs, the keys are the LSIDs of
     * the overall input objects prior to iteration, the values are the
     * sets of child objects which those have been decomposed into in
     * order to get the correct cardinality types into the next operation
     */
    public Map getInputShredding() {
	return this.inputShredding;
    }

    public Processor getProcessor() {
	return this.processor;
    }

    /**
     * Print a summary of the information contained within this event
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	// Iterate over the keys in the map
	for (Iterator i = structureMapping.keySet().iterator(); i.hasNext(); ) {
	    String keyLSID = (String)i.next();
	    Set componentLSIDs = (Set)structureMapping.get(keyLSID);
	    sb.append("LSID '"+keyLSID+"' built from {\n");
	    for (Iterator j = componentLSIDs.iterator(); j.hasNext(); ) {
		sb.append("  "+(String)j.next()+"\n");
	    }
	    sb.append("}\n");
	}
	for (Iterator i = inputShredding.keySet().iterator(); i.hasNext(); ) {
	    String keyLSID = (String)i.next();
	    Set componentLSIDs = (Set)inputShredding.get(keyLSID);
	    sb.append("LSID '"+keyLSID+"' decomposed to {\n");
	    for (Iterator j = componentLSIDs.iterator(); j.hasNext(); ) {
		sb.append("  "+(String)j.next()+"\n");
	    }
	    sb.append("}\n");
	}
	return sb.toString();
    }

}
