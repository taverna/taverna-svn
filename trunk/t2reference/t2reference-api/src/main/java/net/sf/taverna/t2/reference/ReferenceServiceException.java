package net.sf.taverna.t2.reference;

/**
 * Thrown by methods in the ReferenceService, used to wrap any underlying
 * exceptions from lower layers.
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceServiceException extends RuntimeException {

	private static final long serialVersionUID = -2607675495513408333L;

	public ReferenceServiceException() {
		//
	}

	public ReferenceServiceException(String message) {
		super(message);
	}

	public ReferenceServiceException(Throwable cause) {
		super(cause);
	}

	public ReferenceServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
