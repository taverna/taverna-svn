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



/**
 * Consumes a string list and emits the string list with
 * duplicate entries removed. The first occurance of a
 * duplicate is preserved and all subsequent ones omited, i.e
 * the string list 'a,b,c,b,a,d' is converted to 'a,b,c,d'
 * @author Tom Oinn
 */
public class StringStripDuplicates implements LocalWorker {
    
    public String[] inputNames() {
	return new String[]{"stringlist"};
    }
    public String[] inputTypes() {
	return new String[]{"l('text/plain')"};
    }
    public String[] outputNames() {
	return new String[]{"strippedlist"};
    }
    public String[] outputTypes() {
	return new String[]{"l('text/plain')"};
    }
    
    public Map execute(Map inputs) throws TaskExecutionException {
	try {
	    List inputStringList = (List)((DataThing)inputs.get("stringlist")).getDataObject();	
	    List outputStringList = new ArrayList();
	    for (Iterator i = inputStringList.iterator(); i.hasNext();) {
		String item = (String)i.next();
		if (outputStringList.contains(item)==false) {
		    outputStringList.add(item);
		}
	    }
	    Map outputs = new HashMap();
	    outputs.put("strippedlist",new DataThing(outputStringList));
	    return outputs;
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex);
	}
    }


}
