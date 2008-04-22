package net.sf.taverna.t2.cloudone.refscheme;

/**
 * Thrown if an attempt to dereference a reference scheme instance fails for
 * whatever reason. This failure may be anticipated or unexpected.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public class DereferenceException extends Exception {

	private static final long serialVersionUID = 1L;

	public DereferenceException() {
		super();
	}

	public DereferenceException(String msg) {
		super(msg);
	}

	public DereferenceException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public DereferenceException(Throwable cause) {
		super(cause);
	}

}
