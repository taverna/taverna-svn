package net.sf.taverna.service.rest.client;

public class RESTException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7536445997223794902L;

	public RESTException() {
		super();
	}

	public RESTException(String message, Throwable cause) {
		super(message, cause);
	}

	public RESTException(Throwable cause) {
		super(cause);
	}

	public RESTException(String string) {
		super(string);
	}

}
