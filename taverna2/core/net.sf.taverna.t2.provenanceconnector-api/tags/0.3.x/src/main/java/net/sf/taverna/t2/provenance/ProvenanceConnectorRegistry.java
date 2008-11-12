package net.sf.taverna.t2.provenance;

import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.spi.SPIRegistry;

public class ProvenanceConnectorRegistry extends SPIRegistry<ProvenanceConnector>{
	
	private static ProvenanceConnectorRegistry instance;

	protected ProvenanceConnectorRegistry() {
		super(ProvenanceConnector.class);
	}
	
	public static synchronized ProvenanceConnectorRegistry getInstance() {
		
		if (instance == null) {
			instance = new ProvenanceConnectorRegistry();
		}
		return instance;
	}

}
