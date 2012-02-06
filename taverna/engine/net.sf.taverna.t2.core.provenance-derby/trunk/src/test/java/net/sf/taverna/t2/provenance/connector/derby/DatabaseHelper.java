package net.sf.taverna.t2.provenance.connector.derby;

import java.io.File;

import uk.org.taverna.platform.database.DatabaseConfiguration;

public class DatabaseHelper {

	public static void setUpDataSource(DatabaseConfiguration databaseConfiguration) throws Exception {
		File tempDir = File.createTempFile("provenance-test-derby", "");
		if (tempDir.exists() && !tempDir.isDirectory()) {
			tempDir.delete();
			tempDir.mkdir();
		}

		String jdbcUrl= "jdbc:derby:"+tempDir.toString()+"/database;create=true;upgrade=true";
		databaseConfiguration.setJDBCUri(jdbcUrl);
		databaseConfiguration.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
	}

}
