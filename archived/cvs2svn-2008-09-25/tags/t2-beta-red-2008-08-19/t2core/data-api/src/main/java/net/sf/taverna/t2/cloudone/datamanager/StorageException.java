package net.sf.taverna.t2.cloudone.datamanager;


/**
 * Thrown when a {@link DataManager} can't store an entity or blob, for instance
 * because a disk is full.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class StorageException extends net.sf.taverna.t2.util.beanable.StorageException {

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
