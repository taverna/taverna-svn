/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.*;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import java.lang.String;
import org.embl.ebi.escience.baclava.Base64;

/**
 * Decode base64 string into byte[]
 * @author Tom Oinn
 */
public class DecodeBase64 implements LocalWorker {

    public String[] inputNames() {
	return new String[]{"base64"};
    }
    public String[] inputTypes() {
	return new String[]{LocalWorker.STRING};
    }
    public String[] outputNames() {
	return new String[]{"bytes"};
    }
    public String[] outputTypes() {
	return new String[]{"'application/octet-stream'"};
    }
    
    public Map execute(Map inputs) throws TaskExecutionException {
	Map results = new HashMap();
	String base64 = (String)((DataThing)inputs.get("base64")).getDataObject();
	byte[] bytes = Base64.decode(base64);
	results.put("bytes",new DataThing(bytes));
	return results;
    }
    
}
