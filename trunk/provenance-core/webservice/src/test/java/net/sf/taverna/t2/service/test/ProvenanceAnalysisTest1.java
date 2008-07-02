/**
 * 
 */
package net.sf.taverna.t2.service.test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;

import net.sf.taverna.t2.service.ProvenanceAnalysis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.tools.javac.util.List;

/**
 * @author paolo
 *
 */
public class ProvenanceAnalysisTest1 {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.ProvenanceAnalysis#getLineageTree(java.lang.String)}.
	 * gets all available wfInstanceIDs from the DB, picks one at random to use as the test scope<br/>
	 * then runs the lineage tree analysis on that instance
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Test
	public final void testGetLineageTree() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
				
		ProvenanceAnalysis pa = new ProvenanceAnalysis();
		
		ArrayList<String> IDs = (ArrayList) pa.getWFInstanceIDs();  // ordered by timestamp
		
		if (IDs.size()>0)  {
			
			String WFID = IDs.get(0);
			
			System.out.println("computing lineage tree for all outputs on instance "+WFID);
			
			pa.getLineageTree(WFID);
			
			assertTrue("lineage tree should have been printed above", true);
			
			
		}
		
		
	}

}
