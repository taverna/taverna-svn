package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

public class DataflowOutputPortImpl extends BasicEventForwardingOutputPort
		implements DataflowOutputPort {

	DataflowOutputPortImpl(String portName, int portDepth, int granularDepth) {
		super(portName, portDepth, granularDepth);
		// TODO Auto-generated constructor stub
	}

	public AbstractEventHandlingInputPort getInternalInputPort() {
		// TODO Auto-generated method stub
		return null;
	}

	public Dataflow getDataflow() {
		// TODO Auto-generated method stub
		return null;
	}

}
