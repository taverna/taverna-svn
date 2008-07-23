package net.sf.taverna.t2.reference;

/**
 * Thrown when a problem occurs during de-reference of an ExternalReferenceSPI
 * implementation. This include operations which implicitly de-reference the
 * reference such as those infering character set or data natures.
 * 
 * @author Tom Oinn
 * 
 */
public class DereferenceException extends RuntimeException {

	private static final long serialVersionUID = 8054381613840005541L;

	public DereferenceException() {
		// 
	}

	public DereferenceException(String message) {
		super(message);
	}

	public DereferenceException(Throwable cause) {
		super(cause);
	}

	public DereferenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
