package net.sf.taverna.t2.reference;

/**
 * RuntimeException subclass thrown by the error document service layer
 * interfaces. All underlying exceptions are either handled by the service layer
 * or wrapped in this exception (or a subclass) and rethrown.
 * 
 * @author Tom Oinn
 * 
 */
public class ErrorDocumentServiceException extends RuntimeException {

	private static final long serialVersionUID = 5556399589785258956L;

	public ErrorDocumentServiceException() {
		super();
	}

	public ErrorDocumentServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErrorDocumentServiceException(String message) {
		super(message);
	}

	public ErrorDocumentServiceException(Throwable cause) {
		super(cause);
	}

}
