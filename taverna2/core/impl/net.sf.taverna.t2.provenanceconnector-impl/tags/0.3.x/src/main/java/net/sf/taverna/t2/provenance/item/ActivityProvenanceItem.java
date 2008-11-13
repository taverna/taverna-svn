package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.jdom.Element;

public class ActivityProvenanceItem implements ProvenanceItem {

	private Activity<?> activity;
	private IterationProvenanceItem iterationProvenanceItem;
	private String processId;
	private String identifier;
	private String parentId;
	
	public ActivityProvenanceItem(Activity<?> activity) {
		super();
		this.activity = activity;
	}

	public Element getAsXML(ReferenceService referenceService) {
		Element result = new Element("activity");
		result.setAttribute("id",getActivityID()); 
		result.setAttribute("identifier", this.identifier);
		result.setAttribute("processID", this.processId);
		result.setAttribute("parent", this.parentId);
//		if (iterationProvenanceItem!=null) result.addContent(iterationProvenanceItem.getAsXML(referenceService));
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
		this.identifier = identifier;
		
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
		
	}

	public String getProcessId() {
		// TODO Auto-generated method stub
		return processId;
	}

}
