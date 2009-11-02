package net.sf.taverna.t2.lineageService.capture.test;


public class DataflowTimeoutException extends Exception {

	private static final long serialVersionUID = -4529249797392326164L;

	public DataflowTimeoutException(String msg) {
		super(msg);
		
	}

	public DataflowTimeoutException(Throwable cause) {
		super(cause);
		
	}

	public DataflowTimeoutException(String msg, Throwable cause) {
		super(msg, cause);
		
	}

}
