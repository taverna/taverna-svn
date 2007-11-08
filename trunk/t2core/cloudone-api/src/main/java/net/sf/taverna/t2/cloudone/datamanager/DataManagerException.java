package net.sf.taverna.t2.cloudone.datamanager;

/**
 * Base class for Exceptions thrown by
 * {@link net.sf.taverna.t2.cloudone.datamanager.DataManager} and {@link DataFacade}.
 *
 * @author Stian Soiland
 * @author Ian Dunlop
 *
 */
public class DataManagerException extends Exception {

	private static final long serialVersionUID = 4945769212347469502L;

	public DataManagerException() {
		super();
	}

	public DataManagerException(String message) {
		super(message);
	}

	public DataManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataManagerException(Throwable cause) {
		super(cause);
	}

}
