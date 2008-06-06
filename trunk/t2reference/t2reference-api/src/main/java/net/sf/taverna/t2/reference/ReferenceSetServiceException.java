package net.sf.taverna.t2.reference;

/**
 * RuntimeException subclass thrown by the service layer interfaces. All
 * underlying exceptions are either handled by the service layer or wrapped in
 * this exception (or a subclass) and rethrown.
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceSetServiceException extends RuntimeException {

	private static final long serialVersionUID = -2762995062729638168L;

	public ReferenceSetServiceException() {
		//
	}

	public ReferenceSetServiceException(String message) {
		super(message);
	}

	public ReferenceSetServiceException(Throwable cause) {
		super(cause);
	}

	public ReferenceSetServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
