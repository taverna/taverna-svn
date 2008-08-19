package net.sf.taverna.t2.facade.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.taverna.t2.facade.FailureListener;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.utility.TypedTreeModel;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;

import org.apache.log4j.Logger;

/**
 * Implementation of {@link WorkflowInstanceFacade}
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
public class WorkflowInstanceFacadeImpl implements WorkflowInstanceFacade {

	private static Logger logger = Logger
			.getLogger(WorkflowInstanceFacadeImpl.class);

	protected static AtomicLong owningProcessId = new AtomicLong(0);

	private InvocationContext context;
	public InvocationContext getContext() {
		return context;
	}

	private Dataflow dataflow;
	private ResultListener facadeResultListener;
	private String instanceOwningProcessId;
	private String localName;
	private MonitorManager monitorManager = MonitorManager.getInstance();
	private boolean pushDataCalled = false;
	protected List<FailureListener> failureListeners = new ArrayList<FailureListener>();
	protected List<ResultListener> resultListeners = new ArrayList<ResultListener>();

	public WorkflowInstanceFacadeImpl(final Dataflow dataflow,
			InvocationContext context, String parentProcess)
			throws InvalidDataflowException {
		DataflowValidationReport report = dataflow.checkValidity();
		if (!report.isValid()) {
			throw new InvalidDataflowException(dataflow, report);
		}

		this.dataflow = dataflow;
		this.context = context;
		this.localName = "facade" + owningProcessId.getAndIncrement();
		if (parentProcess.equals("")) {
			this.instanceOwningProcessId = localName;
		} else {
			this.instanceOwningProcessId = parentProcess + ":" + localName;
		}
		facadeResultListener = new FacadeResultListener(dataflow);
	}

	public void addFailureListener(FailureListener listener) {
		failureListeners.add(listener);
	}

	public synchronized void addResultListener(ResultListener listener) {
		if (resultListeners.isEmpty()) {
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
		monitorManager.registerNode(this, instanceOwningProcessId.split(":"),
				new HashSet<MonitorableProperty<?>>());
		dataflow.fire(instanceOwningProcessId, context);
	}

	public Dataflow getDataflow() {
		return dataflow;
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
		if (resultListeners.isEmpty()) {
			for (DataflowOutputPort port : dataflow.getOutputPorts()) {
				port.removeResultListener(facadeResultListener);
			}
		}
	}

	protected class FacadeResultListener implements ResultListener {
		private int portsToComplete;

		public FacadeResultListener(Dataflow dataflow) {
			portsToComplete = dataflow.getOutputPorts().size();
		}

		public void resultTokenProduced(WorkflowDataToken token, String portName) {
			if (instanceOwningProcessId.equals(token.getOwningProcess())) {
				synchronized (this) {
					if (token.getIndex().length == 0) {
						portsToComplete--;
					}
					if (portsToComplete == 0) {
						// Received complete events on all ports, can
						// un-register this node from the monitor
						MonitorManager.getInstance().deregisterNode(
								instanceOwningProcessId.split(":"));
					}
				}
				ArrayList<ResultListener> copyOfListeners = new ArrayList<ResultListener>(
						resultListeners);
				for (ResultListener resultListener : copyOfListeners) {
					try {
						resultListener.resultTokenProduced(token
								.popOwningProcess(), portName);
					} catch (RuntimeException ex) {
						logger.warn("Could not notify result listener "
								+ resultListener, ex);
					}
				}
			}
		}
	}

}
