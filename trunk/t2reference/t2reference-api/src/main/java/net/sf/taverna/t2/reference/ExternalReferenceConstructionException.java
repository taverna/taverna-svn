package net.sf.taverna.t2.reference;

/**
 * Thrown when an exception occurs during construction of an
 * ExternalReferenceSPI instance. This includes construction through direct
 * method invocation, through the ExternalReferenceBuilderSPI interface and
 * through the ExternalReferenceTranslatorSPI interface.
 * 
 * @author Tom Oinn
 * 
 */
public class ExternalReferenceConstructionException extends RuntimeException {

	public ExternalReferenceConstructionException() {
		super();
	}

	public ExternalReferenceConstructionException(String message) {
		super(message);
	}

	public ExternalReferenceConstructionException(Throwable cause) {
		super(cause);
	}

	public ExternalReferenceConstructionException(String message,
			Throwable cause) {
		super(message, cause);
	}

}
