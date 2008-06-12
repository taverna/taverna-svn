package net.sf.taverna.t2.workbench.file.exceptions;

public class FileException extends Exception {

	public FileException() {
	}

	public FileException(String message) {
		super(message);
	}

	public FileException(Throwable cause) {
		super(cause);
	}

	public FileException(String message, Throwable cause) {
		super(message, cause);
	}

}
