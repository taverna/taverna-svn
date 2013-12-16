package org.taverna.server.master.exceptions;

import javax.xml.ws.WebFault;

/**
 * Exception that is thrown to indicate that the user is not permitted to
 * create something because the service load is too high.
 * 
 * @author Donal Fellows
 */
@WebFault(name = "OverloadedFault")
public class OverloadedException extends NoCreateException {
	private static final long serialVersionUID = 3983414645954499352L;

	public OverloadedException() {
		super("too many existing runs");
	}

	public OverloadedException(String string) {
		super(string);
	}

	public OverloadedException(String string, Throwable e) {
		super(string, e);
	}
}
