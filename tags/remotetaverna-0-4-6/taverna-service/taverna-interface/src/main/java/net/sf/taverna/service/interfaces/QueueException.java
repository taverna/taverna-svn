package net.sf.taverna.service.interfaces;


public class QueueException extends TavernaException {
	private static final long serialVersionUID = 4219130113251330296L;
	
	public QueueException(String msg) {
		super(msg);
	}

	public QueueException(String msg, Exception cause) {
		super(msg, cause);
	}

}
