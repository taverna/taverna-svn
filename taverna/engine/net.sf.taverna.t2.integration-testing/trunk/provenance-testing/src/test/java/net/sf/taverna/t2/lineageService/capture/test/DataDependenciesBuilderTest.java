/**
 * 
 */
package net.sf.taverna.t2.lineageService.capture.test;

import static org.junit.Assert.*;

import java.sql.SQLException;

import net.sf.taverna.t2.provenance.lineageservice.mysql.DataDependenciesBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author paolo
 *
 */
public class DataDependenciesBuilderTest {

	/**
	 * Test method for {@link net.sf.taverna.t2.provenance.lineageservice.mysql.DataDependenciesBuilder#buildDD()}.
	 */
	@Test
	public final void testBuildDD() {
		
		String DB_URL_LOCAL = "localhost";  // URL of database server //$NON-NLS-1$
		String DB_USER = "paolo";                        // database user id //$NON-NLS-1$
		String DB_PASSWD = "riccardino"; //$NON-NLS-1$

		String location = DB_URL_LOCAL+"/T2Provenance?user="+DB_USER+"&password="+DB_PASSWD; //$NON-NLS-1$ //$NON-NLS-2$

		String jdbcString = "jdbc:mysql://"+location;

		String clearDB = propertiesReader.getString("clearDB");
		
		DataDependenciesBuilder ddBuilder = new DataDependenciesBuilder(jdbcString, clearDB);
				
		try {
			ddBuilder.buildDD();
			
			assertTrue("DONE", true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			fail("caught an exception and died");
		}
	}

}
