/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.facade.impl;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.taverna.t2.facade.FacadeListener;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowRunCancellation;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.NoSuchPropertyException;
import net.sf.taverna.t2.provenance.item.DataflowRunComplete;
import net.sf.taverna.t2.provenance.item.WorkflowDataProvenanceItem;
import net.sf.taverna.t2.provenance.item.WorkflowProvenanceItem;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.WorkflowRunIdEntity;
import net.sf.taverna.t2.utility.TypedTreeModel;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorFinishedEvent;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ErrorBounce;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.IntermediateProvenance;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Stop;

import org.apache.log4j.Logger;

/**
 * Implementation of {@link WorkflowInstanceFacade}
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * @author Ian Dunlop
 * @author Alex Nenadic
 * 
 */
public class WorkflowInstanceFacadeImpl implements WorkflowInstanceFacade {
	
	private static Logger logger = Logger
			.getLogger(WorkflowInstanceFacadeImpl.class);

	protected static AtomicLong owningProcessId = new AtomicLong(0);

	private InvocationContext context;
	
	private State state = State.prepared;
	public Date stateLastModified = new Date();

	public InvocationContext getContext() {
		return context;
	}

	private Dataflow dataflow;
	private FacadeResultListener facadeResultListener;
	// How many processors have finished so far
	private int processorsToComplete;
	private String instanceOwningProcessId;
	private String localName;
	private MonitorManager monitorManager = MonitorManager.getInstance();
	protected List<FacadeListener> facadeListeners = Collections.synchronizedList(new ArrayList<FacadeListener>());
	protected List<ResultListener> resultListeners = Collections.synchronizedList(new ArrayList<ResultListener>());

	private boolean provEnabled = false;
	
	private WeakHashMap<String, T2Reference> pushedDataMap = new WeakHashMap<String, T2Reference> ();

	// Id of this run
	private String workflowRunId;

	private Timestamp workflowStarted;

	private WorkflowProvenanceItem workflowItem = null;

	private int portsToComplete;
	
	private enum WorkflowInstanceFacadeChange {CANCELLATION, PORT_DECREMENT, PROCESSOR_DECREMENT};

	public WorkflowInstanceFacadeImpl(final Dataflow dataflow,
			InvocationContext context, String parentProcess)
			throws InvalidDataflowException {
		if (dataflow == null) {
			logger.error("Dataflow is null");
		}
		DataflowValidationReport report = dataflow.checkValidity();
		if (!report.isValid()) {
			throw new InvalidDataflowException(dataflow, report);
		}

		this.dataflow = dataflow;
		this.context = context;
		this.portsToComplete = dataflow.getOutputPorts().size();
		this.processorsToComplete = dataflow.getProcessors().size();
		this.localName = "facade" + owningProcessId.getAndIncrement();
		// Set the wf run id
		workflowRunId = UUID.randomUUID().toString();
		if (parentProcess.equals("")) {
			// Top-level workflow
			
			// add top level workflow run so that reference service can generate
			// identifiers linked to our run
			context.addEntity(new WorkflowRunIdEntity(workflowRunId));
			this.instanceOwningProcessId = localName;
			
			// Add this WorkflowInstanceFacade to the map of all workflow run IDs 
			// against the corresponding WorkflowInstanceFacadeS/ - to be used
			// by DependencyActivity's such as API consumer and Beanshell
			workflowRunFacades.put(localName, new WeakReference<WorkflowInstanceFacade>(this));
			// Note that we do not put the IDs for nested workflows, just for the main ones!
		} else {
			// Nested workflow
			this.instanceOwningProcessId = parentProcess + ":" + localName;
		}		
				
		if (context.getProvenanceReporter() != null) {

			provEnabled = true;
			workflowItem = new WorkflowProvenanceItem();
			workflowItem.setDataflow(dataflow);
			workflowItem.setProcessId(instanceOwningProcessId);
			workflowItem.setIdentifier(workflowRunId);
			workflowItem.setParentId(dataflow.getIdentifier());
			workflowItem.setWorkflowId(dataflow.getIdentifier());
			
			// FIXME: workflowItem is local to each instanceOwningProcessId, but
			// might be added to the Processor within different concurrent
			// runs.  (T3-930)
			addProvenanceLayerToProcessors(workflowItem);
			context.getProvenanceReporter().setSessionID(workflowRunId);

		}
		facadeResultListener = new FacadeResultListener(dataflow, workflowItem);
		
		// Register an observer with each of the processors
		for (Processor processor: dataflow.getProcessors()){			
			String expectedProcessId = instanceOwningProcessId + ":" + dataflow.getLocalName() + ":" + processor.getLocalName();
			ProcessorFinishedObserver observer = new ProcessorFinishedObserver(workflowItem, expectedProcessId);
			((ProcessorImpl) processor).addObserver(observer);
		}
	
	}

