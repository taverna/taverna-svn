package net.sf.taverna.t2.invocation;

/**
 * Thrown when attempting to create an invalid process identifier, either by
 * popping an empty one (this by definition has no parents) or by pushing a
 * local name including a colon ':' character.
 * 
 * @author Tom Oinn
 * 
 */
public class ProcessIdentifierException extends RuntimeException {

	private static final long serialVersionUID = -221443591753067425L;

	public ProcessIdentifierException() {
		super();
	}

	public ProcessIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessIdentifierException(String message) {
		super(message);
	}

	public ProcessIdentifierException(Throwable cause) {
		super(cause);
	}

}
