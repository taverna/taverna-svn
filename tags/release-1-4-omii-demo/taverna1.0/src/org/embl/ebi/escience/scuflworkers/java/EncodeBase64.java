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
import org.embl.ebi.escience.baclava.Base64;

/**
 * Encode byte[] data into base64 string
 * 
 * @author Tom Oinn
 */
public class EncodeBase64 implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "bytes" };
	}

	public String[] inputTypes() {
		return new String[] { "'application/octet-stream'" };
	}

	public String[] outputNames() {
		return new String[] { "base64" };
	}

	public String[] outputTypes() {
		return new String[] { LocalWorker.STRING };
	}

	public Map execute(Map inputs) throws TaskExecutionException {
		Map results = new HashMap();
		byte[] bytes = (byte[]) ((DataThing) inputs.get("bytes"))
				.getDataObject();
		String base64 = Base64.encodeBytes(bytes);
		results.put("base64", new DataThing(base64));
		return results;
	}

}
