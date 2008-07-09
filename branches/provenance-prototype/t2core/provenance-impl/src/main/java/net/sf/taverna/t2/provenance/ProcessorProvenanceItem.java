package net.sf.taverna.t2.provenance;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;

import org.jdom.Element;

public class ProcessorProvenanceItem implements ProvenanceItem {

	private ActivityProvenanceItem activityProvenanceItem;
	private String processorID;
	
	public ProcessorProvenanceItem(String processorID) {
		super();
		this.processorID = processorID;
	}


	public Element getAsXML(DataFacade dataFacade) {
		Element result = new Element("processor");
		result.setAttribute("id",processorID);
		if (activityProvenanceItem!=null) {
			result.addContent(activityProvenanceItem.getAsXML(dataFacade));
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
		// TODO Auto-generated method stub
		return null;
	}


	public String getProcessorID() {
		return processorID;
	}


	public String getEventType() {
		return SharedVocabulary.PROCESSOR_EVENT_TYPE;
	}

}
