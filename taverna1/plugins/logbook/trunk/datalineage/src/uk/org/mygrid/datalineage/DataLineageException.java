package uk.org.mygrid.datalineage;

public class DataLineageException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataLineageException() {
		// default
	}

	public DataLineageException(String message) {
		super(message);
	}

	public DataLineageException(Throwable cause) {
		super(cause);
	}

	public DataLineageException(String message, Throwable cause) {
		super(message, cause);
	}

}
