package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.Element;

public class ProcessorProvenanceItem implements ProvenanceItem {

	private ActivityProvenanceItem activityProvenanceItem;
	private String processorID;
	private String processId;
	private String parentId;
	private String identifier;
	
	public ProcessorProvenanceItem(String processorID) {
		super();
		this.processId = processorID;
	}


	public Element getAsXML(ReferenceService referenceService) {
		Element result = new Element("processor");
//		result.setAttribute("id",processorID);
		result.setAttribute("identifier", this.identifier);
		result.setAttribute("processID", this.processId);
		result.setAttribute("parent", this.parentId);
		if (activityProvenanceItem!=null) {
			result.addContent(activityProvenanceItem.getAsXML(referenceService));
		}
		
		return result;
	}


	public void setActivityProvenanceItem(
			ActivityProvenanceItem activityProvenanceItem) {
		this.activityProvenanceItem = activityProvenanceItem;
	}
	
	public ActivityProvenanceItem getActivityProvenanceItem() {
		return activityProvenanceItem;
	}


	public String getAsString() {
		return null;
	}


	public String getProcessorID() {
		return identifier;
	}


	public String getEventType() {
		return SharedVocabulary.PROCESSOR_EVENT_TYPE;
	}


	public void setProcessorID(String processorID) {
		this.processorID = processorID;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	public String getParentId() {
		// TODO Auto-generated method stub
		return parentId;
	}



	public void setIdentifier(String identifier) {
		String facadeId = processId.split(":")[0];
		//facade0(abcd-qefqw-sdssvsv-svf)
		this.identifier = facadeId+ "(" + identifier + ")";
		// TODO Auto-generated method stub
		
	}



	public void setParentId(String parentId) {
		this.parentId = parentId;
		// TODO Auto-generated method stub
		
	}


	public String getProcessId() {
		// TODO Auto-generated method stub
		return processId;
	}

}
