package net.sf.taverna.t2.cloudone;

/**
 * Thrown when a request is made for resolution of an entity that cannot be
 * found by the data manager to which the request is made.
 * 
 * @author Tom
 * 
 */
public class EntityNotFoundException extends Exception {

	private static final long serialVersionUID = -1069998094174721609L;

	public EntityNotFoundException() {
	}

	public EntityNotFoundException(String msg) {
		super(msg);
	}

	public EntityNotFoundException(Throwable throwable) {
		super(throwable);
	}

	public EntityNotFoundException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

}
