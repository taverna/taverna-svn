package net.sf.taverna.t2.provenance.connector.mysql;

import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;

public class MySQLProvenanceConnectorFactory implements ProvenanceConnectorFactory{

	public String getConnectorType() {
		return ProvenanceConnectorType.MYSQL;
	}

	public ProvenanceConnector getProvenanceConnector() {
		return new MySQLProvenanceConnector();
	}

}
