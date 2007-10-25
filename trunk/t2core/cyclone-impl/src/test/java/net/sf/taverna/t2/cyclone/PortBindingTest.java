package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.junit.Test;

public class PortBindingTest extends TranslatorTestHelper {

	@Test
	public void testRemovalOfUnboundPorts() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("unbound_ports.xml");
//		Processor processorA=null;
//		Processor processorB=null;
//		
//		assertEquals("there should only be 2 processors",2,dataflow.getProcessors().size());
//		for (Processor p : dataflow.getProcessors()) {
//			if (p.getLocalName().equals("Processor_A")) processorA=p;
//			if (p.getLocalName().equals("Processor_B")) processorB=p;
//		}
//		
//		assertNotNull("Could not find processor A",processorA);
//		assertNotNull("Could not find processor B",processorB);
//		
//		assertEquals("processorA should have no inputs",0,processorA.getInputPorts().size());
//		assertEquals("processorA should have 1 output",1,processorA.getOutputPorts().size());
//		assertEquals("processorB should have 1 input",1,processorB.getInputPorts().size());
//		assertEquals("processorB should have 1 input named input_1","input_1",processorB.getInputPorts().get(0).getName());
//		assertEquals("processorB should have 1 output",1,processorB.getOutputPorts().size());
//		
//		assertEquals("processorB should have 1 activity",1,processorB.getActivityList().size());
//		Activity<?>activity = processorB.getActivityList().get(0).getActivity();
//		
//		assertEquals("activity should have 1 input port",1,activity.getInputPorts().size());
//		assertEquals("activity input should be named input_1","input_1",((InputPort)activity.getInputPorts().toArray()[0]).getName());
	}
	
	@Test
	public void testReplaceDefaultWithStringConstant() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("port_with_default.xml");
		Processor processorA = null;
		Processor processorStringConstant = null;
		
//		assertEquals("There should be 2 processors",2,dataflow.getProcessors().size());
//		for (Processor p : dataflow.getProcessors()) {
//			if (p.getLocalName().equals("processorA")) {
//				processorA = p;
//			}
//			else {
//				processorStringConstant = p;
//			}
//		}
//		
//		assertNotNull("There should be a processor named processorA",processorA);
//		assertEquals("processorA should have 1 input",1,processorA.getInputPorts().size());
//		assertEquals("The should only be 1 activity",1,processorStringConstant.getActivityList().size());
//		assertEquals("There should be a processor that contains a string constant activity",StringConstantActivity.class,processorStringConstant.getActivityList().get(0).getActivity().getClass());
//		
//		StringConstantActivity activity = (StringConstantActivity) processorStringConstant.getActivityList().get(0).getActivity();
//		assertEquals("activity should have value 'Some Data'","Some Data",activity.getStringValue());
//		
//		assertEquals("The string constant shoudl have 1 output port",1,processorStringConstant.getOutputPorts().size());
//		assertEquals("There should only be 1 outgoing link",1,processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().size());
//		
//		Datalink link = (Datalink)processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().toArray()[0];
//		assertEquals("it should be linked to processorA",processorA.getInputPorts().get(0),link.getSink());
		
	}
	
	@Test
	public void testDefaultAndUnboundMixed() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("unbound_ports_with_default.xml");
		Processor processorA = null;
		Processor processorStringConstant = null;
		
//		assertEquals("There should be 2 processors",2,dataflow.getProcessors().size());
//		for (Processor p : dataflow.getProcessors()) {
//			if (p.getLocalName().equals("processorA")) {
//				processorA = p;
//			}
//			else {
//				processorStringConstant = p;
//			}
//		}
//		
//		assertNotNull("There should be a processor named processorA",processorA);
//		assertEquals("processorA should have 1 input",1,processorA.getInputPorts().size());
//		assertEquals("The should only be 1 activity",1,processorStringConstant.getActivityList().size());
//		assertEquals("There should be a processor that contains a string constant activity",StringConstantActivity.class,processorStringConstant.getActivityList().get(0).getActivity().getClass());
//		
//		StringConstantActivity activity = (StringConstantActivity) processorStringConstant.getActivityList().get(0).getActivity();
//		assertEquals("activity should have value 'Some Data'","Some Data",activity.getStringValue());
//		
//		assertEquals("The string constant shoudl have 1 output port",1,processorStringConstant.getOutputPorts().size());
//		assertEquals("There should only be 1 outgoing link",1,processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().size());
//		
//		Datalink link = (Datalink)processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().toArray()[0];
//		assertEquals("it should be linked to processorA",processorA.getInputPorts().get(0),link.getSink());
	}
	
	@Test
	public void testBoundPortOverridesDefault() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("bound_port_overides_default.xml");
		assertEquals("there should only be 2 processors",2,dataflow.getProcessors().size());
		
//		Processor processorA=null;
//		Processor processorB=null;
//		
//		for (Processor p : dataflow.getProcessors()) {
//			if (p.getLocalName().equals("Processor_A")) processorA=p;
//			if (p.getLocalName().equals("Processor_B")) processorB=p;
//		}
//		
//		assertNotNull("Could not find processor A",processorA);
//		assertNotNull("Could not find processor B",processorB);
//		
//		assertEquals("processorA should have no inputs",0,processorA.getInputPorts().size());
//		assertEquals("processorA should have 1 output",1,processorA.getOutputPorts().size());
//		assertEquals("processorB should have 1 input",1,processorB.getInputPorts().size());
//		assertEquals("processorB should have 1 input named input_1","input_1",processorB.getInputPorts().get(0).getName());
//		assertEquals("processorB should have 1 output",1,processorB.getOutputPorts().size());
//		
//		assertEquals("processorB should have 1 activity",1,processorB.getActivityList().size());
//		Activity<?>activity = processorB.getActivityList().get(0).getActivity();
//		
//		assertEquals("activity should have 1 input port",1,activity.getInputPorts().size());
//		assertEquals("activity input should be named input_1","input_1",((InputPort)activity.getInputPorts().toArray()[0]).getName());
	}
	
	private Dataflow loadAndTranslateWorkflow(String resourceName) throws Exception {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl(resourceName);
		return WorkflowModelTranslator.doTranslation(model);
	}
}
