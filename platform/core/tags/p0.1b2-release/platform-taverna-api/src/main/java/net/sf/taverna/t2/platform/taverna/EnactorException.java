package net.sf.taverna.t2.platform.taverna;

/**
 * Used to wrap exceptions thrown by the Enactor interface in runtime exceptions
 * to simplify client code
 * 
 * @author Tom Oinn
 * 
 */
public class EnactorException extends RuntimeException {

	private static final long serialVersionUID = 1359157918898413151L;

	public EnactorException() {
		// TODO Auto-generated constructor stub
	}

	public EnactorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public EnactorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public EnactorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
