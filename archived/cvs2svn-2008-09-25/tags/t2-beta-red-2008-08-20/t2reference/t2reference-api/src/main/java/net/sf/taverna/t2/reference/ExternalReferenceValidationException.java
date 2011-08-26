package net.sf.taverna.t2.reference;

/**
 * Thrown by setter methods and constructors of ExternalReferenceSPI
 * implementations when fed parameters which cause some kind of format or
 * validation error. These might include badly formed URL or file paths or any
 * other property that fails to validate against some reference type specific
 * scheme.
 * 
 * @author Tom Oinn
 * 
 */
public class ExternalReferenceValidationException extends RuntimeException {

	private static final long serialVersionUID = 3031393671457773057L;

	public ExternalReferenceValidationException() {
		// 
	}

	public ExternalReferenceValidationException(String message) {
		super(message);
	}

	public ExternalReferenceValidationException(Throwable cause) {
		super(cause);
	}

	public ExternalReferenceValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
