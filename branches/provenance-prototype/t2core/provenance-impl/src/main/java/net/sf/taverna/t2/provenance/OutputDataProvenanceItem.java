package net.sf.taverna.t2.provenance;

import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public class OutputDataProvenanceItem extends DataProvenanceItem {

	protected boolean isInput() {
		return false;
	}
	
	public OutputDataProvenanceItem(Map<String, EntityIdentifier> dataMap) {
		super(dataMap);
	}

	public String getAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEventType() {
		return SharedVocabulary.OUTPUTDATA_EVENT_TYPE;
	}

}
