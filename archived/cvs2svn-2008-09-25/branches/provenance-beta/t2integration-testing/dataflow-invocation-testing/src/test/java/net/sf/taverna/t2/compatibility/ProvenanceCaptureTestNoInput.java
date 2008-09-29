package net.sf.taverna.t2.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.testing.CaptureResultsListener;
import net.sf.taverna.t2.testing.InvocationTestHelper;
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
public class ProvenanceCaptureTestNoInput extends InvocationTestHelper {
	
	String[] scuflFiles = 
			{ "provenance-testing/test_iterate_list_of_lists.t2flow",  //0
			  "provenance-testing/test1.t2flow",  //1
			  "provenance-testing/test2.t2flow",  //2
			  "provenance-testing/test3.t2flow",  //3
			  "provenance-testing/test4.t2flow",  //4
			  "provenance-testing/test5.t2flow",  //5
			  "",
			  "",
			  "",
			  "",
			  "provenance-testing/test10.t2flow",  //10  
			  "provenance-testing/test11.t2flow",  //11
			  "provenance-testing/test12.t2flow",  //12 
			  "provenance-testing/test13.t2flow",  //13
			  "provenance-testing/test14.t2flow",  //14
			  "provenance-testing/test15.t2flow",   //15
			  "provenance-testing/test16.t2flow",   //16
			  "provenance-testing/test17.t2flow"   //17
			  }; 
	
	@Test
	public void testNoInput() throws Exception {
		
		String T2File = scuflFiles[1];

		Dataflow dataflow = null;
		
		try {
			dataflow = loadDataflow(T2File);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataflowValidationReport report = validateDataflow(dataflow);
		
		
		System.out.println("input workflow: ["+T2File+"]");
		
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());

		WorkflowInstanceFacade facade;
		facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);

		facade.fire();

		waitForCompletion(listener);
		
		assertTrue("ok as long as we got this far", true);
	}
	
	
	
	
}


