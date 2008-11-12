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
package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.item.ActivityProvenanceItem;
import net.sf.taverna.t2.provenance.item.ErrorProvenanceItem;
import net.sf.taverna.t2.provenance.item.InputDataProvenanceItem;
import net.sf.taverna.t2.provenance.item.IterationProvenanceItem;
import net.sf.taverna.t2.provenance.item.OutputDataProvenanceItem;
import net.sf.taverna.t2.provenance.item.ProcessProvenanceItem;
import net.sf.taverna.t2.provenance.item.ProcessorProvenanceItem;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.item.WorkflowProvenanceItem;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchCompletionEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchJobQueueEvent;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchResultEvent;

/**
 * Sits above the {@link Invoke} layer and collects information about the
 * current workflow run to be stored by the {@link ProvenanceConnector}.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland-Reyes
 * 
 */
public class IntermediateProvenance extends AbstractDispatchLayer<String> {

	private ProvenanceConnector connector;

	Map<String, Map<String, IterationProvenanceItem>> processToIndexes = new HashMap<String, Map<String, IterationProvenanceItem>>();

	private WorkflowProvenanceItem workflowItem;

	public void configure(String o) {
	}

	@Override
	public void finishedWith(String owningProcess) {
	}

	public String getConfiguration() {
		return null;
	}

	protected Map<String, IterationProvenanceItem> getIndexesByProcess(
			String owningProcess) {
		synchronized (processToIndexes) {
			Map<String, IterationProvenanceItem> indexes = processToIndexes
					.get(owningProcess);
			if (indexes == null) {
				indexes = new HashMap<String, IterationProvenanceItem>();
				processToIndexes.put(owningProcess, indexes);
			}
			return indexes;
		}
	}

	protected IterationProvenanceItem getIterationProvItem(Event<?> event) {
		String owningProcess = event.getOwningProcess();
		int[] index = event.getIndex();
		String indexStr = indexStr(index);
		Map<String, IterationProvenanceItem> indexes = getIndexesByProcess(owningProcess);
		IterationProvenanceItem iterationProvenanceItem;
		synchronized (indexes) {
			iterationProvenanceItem = indexes.get(indexStr);
			if (iterationProvenanceItem == null) {
				iterationProvenanceItem = new IterationProvenanceItem(index);
				iterationProvenanceItem.setProcessId(owningProcess);

				indexes.put(indexStr, iterationProvenanceItem);
			}
		}
		return iterationProvenanceItem;
	}

	private String indexStr(int[] index) {
		String indexStr = "";
		for (int ind : index) {
			indexStr += ":" + ind;
		}
		return indexStr;
	}

	/**
	 * Create an {@link ErrorProvenanceItem} and send across to the
	 * {@link ProvenanceConnector}
	 */
	@Override
	public void receiveError(DispatchErrorEvent errorEvent) {
		IterationProvenanceItem iterationProvItem = getIterationProvItem(errorEvent);
		// get using errorEvent.getOwningProcess();
		ErrorProvenanceItem errorItem = new ErrorProvenanceItem(errorEvent
				.getCause(), errorEvent.getMessage(), errorEvent
				.getFailureType(), errorEvent.getOwningProcess());
		errorItem.setIdentifier(UUID.randomUUID().toString());
		errorItem.setParentId(iterationProvItem.getIdentifier());
		// iterationProvItem.setErrorItem(errorItem);
		// FIXME don't need to add to the processor item earlier
		getConnector().addProvenanceItem(errorItem);
		super.receiveError(errorEvent);
	}

