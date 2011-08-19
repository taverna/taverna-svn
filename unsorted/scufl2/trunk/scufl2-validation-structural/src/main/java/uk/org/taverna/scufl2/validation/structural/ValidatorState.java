/**
 * 
 */
package uk.org.taverna.scufl2.validation.structural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.port.Port;
import uk.org.taverna.scufl2.api.port.ReceiverPort;
import uk.org.taverna.scufl2.api.port.SenderPort;
import uk.org.taverna.scufl2.api.profiles.Profile;

/**
 * @author alanrw
 *
 */
public class ValidatorState {

	private WorkflowBundle workflowBundle;
	private Workflow workflow;
	private Profile profile;
	private Processor processor;
	private Map<DataLink, Integer> dataLinkResolvedDepthMap = new HashMap<DataLink, Integer> ();
	private Map<SenderPort, List<DataLink>> senderDataLinkMap = new HashMap<SenderPort, List<DataLink>>();
	private Map<ReceiverPort, List<DataLink>> receiverDataLinkMap = new HashMap<ReceiverPort, List<DataLink>>();
	private Map<Port, Integer> portResolvedDepthMap = new HashMap<Port, Integer> ();
	private StructuralValidationEventListener eventListener = new DefaultStructuralValidationEventListener();

	public void setWorkflowBundle(WorkflowBundle workflowBundle) {
		this.workflowBundle = workflowBundle;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public WorkflowBundle getWorkflowBundle() {
		return workflowBundle;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setDataLinkResolvedDepth(DataLink dl, Integer i) {
		dataLinkResolvedDepthMap.put(dl, i);
	}
	
	public Integer getDataLinkResolvedDepth(DataLink dl) {
		return dataLinkResolvedDepthMap.get(dl);
	}

	public void rememberDataLinkSender(DataLink dl) {
		SenderPort sender = dl.getReceivesFrom();
		if (sender != null) {
			if (!senderDataLinkMap.containsKey(sender)) {
				senderDataLinkMap.put(sender, new ArrayList<DataLink>());
			}
			senderDataLinkMap.get(sender).add(dl);
		}
	}

	public void rememberDataLinkReceiver(DataLink dl) {
		ReceiverPort receiver = dl.getSendsTo();
		if (receiver != null) {
			if (!receiverDataLinkMap.containsKey(receiver)) {
				receiverDataLinkMap.put(receiver, new ArrayList<DataLink>());
			}
			receiverDataLinkMap.get(receiver).add(dl);
		}
	}

	public List<DataLink> getOutgoingDataLinks(SenderPort iwp) {
		List<DataLink> result = senderDataLinkMap.get(iwp);
		if (result == null) {
			result = Collections.emptyList();
		}
		return result;
	}
	
	public List<DataLink> getIncomingDataLinks(ReceiverPort rp) {
		List<DataLink> result = receiverDataLinkMap.get(rp);
		if (result == null) {
			result = Collections.emptyList();
		}
		return result;		
	}
	
	public DataLink getMainIncomingDataLink(ReceiverPort rp) {
		List<DataLink> incomingLinks = getIncomingDataLinks(rp);
		if (incomingLinks.isEmpty()) {
			return null;
		}
		if (incomingLinks.size() == 1) {
			return incomingLinks.get(0);
		}
		for (DataLink dl : incomingLinks) {
			if (dl.getMergePosition() == 0) {
				return dl;
			}
		}
		return null;
	}
	
	public boolean isMergedPort(ReceiverPort rp) {
		return (getIncomingDataLinks(rp).size() > 1);
	}

	public void setPortResolvedDepth(Port owp, Integer i) {
		portResolvedDepthMap.put(owp, i);
	}
	
	public Integer getPortResolvedDepth(Port p) {
		return portResolvedDepthMap.get(p);
	}

	public void setProcessor(Processor p) {
		this.processor = p;
	}

	public Processor getProcessor() {
		return this.processor;
	}

	public void setEventListener(StructuralValidationEventListener eventListener) {
		this.eventListener = eventListener;
	}

	public StructuralValidationEventListener getEventListener() {
		return eventListener;
	}

	public void clearWorkflowData() {
		for (DataLink dl : workflow.getDataLinks()) {
			dataLinkResolvedDepthMap.remove(dl);
		}
		for (InputWorkflowPort iwp : workflow.getInputPorts()) {
			senderDataLinkMap.remove(iwp);
			portResolvedDepthMap.remove(iwp);
		}
		for (Processor p : workflow.getProcessors()) {
			for (InputProcessorPort ipp : p.getInputPorts()) {
				portResolvedDepthMap.remove(ipp);
				receiverDataLinkMap.remove(ipp);
			}
			for (OutputProcessorPort opp : p.getOutputPorts()) {
				portResolvedDepthMap.remove(opp);
				senderDataLinkMap.remove(opp);
			}
		}
		for (OutputWorkflowPort owp : workflow.getOutputPorts()) {
			portResolvedDepthMap.remove(owp);
			receiverDataLinkMap.remove(owp);
			
		}
	}

}
