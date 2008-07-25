package net.sf.taverna.t2.provenance;

import java.util.Map;

import net.sf.taverna.t2.reference.T2Reference;

public class InputDataProvenanceItem extends DataProvenanceItem {

	protected boolean isInput() {
		return true;
	}
	
	public InputDataProvenanceItem(Map<String, T2Reference> dataMap) {
		super(dataMap);
	}

	public String getEventType() {
		return SharedVocabulary.INPUTDATA_EVENT_TYPE;
	}

	public String getAsString() {
		// TODO Auto-generated method stub
		return null;
	}

}
