package net.sf.taverna.t2.util.beanable;

/**
 * Thrown when an object can be stored, for instance because a disk is full.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class StorageException extends RuntimeException {

	public StorageException() {
	}

	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}

	public StorageException(Throwable cause) {
		super(cause);
	}

}
