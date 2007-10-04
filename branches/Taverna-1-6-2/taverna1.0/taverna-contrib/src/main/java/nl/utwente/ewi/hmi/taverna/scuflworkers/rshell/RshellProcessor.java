/*
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-20 14:51:32 $
 * $Revision: 1.1 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import java.util.Properties;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;

/**
 * A processor that uses the Rshell scripting engine to allow R (the free
 * version of S) to be invoked on workflow data. Rshell (which allows R scripts
 * to be executed remotely) must be runniTomng on localhost. Support for other
 * hosts and authentication will be added later.
 * 
 * @author Stian Soiland, Ingo Wassink
 */
public class RshellProcessor extends Processor implements java.io.Serializable {

	private static final long serialVersionUID = 1652784664361353135L;

	private String script = "";

	private RshellConnectionSettings connectionSettings;

	public RshellProcessor(ScuflModel model, String name)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, name);

		connectionSettings = new RshellConnectionSettings();
	}

	/**
	 * Set the script
	 * 
	 * @param newScript
	 *            the new script
	 */
	public void setScript(String script) {
		this.script = (script != null) ? script : "";
		fireModelEvent(new ScuflModelEvent(this, "Script modified"));
	}

	/**
	 * Get the script
	 * 
	 * @return the script
	 */
	public String getScript() {
		return this.script;
	}

	/**
	 * Get the properties for this processor for display purposes
	 * 
	 * @return the properties
	 */
	public Properties getProperties() {
		Properties properties = new Properties();
		properties.put("Script", "See configurator for more information");
		return properties;
	}

	/**
	 * Method for getting the connection settings
	 * 
	 * @return the connection settings
	 */
	public RshellConnectionSettings getConnectionSettings() {
		return connectionSettings;
	}
}
