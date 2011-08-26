package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabularly.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.Element;

public class ProcessorProvenanceItem implements ProvenanceItem {

	private ActivityProvenanceItem activityProvenanceItem;
	private String processorID;
	
	public ProcessorProvenanceItem(String processorID) {
		super();
		this.processorID = processorID;
	}


	public Element getAsXML(ReferenceService referenceService) {
		Element result = new Element("processor");
		result.setAttribute("id",processorID);
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
		return processorID;
	}


	public String getEventType() {
		return SharedVocabulary.PROCESSOR_EVENT_TYPE;
	}


	public void setProcessorID(String processorID) {
		this.processorID = processorID;
	}

}
