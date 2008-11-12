package net.sf.taverna.t2.provenance.connector;

import java.util.List;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.reference.ReferenceService;

public interface ProvenanceConnector {
	
	public List<ProvenanceItem> getProvenanceCollection();

	public void store(ProvenanceItem provenanceItem);
	
	public void createDatabase();
	
	public void deleteDatabase();

	public void setDBLocation(String location);
	
	public void addProvenanceItem(ProvenanceItem provenanceItem);
	
	public void setReferenceService(ReferenceService referenceService);
		
	public ReferenceService getReferenceService();
	/**
	 * The name for this type of provenance connector
	 * @return
	 */
	public String getName();
	
	public void setSessionId(String identifier);

	public String getSessionId();


}
