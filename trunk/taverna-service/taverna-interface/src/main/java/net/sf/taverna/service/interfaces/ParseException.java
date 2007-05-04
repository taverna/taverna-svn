package net.sf.taverna.service.interfaces;

public class ParseException extends TavernaException {
	public ParseException(String msg) {
		super(msg);
	}

	public ParseException(String msg, Exception cause) {
		super(msg, cause);
	}
}
