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

/**
 * Outputs "replacelsid:input" which should be substituted for the input's lsid
 * by the ProcessorTask.
 * 
 * @author Chris Greenhalgh
 */
public class GetLSID implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "input" };
	}

	public String[] inputTypes() {
		return new String[] { "'text/plain'" };
	}

	public String[] outputNames() {
		return new String[] { "replacelsid" };
	}

	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

	public Map execute(Map inputs) throws TaskExecutionException {
		try {
			Map outputs = new HashMap();
			outputs.put("replacelsid", new DataThing("replacelsid:input"));
			return outputs;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}
