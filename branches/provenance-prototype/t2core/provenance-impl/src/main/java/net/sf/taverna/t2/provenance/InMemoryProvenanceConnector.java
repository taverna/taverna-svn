package net.sf.taverna.t2.provenance;

import org.apache.log4j.Logger;

public class InMemoryProvenanceConnector implements ProvenanceConnector {

	private static Logger logger = Logger.getLogger(InMemoryProvenanceConnector.class);
	
	private String provenance;
	
	public String getProvenance() {
		return provenance;
	}

	public void saveProvenance(String annotation) {
		System.out.println(annotation);
		provenance = annotation;
	}

}
