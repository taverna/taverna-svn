package net.sf.taverna.t2.plugin.input;

import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

public class InputPortException extends Exception {

	private final DataflowInputPort port;

	public InputPortException(DataflowInputPort port, Exception cause) {
		super(cause);		
		this.port = port;
	}

	public InputPortException(DataflowInputPort port, String message) {
		super(message);
		this.port = port;
	}

	public DataflowInputPort getPort() {
		return port;
	}

}
