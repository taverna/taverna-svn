package net.sf.taverna.t2.cloudone.datamanager;

import net.sf.taverna.t2.cloudone.DataManager;

/**
 * Base class for Exceptions thrown by {@link DataManager} and
 * {@link DataFacade}
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

	public DataManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataManagerException(String message) {
		super(message);
	}

	public DataManagerException(Throwable cause) {
		super(cause);
	}


}
