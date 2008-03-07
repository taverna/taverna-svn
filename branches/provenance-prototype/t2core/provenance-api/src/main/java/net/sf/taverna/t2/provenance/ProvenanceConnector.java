package net.sf.taverna.t2.provenance;

public interface ProvenanceConnector {
	
	public void saveProvenance(String annotation);
	
	public String getProvenance();

}
