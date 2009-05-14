package net.sf.taverna.t2.platform.raven;

/**
 * Runtime exception thrown when Raven encounters a problem it cannot resolve
 * directly
 * 
 * @author Tom Oinn
 * 
 */
public class RavenException extends RuntimeException {

	private static final long serialVersionUID = 1953317227305714769L;

	public RavenException() {
		// 
	}

	public RavenException(String message) {
		super(message);
	}

	public RavenException(Throwable cause) {
		super(cause);
	}

	public RavenException(String message, Throwable cause) {
		super(message, cause);
	}

}
