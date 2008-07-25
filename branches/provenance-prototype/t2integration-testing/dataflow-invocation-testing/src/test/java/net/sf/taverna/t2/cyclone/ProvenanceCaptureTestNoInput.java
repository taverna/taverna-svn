package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.junit.Ignore;
import org.junit.Test;


/**
 * @author Paolo Missier
 * 
 */
public class ProvenanceCaptureTestNoInput extends TranslatorTestHelper {
	
	String[] scuflFiles = 
			{ "provenance-testing/test_iterate_list_of_lists.xml",  //0
			  "provenance-testing/test1.xml",  //1
			  "provenance-testing/test2.xml",  //2
			  "provenance-testing/test3.xml",  //3
			  "provenance-testing/test4.xml",  //4
			  "provenance-testing/test5.xml",  //5
			  "",
			  "",
			  "",
			  "",
			  "provenance-testing/test10.xml",  //10  
			  "provenance-testing/test11.xml",  //11
			  "provenance-testing/test12.xml",  //12 
			  "provenance-testing/test13.xml",  //13
			  "provenance-testing/test14.xml",  //14
			  "provenance-testing/test15.xml"   //15
			  }; 
	
	@Test
	public void testNoInput() throws Exception {
		
		String scuflFile = scuflFiles[12];

		Dataflow dataflow = translateScuflFile(scuflFile);

		System.out.println("input workflow: ["+scuflFile+"]");
		
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());

		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,dataFacade);
		facade.addResultListener(listener);

		facade.fire();

		waitForCompletion(listener);
		
		assertTrue("ok as long as we got this far", true);
	}
	
	
	
	
}


