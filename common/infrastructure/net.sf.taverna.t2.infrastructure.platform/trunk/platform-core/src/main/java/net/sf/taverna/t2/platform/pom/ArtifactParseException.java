package net.sf.taverna.t2.platform.pom;

/**
 * Thrown if problems occur during the creation of an ArtifactDescription
 * instance. This can include nested exceptions from the download process.
 * 
 * @author Tom Oinn
 */
public class ArtifactParseException extends RuntimeException {

	private static final long serialVersionUID = 2069846241562946761L;

	public ArtifactParseException() {
		// 
	}

	public ArtifactParseException(String message) {
		super(message);
	}

	public ArtifactParseException(Throwable cause) {
		super(cause);
	}

	public ArtifactParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
