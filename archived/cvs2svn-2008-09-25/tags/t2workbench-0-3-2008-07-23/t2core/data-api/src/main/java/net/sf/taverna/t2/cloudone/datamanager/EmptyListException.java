package net.sf.taverna.t2.cloudone.datamanager;

/**
 * Thrown on attempt to register an empty list or a list containing only empty
 * lists (recursively).
 *
 * @author Stian Soiland
 * @author Ian Dunlop
 *
 */
public class EmptyListException extends ListException {

	private static final long serialVersionUID = -2348895343632695498L;

	public EmptyListException() {
		super();
	}

	public EmptyListException(String message) {
		super(message);
	}

	public EmptyListException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmptyListException(Throwable cause) {
		super(cause);
	}

}
