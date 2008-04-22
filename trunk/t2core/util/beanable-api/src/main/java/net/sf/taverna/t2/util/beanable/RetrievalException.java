package net.sf.taverna.t2.util.beanable;


/**
 * Thrown when someething can't be retrieved, for instance because of access
 * failure.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class RetrievalException extends RuntimeException {

	private static final long serialVersionUID = -3674091401600650755L;

	public RetrievalException() {
	}

	public RetrievalException(String message) {
		super(message);
	}

	public RetrievalException(String message, Throwable cause) {
		super(message, cause);
	}

	public RetrievalException(Throwable cause) {
		super(cause);
	}

}
