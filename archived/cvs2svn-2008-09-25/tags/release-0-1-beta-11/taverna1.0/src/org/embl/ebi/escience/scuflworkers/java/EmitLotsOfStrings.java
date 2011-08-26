/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;
import java.util.*;
import org.embl.ebi.escience.baclava.*;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Spit out a list of 40 strings
 */
public class EmitLotsOfStrings implements LocalWorker {

    public String[] inputNames() {
	return new String[]{};
    }
    public String[] inputTypes() {
	return new String[]{};
    }
    public String[] outputNames() {
	return new String[]{"strings"};
    }
    public String[] outputTypes() {
	return new String[]{"l('text/plain')"};
    }
    
    /**
     * Just throw an exception!
     */
    public Map execute(Map inputs) throws TaskExecutionException {
	String[] results = new String[40];
	for (int i = 0; i < 40; i++) {
	    results[i] = "String"+i;
	}
	DataThing resultThing = new DataThing(results);
	Map outputs = new HashMap();
	outputs.put("strings",resultThing);
	return outputs;
    }
}
