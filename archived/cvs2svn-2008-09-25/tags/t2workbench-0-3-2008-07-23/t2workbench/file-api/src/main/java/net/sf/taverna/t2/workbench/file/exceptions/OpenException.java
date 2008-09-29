package net.sf.taverna.t2.workbench.file.exceptions;

public class OpenException extends FileException {

	public OpenException() {
	}

	public OpenException(String message) {
		super(message);
	}

	public OpenException(Throwable cause) {
		super(cause);
	}

	public OpenException(String message, Throwable cause) {
		super(message, cause);
	}

}
