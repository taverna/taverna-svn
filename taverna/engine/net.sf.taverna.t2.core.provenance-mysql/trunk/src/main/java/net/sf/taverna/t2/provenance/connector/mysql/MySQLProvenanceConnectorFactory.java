package net.sf.taverna.t2.provenance.connector.mysql;

import org.osgi.service.jndi.JNDIContextManager;

import uk.org.taverna.platform.database.DatabaseManager;

import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;

public class MySQLProvenanceConnectorFactory implements ProvenanceConnectorFactory{

	private DatabaseManager databaseManager;

	public String getConnectorType() {
		return ProvenanceConnectorType.MYSQL;
	}

	public ProvenanceConnector getProvenanceConnector() {
		return new MySQLProvenanceConnector(databaseManager);
	}

	/**
	 * Sets the databaseManager.
	 *
	 * @param databaseManager the new value of databaseManager
	 */
	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

}
