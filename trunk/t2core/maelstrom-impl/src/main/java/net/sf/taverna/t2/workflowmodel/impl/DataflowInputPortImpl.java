package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;

public class DataflowInputPortImpl extends AbstractEventHandlingInputPort
		implements DataflowInputPort {

	protected BasicEventForwardingOutputPort internalOutput;

	private int granularInputDepth;

	private Dataflow dataflow;
	
	DataflowInputPortImpl(String name, int depth, int granularDepth, Dataflow df) {
		super(name, depth);
		granularInputDepth = granularDepth;
		dataflow = df;
		internalOutput = new BasicEventForwardingOutputPort(name, depth,
				granularDepth);
	}

	public int getGranularInputDepth() {
		return granularInputDepth;
	}

	public EventForwardingOutputPort getInternalOutputPort() {
		return internalOutput;
	}

	/**
	 * Receive an input event, relay it through the internal output port to all
	 * connected entities
	 */
	public void receiveEvent(Event e) {
		for (Datalink dl : internalOutput.getOutgoingLinks()) {
			dl.getSink().receiveEvent(e);
		}
	}

	public Dataflow getDataflow() {
		return dataflow;
	}

}
