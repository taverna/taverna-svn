package net.sf.taverna.t2.workbench.file.exceptions;

public class OverwriteException extends SaveException {
	private final Object destination;

	public OverwriteException(Object destination) {
		super("Save would overwrite existing destination " + destination);
		this.destination = destination;
	}

	public Object getDestination() {
		return destination;
	}
}
