/**
 * 
 */
package net.sf.taverna.t2.lineageService.analysis.test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.lineageService.capture.test.testFiles;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResult;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.provenance.lineageservice.LineageSQLQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author paolo
 * this test assumes that the DB has been populated in the capture phase
 * 
 *
 */
public class testSimpleLineageQuery {

	private String wfInstance = null;
	private ProvenanceQuery pq = null;
	private String location;
	private String pname;
	private String vname;
	private String iteration;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		String DB_URL_LOCAL = testFiles.getString("dbhost");  // URL of database server //$NON-NLS-1$
		String DB_USER = testFiles.getString("dbuser");                        // database user id //$NON-NLS-1$
		String DB_PASSWD = testFiles.getString("dbpassword"); //$NON-NLS-1$
		  
		location = DB_URL_LOCAL+"/T2Provenance?user="+DB_USER+"&password="+DB_PASSWD; //$NON-NLS-1$ //$NON-NLS-2$

		String jdbcString = "jdbc:mysql://"+location;
		
		pq = new MySQLProvenanceQuery();
		pq.setDbURL(jdbcString);
		
		// target proc
		pname = AnalysisTestFiles.getString("query.pname");
		if (pname == null) {
			fail("no target processor in properties file AnalysisTestFiles.properties");			
		}
		
		// target var
		vname = AnalysisTestFiles.getString("query.vname");
		if (vname == null) {
			fail("no target variable in properties file AnalysisTestFiles.properties");			
		}
		
		// target iteration
		iteration = AnalysisTestFiles.getString("query.iteration");
		if (iteration.equals("!query.iteration!")) {
			System.out.println("no target iteration in properties file AnalysisTestFiles.properties -- assuming []");
			iteration = "[]";
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceQuery#simpleLineageQuery(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testSimpleLineageQuery() {
		
		//////////////
		// set the run instances (scope)
		//////////////
		String WFID = null;  // TODO only support one instance query at this time
		ArrayList<String> instances = null;
		try {
			instances = (ArrayList<String>) pq.getWFInstanceIDs();
		} catch (SQLException e) {
			e.printStackTrace();
			fail("SQL exception");
		}  // ordered by timestamp

		if (instances.size()>0)  {  
			wfInstance = instances.get(0);
			System.out.println("instance "+WFID);
		} else {
			assertFalse("FATAL: no wfinstances in DB -- terminating", instances.size() == 0);
		}

		
		LineageSQLQuery lq = pq.simpleLineageQuery(wfInstance, pname, vname, iteration);
		
		LineageQueryResult result = null;
		
		try {
			result = pq.runLineageQuery(lq, false);  // false -> do not return actual data values
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Query result: ");
		
		List<LineageQueryResultRecord> records = result.getRecords();
		for (LineageQueryResultRecord record:records) {
			System.out.println("******\n");
			System.out.println("processor: "+record.getPname());
			System.out.println("port name: "+record.getVname()+ "["+record.getIteration()+"] = "+ record.getValue());
		}
		
		
		assertTrue("test returned non-null result", result != null);
	}

}
