package net.sf.taverna.t2.platform.taverna.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowInstanceListener;
import net.sf.taverna.t2.facade.WorkflowInstanceStatus;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.taverna.Enactor;
import net.sf.taverna.t2.platform.taverna.EnactorException;
import net.sf.taverna.t2.platform.util.reflect.ReflectionHelper;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

/**
 * Implementation of Enactor that returns WorkflowInstanceFacadeImpl objects
 * from the workflowmodel-impl module when configured with an appropriate
 * ReferenceService and PluginManager
 * 
 * @author Tom Oinn
 */
public class EnactorImpl implements Enactor {

	private Edits edits;
	private PluginManager manager;
	private ReferenceService referenceService;
	private ReflectionHelper reflectionHelper;

	public void setPluginManager(PluginManager manager) {
		this.manager = manager;
		this.edits = new EditsImpl(manager);
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public void setReflectionHelper(ReflectionHelper helper) {
		this.reflectionHelper = helper;
	}

	public WorkflowInstanceFacade createFacade(Dataflow workflow)
			throws EnactorException {
		InvocationContextImpl ici = new InvocationContextImpl();
		ici.setReferenceService(referenceService);
		return createFacade(workflow, ici);
	}

	public WorkflowInstanceFacade createFacade(Dataflow workflow,
			InvocationContext context) throws EnactorException {
		try {
			return new WorkflowInstanceFacadeImpl(workflow, context,
					new ProcessIdentifier(), edits, manager, reflectionHelper);
		} catch (InvalidDataflowException ide) {
			throw new EnactorException(ide);
		}
	}

	public WorkflowInstanceFacade createFacade(Dataflow workflow,
			InvocationContext context, ProcessIdentifier parentProcess) throws EnactorException {
		try {
			return new WorkflowInstanceFacadeImpl(workflow, context,
					parentProcess, edits, manager, reflectionHelper);
		} catch (InvalidDataflowException ide) {
			throw new EnactorException(ide);
		}
	}

	public void pushData(WorkflowInstanceFacade facade, String inputPortName,
			T2Reference data) throws EnactorException {
		// Check that the input port exists!
		boolean foundPort = false;
		for (DataflowInputPort port : facade.getDataflow().getInputPorts()) {
			if (port.getName().equals(inputPortName)) {
				foundPort = true;
				break;
			}
		}
		if (!foundPort) {
			throw new EnactorException(
					"Workflow doesn't contain an input port called '"
							+ inputPortName + "'");
		}
		try {
			facade.pushData(new WorkflowDataToken(new ProcessIdentifier(),
					new int[0], data, facade.getContext()), inputPortName);
		} catch (TokenOrderException toe) {
			// This won't happen, rethrow though if I'm wrong!
			throw new EnactorException(toe);
		}
	}

	public Map<String, T2Reference> waitForCompletion(
			WorkflowInstanceFacade facade) throws EnactorException {
		final Map<String, T2Reference> result = new HashMap<String, T2Reference>();
		final CompletionMonitor monitor = new CompletionMonitor();
		final Thread currentThread = Thread.currentThread();
		WorkflowInstanceListener listener = new WorkflowInstanceListener() {

			public synchronized void resultTokenProduced(
					WorkflowDataToken output, String outputName) {
				if (output.isFinal()) {
					result.put(outputName, output.getData());
				}
			}

			public void workflowCompleted(ProcessIdentifier owningProcess) {
				monitor.complete = true;
				currentThread.interrupt();
			}

			public void workflowStatusChanged(WorkflowInstanceStatus oldStatus,
					WorkflowInstanceStatus newStatus) {
				// TODO Auto-generated method stub

			}

			public void workflowFailed(ProcessIdentifier failedProcess,
					InvocationContext invocationContext,
					NamedWorkflowEntity workflowEntity, String message,
					Throwable cause) {
				monitor.failureMessage = message;
				monitor.failureCause = cause;
				monitor.failed = true;
				currentThread.interrupt();
			}

		};
		facade.addWorkflowInstanceListener(listener);
		while (!monitor.complete && !monitor.failed) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ie) {
				// check for completion
			}
		}

		facade.removeWorkflowInstanceListener(listener);
		if (monitor.complete) {
			return result;
		} else {
			if (monitor.failureCause == null) {
				throw new EnactorException(monitor.failureMessage);
			} else {
				throw new EnactorException(monitor.failureMessage,
						monitor.failureCause);
			}
		}
	}

	public T2Reference waitForResult(WorkflowInstanceFacade facade,
			final String outputPortName) throws EnactorException {
		// Check that port exists...
		boolean foundPort = false;

		for (DataflowOutputPort port : facade.getDataflow().getOutputPorts()) {
			if (port.getName().equals(outputPortName)) {
				foundPort = true;
				break;
			}
		}
		if (!foundPort) {
			throw new EnactorException(
					"Workflow doesn't contain an output port called '"
							+ outputPortName + "'");
		}
		final CompletionMonitor monitor = new CompletionMonitor();
		final Thread currentThread = Thread.currentThread();
		WorkflowInstanceListener listener = new WorkflowInstanceListener() {
			public void resultTokenProduced(WorkflowDataToken output,
					String outputName) {
				if (outputName.equals(outputPortName) && output.isFinal()) {
					monitor.complete = true;
					monitor.result = output.getData();
					currentThread.interrupt();
				}
			}

			public void workflowCompleted(ProcessIdentifier owningProcess) {
				// TODO Auto-generated method stub

			}

			public void workflowStatusChanged(WorkflowInstanceStatus oldStatus,
					WorkflowInstanceStatus newStatus) {
				// TODO Auto-generated method stub

			}

			public void workflowFailed(ProcessIdentifier failedProcess,
					InvocationContext invocationContext,
					NamedWorkflowEntity workflowEntity, String message,
					Throwable cause) {
				monitor.failureMessage = message;
				monitor.failureCause = cause;
				monitor.failed = true;
				currentThread.interrupt();
			}
		};
		facade.addWorkflowInstanceListener(listener);
		while (!monitor.complete && !monitor.failed) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ie) {
				// check for completion
			}
		}

		facade.removeWorkflowInstanceListener(listener);
		if (monitor.complete) {
			return monitor.result;
		} else {
			if (monitor.failureCause == null) {
				throw new EnactorException(monitor.failureMessage);
			} else {
				throw new EnactorException(monitor.failureMessage,
						monitor.failureCause);
			}
		}
	}

	class CompletionMonitor {
		boolean complete, failed = false;
		T2Reference result = null;
		String failureMessage = null;
		Throwable failureCause = null;
	}

}
