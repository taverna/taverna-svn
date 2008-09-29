/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Returns the items that are different between two sets or lists of string
 * types where elements only exist in the output if they occur in either input,
 * but not both.
 * 
 * @author Tom Oinn
 * @author Kevin Glover
 */
public class StringSetDifference implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "list1", "list2" };
	}

	public String[] inputTypes() {
		return new String[] { LocalWorker.STRING_ARRAY, LocalWorker.STRING_ARRAY };
	}

	public String[] outputNames() {
		return new String[] { "difference" };
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
		Collection list1 = (Collection) ((DataThing) inputs.get("list1")).getDataObject();
		Collection list2 = (Collection) ((DataThing) inputs.get("list2")).getDataObject();
		List resultList = new ArrayList();
		for (Iterator i = list1.iterator(); i.hasNext();) {
			Object o = i.next();
			if (!list2.contains(o)) {
				resultList.add(o);
			}
		}
		for (Iterator i = list2.iterator(); i.hasNext();) {
			Object o = i.next();
			if (!list1.contains(o)) {
				resultList.add(o);
			}
		}
		Map outputs = new HashMap();
		outputs.put("difference", new DataThing(resultList));
		return outputs;
	}
}
