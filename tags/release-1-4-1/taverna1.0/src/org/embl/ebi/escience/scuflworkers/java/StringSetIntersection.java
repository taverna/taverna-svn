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
import java.lang.Object;
import java.lang.String;

/**
 * Returns the intersection of two sets or lists of string types where elements
 * only exist in the output if they occur in both inputs.
 * 
 * @author Tom Oinn
 */
public class StringSetIntersection implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "list1", "list2" };
	}

	public String[] inputTypes() {
		return new String[] { LocalWorker.STRING_ARRAY,
				LocalWorker.STRING_ARRAY };
	}

	public String[] outputNames() {
		return new String[] { "intersection" };
	}

	public String[] outputTypes() {
		return new String[] { LocalWorker.STRING_ARRAY };
	}

	/**
	 * Fetch the web page pointed to by the URL supplied as the 'url' parameter
	 * into the service, the 'base' parameter specifies a URL to use as the base
	 * for relative URL resolution.
	 */
	public Map execute(Map inputs) throws TaskExecutionException {
		Collection list1 = (Collection) ((DataThing) inputs.get("list1"))
				.getDataObject();
		Collection list2 = (Collection) ((DataThing) inputs.get("list2"))
				.getDataObject();
		List resultList = new ArrayList();
		for (Iterator i = list1.iterator(); i.hasNext();) {
			Object o = i.next();
			if (list2.contains(o)) {
				resultList.add(o);
			}
		}
		Map outputs = new HashMap();
		outputs.put("intersection", new DataThing(resultList));
		return outputs;
	}

}
