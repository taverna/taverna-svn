/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import java.lang.String;



/**
 * Consume a list of lists and emit a list containing the first
 * level flattening of the input.
 * @author Tom Oinn
 */
public class FlattenList implements LocalWorker {
    
    public String[] inputNames() {
	return new String[]{"inputlist"};
    }
    public String[] inputTypes() {
	return new String[]{"l(l(''))"};
    }
    public String[] outputNames() {
	return new String[]{"outputlist"};
    }
    public String[] outputTypes() {
	return new String[]{"l('')"};
    }
    
    /**
     * Copy each entry in the input list into the output list iff
     * it matches the supplied regular expression.
     */
    public Map execute(Map inputs) throws TaskExecutionException {
	List inputList = (List)((DataThing)(inputs.get("inputlist"))).getDataObject();
	List outputList = new ArrayList();
	for (Iterator i = inputList.iterator(); i.hasNext(); ) {
	    List nestedList = (List)i.next();
	    for (Iterator j = nestedList.iterator(); j.hasNext(); ) {
		outputList.add(j.next());
	    }
	}
	Map outputs = new HashMap();
	outputs.put("outputlist", new DataThing(outputList));
	return outputs;
    }

}
