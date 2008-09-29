/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Stian Soiland, myGrid
 */

// This package is highly "inspired" by BeanShellProcessor by Tom Oinn

package org.embl.ebi.escience.scuflworkers.rserv;

import java.util.Properties;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;

/**
 * A processor that uses the Rserv scripting engine to allow R (the free version
 * of S) to be invoked on workflow data. Rserv (which allows R scripts to be
 * executed remotely) must be runniTomng on localhost. Support for other hosts and
 * authentication will be added later.
 * 
 * @author Stian Soiland
 */
public class RservProcessor extends Processor implements java.io.Serializable {

	private String script = "";
	// Rserv connection info -- null/0 means use default values
	private String hostname = null; 
	private int port = 0;
	private String username = null;
	private String password = null;

	
	public RservProcessor(ScuflModel model, String name)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		this(model, name, "", new String[0]);
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
			this.script = script;
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
	public void setScript(String script) {
		if (script == null) {
			script = "";
		}
		this.script = script;
		fireModelEvent(new ScuflModelEvent(this, "Script modified"));
	}

	/**
	 * Get the script
	 */
	public String getScript() {
		return this.script;
	}

	/**
	 * Get the properties for this processor for display purposes
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		props.put("Script", "See configurator for more information");
		return props;
	}

	public String getHostname() {
		if (hostname == null) {
			return "";
		}
		return hostname;
	}

	public void setHostname(String hostname) {
		if (hostname != null && hostname.equals("")) {
			hostname = null;
		}
		this.hostname = hostname;
	}

	public String getPassword() {
		if (password == null) {
			return "";
		}
		return password;
	}

	public void setPassword(String password) {
		if (password != null && password.equals("")) {
			password = null;
		}
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		// 0 is allowed for us, it means "Use default port"
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port number must be in range 1-65535");
		}
		this.port = port;
	}

	public String getUsername() {
		if (username == null) {
			return "";
		}
		return username;
	}

	public void setUsername(String username) {
		if (username != null && username.equals("")) {
			username = null;
		}
		this.username = username;
	}
	


}