	/**
	 * Create the {@link ProvenanceItem}s and send them all across to the
	 * {@link ProvenanceConnector} except for the
	 * {@link IterationProvenanceItem}, this one is told what it's inputs are
	 * but has to wait until the results are received before being sent across.
	 * Each item has a unique identifier and also knows who its parent item is
	 */
	@Override
	public void receiveJob(DispatchJobEvent jobEvent) {
		// FIXME do we need this ProcessProvenanceItem?
		ProcessProvenanceItem provenanceItem;
		provenanceItem = new ProcessProvenanceItem(jobEvent.getOwningProcess());
		provenanceItem.setIdentifier(UUID.randomUUID().toString());
		provenanceItem.setParentId(workflowItem.getIdentifier());
		ProcessorProvenanceItem processorProvItem;
		processorProvItem = new ProcessorProvenanceItem(jobEvent
				.getOwningProcess());
		processorProvItem.setIdentifier(UUID.randomUUID().toString());
		processorProvItem.setParentId(provenanceItem.getIdentifier());
		provenanceItem.setProcessId(jobEvent.getOwningProcess());
		getConnector().addProvenanceItem(provenanceItem);
		getConnector().addProvenanceItem(processorProvItem);

		IterationProvenanceItem iterationProvItem = null;
		iterationProvItem = new IterationProvenanceItem(jobEvent.getIndex());
		iterationProvItem.setIdentifier(UUID.randomUUID().toString());
		ReferenceService referenceService = jobEvent.getContext()
				.getReferenceService();

		InputDataProvenanceItem inputDataItem = new InputDataProvenanceItem(
				jobEvent.getData(), referenceService);
		inputDataItem.setIdentifier(UUID.randomUUID().toString());
		inputDataItem.setParentId(iterationProvItem.getIdentifier());
		inputDataItem.setProcessId(jobEvent.getOwningProcess());
		iterationProvItem.setInputDataItem(inputDataItem);
		iterationProvItem.setIteration(jobEvent.getIndex());
		iterationProvItem.setProcessId(jobEvent.getOwningProcess());

		for (Activity<?> activity : jobEvent.getActivities()) {
			if (activity instanceof AsynchronousActivity) {
				ActivityProvenanceItem activityProvItem = new ActivityProvenanceItem(
						activity);
				activityProvItem.setIdentifier(UUID.randomUUID().toString());
				iterationProvItem.setParentId(activityProvItem.getIdentifier());
				// getConnector().addProvenanceItem(iterationProvItem);
				activityProvItem.setParentId(processorProvItem.getIdentifier());
				// processorProvItem.setActivityProvenanceItem(activityProvItem);
				activityProvItem.setProcessId(jobEvent.getOwningProcess());
				// activityProvItem.setIterationProvenanceItem(iterationProvItem);
				getConnector().addProvenanceItem(activityProvItem);
				break;
			}
		}
		getIndexesByProcess(jobEvent.getOwningProcess()).put(
				indexStr(jobEvent.getIndex()), iterationProvItem);
		super.receiveJob(jobEvent);
	}

	@Override
	public void receiveJobQueue(DispatchJobQueueEvent jobQueueEvent) {
		// TODO Auto-generated method stub
		super.receiveJobQueue(jobQueueEvent);
	}

	/**
	 * Populate an {@link OutputDataProvenanceItem} with the results and attach
	 * it to the appropriate {@link IterationProvenanceItem}. Then send the
	 * {@link IterationProvenanceItem} across to the {@link ProvenanceConnector}
	 */
	@Override
	public void receiveResult(DispatchResultEvent resultEvent) {
		// FIXME use the connector from the result event context
		IterationProvenanceItem iterationProvItem = getIterationProvItem(resultEvent);
		ReferenceService referenceService = resultEvent.getContext()
				.getReferenceService();

		OutputDataProvenanceItem outputDataItem = new OutputDataProvenanceItem(
				resultEvent.getData(), referenceService);
		outputDataItem.setIdentifier(UUID.randomUUID().toString());
		outputDataItem.setProcessId(resultEvent.getOwningProcess());
		outputDataItem.setParentId(iterationProvItem.getIdentifier());
		iterationProvItem.setOutputDataItem(outputDataItem);

		getConnector().addProvenanceItem(iterationProvItem);
		// getConnector().addProvenanceItem(outputDataItem);
		super.receiveResult(resultEvent);
	}

	@Override
	public void receiveResultCompletion(DispatchCompletionEvent completionEvent) {
		// TODO Auto-generated method stub
		super.receiveResultCompletion(completionEvent);
	}

	/**
	 * Tell this layer what {@link ProvenanceConnector} implementation is being
	 * used to capture the {@link ProvenanceItem}s. NOTE: should probably use
	 * the connector from the result events context where possible
	 * 
	 * @param connector
	 */
	public void setConnector(ProvenanceConnector connector) {
		this.connector = connector;
	}

	public ProvenanceConnector getConnector() {
		return connector;
	}

	/**
	 * So that the {@link ProvenanceItem}s know which {@link Dataflow} has been
	 * enacted this layer has to know about the {@link WorkflowProvenanceItem}
	 * 
	 * @param workflowItem
	 */
	public void setWorkflow(WorkflowProvenanceItem workflowItem) {
		this.workflowItem = workflowItem;
	}

}
