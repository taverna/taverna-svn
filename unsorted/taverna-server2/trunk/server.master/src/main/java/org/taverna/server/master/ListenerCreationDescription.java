package org.taverna.server.master;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Description of what sort of event listener to create and attach to a workflow
 * run.
 * 
 * @author Donal Fellows
 */
@XmlRootElement(name = "listenerDefinition")
public class ListenerCreationDescription {
	/**
	 * The type of event listener to create.
	 */
	@XmlAttribute
	public String type;
	/**
	 * How the event listener should be configured.
	 */
	@XmlValue
	public String configuration;
}
