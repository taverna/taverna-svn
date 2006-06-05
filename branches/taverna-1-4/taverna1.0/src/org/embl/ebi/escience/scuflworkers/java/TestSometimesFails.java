/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.*;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import java.lang.String;

/**
 * Processor which fails every four invocations.
 * 
 * @author Tom Oinn
 */
public class TestSometimesFails implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "in" };
	}

	public String[] inputTypes() {
		return new String[] { "'text/plain'" };
	}

	public String[] outputNames() {
		return new String[] { "out" };
	}

	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

	private static int counter = 0;

	/**
	 * Just throw an exception!
	 */
	public Map execute(Map inputs) throws TaskExecutionException {
		synchronized (this) {
			if (counter == 4) {
				counter = 0;
				throw new TaskExecutionException("Fails every four runs!");
			} else {
				Map output = new HashMap();
				output.put("out", inputs.get("in"));
				counter++;
				return output;
			}
		}
	}

}
