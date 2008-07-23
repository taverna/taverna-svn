package net.sf.taverna.t2.reference;

/**
 * Thrown by methods in the ListService interface if anything goes wrong with
 * list registration or retrieval. Any underlying exceptions that can't be
 * handled in the service layer are wrapped in this and re-thrown.
 * 
 * @author Tom Oinn
 */
public class ListServiceException extends RuntimeException {

	private static final long serialVersionUID = 5049346991071587866L;

	public ListServiceException() {
		super();
	}

	public ListServiceException(String message) {
		super(message);
	}

	public ListServiceException(Throwable cause) {
		super(cause);
	}

	public ListServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
