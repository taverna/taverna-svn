package net.sf.taverna.t2.provenance;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.jdom.Element;

public class ActivityProvenanceItem implements ProvenanceItem {

	private Activity<?> activity;
	private IterationProvenanceItem iterationProvenanceItem;
	
	public ActivityProvenanceItem(Activity<?> activity) {
		super();
		this.activity = activity;
	}

	public Element getAsXML(DataFacade dataFacade) {
		Element result = new Element("activity");
		result.setAttribute("id",getActivityID()); 
		if (iterationProvenanceItem!=null) result.addContent(iterationProvenanceItem.getAsXML(dataFacade));
		return result;
	}

	private String getActivityID() {
		return activity.getClass().getSimpleName();
	}

	public void setIterationProvenanceItem(
			IterationProvenanceItem iterationProvenanceItem) {
		this.iterationProvenanceItem = iterationProvenanceItem;
	}
	
	public IterationProvenanceItem getIterationProvenanceItem() {
		return iterationProvenanceItem;
	}

	public String getAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEventType() {
		// TODO Auto-generated method stub
		return SharedVocabulary.ACTIVITY_EVENT_TYPE;
	}
}
