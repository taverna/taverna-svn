package net.sf.taverna.t2.cloudone.identifier;

/**
 * Thrown when an attempt is made to construct an entity identifier subclass
 * from a string and where that string doesn't contain a well formed identifier.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public class MalformedIdentifierException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1536349476919336947L;

	public MalformedIdentifierException(String message) {
		super(message);
	}

	public MalformedIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}

}
