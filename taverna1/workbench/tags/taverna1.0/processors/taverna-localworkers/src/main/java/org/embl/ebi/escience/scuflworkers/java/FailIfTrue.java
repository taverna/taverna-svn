/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A processor which fails if the test input matches the string 'true'
 * 
 * @author Tom Oinn
 */
public class FailIfTrue implements LocalWorker {

	String compareString = null;

	protected FailIfTrue(String compare) {
		this.compareString = compare;
	}

	public FailIfTrue() {
		this("true");
	}

	public String[] inputNames() {
		return new String[] { "test" };
	}

	public String[] inputTypes() {
		return new String[] { "'text/plain'" };
	}

	public String[] outputNames() {
		return new String[0];
	}

	public String[] outputTypes() {
		return new String[0];
	}

	public Map execute(Map inputs) throws TaskExecutionException {
		String condition = (String) ((DataThing) (inputs.get("test"))).getDataObject();
		if (condition.equalsIgnoreCase(this.compareString)) {
			throw new TaskExecutionException("Test matches, aborting downstream processors");
		}
		return new HashMap();
	}

}
