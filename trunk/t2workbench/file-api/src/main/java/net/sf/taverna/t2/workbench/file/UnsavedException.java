package net.sf.taverna.t2.workbench.file;


public class UnsavedException extends FileException {

	public UnsavedException() {
		super();
	}

	public UnsavedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsavedException(String message) {
		super(message);
	}

	public UnsavedException(Throwable cause) {
		super(cause);
	}

}
