package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabularly.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.jdom.Element;

public class ActivityProvenanceItem implements ProvenanceItem {

	private Activity<?> activity;
	private IterationProvenanceItem iterationProvenanceItem;
	
	public ActivityProvenanceItem(Activity<?> activity) {
		super();
		this.activity = activity;
	}

	public Element getAsXML(ReferenceService referenceService) {
		Element result = new Element("activity");
		result.setAttribute("id",getActivityID()); 
		if (iterationProvenanceItem!=null) result.addContent(iterationProvenanceItem.getAsXML(referenceService));
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
		return null;
	}

	public String getEventType() {
		return SharedVocabulary.ACTIVITY_EVENT_TYPE;
	}

	public Activity<?> getActivity() {
		return activity;
	}

	public void setActivity(Activity<?> activity) {
		this.activity = activity;
	}

}
