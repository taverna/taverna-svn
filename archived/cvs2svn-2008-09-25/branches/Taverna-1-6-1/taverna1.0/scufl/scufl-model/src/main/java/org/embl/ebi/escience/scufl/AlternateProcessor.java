/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * Represents an alternate processor to be used in case of failures in the
 * primary. Contains the Processor object to use as an alternate and two Map
 * objects, these act as translations between the input and output names of the
 * original and substitute processor.
 * <p>
 * For example, suppose the original processor has inputs as follows :
 * <p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr bgcolor="#eeeeff">
 * <td>Name</td>
 * <td>Description</td>
 * <td>Type</td>
 * </tr>
 * <tr>
 * <td>inseq</td>
 * <td>Input sequence</td>
 * <td>'text/plain'</td>
 * </tr>
 * <tr>
 * <td>format</td>
 * <td>Desired format</td>
 * <td>'text/plain'</td>
 * </tr>
 * </table>
 * <p>
 * and the substitute has a similar set of inputs but with slightly different
 * names :
 * <p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr bgcolor="#eeeeff">
 * <td>Name</td>
 * <td>Description</td>
 * <td>Type</td>
 * </tr>
 * <tr>
 * <td>seq</td>
 * <td>Input sequence</td>
 * <td>'text/plain'</td>
 * </tr>
 * <tr>
 * <td>sformat</td>
 * <td>Desired format</td>
 * <td>'text/plain'</td>
 * </tr>
 * </table>
 * <p>
 * In this case the inputMapping object would contain the following :
 * <p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr bgcolor="#eeeeff">
 * <td>Key</td>
 * <td>Value</td>
 * </tr>
 * <tr>
 * <td>inseq</td>
 * <td>seq</td>
 * </tr>
 * <tr>
 * <td>format</td>
 * <td>sformat</td>
 * </tr>
 * </table>
 * <p>
 * This allows the task implementation to connect the appropriate inputs to the
 * alternate processor. A similar map would exist for the outputs, again with
 * the original or primary name as the key and alternate name as the value.
 */
public class AlternateProcessor implements Serializable {

	private static Logger logger = Logger.getLogger(AlternateProcessor.class);
	
	private Processor alternate;

	private Processor original;

	private Map<String,String> inputMapping = new Hashtable<String,String>();

	private Map<String,String> outputMapping = new Hashtable<String,String>();

	public AlternateProcessor(Processor alternate) {
		this.alternate = alternate;
	}

	/**
	 * Return the alternate processor object. This Processor object will not be
	 * bound to the same workflow model instance as the alternate, it will most
	 * of the time not be bound to one at all. It is only intended to be used to
	 * provide the alternate functionality and not as the primary description.
	 * For this reason it is certainly recommended to only use substitute
	 * processors which are at least vaguely equivalent in operation, but of
	 * course there's no way we either should or could enforce this. Use with
	 * care!
	 */
	public Processor getProcessor() {
		return alternate;
	}

	/**
	 * Return the processor for which this container holds the alternate
	 * implementation. This will return null if no original processor has been
	 * defined for the alternate.
	 */
	public Processor getOriginalProcessor() {
		return original;
	}

	public void setOriginalProcessor(Processor p) {
		original = p;
		alternate.parentProcessor = p;
		// If there are ports that are the same name on the
		// original and this processor, and these are not
		// already defined in the mapping section, then create
		// simple identity mappings.
		for (Port port : original.getPorts()) {
			String portName = port.getName();
			// Check for an existing mapping
			if ((port instanceof InputPort && inputMapping.containsKey(portName))
					|| (port instanceof OutputPort && outputMapping.containsKey(portName))) {
				continue;
			}
			// Does the alternate have a named port with the same name?
			Port targetPort;
			try {
				targetPort = alternate.locatePort(portName);
			} catch (UnknownPortException e) {
				logger.debug("Could not match port " + portName);
				continue;
			}
			if (targetPort instanceof InputPort
					&& port instanceof InputPort) {
				inputMapping.put(portName, targetPort.getName());
			} else if (targetPort instanceof OutputPort
					&& port instanceof OutputPort) {
				outputMapping.put(portName, targetPort.getName());
			} else {
				logger.debug("Found port " + portName + " does not match input/output");
			}
		}
	}

	/**
	 * Get the input mappings, a Map object with keys being the original or
	 * primary input names and values being the corresponding names of inputs on
	 * the alternate processor this holder class contains.
	 */
	public Map<String,String> getInputMapping() {
		return inputMapping;
	}

	/**
	 * Get the output mappings, a Map object with keys being the original or
	 * primary output names and values being the corresponding names of outputs
	 * on the alternate processor this holder class contains.
	 */
	public Map<String,String> getOutputMapping() {
		return outputMapping;
	}

	/**
	 * Use the toString method of the underlying processor
	 */
	public String toString() {
		return alternate.toString();
	}

	/**
	 * For a given port name in this alternate, fetch the original port name.
	 * This is the opposite way around to the stored mapping, but UI components
	 * may want this information.
	 * 
	 * @return null if there is no mapping for the named port or the String name
	 *         of the original port otherwise.
	 */
	public String getPortTranslation(String alternatePort) {
		for (Entry<String,String> mapping : inputMapping.entrySet()) {
			if (mapping.getValue().equalsIgnoreCase(alternatePort)) {
				return mapping.getKey();
			}
		}
		for (Entry<String,String> mapping : outputMapping.entrySet()) {
			if (mapping.getValue().equalsIgnoreCase(alternatePort)) {
				return mapping.getKey();
			}
		}
		return null;
	}

}
