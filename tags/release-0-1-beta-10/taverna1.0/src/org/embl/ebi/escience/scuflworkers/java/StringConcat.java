/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import java.lang.String;



/**
 * Returns the result of appending firststring to secondstring
 * @author Tom Oinn
 */
public class StringConcat implements LocalWorker {
    
    public String[] inputNames() {
	return new String[]{"string1","string2"};
    }
    public String[] inputTypes() {
	return new String[]{"'text/plain'","'text/plain'"};
    }
    public String[] outputNames() {
	return new String[]{"output"};
    }
    public String[] outputTypes() {
	return new String[]{"'text/plain'"};
    }
    
    public Map execute(Map inputs) throws TaskExecutionException {
	try {
	    String firstString = (String)((DataThing)inputs.get("string1")).getDataObject();	
	    String secondString = (String)((DataThing)inputs.get("string2")).getDataObject();
	    Map outputs = new HashMap();
	    outputs.put("output",new DataThing(firstString+secondString));
	    return outputs;
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex);
	}
    }


}
