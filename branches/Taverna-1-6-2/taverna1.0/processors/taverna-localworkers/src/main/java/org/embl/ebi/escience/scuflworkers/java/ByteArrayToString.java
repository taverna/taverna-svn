package org.embl.ebi.escience.scuflworkers.java;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Constructs a new String from the supplied byte[]
 * 
 * @author Tom Oinn
 */
public class ByteArrayToString implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "bytes" };
	}

	public String[] inputTypes() {
		return new String[] { "'application/octet-stream'" };
	}

	public String[] outputNames() {
		return new String[] { "string" };
	}

	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

	public Map execute(Map inputs) throws TaskExecutionException {
		try {
			Map outputs = new HashMap();
			byte[] bytes = (byte[]) ((DataThing) inputs.get("bytes")).getDataObject();
			outputs.put("string", new DataThing(new String(bytes)));
			return outputs;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

}
