package net.sf.taverna.t2.cloudone;

/**
 * 
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 *
 */
public class ListException extends Exception {

	private static final long serialVersionUID = -3724934585127601880L;

	public ListException() {
		super();
	}

	public ListException(String message, Throwable cause) {
		super(message, cause);
	}

	public ListException(String message) {
		super(message);
	}

	public ListException(Throwable cause) {
		super(cause);
	}

}
