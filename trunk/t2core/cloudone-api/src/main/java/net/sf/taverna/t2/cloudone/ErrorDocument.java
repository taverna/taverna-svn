package net.sf.taverna.t2.cloudone;

/**
 * Represents a single error document within the data manager. The error
 * document contains a Throwable and a message, either of which may be null.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class ErrorDocument implements Entity<ErrorDocumentIdentifier> {
	private final ErrorDocumentIdentifier id;

	private final String message;

	private final Throwable cause;

	public ErrorDocument(final ErrorDocumentIdentifier id,
			final String message, final Throwable cause) {
		this.id = id;
		this.message = message;
		this.cause = cause;
	}

	public ErrorDocumentIdentifier getIdentifier() {
		return id;
	}

	public Throwable getCause() {
		return this.cause;
	}

	public String getMessage() {
		return this.message;
	}

}
