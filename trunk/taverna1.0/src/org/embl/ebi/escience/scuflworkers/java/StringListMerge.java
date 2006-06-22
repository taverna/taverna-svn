/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Consumes a string list and optional seperator character and emits a string
 * formed from the concatenation of all items in the list with the seperator
 * (default newline) interposed between them.
 * 
 * @author Tom Oinn
 */
public class StringListMerge implements LocalWorker {

	public String[] inputNames() {
		return new String[] { "stringlist", "seperator" };
	}

	public String[] inputTypes() {
		return new String[] { "l('text/plain')", "'text/plain'" };
	}

	public String[] outputNames() {
		return new String[] { "concatenated" };
	}

	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

	public Map execute(Map inputs) throws TaskExecutionException {
		try {
			List inputStringList = (List) ((DataThing) inputs.get("stringlist")).getDataObject();
			String seperator = "\n";
			if (inputs.containsKey("seperator")) {
				seperator = (String) ((DataThing) inputs.get("seperator")).getDataObject();
			}
			Map outputs = new HashMap();
			StringBuffer sb = new StringBuffer();
			for (Iterator i = inputStringList.iterator(); i.hasNext();) {
				String item = (String) i.next();
				sb.append(item);
				if (i.hasNext()) {
					sb.append(seperator);
				}
			}
			outputs.put("concatenated", new DataThing(sb.toString()));
			return outputs;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

}
