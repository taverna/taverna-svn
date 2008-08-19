package net.sf.taverna.raven;

public class RavenException extends Exception {
	private static final long serialVersionUID = 1L;

	public RavenException() {
		super();
	}

	public RavenException(String message) {
		super(message);
	}

	public RavenException(String message, Throwable cause) {
		super(message, cause);
	}

	public RavenException(Throwable cause) {
		super(cause);
	}

}
