package org.taverna.server.master;

import java.net.URL;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

/**
 * Encapsulation of a SCUFL document.
 * @author dkf
 */
@XmlRootElement(name="scufl")
public class SCUFL {
	/**
	 * Pointer to a document.
	 */
	@XmlAttribute(required=false)
	public URL ref;

	/**
	 * Literal document.
	 */
	// TODO Use the real definition of SCUFL
	@XmlAnyElement(lax=true)
	public Element[] content;
}
