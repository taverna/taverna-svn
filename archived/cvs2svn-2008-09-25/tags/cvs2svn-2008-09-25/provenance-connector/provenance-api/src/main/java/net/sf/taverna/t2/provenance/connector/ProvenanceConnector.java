package net.sf.taverna.t2.provenance.connector;

import java.util.List;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.reference.ReferenceService;

public interface ProvenanceConnector {
	
	public List<ProvenanceItem> getProvenanceCollection();

	public void store(ReferenceService referenceService);

}
