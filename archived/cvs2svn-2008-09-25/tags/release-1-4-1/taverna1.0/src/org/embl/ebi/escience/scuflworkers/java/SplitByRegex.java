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
 * Split an input string into a list of strings using the given regular
 * expression to determine the delimiter. If the regular expression is not
 * supplied then it will default to the ',' character
 * 
 * @author Tom Oinn
 */
public class SplitByRegex implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "string", "regex" };
	}

	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'" };
	}

	public String[] outputNames() {
		return new String[] { "split" };
	}

	public String[] outputTypes() {
		return new String[] { "l('text/plain')" };
	}

	/**
	 * Use the String.split() method to split the input
	 */
	public Map execute(Map inputs) throws TaskExecutionException {
		String input = (String) ((DataThing) (inputs.get("string")))
				.getDataObject();
		List output = new ArrayList();
		// ktg: Added check so an empty string returns an empty list
		// Any cases where this isn't true?
		if (!input.equals("")) {
			String regex = ",";
			if (inputs.containsKey("regex")) {
				regex = (String) ((DataThing) (inputs.get("regex")))
						.getDataObject();
			}
			String[] result = input.split(regex);
			for (int i = 0; i < result.length; i++) {
				output.add(result[i]);
			}
		}
		Map outputs = new HashMap();
		outputs.put("split", new DataThing(output));
		return outputs;
	}
}