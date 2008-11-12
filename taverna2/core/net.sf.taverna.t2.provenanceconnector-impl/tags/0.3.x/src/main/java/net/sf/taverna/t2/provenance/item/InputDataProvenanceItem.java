package net.sf.taverna.t2.provenance.item;

import java.util.Map;

import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

public class InputDataProvenanceItem extends DataProvenanceItem {

	private String processId;
	private String identifier;
	private String parentId;
	private ReferenceService referenceService;

	protected boolean isInput() {
		return true;
	}

	public InputDataProvenanceItem(Map<String, T2Reference> dataMap,
			ReferenceService referenceService) {
		super(dataMap, referenceService);
		this.referenceService = referenceService;
	}

	public String getEventType() {
		return SharedVocabulary.INPUTDATA_EVENT_TYPE;
	}

	public String getAsString() {
		// TODO Auto-generated method stub
		return null;
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
		this.parentId = parentId;
		// TODO Auto-generated method stub

	}

	public String getProcessId() {
		// TODO Auto-generated method stub
		return processId;
	}

}
