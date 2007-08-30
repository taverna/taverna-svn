package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

public class DataflowOutputPortImpl extends BasicEventForwardingOutputPort
		implements DataflowOutputPort {

	private AbstractEventHandlingInputPort ip;

	private Dataflow dataflow;

	DataflowOutputPortImpl(Dataflow dataflow, String portName) {
		super(portName, -1, -1);
		this.dataflow = dataflow;
		this.ip = new AbstractEventHandlingInputPort(name, -1) {
			/**
			 * Forward the event through the output port
			 */
			public void receiveEvent(Event e) {
				sendEvent(e);
			}

			/**
			 * Always copy the value of the enclosing dataflow output port
			 */
			public int getDepth() {
				return DataflowOutputPortImpl.this.getDepth();
			}
		};
	}

	public EventHandlingInputPort getInternalInputPort() {
		return this.ip;
	}

	public Dataflow getDataflow() {
		return this.dataflow;
	}

	void setDepths(int depth, int granularDepth) {
		this.depth = depth;
		this.granularDepth = granularDepth;
	}

}
