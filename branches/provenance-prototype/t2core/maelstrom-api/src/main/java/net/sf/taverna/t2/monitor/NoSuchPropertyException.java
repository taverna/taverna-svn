package net.sf.taverna.t2.monitor;

/**
 * Thrown when an attempt is made to access a monitorable property which is no
 * longer current. This is quite a common event as the properties can change
 * extremely quickly whereas the logic accessing them is expected not to.
 * Consumers of state data must cope with this disparity by handling this
 * exception where it is thrown.
 * 
 * @author Tom Oinn
 * 
 */
public class NoSuchPropertyException extends Exception {

	private static final long serialVersionUID = 6320919057517500603L;

	public NoSuchPropertyException() {
		super();
	}

	public NoSuchPropertyException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NoSuchPropertyException(String arg0) {
		super(arg0);
	}

	public NoSuchPropertyException(Throwable arg0) {
		super(arg0);
	}

}
