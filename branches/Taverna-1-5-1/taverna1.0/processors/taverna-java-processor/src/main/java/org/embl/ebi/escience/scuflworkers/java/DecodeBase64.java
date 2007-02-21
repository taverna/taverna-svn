/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.Base64;
import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Decode base64 string into byte[]
 * 
 * @author Tom Oinn
 */
public class DecodeBase64 implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "base64" };
	}

	public String[] inputTypes() {
		return new String[] { LocalWorker.STRING };
	}

	public String[] outputNames() {
		return new String[] { "bytes" };
	}

	public String[] outputTypes() {
		return new String[] { "'application/octet-stream'" };
	}

	public Map execute(Map inputs) throws TaskExecutionException {
		Map results = new HashMap();
		Object data = ((DataThing)inputs.get("base64")).getDataObject();
		String base64;
		if (String.class.isAssignableFrom(data.getClass())) {
			base64 = (String) ((DataThing) inputs.get("base64")).getDataObject();
		}
		else {
			throw new TaskExecutionException("Input data cannot be assigned to a String, so is not a base64 encoding. Its type is:"+data.getClass().getName());
		}
		byte[] bytes = Base64.decode(base64);
		results.put("bytes", new DataThing(bytes));
		return results;
	}

}
