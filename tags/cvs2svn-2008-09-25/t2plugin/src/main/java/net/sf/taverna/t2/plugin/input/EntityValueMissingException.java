package net.sf.taverna.t2.plugin.input;

import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

public class EntityValueMissingException extends InputPortException {

	public EntityValueMissingException(DataflowInputPort port) {
		super(port, "Missing value for " + port);
	}
	

}
