package net.sf.taverna.t2.cloudone.datamanager;

import net.sf.taverna.t2.cloudone.DataManager;

/**
 * Thrown when an entity can't be retrieved from a {@link DataManager}, for
 * instance because of access failure.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class EntityRetrievalException extends RuntimeException {

	private static final long serialVersionUID = -3674091401600650755L;

	public EntityRetrievalException() {
	}

	public EntityRetrievalException(String message) {
		super(message);
	}

	public EntityRetrievalException(Throwable cause) {
		super(cause);
	}

	public EntityRetrievalException(String message, Throwable cause) {
		super(message, cause);
	}

}
