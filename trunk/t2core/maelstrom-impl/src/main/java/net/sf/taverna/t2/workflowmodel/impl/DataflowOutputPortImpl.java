package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

public class DataflowOutputPortImpl extends BasicEventForwardingOutputPort
		implements DataflowOutputPort {

	protected AbstractEventHandlingInputPort internalInput;
	protected List<ResultListener> resultListeners = new ArrayList<ResultListener>();

	private Dataflow dataflow;

	DataflowOutputPortImpl(String portName, Dataflow dataflow) {
		super(portName, -1, -1);
		this.dataflow = dataflow;
		this.internalInput = new AbstractEventHandlingInputPort(name, -1) {
			/**
			 * Forward the event through the output port
			 * Also informs any ResultListeners on the output port to the new token.
			 */
			public void receiveEvent(WorkflowDataToken token) {
				sendEvent(token);
				for (ResultListener listener : resultListeners.toArray(new ResultListener[]{})) {
					listener.resultTokenProduced(token.getData(), token.getIndex(), this.getName());
				}
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
		return this.internalInput;
	}

	public Dataflow getDataflow() {
		return this.dataflow;
	}

	void setDepths(int depth, int granularDepth) {
		this.depth = depth;
		this.granularDepth = granularDepth;
	}

	public void addResultListener(ResultListener listener) {
		resultListeners.add(listener);
	}

	public void removeResultListener(ResultListener listener) {
		resultListeners.remove(listener);
	}
}
