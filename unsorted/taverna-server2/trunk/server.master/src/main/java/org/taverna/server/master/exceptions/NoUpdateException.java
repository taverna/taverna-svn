package org.taverna.server.master.exceptions;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebFault;

/**
 * Exception that is thrown to indicate that the user is not permitted to update
 * something.
 * 
 * @author Donal Fellows
 */
@WebFault(name = "NoUpdateFault")
@XmlSeeAlso( { NoCreateException.class, NoDestroyException.class })
public class NoUpdateException extends Exception {
	private static final long serialVersionUID = 4230987102653846379L;

	public NoUpdateException() {
		super("not permitted to update");
	}

	NoUpdateException(String msg) {
		super(msg);
	}
}