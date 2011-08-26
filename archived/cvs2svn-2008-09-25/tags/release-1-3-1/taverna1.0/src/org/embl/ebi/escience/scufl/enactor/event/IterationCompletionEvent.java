/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.event;
import java.util.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.baclava.*;

public class IterationCompletionEvent extends WorkflowInstanceEvent {
    
    private Processor processor;
    private Map structureMapping, inputShredding, inputMap, outputMap;

    public IterationCompletionEvent(Map structureMapping,
				    Map inputShredding,
				    WorkflowInstance wf,
				    Processor activeProcessor,
				    Map inputs,
				    Map outputs) {
	super(wf);
	this.structureMapping = structureMapping;
	this.inputShredding = inputShredding;
	this.processor = activeProcessor;
	this.inputMap = inputs;
	this.outputMap = outputs;
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
     * Returns a map of port name -> datathing corresponding to the
     * overall inputs and outputs of the process which has been iterated
     * over. These input values are the inputs as fed to the process
     * before any iteration or composition has occured
     */
    public Map getOverallInputs() {
	return this.inputMap;
    }

    /**
     * As for the getOverallInputs but returns the final result map
     * for this processor
     */
    public Map getOverallOutputs() {
	return this.outputMap;
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
	    sb.append("'"+findNameFromLSID(keyLSID)+"'->"+keyLSID+" built from {\n");
	    for (Iterator j = componentLSIDs.iterator(); j.hasNext(); ) {
		sb.append("  "+(String)j.next()+"\n");
	    }
	    sb.append("}\n");
	}
       	for (Iterator i = inputShredding.keySet().iterator(); i.hasNext(); ) {
	    String keyLSID = (String)i.next();
	    Set componentLSIDs = (Set)inputShredding.get(keyLSID);
	    sb.append("\n'"+findNameFromLSID(keyLSID)+"'->"+keyLSID+" decomposed to {\n");
	    for (Iterator j = componentLSIDs.iterator(); j.hasNext(); ) {
		sb.append("  "+(String)j.next()+"\n");
	    }
	    sb.append("}\n");
	}
	return sb.toString();
    }

    /**
     * Utility method to find the input or output name for a given LSID,
     * returns the empty string if none is found. This only searches
     * within the scope of this event!
     */
    public String findNameFromLSID(String LSID) {
	for (Iterator i = this.inputMap.keySet().iterator(); i.hasNext();) {
	    String name = (String)i.next();
	    DataThing thing = (DataThing)inputMap.get(name);
	    if (thing.getLSID(thing.getDataObject()).equals(LSID)) {
		return name;
	    }
	}
	for (Iterator i = this.outputMap.keySet().iterator(); i.hasNext();) {
	    String name = (String)i.next();
	    DataThing thing = (DataThing)outputMap.get(name);
	    if (thing.getLSID(thing.getDataObject()).equals(LSID)) {
		return name;
	    }
	}
	return "";
    }
}
