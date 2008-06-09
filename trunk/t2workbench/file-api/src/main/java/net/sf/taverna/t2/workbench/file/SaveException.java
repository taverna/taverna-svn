package net.sf.taverna.t2.workbench.file;

public class SaveException extends FileException {

	public SaveException() {
	}

	public SaveException(String message) {
		super(message);
	}

	public SaveException(Throwable cause) {
		super(cause);
	}

	public SaveException(String message, Throwable cause) {
		super(message, cause);
	}

}
