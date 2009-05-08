package net.sf.taverna.t2.platform.spring.profile;

/**
 * Thrown when attempting to instantiate a context profile bound to an
 * application context which does not contain the required beans
 * 
 * @author Tom Oinn
 * 
 */
public class ContextProfileException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -79645889257993762L;

	public ContextProfileException() {
		// TODO Auto-generated constructor stub
	}

	public ContextProfileException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ContextProfileException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public ContextProfileException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
