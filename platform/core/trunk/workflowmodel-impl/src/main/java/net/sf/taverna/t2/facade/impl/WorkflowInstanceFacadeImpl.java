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

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowInstanceListener;
import net.sf.taverna.t2.facade.WorkflowInstanceStatus;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.monitor.MonitorReceiver;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.util.reflect.ReflectionHelper;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;

/**
 * Implementation of {@link WorkflowInstanceFacade}
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * @author Ian Dunlop
 * 
 */
public class WorkflowInstanceFacadeImpl implements WorkflowInstanceFacade {

	protected static AtomicLong owningProcessId = new AtomicLong(0);

	private InvocationContext context;

	public InvocationContext getContext() {
		return context;
	}

	private Dataflow dataflow;
	private ProcessIdentifier instanceOwningProcessId;
	private String localName;
	private boolean pushDataCalled = false;
	private ProcessIdentifier dataflowName = null;
	@SuppressWarnings("unused")
	private boolean provEnabled = false;

	/**
	 * Construct a new workflow instance facade - note: if you are using the
	 * monitoring system you must ensure that the monitor receiver is present in
	 * the context before this is called.
	 * 
	 * @param dataflow
	 * @param context
	 * @param parentProcess
	 * @param edits
	 * @param manager
	 * @param reflectionHelper
	 * @throws InvalidDataflowException
	 */
	public WorkflowInstanceFacadeImpl(final Dataflow dataflow,
			InvocationContext context, ProcessIdentifier parentProcess,
			Edits edits, PluginManager manager,
			ReflectionHelper reflectionHelper) throws InvalidDataflowException {

		DataflowValidationReport report = dataflow.checkValidity();

		if (!report.isValid()) {
			throw new InvalidDataflowException(dataflow, report);
		}

		this.dataflow = dataflow;
		this.context = context;
		this.localName = "facade" + owningProcessId.getAndIncrement();

		this.instanceOwningProcessId = parentProcess.createChild(localName);

		this.dataflowName = this.instanceOwningProcessId
				.createChild(this.dataflow.getLocalName());

		final List<? extends MonitorReceiver> monitors = context
				.getEntities(MonitorReceiver.class);
		if (!monitors.isEmpty()) {
			final ProcessIdentifier nodeId = instanceOwningProcessId;
			for (MonitorReceiver receiver : monitors) {
				receiver.registerNode(this, nodeId,
						new HashSet<MonitorableProperty<?>>());
			}
			addWorkflowInstanceListener(new WorkflowInstanceListener() {

				public void resultTokenProduced(WorkflowDataToken token,
						String portName) {
					// 
				}

				public void workflowCompleted(ProcessIdentifier owningProcess) {
					for (MonitorReceiver monitor : monitors) {
						monitor.deregisterNode(nodeId);
					}
				}

				public void workflowFailed(ProcessIdentifier failedProcess,
						InvocationContext invocationContext,
						NamedWorkflowEntity workflowEntity, String message,
						Throwable cause) {
					for (MonitorReceiver monitor : monitors) {
						monitor.deregisterNode(nodeId);
					}
				}

				public void workflowStatusChanged(
						WorkflowInstanceStatus oldStatus,
						WorkflowInstanceStatus newStatus) {
					//
				}

			});
		}
	}

	public void fire() throws IllegalStateException {
		if (pushDataCalled)
			throw new IllegalStateException(
					"Data has already been pushed, fire must be called first!");

		dataflow.fire(instanceOwningProcessId, context);
	}

	public Dataflow getDataflow() {
		return dataflow;
	}

	public void pushData(WorkflowDataToken token, String portName)
			throws TokenOrderException {
		if (!token.isActive()) {
			return;
		}
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			if (portName.equals(port.getName())) {
				port.receiveEvent(token.pushOwningProcess(localName));
				break;
			}
		}
		pushDataCalled = true;
	}

	public synchronized void addWorkflowInstanceListener(
			WorkflowInstanceListener listener) {
		dataflow.addWorkflowInstanceListener(dataflowName, listener);
	}

	public synchronized void removeWorkflowInstanceListener(
			WorkflowInstanceListener listener) {
		dataflow.removeWorkflowInstanceListener(dataflowName, listener);
	}

	public synchronized WorkflowInstanceStatus getStatus() {
		return dataflow.getInstanceStatus(dataflowName);
	}

	public void cancel() {
		dataflow.cancelWorkflowInstance(dataflowName, context);
	}

	public ProcessIdentifier getFacadeProcessIdentifier() {
		return instanceOwningProcessId;
	}

	public boolean isPaused() {
		return context.isPaused();
	}

	public void setPaused(boolean paused) {
		context.setPaused(paused);
	}

}
