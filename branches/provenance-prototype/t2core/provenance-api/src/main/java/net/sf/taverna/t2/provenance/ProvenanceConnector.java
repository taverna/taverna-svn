package net.sf.taverna.t2.provenance;

import java.util.List;

public interface ProvenanceConnector {
	
	public List<ProvenanceItem> getProvenanceCollection();

	public void store();

}
