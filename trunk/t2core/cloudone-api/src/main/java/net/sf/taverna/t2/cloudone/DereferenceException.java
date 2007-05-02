package net.sf.taverna.t2.cloudone;

/**
 * Thrown if an attempt to dereference a reference scheme instance fails for
 * whatever reason. This failure may be anticipated or unexpected.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class DereferenceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DereferenceException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DereferenceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public DereferenceException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public DereferenceException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
