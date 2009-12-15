package net.sf.taverna.t2.lineageService.capture.test;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ProvenanceCaptureTest_KeggGenes2KeggPathways_PM extends ProvenanceCaptureTestHelper {

	@Test
	public void testProvenanceCapture() throws Exception {

		ProvenanceCaptureTestHelper helper = this;
		helper.createEventsDir(); 

		Dataflow dataflow = helper.setup("ProvenanceCaptureTest");

		helper.createEventsDir(); 

		Map<String, T2Reference> references = new HashMap<String, T2Reference>();


		List<List<String>> geneListOfLists = new ArrayList<List<String>>();

		List<String> geneList1 = new ArrayList<String>();
		List<String> geneList2 = new ArrayList<String>();
		
		geneList1.add("mmu:26416");
		geneList2.add("mmu:100047666");

		geneListOfLists.add(geneList1);
		geneListOfLists.add(geneList2);
		
		T2Reference entity = context.getReferenceService().register(geneListOfLists, 2,true, context);
		references.put("list_of_geneIDList", entity);
		
		// provide inputs to ports
		for (DataflowInputPort port : dataflow.getInputPorts()) {

			System.out.println("populating port "+port.getName());

			if (port.getName().equals("list_of_geneIDList")) {

				System.out.println("populating port "+port.getName()+" with input" +geneListOfLists);

				WorkflowDataToken inputToken1 = new WorkflowDataToken("",new int[]{}, entity, context);
				helper.getFacade().pushData(inputToken1, port.getName());
			}

		}

		helper.waitForCompletion();

		System.out.println("output: \n"+helper.getListener().getResult("paths_per_gene"));
		System.out.println("output: \n"+helper.getListener().getResult("commonPathways"));
		assertTrue("ok as long as we got this far", true);
	}


}


