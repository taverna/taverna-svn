package net.sf.taverna.t2.provenance;

import java.util.Map;

import net.sf.taverna.t2.reference.T2Reference;

public class OutputDataProvenanceItem extends DataProvenanceItem {

	protected boolean isInput() {
		return false;
	}
	
	public OutputDataProvenanceItem(Map<String, T2Reference> dataMap) {
		super(dataMap);
	}

	public String getEventType() {
		return SharedVocabulary.OUTPUTDATA_EVENT_TYPE;
	}


}
