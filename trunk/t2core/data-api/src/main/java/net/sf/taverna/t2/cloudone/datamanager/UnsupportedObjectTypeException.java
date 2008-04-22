package net.sf.taverna.t2.cloudone.datamanager;

/**
 * Thrown on attempt to register an unsupported object type. For a list of
 * supported objects, see {@link DataFacade#register(Object, int)}
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
public class UnsupportedObjectTypeException extends DataManagerException {

	private static final long serialVersionUID = 3312743037019550863L;

	public UnsupportedObjectTypeException() {
	}

	public UnsupportedObjectTypeException(String message) {
		super(message);
	}

	public UnsupportedObjectTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedObjectTypeException(Throwable cause) {
		super(cause);
	}

}
