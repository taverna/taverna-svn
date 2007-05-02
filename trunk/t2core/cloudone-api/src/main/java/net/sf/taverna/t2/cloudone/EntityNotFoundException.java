package net.sf.taverna.t2.cloudone;

/**
 * Thrown when a request is made for resolution of an entity that cannot be
 * found by the data manager to which the request is made.
 * 
 * @author Tom
 * 
 */
public class EntityNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1069998094174721609L;

	public EntityNotFoundException() {
		// TODO Auto-generated constructor stub
	}

	public EntityNotFoundException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public EntityNotFoundException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public EntityNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
