package net.sf.taverna.t2.invocation;

/**
 * Runtime exception thrown when a call is made to retrieve an entity from the
 * invocation context but where there are either no such entities or more than a
 * single one.
 * 
 * @author Tom Oinn
 * 
 */
public class InvocationContextException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6736707536758103994L;

	public InvocationContextException() {
		//
	}

	public InvocationContextException(String message) {
		super(message);
	}

	public InvocationContextException(Throwable cause) {
		super(cause);
	}

	public InvocationContextException(String message, Throwable cause) {
		super(message, cause);
	}

}
