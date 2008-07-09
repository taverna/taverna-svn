/**
 * 
 */
package net.sf.taverna.t2.service.test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sf.taverna.t2.service.ProvenanceAnalysis;
import net.sf.taverna.t2.service.ProvenanceQuery;
import net.sf.taverna.t2.service.util.Arc;
import net.sf.taverna.t2.service.util.Processor;
import net.sf.taverna.t2.service.util.Var;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author paolo
 *
 */
public class ProvenanceAnalysisTest1 {

	private static final String annotationsFileName = null;


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

	
	@Test
	public final void testComputeLineagePaths() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		String[] annotationFiles = 
		{ "webservice/src/test/resources/simple_workflow2_with_2_inputs.xml",  //0
		  "webservice/src/test/resources/test_iterate_list_of_lists.xml",  //1
		  "webservice/src/test/resources/test1.annot.xml",  //2
		  "webservice/src/test/resources/test2.annot.xml",  //3
		  "webservice/src/test/resources/test3.annot.xml",  //4
		  "webservice/src/test/resources/test4.annot.xml",  //5
		  "webservice/src/test/resources/test5.annot.xml",  //6
		  "webservice/src/test/resources/test6.annot.xml"};  //7

		
		String proc = "_OUTPUT_"; // we test from the outputs of the workflow
		
		Set<String> selectedProcessors = new HashSet<String>();

		// this is what we certainly want to test
		int targetIteration = 0;
		String targetProc = "P2";
		
		selectedProcessors.add(targetProc);
		
		// do this for one wfInstances -- there should actually be only one if clear() is used
		ProvenanceAnalysis pa = new ProvenanceAnalysis();
		
		pa.setAnnotationFile(annotationFiles[5]);
		
		ArrayList<String> IDs = (ArrayList<String>) pa.getWFInstanceIDs();  // ordered by timestamp
		
		if (IDs.size()>0)  {
			
			String WFID = IDs.get(0);
			
			System.out.println("computing lineage paths on instance "+WFID);
			
			// get outputs for this workflow
			// retrieve all Arcs ending with (var,proc)
			Map<String, String>  varsQueryConstraints = new HashMap<String, String>();

			varsQueryConstraints.put("wfInstanceRef", WFID);
			varsQueryConstraints.put("PNameRef", proc);  
			
			ProvenanceQuery pq = new ProvenanceQuery();		
			
			List<Var> outVars = pq.getVars(varsQueryConstraints);
			
			if (outVars.isEmpty()) {
				assertFalse("no output vars to trace -- terminating", true);
			}
			
			// collect all processors and select a random subset
			Map<String, String>  procConstraints = new HashMap<String, String>();

			procConstraints.put("wfInstanceRef", WFID);

			List<Processor> processors = pq.getProcessors(procConstraints);
			
			Random r = new Random();					
			
			for (int i=0; i<processors.size() / 2; i++) {
				int  n = r.nextInt(processors.size());
				String pname = processors.get(n).getPname();
				selectedProcessors.add(pname);
			}
			
			System.out.println("selected processors:");
			for (String s:selectedProcessors) {
				System.out.print(s+" ");
			}
			System.out.println();
			
			pa.computeLineagePaths(annotationsFileName, WFID, 
								   outVars.get(0).getVName(), 
								   outVars.get(0).getPName(), 
								   targetIteration, 
								   selectedProcessors);
			
			assertTrue("lineage tree should have been printed above", true);
		}
		
		
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


}
