package net.sf.taverna.t2.provenance.connector;

import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;

public class DerbyProvenanceConnectorFactory implements ProvenanceConnectorFactory{

	public ProvenanceConnector getProvenanceConnector() {
		return new DerbyProvenanceConnector();
	}

	public String getConnectorType() {
		return ProvenanceConnectorType.DERBY;
	}

}
