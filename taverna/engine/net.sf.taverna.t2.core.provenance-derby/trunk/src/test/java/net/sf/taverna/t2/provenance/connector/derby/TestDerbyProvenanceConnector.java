package net.sf.taverna.t2.provenance.connector.derby;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.taverna.configuration.database.DatabaseConfiguration;
import uk.org.taverna.configuration.database.impl.DatabaseConfigurationImpl;
import uk.org.taverna.configuration.database.impl.DatabaseManagerImpl;
import uk.org.taverna.configuration.app.impl.ApplicationConfigurationImpl;


public class TestDerbyProvenanceConnector {

	private DatabaseManagerImpl databaseManager;

	@Before
	public void setupDataSource() throws Exception {
		DatabaseConfiguration databaseConfiguration = new DatabaseConfigurationImpl(null);
		DatabaseHelper.setUpDataSource(databaseConfiguration);
		databaseManager = new DatabaseManagerImpl(new ApplicationConfigurationImpl(), databaseConfiguration);
	}

	@Test
	@Ignore
	public void testCreateDataBase() throws Exception {
		DerbyProvenanceConnector connector = new DerbyProvenanceConnector(databaseManager, null);
		Connection con = databaseManager.getConnection();
		try {
			assertFalse(con.prepareStatement("select * from Datalink").execute());
			fail("Query should have failed since table does not exist");
		}
		catch(Exception e) {
			//good
		}
		finally{
			con.close();
		}


		connector.createDatabase();

		con = databaseManager.getConnection();
		try {
			assertTrue(con.prepareStatement("select * from Datalink").execute());
		}
		catch(Exception e) {
			fail("Query should have succeeded since table exists");
		}
		finally{
			con.close();
		}

		connector.createDatabase();

		con = databaseManager.getConnection();
		try {
			assertTrue(con.prepareStatement("select * from Datalink").execute());
		}
		catch(Exception e) {
			fail("Query should have succeeded since table exists");
		}
		finally{
			con.close();
		}

	}
}
