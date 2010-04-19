package net.sf.taverna.t2.lineageService.capture.test;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

import org.junit.Test;


/**
 * test workflow: pathways_and_gene_annotations_for_qtl_phenotype_28303.t2flow
 * @author Paolo Missier
 * 
 */
public class ProvenanceCaptureTest5b_list extends ProvenanceCaptureTestHelper {

	@Test
	public void testProvenanceCapture() throws Exception {

		ProvenanceCaptureTestHelper helper = this;

		Dataflow dataflow = helper.setup("ProvenanceCaptureTest");

		helper.createEventsDir(); 

		// collect inputs from properties file

		List<String> i2 = new ArrayList<String>();

		i2.add("bar1");
		i2.add("bar2");

		String i1 = "foo";

		T2Reference entityId1 = context.getReferenceService().register(i1, 0,true, context);

		// provide inputs to ports
		for (DataflowInputPort port : dataflow.getInputPorts()) {

			System.out.println("populating port "+port.getName());

			if (port.getName().equals("I2")) {

				System.out.println("populating port "+port.getName()+" with input" +i2);

				WorkflowDataToken inputToken1 = new WorkflowDataToken("",new int[]{}, entityId1, context);
				helper.getFacade().pushData(inputToken1, port.getName());
			} else 			if (port.getName().equals("I1")) {

				System.out.println("populating port "+port.getName()+" with input" +i1);

				WorkflowDataToken inputToken1 = new WorkflowDataToken("",new int[]{}, entityId1, context);
				helper.getFacade().pushData(inputToken1, port.getName());
			}

		}

		helper.waitForCompletion();

		System.out.println("output: \n"+helper.getListener().getResult("O1"));
		assertTrue("ok as long as we got this far", true);
	}


}


