package net.sf.taverna.t2.provenance.connector.derby;

import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import uk.org.taverna.configuration.database.DatabaseManager;

public class DerbyProvenanceConnectorFactory implements ProvenanceConnectorFactory{

	private DatabaseManager databaseManager;
	private XMLSerializer xmlSerializer;

	public ProvenanceConnector getProvenanceConnector() {
		return new DerbyProvenanceConnector(databaseManager, xmlSerializer);
	}

	public String getConnectorType() {
		return ProvenanceConnectorType.DERBY;
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
