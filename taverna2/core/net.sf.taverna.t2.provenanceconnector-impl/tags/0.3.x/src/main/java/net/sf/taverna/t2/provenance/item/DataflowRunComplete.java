package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.Element;

public class DataflowRunComplete implements ProvenanceItem {

	private String processId;
	private String parentId;
	private String identifier;

	public String getAsString() {
		return "<DataflowRunComplete/>";
	}

	public Element getAsXML(ReferenceService referenceService) {
		return null;
	}

	public String getEventType() {
		return SharedVocabulary.END_WORKFLOW_EVENT_TYPE;
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
		// TODO Auto-generated method stub
		
	}

	public void setParentId(String parentId) {
		processId = parentId;
		// TODO Auto-generated method stub
		
	}
	public String getProcessId() {
		// TODO Auto-generated method stub
		return processId;
	}

}
