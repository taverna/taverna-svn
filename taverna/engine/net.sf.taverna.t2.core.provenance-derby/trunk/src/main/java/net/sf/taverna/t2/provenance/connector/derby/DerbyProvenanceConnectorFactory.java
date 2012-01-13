package net.sf.taverna.t2.provenance.connector.derby;

import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;

public class DerbyProvenanceConnectorFactory implements ProvenanceConnectorFactory{

	public ProvenanceConnector getProvenanceConnector() {
		return new DerbyProvenanceConnector();
	}

	public String getConnectorType() {
		return ProvenanceConnectorType.DERBY;
	}

}
