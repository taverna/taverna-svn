package net.sf.taverna.t2.util.beanable;


/**
 * Thrown when an object can't be retrieved from the underlying store,
 * for instance because of access failure.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class RetrievalException extends RuntimeException {

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
