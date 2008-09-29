package net.sf.taverna.t2.workflowmodel;

/**
 * Potentially thrown when an edit fails due to naming of entities created or
 * modified by the edit. This could be because there are duplicate names in e.g.
 * processor input ports or invalid characters in the name itself
 * 
 * @author Tom Oinn
 * 
 */
public class NamingException extends EditException {

	private static final long serialVersionUID = -6945542133180017313L;

	public NamingException(String message) {
		super(message);
	}

	public NamingException(Throwable cause) {
		super(cause);
	}

	public NamingException(String message, Throwable cause) {
		super(message, cause);
	}

}
