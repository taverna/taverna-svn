package net.sf.taverna.t2.facade.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.FailureListener;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.utility.TypedTreeModel;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;

public class WorkflowInstanceFacadeImpl implements WorkflowInstanceFacade {

	private Dataflow dataflow;
	protected List<FailureListener> failureListeners = new ArrayList<FailureListener>();
	protected List<ResultListener> resultListeners = new ArrayList<ResultListener>();
	protected static AtomicLong owningProcessId = new AtomicLong(0);
	private String instanceOwningProcessId;
	private boolean pushDataCalled = false;
	private ResultListener facadeResultListener;
	private InvocationContext context;

	public WorkflowInstanceFacadeImpl(Dataflow dataflow, InvocationContext context) {
		this.dataflow = dataflow;
		this.context = context;
		instanceOwningProcessId = dataflow.getLocalName() + "_"
				+ owningProcessId.getAndIncrement();

		facadeResultListener = new ResultListener() {

			public void resultTokenProduced(WorkflowDataToken token, String portName, String owningProcess) {
				if (instanceOwningProcessId.equals(owningProcess)) {
					for (ResultListener resultListener : resultListeners.toArray(new ResultListener[resultListeners.size()])) {
						resultListener.resultTokenProduced(token,
								portName, owningProcess);
					}
				}
			}

		};
	}

	public void addFailureListener(FailureListener listener) {
		failureListeners.add(listener);
	}

	public synchronized void addResultListener(ResultListener listener) {
		if (resultListeners.size() == 0) {
			for (DataflowOutputPort port : dataflow.getOutputPorts()) {
				port.addResultListener(facadeResultListener);
			}
		}
		resultListeners.add(listener);
	}

	public void fire() throws IllegalStateException {
		if (pushDataCalled)
			throw new IllegalStateException(
					"Data has already been pushed, fire must be called first!");
		dataflow.fire(instanceOwningProcessId, context);
	}

	public TypedTreeModel<MonitorNode> getStateModel() {
		// TODO WorkflowInstanceFacade.getStateModel not yet implemented
		return null;
	}

	public void pushData(EntityIdentifier token, int[] index, String portName)
			throws TokenOrderException {
		// TODO: throw TokenOrderException when token stream is violates order
		// constraints.
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			if (portName.equals(port.getName())) {
				port.receiveEvent(new WorkflowDataToken(instanceOwningProcessId, index, token, context));
			}
		}
		pushDataCalled = true;
	}

	public void removeFailureListener(FailureListener listener) {
		failureListeners.remove(listener);
	}

	public synchronized void removeResultListener(ResultListener listener) {
		resultListeners.remove(listener);
		if (resultListeners.size() == 0) {
			for (DataflowOutputPort port : dataflow.getOutputPorts()) {
				port.removeResultListener(facadeResultListener);
			}
		}
	}

	public Dataflow getDataflow() {
		return dataflow;
	}

}