	private void addProvenanceLayerToProcessors(WorkflowProvenanceItem workflowItem) {
		for (Processor processor : dataflow.getProcessors()) {
		    // Synchronized per processor as we might be modifying its dispatch stack
		    // (fixes T3-929)
		    synchronized (processor) {               
		        DispatchStack dispatchStack = processor.getDispatchStack();
    			List<DispatchLayer<?>> layers = dispatchStack.getLayers();
    			if (isProvenanceAlreadyAdded(layers)) {
    				continue;
    			}
				DispatchLayer<?> provenance = new IntermediateProvenance();
				IntermediateProvenance intermediateProvenance = (IntermediateProvenance) provenance;
				intermediateProvenance.setWorkflow(workflowItem);
				intermediateProvenance.setReporter(context
						.getProvenanceReporter());

				Edits edits = EditsRegistry.getEdits();
				try {
					edits.getAddDispatchLayerEdit(dispatchStack, provenance,
					        provenancePosition(layers)).doEdit();
				} catch (EditException e) {
					logger.warn("adding provenance layer to dispatch stack failed "
									+ e.toString());
				}
		    }
		}
	}

    private boolean isProvenanceAlreadyAdded(List<DispatchLayer<?>> layers) {
        for (DispatchLayer<?> layer : layers) {
        	if (layer instanceof IntermediateProvenance) {
        		return true;
        	}
        }
        return false;
    }

    private int provenancePosition(List<DispatchLayer<?>> layers) {
        int position=0; // fallback - beginning of list
        for (int i = 0; i < layers.size(); i++) {
            DispatchLayer<?> layer = layers.get(i);
        	if (layer instanceof Parallelize) {
        	    // Below Parallelize (should be there!)
        	    position = i+1;
        	} else if (layer instanceof ErrorBounce) {
        	    // and inserted just above ErrorBounce (if it's there)
        		position = i;
        	}
        }
        return position;
    }

	public void addFacadeListener(FacadeListener listener) {
		facadeListeners.add(listener);
	}

	public void addResultListener(ResultListener listener) {
		synchronized (resultListeners) {
			if (resultListeners.isEmpty()) {
				for (DataflowOutputPort port : dataflow.getOutputPorts()) {
					port.addResultListener(facadeResultListener);
				}
			}
			resultListeners.add(listener); 
		}		
	}

	public synchronized void fire() throws IllegalStateException {
		if (getState().equals(State.running)) {
			throw new IllegalStateException(
					"Workflow is already running!");
		}
		workflowStarted = new Timestamp(System.currentTimeMillis());
		setState(State.running);
		if (provEnabled) {
			workflowItem.setInvocationStarted(workflowStarted);
			context.getProvenanceReporter().addProvenanceItem(workflowItem);
		}
		
		HashSet<MonitorableProperty<?>> properties = new HashSet<MonitorableProperty<?>>();
		properties.add(new StateProperty());
		monitorManager.registerNode(this, instanceOwningProcessId.split(":"),				
				properties);
		dataflow.fire(instanceOwningProcessId, context);
		
	}

