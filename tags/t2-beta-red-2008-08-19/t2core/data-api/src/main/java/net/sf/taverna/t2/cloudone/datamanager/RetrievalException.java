package net.sf.taverna.t2.cloudone.datamanager;


/**
 * Thrown when an entity or blob can't be retrieved from a {@link DataManager},
 * for instance because of access failure.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class RetrievalException extends net.sf.taverna.t2.util.beanable.RetrievalException {

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
