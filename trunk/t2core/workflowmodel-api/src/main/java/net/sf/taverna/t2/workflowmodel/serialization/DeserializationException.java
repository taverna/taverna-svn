package net.sf.taverna.t2.workflowmodel.serialization;

import net.sf.taverna.t2.workflowmodel.EditException;

public class DeserializationException extends Exception {

	public DeserializationException(String msg) {
		super(msg);
	}

	public DeserializationException(String msg, EditException cause) {
		super(msg,cause);
	}

	private static final long serialVersionUID = -5905705659863088259L;

}
