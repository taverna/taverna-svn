package net.sf.taverna.t2.provenance.connector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


public class TestDerbyProvenanceConnector {

	@Before
	public void setupDataSource() throws Exception {
		DatabaseHelper.setUpDataSource();
	}
	
	@Test
	public void testCreateDataBase() throws Exception {
		DerbyProvenanceConnector connector = new DerbyProvenanceConnector();
		Connection con = DatabaseHelper.getConnection();
		try {
			assertFalse(con.prepareStatement("select * from Arc").execute());
			fail("Query should have failed since table does not exist");
		}
		catch(Exception e) {
			//good
		}
		finally{
			con.close();
		}
		
		
		connector.createDatabase();
		
		con = DatabaseHelper.getConnection();
		try {
			assertTrue(con.prepareStatement("select * from Arc").execute());			
		}
		catch(Exception e) {
			fail("Query should have succeeded since table exists");
		}
		finally{
			con.close();
		}
		
		connector.createDatabase();
		
		con = DatabaseHelper.getConnection();
		try {
			assertTrue(con.prepareStatement("select * from Arc").execute());			
		}
		catch(Exception e) {
			fail("Query should have succeeded since table exists");
		}
		finally{
			con.close();
		}
		
	}
}
