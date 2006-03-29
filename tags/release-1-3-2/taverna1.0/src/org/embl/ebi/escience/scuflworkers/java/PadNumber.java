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
 * Pad a numeral with leading zeroes to take it up to
 * a specified length, which defaults to seven.
 *
 * @author Tom Oinn
 */
public class PadNumber implements LocalWorker {
    
    public String[] inputNames() {
	return new String[]{"input","targetlength"};
    }
    public String[] inputTypes() {
	return new String[]{"'text/plain'","'text/plain'"};
    }
    public String[] outputNames() {
	return new String[]{"padded"};
    }
    public String[] outputTypes() {
	return new String[]{"'text/plain'"};
    }
    
    public Map execute(Map inputs) throws TaskExecutionException {
	String input = (String)((DataThing)(inputs.get("input"))).getDataObject();
	int targetLength = 7;
	if (inputs.containsKey("targetlength")) {
	    targetLength = Integer.parseInt((String)((DataThing)(inputs.get("targetlength"))).getDataObject());
	}
	int currentLength = input.length();
	while (input.length() < targetLength) {
	    input = "0"+input;
	}
	Map outputs = new HashMap();
	outputs.put("padded", new DataThing(input));
	return outputs;
    }


}
