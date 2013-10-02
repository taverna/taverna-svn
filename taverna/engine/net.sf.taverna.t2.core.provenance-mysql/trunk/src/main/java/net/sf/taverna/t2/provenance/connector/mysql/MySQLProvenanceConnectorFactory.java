package net.sf.taverna.t2.provenance.connector.mysql;

import uk.org.taverna.configuration.database.DatabaseManager;

import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;

public class MySQLProvenanceConnectorFactory implements ProvenanceConnectorFactory{

	private DatabaseManager databaseManager;
	private XMLSerializer xmlSerializer;

	public String getConnectorType() {
		return ProvenanceConnectorType.MYSQL;
	}

	public ProvenanceConnector getProvenanceConnector() {
		return new MySQLProvenanceConnector(databaseManager, xmlSerializer);
	}

	/**
	 * Sets the databaseManager.
	 *
	 * @param databaseManager the new value of databaseManager
	 */
	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void setXmlSerializer(XMLSerializer xmlSerializer) {
		this.xmlSerializer = xmlSerializer;
	}

}
