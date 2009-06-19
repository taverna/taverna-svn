/**
 * 
 */
package net.sf.taverna.t2.lineageService.generation.test;


import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.t2.lineageService.analysis.test.AnalysisTestFiles;
import net.sf.taverna.t2.lineageService.analysis.test.ProvenanceAnalysisTest;
import net.sf.taverna.t2.lineageService.capture.test.testFiles;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.utils.Arc;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceAnalysis;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author paolo
 *
 */
public class DataflowGeneratorTest {

	private static final String TARGET_FILE_BASE   = Messages.getString("targetFileBase"); //$NON-NLS-1$
	private static final String TARGET_FILE_LINEAR = Messages.getString("targetFileLinear"); //$NON-NLS-1$
	private static final String TARGET_FILE_LARGELIST = Messages.getString("targetFileLargeList"); //$NON-NLS-1$
	private static final String TARGET_FILE_FROM_PROVENANCE = Messages.getString("targetFileFromProvenance"); //$NON-NLS-1$

	private static final String TEMPLATE_FILE = Messages.getString("templateFile"); //$NON-NLS-1$
	private static final String DEFAULT_CHAIN_LEN = "5";
	private static String len = Messages.getString("linearChainLength");
	private static String inputDepthStr = Messages.getString("inputDepth");
	private static String serial = Messages.getString("serial");  // add control links?

	String dataflowName, dataflowID  = null;
	private static int inputDepth = 0;
	private static boolean isSerial = false;

	private static Logger logger = Logger.getLogger(DataflowGeneratorTest.class);

	Element templateDataflowElement = null;

	MySQLProvenanceQuery pq = null;

	static {
		PluginManager.setRepository(ApplicationRuntime.getInstance().getRavenRepository());
		PluginManager.getInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		dataflowName = Messages.getString("dataflowName");
		if (dataflowName == null)  dataflowName = "myFirstLinearDataflow";

		dataflowID = Messages.getString("dataflowID");
		if (dataflowID == null)  dataflowID = "dataflowID32";

		if (len == null)  len = DEFAULT_CHAIN_LEN;

		if (inputDepthStr == null) inputDepth = 0; 
		else inputDepth = Integer.parseInt(inputDepthStr);

		if (serial != null)  
			isSerial = Boolean.parseBoolean(serial);

		String DB_URL_LOCAL = testFiles.getString("dbhost");  // URL of database server //$NON-NLS-1$
		String DB_USER = testFiles.getString("dbuser");                        // database user id //$NON-NLS-1$
		String DB_PASSWD = testFiles.getString("dbpassword"); //$NON-NLS-1$

		String derbyjdbcString = "jdbc:derby:/Users/paolo/Library/Application Support/taverna-2.1-SNAPSHOT-20090511/db";

		String mySQLjdbcString = "jdbc:mysql://" + DB_URL_LOCAL + "/T2Provenance?user="
		+ DB_USER + "&password=" + DB_PASSWD;

		pq = new MySQLProvenanceQuery();	
		pq.setDbURL(mySQLjdbcString);

	}


	// @Test
	public final void generateWorkflowTestBase() throws EditException, SerializationException, IOException, DeserializationException, JDOMException {

		BaseWorkflowGenerator bfg = new BaseWorkflowGenerator();

		Dataflow df = bfg.createEmptyDataflow(dataflowName, dataflowID); //$NON-NLS-1$ //$NON-NLS-2$		
		df = bfg.generateBaseWorkflow(df);
		bfg.writeSerializedDataflow(df, TARGET_FILE_BASE);

		System.out.println("workflow saved to "+TARGET_FILE_BASE);
	}


	@Test
	public final void generateLinearChainTest() throws EditException, SerializationException, IOException, DeserializationException, JDOMException {

		// read chain length
		int length = Integer.parseInt(len);

		System.out.println("generating linear workflow with ");
		System.out.println("input depth: "+inputDepth);
		System.out.println("chain length: "+len);

		LinearChainGenerator lcg = new LinearChainGenerator();
		lcg.setTemplateFileName(TEMPLATE_FILE);

		lcg.setSerial(isSerial);
		System.out.println("isSerial: "+isSerial);

		Dataflow df = lcg.createEmptyDataflow(dataflowName, dataflowID); //$NON-NLS-1$ //$NON-NLS-2$

		// create single input port
		lcg.createInputPort(df, inputDepth, "I1");

		df = lcg.generateLinearWorkflow(df, Messages.getString("linearBlockProcessorName"), length); //$NON-NLS-1$

		// connect this chain to input port I1
		df = lcg.connectGlobalInput(df, "I1", lcg.getFirst(), "X"); //$NON-NLS-1$ //$NON-NLS-2$

		// create output port O
		lcg.createOutputPort(df, "O");

		// connect output
		df = lcg.connectGlobalOutput(df, "O", lcg.getLast(), "Y");  //$NON-NLS-1$ //$NON-NLS-2$

		lcg.writeSerializedDataflow(df, TARGET_FILE_LINEAR);

		System.out.println("workflow saved to "+TARGET_FILE_LINEAR);
	}



