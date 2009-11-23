package net.sf.taverna.t2.provenance.connector;

import java.io.File;
import java.sql.Connection;

import net.sf.taverna.t2.provenance.api.ProvenanceAccess;

public class DatabaseHelper {

	public static void setUpDataSource() throws Exception {
		File tempDir = File.createTempFile("provenance-test-derby", "");
		if (tempDir.exists() && !tempDir.isDirectory()) {
			tempDir.delete();
			tempDir.mkdir();
		}
		
		String jdbcUrl= "jdbc:derby:"+tempDir.toString()+"/database;create=true;upgrade=true";
		ProvenanceAccess.initDataSource("org.apache.derby.jdbc.EmbeddedDriver", jdbcUrl);
	}
	
	public static Connection getConnection() throws Exception {
		return JDBCConnector.getConnection();
	}
}
