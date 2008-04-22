package net.sf.taverna.t2.invocation;

/**
 * Thrown when tokens are supplied in an invalid order. Examples of this are
 * where duplicate indices are supplied in the same token stream or where list
 * items are emitted at a point where the individual members haven't been fully
 * populated.
 * 
 * @author Tom Oinn
 * 
 */
public class TokenOrderException extends Exception {

	public TokenOrderException() {
		super();
	}

	public TokenOrderException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public TokenOrderException(String arg0) {
		super(arg0);
	}

	public TokenOrderException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7870614853928171878L;

}
