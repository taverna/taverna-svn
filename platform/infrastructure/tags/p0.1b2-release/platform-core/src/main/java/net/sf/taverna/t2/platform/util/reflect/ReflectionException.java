package net.sf.taverna.t2.platform.util.reflect;

/**
 * Generic exception used to wrap checked exceptions from the various methods in
 * the reflection helper.
 * 
 * @author Tom Oinn
 * 
 */
public class ReflectionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3702010115153910716L;

	/**
	 * 
	 */
	public ReflectionException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ReflectionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ReflectionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ReflectionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
