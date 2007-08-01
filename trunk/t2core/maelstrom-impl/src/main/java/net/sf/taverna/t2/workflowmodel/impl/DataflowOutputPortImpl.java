package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

public class DataflowOutputPortImpl extends BasicEventForwardingOutputPort
		implements DataflowOutputPort {

	private AbstractEventHandlingInputPort ip;

	private Dataflow dataflow;

	DataflowOutputPortImpl(Dataflow dataflow, String portName, int portDepth,
			int granularDepth) {
		super(portName, portDepth, granularDepth);
		this.dataflow = dataflow;
		this.ip = new AbstractEventHandlingInputPort(name, granularDepth) {
			public void receiveEvent(Event e) {
				// Forward the event through the output port
				sendEvent(e);
			}
		};
	}

	public EventHandlingInputPort getInternalInputPort() {
		return this.ip;
	}

	public Dataflow getDataflow() {
		return this.dataflow;
	}

}
