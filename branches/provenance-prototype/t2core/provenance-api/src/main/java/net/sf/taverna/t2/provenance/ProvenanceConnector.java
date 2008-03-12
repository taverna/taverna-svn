package net.sf.taverna.t2.provenance;

import java.util.List;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;

public interface ProvenanceConnector {
	
	public List<ProvenanceItem> getProvenanceCollection();

	public void store(DataFacade dataFacade);

}
