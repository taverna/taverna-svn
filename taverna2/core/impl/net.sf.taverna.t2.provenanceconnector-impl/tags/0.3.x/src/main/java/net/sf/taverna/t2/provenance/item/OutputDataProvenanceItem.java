package net.sf.taverna.t2.provenance.item;

import java.util.Map;

import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

public class OutputDataProvenanceItem extends DataProvenanceItem {

	private String processId;
	private String parentId;
	private String identifier;
	private ReferenceService referenceService;

	protected boolean isInput() {
		return false;
	}
	
	public OutputDataProvenanceItem(Map<String, T2Reference> dataMap, ReferenceService referenceService) {
		super(dataMap, referenceService);
		this.referenceService = referenceService;
	}

	public String getEventType() {
		return SharedVocabulary.OUTPUTDATA_EVENT_TYPE;
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
