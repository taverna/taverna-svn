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
 * @author Paolo Missier
 * expects ProvenanceCaptureTestWithInput=Retrieve SNPs from regions around known genes-simplified.t2flow in CaptureTestFiles.properties<br/>
 * this has got a list input -- I have no examples for how you supply values to the inputs..
 */
public class ProvenanceCaptureTest_SNPsFromGenesSimplified extends ProvenanceCaptureTestHelper {

	@Test
	public void testProvenanceCapture_SNPsFromGenes() throws Exception {

	//	ProvenanceCaptureTestHelper helper = new ProvenanceCaptureTestHelper();

		Dataflow dataflow = this.setup("ProvenanceCaptureTestWithInput");

		List<String> inputGenes = new ArrayList<String>();
		
		inputGenes.add("ENSG00000139618");
	//	inputGenes.add("ENSG00000083093");
		
		T2Reference entityId1 = context.getReferenceService().register(inputGenes, 1,true, context);

		// provide inputs to ports
		for (DataflowInputPort port : dataflow.getInputPorts()) {

			if (port.getName().equals("GeneIDList")) {
				WorkflowDataToken inputToken1 = new WorkflowDataToken("",new int[]{}, entityId1, context);
				this.getFacade().pushData(inputToken1, port.getName());
			}

		}

		this.waitForCompletion();

		System.out.println("output: \n"+this.getListener().getResult("O1"));
		assertTrue("ok as long as we got this far", true);
	}


}


