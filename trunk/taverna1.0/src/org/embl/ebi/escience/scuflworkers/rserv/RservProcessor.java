/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Stian Soiland, myGrid
 */

// This package is highly "inspired" by BeanShellProcessor by Tom Oinn

package org.embl.ebi.escience.scuflworkers.rserv;

import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.Properties;

import java.lang.String;

/**
 * A processor that uses the Rserv scripting engine to allow R (the free version
 * of S) to be invoked on workflow data. Rserv (which allows R scripts to be
 * executed remotely) must be runniTomng on localhost. Support for other hosts and
 * authentication will be added later.
 * 
 * @author Stian Soiland
 */
public class RservProcessor extends Processor implements java.io.Serializable {

	private String theScript = "";

	
	public RservProcessor(ScuflModel model, String name)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, name);
	}

	/**
	 * Construct a new processor with the given model and name, delegates to the
	 * superclass and sets the script up correctly along with the input and
	 * output port name arrays.
	 */
	public RservProcessor(ScuflModel model, String name, String script,
			String[] inputs)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, name);
		// Create appropriate inputs and outputs from the arrays
		try {
			this.theScript = script;
			// Iterate over inputs...
			for (int i = 0; i < inputs.length; i++) {
				Port p = new RservInputPort(this, inputs[i]);
				p.setSyntacticType("l'text/plain')");
				addPort(p);
			}
			Port p = new OutputPort(this, "value");
			p.setSyntacticType("l('text/plain')");
			addPort(p);
		} catch (DuplicatePortNameException dpne) {
			throw new ProcessorCreationException(
					"The supplied specification for the rserv processor '"
							+ name + "' contained a duplicate port '"
							+ dpne.getMessage() + "'");
		} catch (PortCreationException pce) {
			throw new ProcessorCreationException(
					"An error occured whilst generating ports for the rserv processor "
							+ pce.getMessage());
		}
	}

	/**
	 * Set the script
	 */
	public void setScript(String theScript) {
		if (theScript != null) {
			this.theScript = theScript;
		} else {
			this.theScript = "";
		}
		fireModelEvent(new ScuflModelEvent(this, "Script modified"));
	}

	/**
	 * Get the script
	 */
	public String getScript() {
		return this.theScript;
	}

	/**
	 * Get the properties for this processor for display purposes
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		props.put("Script", "See configurator for more information");
		return props;
	}

}
