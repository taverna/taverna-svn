package net.sf.taverna.service.queue;

public class QueueException extends Exception {
	private static final long serialVersionUID = 4219130113251330296L;

	public QueueException() {
		super();
	}
	
	public QueueException(String msg) {
		super(msg);
	}
	
	public QueueException(String msg, Exception cause) {
		super(msg, cause);
	}

}
