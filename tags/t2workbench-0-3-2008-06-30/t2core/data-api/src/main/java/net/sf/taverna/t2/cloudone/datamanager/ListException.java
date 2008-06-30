package net.sf.taverna.t2.cloudone.datamanager;

import java.util.List;

/**
 * Base class for {@link List}-related exceptions, such as
 * {@link MalformedListException} and {@link EmptyListException}.
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
public class ListException extends DataManagerException {

	private static final long serialVersionUID = -8446841141159877165L;

	public ListException() {
		super();
	}

	public ListException(String message) {
		super(message);
	}

	public ListException(String message, Throwable cause) {
		super(message, cause);
	}

	public ListException(Throwable cause) {
		super(cause);
	}

}
