/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.embl.ebi.escience.baclava.DataThing;
import org.apache.log4j.Logger;

/**
 * Apply a regular expression to a string, returning a group that matches if
 * there is a match.
 * 
 * @author Matthew Pocock
 */
public class RegularExpressionStringList implements LocalWorker {
	Logger LOG = Logger.getLogger(RegularExpressionStringList.class);

	public String[] inputNames() {
		return new String[] { "stringlist", "regex", "group" };
	}

	public String[] inputTypes() {
		return new String[] { "l('text/plain')", "'text/plain'", "'text/plain'" };
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
		List inputList = (List) ((DataThing) (inputs.get("stringlist")))
				.getDataObject();
		String pattern = (String) ((DataThing) (inputs.get("regex")))
				.getDataObject();
		String group = (String) ((DataThing) (inputs.get("group")))
				.getDataObject();
		List outputList = new ArrayList();
		Pattern thePat = Pattern.compile(pattern);
		int theGroup = Integer.parseInt(group);

		LOG.debug("pattern: " + pattern);
		LOG.debug("group: " + group);

		for (Iterator i = inputList.iterator(); i.hasNext();) {
			String item = (String) i.next();
			Matcher matcher = thePat.matcher(item);

			LOG.debug("matching against: " + item);

			if (matcher.find()) {
				LOG.debug("it matched");
				outputList.add(matcher.group(theGroup));
			}
		}
		Map outputs = new HashMap();
		outputs.put("filteredlist", new DataThing(outputList));
		return outputs;
	}

}
