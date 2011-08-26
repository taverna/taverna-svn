/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Filter a list of Strings, only passing through those that match the supplied
 * regular expression.
 * 
 * @author Tom Oinn
 */
public class FilterStringList implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "stringlist", "regex" };
	}

	public String[] inputTypes() {
		return new String[] { "l('text/plain')", "'text/plain'" };
	}

	public String[] outputNames() {
		return new String[] { "filteredlist" };
	}

	public String[] outputTypes() {
		return new String[] { "l('text/plain')" };
	}

	/**
	 * Copy each entry in the input list into the output list iff it matches the
	 * supplied regular expression.
	 */
	public Map execute(Map inputs) throws TaskExecutionException {
		List inputList = (List) ((DataThing) (inputs.get("stringlist"))).getDataObject();
		String pattern = (String) ((DataThing) (inputs.get("regex"))).getDataObject();
		List outputList = new ArrayList();
		for (Iterator i = inputList.iterator(); i.hasNext();) {
			String item = (String) i.next();
			if (item.matches(pattern)) {
				outputList.add(item);
			}
		}
		Map outputs = new HashMap();
		outputs.put("filteredlist", new DataThing(outputList));
		return outputs;
	}

}
