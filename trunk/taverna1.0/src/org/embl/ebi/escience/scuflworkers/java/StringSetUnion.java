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
 * Provide the union of two lists of strings, the
 * result being a string list containing all strings
 * that occur in either of the input lists.
 * @author Tom Oinn
 */
public class StringSetUnion implements LocalWorker {
    
    public String[] inputNames() {
	return new String[]{"list1","list2"};
    }
    public String[] inputTypes() {
	return new String[]{LocalWorker.STRING_ARRAY,LocalWorker.STRING_ARRAY};
    }
    public String[] outputNames() {
	return new String[]{"union"};
    }
    public String[] outputTypes() {
	return new String[]{LocalWorker.STRING_ARRAY};
    }
    
    public Map execute(Map inputs) throws TaskExecutionException {
	try {
	    Set results = new HashSet();
	    Collection list1 = (Collection)((DataThing)inputs.get("list1")).getDataObject();
	    Collection list2 = (Collection)((DataThing)inputs.get("list2")).getDataObject();
	    results.addAll(list1);
	    results.addAll(list2);
	    List resultList = new ArrayList();
	    resultList.addAll(results);
	    Map outputs = new HashMap();
	    outputs.put("union",new DataThing(resultList));
	    return outputs;
	}
	catch (NullPointerException npe) {
	    throw new TaskExecutionException("Must specify both input lists!");
	}
    }

}
