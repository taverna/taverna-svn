package net.sf.taverna.t2.lineageService.capture.test;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lineageService.analysis.test.AnalysisTestFiles;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

import org.junit.Test;


/**
 * @author Paolo Missier
 * 
 */
public class ProvenanceCaptureTest extends ProvenanceCaptureTestHelper  {

	boolean activateProvenance = true;

	@Test
	public void testInput() throws Exception {

		ProvenanceCaptureTestHelper helper = new ProvenanceCaptureTestHelper();

		helper.createEventsDir(); 

		Dataflow dataflow = helper.setup("ProvenanceCaptureTest");

		// collect inputs from properties file
		String inputValues = testFiles.getString("workflow.inputs");

		if (inputValues.equals("!workflow.inputs!")) {
			System.out.println("no inputs -- hope that's ok");			
		} else {

			String[] valuepairs = inputValues.split(",");

			Map<String, T2Reference> references = new HashMap<String, T2Reference>();

			for (String vp:valuepairs) {

				String[] pair = vp.split(":");

				if (pair.length != 2) {
					fail("expecting name:value pairs, found "+vp);
				}

				T2Reference entity = context.getReferenceService().register(pair[1], 0,true, context);
				references.put(pair[0], entity);
			}

			// provide inputs to ports
			for (DataflowInputPort port : dataflow.getInputPorts()) {

				T2Reference entity = references.get(port.getName());

				if ( entity != null ) {

					System.out.println("populating port "+port.getName()+" with input" +entity.toString());

					WorkflowDataToken inputToken = new WorkflowDataToken("",new int[]{}, entity, context);
					helper.getFacade().pushData(inputToken, port.getName());
				}
			}
		}

		helper.waitForCompletion();

		assertTrue("ok as long as we got this far", true); //$NON-NLS-1$
	}




}


