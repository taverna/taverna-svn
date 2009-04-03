/**
 * 
 */
package net.sf.taverna.t2.lineageService.generation.test;


import java.io.IOException;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;

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
	
	private static final String TEMPLATE_FILE = Messages.getString("templateFile"); //$NON-NLS-1$
	private static final String DEFAULT_CHAIN_LEN = "5";
	private static String len = Messages.getString("linearChainLength");
	private static String inputDepthStr = Messages.getString("inputDepth");

	String dataflowName, dataflowID  = null;
	private static int inputDepth = 0;

	Element templateDataflowElement = null;

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

		Dataflow df = lcg.createEmptyDataflow(dataflowName, dataflowID); //$NON-NLS-1$ //$NON-NLS-2$

		// create single input port
		lcg.createInputPort(df, inputDepth, "I1");

		df = lcg.generateLinearWorkflow(df, Messages.getString("linearBlockProcessorName"), length); //$NON-NLS-1$

		// connect this chain to input port I1
		df = lcg.connectInput(df, "I1", lcg.getFirst(), "X"); //$NON-NLS-1$ //$NON-NLS-2$

		// create output port O
		lcg.createOutputPort(df, "O");

		// connect output
		df = lcg.connectOutput(df, "O", lcg.getLast(), "Y");  //$NON-NLS-1$ //$NON-NLS-2$

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

		// create input port I1
		dfg.createInputPort(df, 0, "I1");

		// create input port ListSize
		dfg.createInputPort(df, 0, "ListSize");

		// create CROSS2 processor with inputs X1, X2
		Processor listGen = dfg.addSingleProcessor(df, "LISTGEN", "LISTGEN_1");

		//  connect I1 to X1
		df = dfg.connectInput(df, "I1", listGen, "X1"); //$NON-NLS-1$ //$NON-NLS-2$

		// connect ListSize to X2
		df = dfg.connectInput(df, "ListSize", listGen, "X2"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// create fist chain of length <len>
		lcg1 = new LinearChainGenerator();
		lcg1.setTemplateFileName(TEMPLATE_FILE);
		lcg1.setPnameSuffix("_A");

		df = lcg1.generateLinearWorkflow(df, Messages.getString("linearBlockProcessorName"), length); //$NON-NLS-1$

		// connect this chain to LISTGEN:Y1
		df = dfg.connectPorts(df, listGen, "Y1", lcg1.getFirst(), "X"); //$NON-NLS-1$ //$NON-NLS-2$
		
		df = dfg.addControlLink(df, listGen, lcg1.getFirst());  // CHECK 

		// create second chain of length <len>
		lcg2 = new LinearChainGenerator();
		lcg2.setTemplateFileName(TEMPLATE_FILE);
		lcg2.setPnameSuffix("_B");

		df = lcg2.generateLinearWorkflow(df, Messages.getString("linearBlockProcessorName"), length); //$NON-NLS-1$

		// connect this chain to LISTGEN:Y1
		df = dfg.connectPorts(df, listGen, "Y2", lcg2.getFirst(), "X"); //$NON-NLS-1$ //$NON-NLS-2$

		df = dfg.addControlLink(df, listGen, lcg2.getFirst());  // CHECK 

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
		df = dfg.connectOutput(df, "O", two2One, "Y");  //$NON-NLS-1$ //$NON-NLS-2$

		dfg.writeSerializedDataflow(df, TARGET_FILE_LARGELIST);

		System.out.println("workflow saved to "+TARGET_FILE_LARGELIST);

	}

//	DataflowInputPort ip;
//	ip = edits.createDataflowInputPort("I", 0, 0, df);
//	edits.getAddDataflowInputPortEdit(df, ip).doEdit();

//	// create single output port
//	DataflowOutputPort op;		
//	op = edits.createDataflowOutputPort("O", df);
//	edits.getAddDataflowOutputPortEdit(df, op).doEdit();

//	Processor startP = getProcessorFromTemplate("2OUT");
//	edits.getAddProcessorEdit(df, startP).doEdit();

//	connectInput(df, "I", startP, "X");

//	df = generateLinearWorkflow(df, "LINEARBLOCK", leftChainLength);
//	df = generateLinearWorkflow(df, "LINEARBLOCK", rightChainLength);

////	connectPorts(df, startP, "Y1", chainHead1, "X");
////	connectPorts(df, startP, "Y2", chainHead2, "X");

//	}



}
