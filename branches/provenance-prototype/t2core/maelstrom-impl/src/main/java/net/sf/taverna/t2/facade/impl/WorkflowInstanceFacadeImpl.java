package net.sf.taverna.t2.facade.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.facade.FailureListener;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.impl.MonitorImpl;
import net.sf.taverna.t2.provenance.WorkflowProvenanceItem;
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
	private String localName;

	public WorkflowInstanceFacadeImpl(final Dataflow dataflow,
			InvocationContext context, String parentProcess) {
		this.dataflow = dataflow;
		this.context = context;
		this.localName = "facade(" + UUID.randomUUID() +")"+ owningProcessId.getAndIncrement();
		if (parentProcess.equals("")) {
			this.instanceOwningProcessId = localName;
		} else {
			this.instanceOwningProcessId = parentProcess + ":" + localName;
		}
		facadeResultListener = new ResultListener() {
			private int portsToComplete = dataflow.getOutputPorts().size();

			public void resultTokenProduced(WorkflowDataToken token,
					String portName) {
				if (instanceOwningProcessId.equals(token.getOwningProcess())) {
					synchronized (this) {
						if (token.getIndex().length == 0) {
							portsToComplete--;
						}
						if (portsToComplete == 0) {
							// Received complete events on all ports, can
							// un-register this node from the monitor
							MonitorImpl.getMonitor().deregisterNode(
									instanceOwningProcessId.split(":"));
						}
					}
					for (ResultListener resultListener : resultListeners
							.toArray(new ResultListener[resultListeners.size()])) {
						resultListener.resultTokenProduced(token
								.popOwningProcess(), portName);
					}
				}
			}

		};

		MonitorImpl.getMonitor().registerNode(this,
				instanceOwningProcessId.split(":"),
				new HashSet<MonitorableProperty<?>>());

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
		//send dataflow across to the provenance context and store it
		WorkflowProvenanceItem worklowItem = new WorkflowProvenanceItem(dataflow);
		context.getProvenanceManager().getProvenanceCollection().add(worklowItem);
		context.getProvenanceManager().store(new DataFacade(context.getDataManager()));
		
		dataflow.fire(instanceOwningProcessId, context);
	}

	public TypedTreeModel<MonitorNode> getStateModel() {
		// TODO WorkflowInstanceFacade.getStateModel not yet implemented
		return null;
	}

	public void pushData(WorkflowDataToken token, String portName)
			throws TokenOrderException {
		// TODO: throw TokenOrderException when token stream is violates order
		// constraints.
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			if (portName.equals(port.getName())) {
				port.receiveEvent(token.pushOwningProcess(localName));
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
