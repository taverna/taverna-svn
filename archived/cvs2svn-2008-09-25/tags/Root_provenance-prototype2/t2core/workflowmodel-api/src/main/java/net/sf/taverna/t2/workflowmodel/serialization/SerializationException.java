package net.sf.taverna.t2.workflowmodel.serialization;


public class SerializationException extends Exception {

	public SerializationException(String msg, Exception cause) {
		super(msg,cause);
	}

	public SerializationException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = -218787623524401819L;

}