	public final class StateProperty implements MonitorableProperty<State> {
		public Date getLastModified() {
			return stateLastModified;
		}

		public String[] getName() {
			return new String[] { "facade", "state" };
		}

		public State getValue() throws NoSuchPropertyException {
			return getState();
		}
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
		State currentState = getState();
		if (! currentState.equals(State.running)) {
			throw new IllegalStateException("Can't push data, current state is not running, but " + currentState);
		}
		// TODO: throw TokenOrderException when token stream is violates order
		// constraints.
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			if (portName.equals(port.getName())) {
				if (token.getIndex().length == 0) {
					if (pushedDataMap.containsKey(portName)) {
						throw new IllegalStateException("Already pushed for port " + portName);
					}
					pushedDataMap.put(portName, token.getData());					
				}
				if (provEnabled) {
					WorkflowDataProvenanceItem workflowDataProvenanceItem = new WorkflowDataProvenanceItem();
					workflowDataProvenanceItem.setPortName(portName);
					workflowDataProvenanceItem.setInputPort(true);
					workflowDataProvenanceItem.setData(token.getData());
					workflowDataProvenanceItem.setReferenceService(context.getReferenceService());
					workflowDataProvenanceItem.setParentId(workflowItem.getIdentifier());
					workflowDataProvenanceItem.setWorkflowId(workflowItem.getParentId());
					workflowDataProvenanceItem.setIdentifier(UUID.randomUUID().toString());
					workflowDataProvenanceItem.setParentId(instanceOwningProcessId);
					workflowDataProvenanceItem.setProcessId(instanceOwningProcessId);
					workflowDataProvenanceItem.setIndex(token.getIndex());
					workflowDataProvenanceItem.setFinal(token.isFinal());
					context.getProvenanceReporter().addProvenanceItem(
							workflowDataProvenanceItem);
				}
				port.receiveEvent(token.pushOwningProcess(localName));
			}
		}
	}

	public void removeFacadeListener(FacadeListener listener) {
		facadeListeners.remove(listener);
	}

	public void removeResultListener(ResultListener listener) {
		synchronized (resultListeners) {
			resultListeners.remove(listener);
			if (resultListeners.isEmpty()) {
				for (DataflowOutputPort port : dataflow.getOutputPorts()) {
					port.removeResultListener(facadeResultListener);
				}
			}
		}
	}

	protected class FacadeResultListener implements ResultListener {
		private final WorkflowProvenanceItem workflowItem;

		public FacadeResultListener(Dataflow dataflow,
				WorkflowProvenanceItem workflowItem) {
			this.workflowItem = workflowItem;
			
		}

		public void resultTokenProduced(WorkflowDataToken token, String portName) {			
			if (!instanceOwningProcessId.equals(token.getOwningProcess())) {
				return;
			}
			if (getState().equals(State.cancelled)) {
				// Throw the token away
				return;
			}
			if (provEnabled) {
				WorkflowDataProvenanceItem workflowDataProvenanceItem = new WorkflowDataProvenanceItem();
				workflowDataProvenanceItem.setPortName(portName);
				workflowDataProvenanceItem.setInputPort(false);
				workflowDataProvenanceItem.setData(token.getData());
				workflowDataProvenanceItem.setReferenceService(context.getReferenceService());
				workflowDataProvenanceItem.setParentId(workflowItem.getIdentifier());
				workflowDataProvenanceItem.setWorkflowId(workflowItem.getParentId());
				workflowDataProvenanceItem.setIdentifier(UUID.randomUUID().toString());
				workflowDataProvenanceItem.setParentId(instanceOwningProcessId);
				workflowDataProvenanceItem.setProcessId(instanceOwningProcessId);
				workflowDataProvenanceItem.setIndex(token.getIndex());
				workflowDataProvenanceItem.setFinal(token.isFinal());
				context.getProvenanceReporter().addProvenanceItem(
						workflowDataProvenanceItem);				
			}
			

			ArrayList<ResultListener> copyOfListeners;
			synchronized (resultListeners) {
				copyOfListeners = new ArrayList<ResultListener>(resultListeners);
			}			
			for (ResultListener resultListener : copyOfListeners) {
				try {
					resultListener.resultTokenProduced(
							token.popOwningProcess(), portName);
				} catch (RuntimeException ex) {
					logger.warn("Could not notify result listener "
							+ resultListener, ex);
				}
			}
			if (token.getIndex().length == 0) {
				checkWorkflowFinished(WorkflowInstanceFacadeChange.PORT_DECREMENT);
			}


		}
	}
	

	/**
	 * An observer of events that occur when a processor finishes with execution.
	 *
	 */
	private class ProcessorFinishedObserver implements Observer<ProcessorFinishedEvent>{

		private WorkflowProvenanceItem workflowItem;
		private final String expectedProcessId;

		public ProcessorFinishedObserver(WorkflowProvenanceItem workflowItem, String expectedProcessId) {
			this.workflowItem = workflowItem;
			this.expectedProcessId = expectedProcessId;
		}

		public void notify(Observable<ProcessorFinishedEvent> sender,
				ProcessorFinishedEvent message) throws Exception {
			if (! message.getOwningProcess().equals(expectedProcessId)) {
				return;
			}
			
			// De-register the processor node from the monitor as it has finished
			monitorManager.deregisterNode(message.getOwningProcess());
			
			// De-register this observer from the processor
			message.getProcessor().removeObserver(this);
			
			// All processors have finished => the workflow run has finished
			checkWorkflowFinished(WorkflowInstanceFacadeChange.PROCESSOR_DECREMENT);
		}
	}

	protected void checkWorkflowFinished(WorkflowInstanceFacadeChange change) {
		synchronized (this) {
			if (WorkflowInstanceFacadeChange.CANCELLATION.equals(change)) {
				processorsToComplete = 0;
				portsToComplete = 0;
			}
			else if (WorkflowInstanceFacadeChange.PORT_DECREMENT.equals(change)) {
				portsToComplete--;
			}
			else if (WorkflowInstanceFacadeChange.PROCESSOR_DECREMENT.equals(change)) {
				processorsToComplete--;
			}
			if (getState().equals(State.cancelled) && processorsToComplete < 0) {
				logger.error("Already cancelled workflow run "
						+ instanceOwningProcessId);
				return;
			}
			if (getState().equals(State.completed)) {
				logger.error("Already finished workflow run "
						+ instanceOwningProcessId, new IllegalStateException());
				return;
			}
			if (processorsToComplete > 0 || portsToComplete > 0) {
				// Not yet finished
				return;
			}
			if (processorsToComplete < 0 || portsToComplete < 0) {
				logger.error("Already finished workflow run "
						+ instanceOwningProcessId, new IllegalStateException());
				return;
			}
			if (!getState().equals(State.cancelled)) {
				setState(State.completed);
			}
			processorsToComplete = -1;
			portsToComplete = -1;
		}	
		// De-register the workflow node from the monitor
		monitorManager.deregisterNode(instanceOwningProcessId + ":" + dataflow.getLocalName());

		// De-register this facade node from the monitor - this will effectively
		// tell the monitor that the workflow run has finished
		monitorManager.deregisterNode(instanceOwningProcessId);
		
		
		if (provEnabled) {
			DataflowRunComplete dataflowRunComplete = new DataflowRunComplete();
			dataflowRunComplete.setInvocationEnded(new Timestamp(System.currentTimeMillis()));
			dataflowRunComplete.setParentId(workflowItem
					.getIdentifier());
			dataflowRunComplete.setWorkflowId(workflowItem.getParentId());
			dataflowRunComplete
					.setProcessId(instanceOwningProcessId);
			dataflowRunComplete.setIdentifier(UUID.randomUUID()
					.toString());
			dataflowRunComplete.setState(getState());
			context.getProvenanceReporter().addProvenanceItem(
					dataflowRunComplete);
		}
		
		
	}


	public WeakHashMap<String, T2Reference> getPushedDataMap() {
		return pushedDataMap;
	}

	public void setWorkflowRunId(String workflowRunId) {
		this.workflowRunId = workflowRunId;
	}

	public String getWorkflowRunId() {
		return workflowRunId;
	}
	
	public synchronized State getState() {
		return state;
	}
	
	public synchronized void setState(State newState) throws IllegalStateException {
		State oldState = state;
		if (newState.equals(state)) {
			return;
		}
		if (newState.equals(State.running)) {
			if (state.equals(State.prepared) || state.equals(State.paused)) {
				stateLastModified = new Date();
				state = newState;
				notifyFacadeListeners(oldState, newState);
				return;
			}
		} else if (newState.equals(State.paused)) {			
			if (state.equals(State.running)) {
				stateLastModified = new Date();
				state = newState;
				notifyFacadeListeners(oldState, newState);
				return;
			}
		} else if (newState.equals(State.completed)) {
			if (state.equals(State.running)) {
				stateLastModified = new Date();
				state = newState;
				notifyFacadeListeners(oldState, newState);
				return;
			} else if (state.equals(State.cancelled)) {
				// Keep as cancelled
				return;
			}
		} else if (newState.equals(State.cancelled)) {
			if (! state.equals(State.completed)) {
				stateLastModified = new Date();
				state = newState;
				notifyFacadeListeners(oldState, newState);
				return;
			}
		}		
		throw new IllegalStateException("Can't change state from " + state  + " to " + newState);		
	}
	
	
	private void notifyFacadeListeners(State oldState, State newState) {
		List<FacadeListener> copyOfListeners = null;
		synchronized (facadeListeners) {
			copyOfListeners = new ArrayList<FacadeListener>(facadeListeners);
		}
		for (FacadeListener facadeListener : copyOfListeners) {
			try {
				facadeListener.stateChange(this, oldState, newState);
			} catch (RuntimeException ex) {
				logger.warn("Could not notify facade listener "
						+ facadeListener, ex);
			}
		}
	}

	public synchronized boolean cancelWorkflowRun() {
		if (getState().equals(State.completed)) {
			return false;
		}
		boolean result = Stop.cancelWorkflow(getWorkflowRunId());
		if (result) {
			setState(State.cancelled);
			logger.info("Cancelled workflow runId=" + getWorkflowRunId()
					+ " processId=" + instanceOwningProcessId);
			List<FacadeListener> copyOfListeners = null;
			synchronized (facadeListeners) {
				copyOfListeners = new ArrayList<FacadeListener>(facadeListeners);
			}
			for (FacadeListener facadeListener : copyOfListeners) {
				try {
					facadeListener.workflowFailed(this, "Workflow was cancelled",
							new WorkflowRunCancellation(getWorkflowRunId()));
				} catch (RuntimeException ex) {
					logger.warn("Could not notify failure listener "
							+ facadeListener, ex);
				}
			}
			checkWorkflowFinished(WorkflowInstanceFacadeChange.CANCELLATION);		
		}
		return result;
	}

	public boolean pauseWorkflowRun() {
		setState(State.paused);
		if (Stop.pauseWorkflow(getWorkflowRunId())) {
			logger.info("Paused workflow runId=" + getWorkflowRunId()
					+ " processId=" + instanceOwningProcessId);
			return true;
		}
		return false;
	}

	public boolean resumeWorkflowRun() {
		setState(State.running);
		if (Stop.resumeWorkflow(getWorkflowRunId())) {
			logger.info("Resumed paused workflow runId=" + getWorkflowRunId()
					+ " processId=" + instanceOwningProcessId);
			return true;
		}
		return false;
	}

}