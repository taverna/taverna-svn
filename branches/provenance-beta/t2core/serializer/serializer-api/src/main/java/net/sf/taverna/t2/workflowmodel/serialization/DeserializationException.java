package net.sf.taverna.t2.workflowmodel.serialization;


public class DeserializationException extends Exception {

	public DeserializationException(String msg) {
		super(msg);
	}

	public DeserializationException(String msg, Exception cause) {
		super(msg,cause);
	}

	private static final long serialVersionUID = -5905705659863088259L;

}
