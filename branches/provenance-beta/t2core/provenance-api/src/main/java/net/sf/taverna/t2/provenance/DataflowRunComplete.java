package net.sf.taverna.t2.provenance;

import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.Element;

public class DataflowRunComplete implements ProvenanceItem {

	public String getAsString() {
		return "<DataflowRunComplete/>";
	}

	public Element getAsXML(ReferenceService referenceService) {
		return null;
	}

	public String getEventType() {
		return SharedVocabulary.END_WORKFLOW_EVENT_TYPE;
	}

}
