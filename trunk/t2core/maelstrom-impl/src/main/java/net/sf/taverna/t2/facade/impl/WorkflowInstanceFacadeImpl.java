package net.sf.taverna.t2.facade.impl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.FailureListener;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.utility.TypedTreeModel;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;


public class WorkflowInstanceFacadeImpl implements WorkflowInstanceFacade {

	private Dataflow dataflow;
	protected List<FailureListener> failureListeners=new ArrayList<FailureListener>();
	protected List<ResultListener> resultListeners=new ArrayList<ResultListener>();
	protected static AtomicLong owningProcessId = new AtomicLong(0);
	private long instanceOwningProcessId;
	private boolean pushDataCalled=false;
	
	public WorkflowInstanceFacadeImpl(Dataflow dataflow) {
		this.dataflow=dataflow;
		instanceOwningProcessId=owningProcessId.getAndIncrement();
	}
	
	public void addFailureListener(FailureListener listener) {
		failureListeners.add(listener);
	}

	public void addResultListener(ResultListener listener) {
		resultListeners.add(listener);
		for (DataflowOutputPort port : dataflow.getOutputPorts()) {
			port.addResultListener(listener);
		}
	}

	public void fire() throws IllegalStateException {
		if (pushDataCalled) throw new IllegalStateException("Data has already been pushed, fire must be called first!");
		for (Processor processor : dataflow.getProcessors()) {
			if (processor.getInputPorts().size() == 0 && processor.getPreconditionList().size() == 0) {
				processor.fire(dataflow.getLocalName()+"_"+instanceOwningProcessId);
			}
		}
	}

	public TypedTreeModel<MonitorNode> getStateModel() {
		// TODO WorkflowInstanceFacade.getStateModel not yet implemented
		return null;
	}

	public void pushData(EntityIdentifier token, int[] index, String portName)
			throws TokenOrderException {
		//TODO: throw TokenOrderException when token stream is violates order constraints.
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			if (portName.equals(port.getName())) {
				port.receiveEvent(new WorkflowDataToken(dataflow.getLocalName()+"_"+instanceOwningProcessId,index,token));
			}
		}
		pushDataCalled=true;
	}

	public void removeFailureListener(FailureListener listener) {
		failureListeners.remove(listener);
	}

	public void removeResultListener(ResultListener listener) {
		resultListeners.remove(listener);
		for (DataflowOutputPort port : dataflow.getOutputPorts()) {
			port.removeResultListener(listener);
		}
		
	}

	public Dataflow getDataflow() {
		return dataflow;
	}

}
