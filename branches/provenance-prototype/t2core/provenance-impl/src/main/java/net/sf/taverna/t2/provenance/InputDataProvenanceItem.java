package net.sf.taverna.t2.provenance;

import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public class InputDataProvenanceItem extends DataProvenanceItem {

	protected boolean isInput() {
		return true;
	}
	
	public InputDataProvenanceItem(Map<String, EntityIdentifier> dataMap) {
		super(dataMap);
	}

}