	@Test
	public void generateLargeCollectionsWorkflow() throws EditException, DeserializationException, JDOMException, IOException, SerializationException  {

		// read chain length
		int length = Integer.parseInt(len);

		DataflowGenerator dfg = new DataflowGenerator();
		dfg.setTemplateFileName(TEMPLATE_FILE);

		LinearChainGenerator lcg1, lcg2;

		Dataflow df = dfg.createEmptyDataflow(dataflowName, dataflowID); //$NON-NLS-1$ //$NON-NLS-2$

		// create global input port I1
		dfg.createInputPort(df, 0, "I1");

		// create global input port ListSize
		dfg.createInputPort(df, 0, "ListSize");

		// create CROSS2 processor with inputs X1, X2
		Processor listGen = dfg.addSingleProcessor(df, "LISTGEN", "LISTGEN_1");

		//  connect I1 to X1
		df = dfg.connectGlobalInput(df, "I1", listGen, "X1"); //$NON-NLS-1$ //$NON-NLS-2$

		// connect ListSize to X2
		df = dfg.connectGlobalInput(df, "ListSize", listGen, "X2"); //$NON-NLS-1$ //$NON-NLS-2$

		// create fist chain of length <len>
		lcg1 = new LinearChainGenerator();
		lcg1.setTemplateFileName(TEMPLATE_FILE);
		lcg1.setPnameSuffix("_A");

		df = lcg1.generateLinearWorkflow(df, Messages.getString("linearBlockProcessorName"), length); //$NON-NLS-1$

		// connect this chain to LISTGEN:Y1
		df = dfg.connectPorts(df, listGen, "Y1", lcg1.getFirst(), "X"); //$NON-NLS-1$ //$NON-NLS-2$

		if (isSerial) df = dfg.addControlLink(df, listGen, lcg1.getFirst());  // CHECK 

		// create second chain of length <len>
		lcg2 = new LinearChainGenerator();
		lcg2.setTemplateFileName(TEMPLATE_FILE);
		lcg2.setPnameSuffix("_B");

		df = lcg2.generateLinearWorkflow(df, Messages.getString("linearBlockProcessorName"), length); //$NON-NLS-1$

		// connect this chain to LISTGEN:Y1
		df = dfg.connectPorts(df, listGen, "Y2", lcg2.getFirst(), "X"); //$NON-NLS-1$ //$NON-NLS-2$

		if (isSerial) df = dfg.addControlLink(df, listGen, lcg2.getFirst());  // CHECK 

		// create 2TO1 processor
		Processor two2One = dfg.addSingleProcessor(df, "2TO1", "2TO1_FINAL");

		// connect end of first chain to X1 and  of second chain to X2
		df = dfg.connectPorts(df, lcg1.getLast(), "Y", two2One, "X1");
		df = dfg.connectPorts(df, lcg2.getLast(), "Y", two2One, "X2");

//		df = dfg.addControlLink(df, lcg1.getLast(), two2One);  // CHECK 
//		df = dfg.addControlLink(df, lcg2.getLast(), two2One);  // CHECK 

		// create output port O
		dfg.createOutputPort(df, "O");

		// connect Y to output
		df = dfg.connectGlobalOutput(df, "O", two2One, "Y");  //$NON-NLS-1$ //$NON-NLS-2$

		dfg.writeSerializedDataflow(df, TARGET_FILE_LARGELIST);

		System.out.println("workflow saved to "+TARGET_FILE_LARGELIST);

	}




	@Test
	public void generateWFFromProvenanceDB() throws SQLException {

		final String DEFAULT_SELECTED_INSTANCES = "LAST";
		final String DEFAULT_SELECTED_WF = "LAST";

		// wf ID
		String selectedWF = Messages.getString("MOPD.input");
		if (selectedWF == null || selectedWF.contains("!")) {

			// get latest instance, and set the WF to be the corresponding WF for that instance
			List<String> wfNames = pq.getWFNamesByTime();
			if (wfNames.size()==0) { // no instances, terminate
				assertFalse("FATAL: no wfinstances in DB -- terminating", wfNames.size() == 0);
			} else {				
				selectedWF = wfNames.get(0);
			}
		}
		logger.info("reconstructing T2 workflow for OPM account: "+selectedWF);
		generateWFFromProvenanceDB(selectedWF);
	}


