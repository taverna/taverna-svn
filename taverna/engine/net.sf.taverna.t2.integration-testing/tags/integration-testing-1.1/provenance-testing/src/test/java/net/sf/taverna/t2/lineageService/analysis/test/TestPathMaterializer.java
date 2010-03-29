/**
 * 
 */
package net.sf.taverna.t2.lineageService.analysis.test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.lineageService.capture.test.propertiesReader;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.mysql.PathMaterializer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * materialize all paths from p1 to p2 as a set of pairs (p1,p2).
 * @author paolo
 *
 */
public class TestPathMaterializer {

	private String dataflowRef = null;
	private MySQLProvenanceQuery pq = null;
	
	private PathMaterializer pm =  null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		String DB_URL_LOCAL = propertiesReader.getString("dbhost");  // URL of database server //$NON-NLS-1$
		String DB_USER = propertiesReader.getString("dbuser");                        // database user id //$NON-NLS-1$
		String DB_PASSWD = propertiesReader.getString("dbpassword"); //$NON-NLS-1$
		  
		String location = DB_URL_LOCAL+"/T2Provenance?user="+DB_USER+"&password="+DB_PASSWD; //$NON-NLS-1$ //$NON-NLS-2$

		String jdbcString = "jdbc:mysql://"+location;

		pm = new PathMaterializer(jdbcString);
		
		//////////////
		// use the workflow corresponding to the latest executed instance
		//////////////
		List<String> executions = null;
		try {
			executions = (List<String>) pm.getPq().getWFNamesByTime();
		} catch (SQLException e) {
			e.printStackTrace();
			fail("SQL exception");
		}  // ordered by timestamp

		if (executions.size()>0)  {  
			dataflowRef = executions.get(0);
			System.out.println("dataflowRef "+dataflowRef);
		} else {
			assertFalse("FATAL: no wfinstances in DB -- terminating", executions.size() == 0);
		}
	}

	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.provenance.lineageservice.mysql.PathMaterializer#materializePathPairs(java.lang.String)}.
	 */
	@Test
	public final void testMaterializePathPairs() {
		
		try {
			Map<String, Set<String>> canBeReachedFrom = pm.materializePathPairs(dataflowRef);
			
			System.out.println("*** reachability map:  ***");
			for (Map.Entry<String, Set<String>>  entry: canBeReachedFrom.entrySet()) {
				
				System.out.println(entry.getKey()+" can be reached from:");
				for (String s:entry.getValue()) { System.out.println("\t"+s); }
								
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(true);
	}

}
