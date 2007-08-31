package net.sf.taverna.t2.cloudone;

/**
 * Thrown when a {@link DataManager} can't store an entity, for instance because
 * a disk is full.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class EntityStorageException extends RuntimeException {

	private static final long serialVersionUID = -7698707100176003408L;

	public EntityStorageException() {
	}

	public EntityStorageException(String message) {
		super(message);
	}

	public EntityStorageException(Throwable cause) {
		super(cause);
	}

	public EntityStorageException(String message, Throwable cause) {
		super(message, cause);
	}

}
