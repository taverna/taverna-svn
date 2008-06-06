package net.sf.taverna.t2.reference;

/**
 * Thrown by the Data Access Object interface methods, wrapping any underlying
 * exception.
 * 
 * @author Tom Oinn
 * 
 */
public class DaoException extends RuntimeException {

	static final long serialVersionUID = 8496141798637577803L;

	public DaoException() {
		super();
	}

	public DaoException(String message) {
		super(message);
	}

	public DaoException(Throwable cause) {
		super(cause);
	}

	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}

}
