package net.sf.taverna.t2.cloudone.bean;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * Bean for serialising {@link ErrorDocument}. An ErrorDocument is serialised
 * as a String identifier from {@link #getIdentifier()}, an optional message in
 * {@link #getMessage()} and optional stack trace in {@link #getStackTrace()}.
 * 
 * @see Beanable
 * @see ErrorDocument
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "errorDocument")
@XmlType(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "errorDocument")
public class ErrorDocumentBean {
	private String identifier;
	private String message;
	private String stackTrace;

	public String getIdentifier() {
		return identifier;
	}

	public String getMessage() {
		return message;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
