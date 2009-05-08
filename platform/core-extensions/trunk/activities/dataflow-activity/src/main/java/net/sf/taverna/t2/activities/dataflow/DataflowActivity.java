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
package net.sf.taverna.t2.activities.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowInstanceListener;
import net.sf.taverna.t2.facade.WorkflowInstanceStatus;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.platform.taverna.EnactorException;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

import org.apache.log4j.Logger;

/**
 * <p>
 * An Activity providing nested Dataflow functionality.
 * </p>
 * 
 * @author David Withers
 */
public class DataflowActivity extends
		AbstractAsynchronousActivity<Dataflow> implements NestedDataflow{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(DataflowActivity.class);
	
	private Dataflow dataflow;
	
	@Override
	public void configure(Dataflow dataflow, Edits edits)
			throws ActivityConfigurationException {
		this.dataflow=dataflow;
		dataflow.checkValidity();
		buildInputPorts(edits);
		buildOutputPorts(edits);
	}

	@Override
	public Dataflow getConfiguration() {
		return dataflow;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {

				final WorkflowInstanceFacade facade;
				try {
					facade = PlatformComponents.getEnactor().createFacade(dataflow, callback.getContext(), callback
									.getParentProcessIdentifier());
				} catch (EnactorException ex) {
					callback.fail("Invalid dataflow", ex);
					return;
				}
				
				facade.addWorkflowInstanceListener(new WorkflowInstanceListener() {

					Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

					public void resultTokenProduced(WorkflowDataToken dataToken,
							String portName) {
						if (dataToken.isFinal()) {
							outputData.put(portName, dataToken.getData());
						}
					}

					public void workflowCompleted(ProcessIdentifier arg0) {
						callback.receiveResult(outputData, new int[0]);
						facade.removeWorkflowInstanceListener(this);
					}

					public void workflowFailed(ProcessIdentifier failedProcess,
							InvocationContext invocationContext, NamedWorkflowEntity workflowEntity,
							String message, Throwable cause) {
						callback.fail(message, cause);
					}

					public void workflowStatusChanged( WorkflowInstanceStatus oldStatus,
							WorkflowInstanceStatus newStatus) {
					}
					
				});

				facade.fire();

				for (Map.Entry<String, T2Reference> entry : data
						.entrySet()) {
					try {
						WorkflowDataToken token = new WorkflowDataToken(
								callback.getParentProcessIdentifier(),
								new int[] {}, entry.getValue(), callback
										.getContext());
						facade.pushData(token, entry.getKey());
					} catch (TokenOrderException e) {
						callback.fail("Failed to push data into facade", e);
					}
				}

			}

		});
	}

	private void buildInputPorts(Edits edits) throws ActivityConfigurationException {
		inputPorts.clear();
		for (DataflowInputPort dataflowInputPort : dataflow.getInputPorts()) {
			addInput(dataflowInputPort.getName(), dataflowInputPort.getDepth(),
					new ArrayList<Class<? extends ExternalReferenceSPI>>(),
					edits);
		}
	}

	private void buildOutputPorts(Edits edits) throws ActivityConfigurationException {
		outputPorts.clear();
		//granular depth same as depth - no streaming of results
		for (DataflowOutputPort dataflowOutputPort : dataflow.getOutputPorts()) {
			addOutput(dataflowOutputPort.getName(), dataflowOutputPort
					.getDepth(), dataflowOutputPort
					.getDepth(), edits);
		}
	}

	public Dataflow getNestedDataflow() {
		return getConfiguration();
	}

}
