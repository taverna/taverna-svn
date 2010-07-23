package org.taverna.server.master.common;

import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Encapsulation of a SCUFL2 or T2flow document.
 * 
 * @author dkf
 */
@XmlType(name = "Workflow")
@XmlSeeAlso(Workflow.T2Flow.class)
public abstract class Workflow {
	/** Literal document. */
	@XmlAnyElement(lax = true)
	public Element[] content;
	/** Literal attributes. */
	@XmlAnyAttribute
	public Map<QName, Object> args;

	@XmlRootElement(name = "workflow", namespace = "http://taverna.sf.net/2008/xml/t2flow")
	@XmlType(name = "t2flow")
	public static class T2Flow extends Workflow {
	}
}
