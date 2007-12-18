package net.sf.taverna.service.interfaces;

public class ParseException extends TavernaException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3731531753517939223L;

	public ParseException(String msg) {
		super(msg);
	}

	public ParseException(String msg, Exception cause) {
		super(msg, cause);
	}
}
