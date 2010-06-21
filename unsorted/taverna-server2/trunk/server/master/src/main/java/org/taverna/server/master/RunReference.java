package org.taverna.server.master;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * A reference to a single workflow run, described using JAXB.
 * 
 * @author Donal Fellows
 * @see org.taverna.server.master.interfaces.TavernaRun TavernaRun
 */
@XmlRootElement
@XmlType(name = "TavernaRun")
@XmlSeeAlso( { SCUFL.class, DirEntryReference.class })
public class RunReference {
	/**
	 * Where to get information about the run. For REST.
	 */
	@XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
	public URI link;
	/**
	 * The name of the run. For SOAP.
	 */
	@XmlValue
	public String name;

	public RunReference() {
	}

	RunReference(String name, UriBuilder ub) {
		this.name = name;
		this.link = ub.build(name);
	}
}