	/**
	 * this reads the static portion of the DB to reconstruct a Taverna workflow from it 
	 * @throws SQLException 
	 */
	public void generateWFFromProvenanceDB(String wfRef) throws SQLException {

		DataflowGenerator dfg = new DataflowGenerator();
		dfg.setTemplateFileName(TEMPLATE_FILE);
		Dataflow df = null;

		try {
			df = dfg.createEmptyDataflow(dataflowName, dataflowID);			
		} catch (EditException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //$NON-NLS-1$ //$NON-NLS-2$

		//////////////
		// logic:
		// for each processor in the database, collate all its inputs and all its output ports
		// then create one T2 processor with those ports
		// finally use the Arcs table to connect up all the processors
		//////////////

		Map<String, List<String>> p2Inputs  = new HashMap<String, List<String>>();
		Map<String, List<String>> p2Outputs = new HashMap<String, List<String>>();
		Set<String>  allProcs = new HashSet<String>();
		Map<String, Processor> pname2proc = new HashMap<String, Processor>();

		// read in all Processors -- we need to distinguish Dataflow processors because their input/output
		// logic is inverted
		List<ProvenanceProcessor> dataflowProcs = pq.getProcessors("net.sf.taverna.t2.activities.dataflow.DataflowActivity", wfRef);
		List<String>  dataflowNames = new ArrayList<String>();
		for (ProvenanceProcessor pp:dataflowProcs) { dataflowNames.add(pp.getPname()); }

		// read in all Var for wfRef
		Map<String, String> queryConstraints = new HashMap<String, String>();
		queryConstraints.put("V.wfInstanceRef", wfRef);
		List<Var> allVars = pq.getVars(queryConstraints);

		for (Var v:allVars) {
			logger.debug("var: "+v.getPName()+"/"+v.getVName());
			if (p2Inputs.get(v.getPName()) == null)  p2Inputs.put(v.getPName(), new ArrayList<String>());
			if (p2Outputs.get(v.getPName()) == null) p2Outputs.put(v.getPName(), new ArrayList<String>());

			if (v.isInput()) 
//				if (dataflowNames.contains(v.getPName())) { // invert i/o logic
//				p2Outputs.get(v.getPName()).add(v.getVName());	
//				} else {
				p2Inputs.get(v.getPName()).add(v.getVName());					
//			}				
//			else if (dataflowNames.contains(v.getPName())) { // invert i/o logic
//			p2Inputs.get(v.getPName()).add(v.getVName());
//			} 
			else {
				p2Outputs.get(v.getPName()).add(v.getVName());
			}
			allProcs.add(v.getPName());  // set semantics
		}

		try {
			// create all processors -- special handling of the global inputs / outputs
			// NOTE: ASSUME NO WF NESTING
			for (String pname: allProcs) {
				if (dataflowNames.contains(pname)) {
					// create global inputs and outputs instead
					for (String globalInputPort:p2Inputs.get(pname)) {
						dfg.createInputPort(df, 0, globalInputPort);
					}	
					for (String globalOutputPort:p2Outputs.get(pname)) {
						dfg.createOutputPort(df, globalOutputPort);
					}	
				}  else {
					Processor p = dfg.createProcessorWithPorts(df, pname, p2Inputs.get(pname), p2Outputs.get(pname));
					pname2proc.put(pname, p);
				}
			}
		} catch (EditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// read in all Arcs from wfRef
		queryConstraints.clear();
		queryConstraints.put("A.wfInstanceRef", wfRef);
		List<Arc> allArcs = pq.getArcs(queryConstraints);

		try {
			// connect all of them up...
			for (Arc arc:allArcs) {

				if (dataflowNames.contains(arc.getSourcePnameRef())) {
					dfg.connectGlobalInput(df, arc.getSourceVarNameRef(), pname2proc.get(arc.getSinkPnameRef()), arc.getSinkVarNameRef());
				} else if (dataflowNames.contains(arc.getSinkPnameRef())) {
					dfg.connectGlobalOutput(df, arc.getSinkVarNameRef(), pname2proc.get(arc.getSourcePnameRef()), arc.getSourceVarNameRef());
				} else
					dfg.connectPorts(df, pname2proc.get(arc.getSourcePnameRef()), arc.getSourceVarNameRef(), 
							pname2proc.get(arc.getSinkPnameRef()), arc.getSinkVarNameRef());
			}

			String targetFile = TARGET_FILE_FROM_PROVENANCE+"_"+wfRef+".t2flow";
			dfg.writeSerializedDataflow(df, targetFile);
			logger.info("workflow saved to "+targetFile);

		} catch (SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}





	}

}
