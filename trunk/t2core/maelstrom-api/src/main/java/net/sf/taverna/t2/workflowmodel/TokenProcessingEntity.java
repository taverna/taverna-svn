package net.sf.taverna.t2.workflowmodel;

import java.util.List;

public interface TokenProcessingEntity extends NamedWorkflowEntity {

	public List<? extends EventHandlingInputPort> getInputPorts();
	
	public List<? extends EventForwardingOutputPort> getOutputPorts();
	
}
