package net.sf.taverna.t2.provenance.connector;

import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;

public class MySQLProvenanceConnectorFactory implements ProvenanceConnectorFactory{

	public String getConnectorType() {
		return ProvenanceConnectorType.MYSQL;
	}

	public ProvenanceConnector getProvenanceConnector() {
		return new MySQLProvenanceConnector();
	}

}
