package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.junit.Test;

public class PortBindingTest extends TranslatorTestHelper {

	@Test
	public void testRemovalOfUnboundPorts() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("unbound_ports.xml");
		Processor processor_A=null;
		Processor processor_B=null;
		
		assertEquals("there should only be 2 processors",2,dataflow.getProcessors().size());
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("Processor_A")) processor_A=p;
			if (p.getLocalName().equals("Processor_B")) processor_B=p;
		}
		
		assertNotNull("Could not find processor A",processor_A);
		assertNotNull("Could not find processor B",processor_B);
		
		assertEquals("processor_A should have no inputs",0,processor_A.getInputPorts().size());
		assertEquals("processor_A should have 1 output",1,processor_A.getOutputPorts().size());
		assertEquals("processor_B should have 1 input",1,processor_B.getInputPorts().size());
		assertEquals("processor_B should have 1 input named input_1","input_1",processor_B.getInputPorts().get(0).getName());
		assertEquals("processor_B should have 1 output",1,processor_B.getOutputPorts().size());
		
		assertEquals("processor_B should have 1 activity",1,processor_B.getActivityList().size());
		Activity<?>activity = processor_B.getActivityList().get(0).getActivity();
		
		assertEquals("activity should have 3 input ports",3,activity.getInputPorts().size());
	}
	
	@Test
	public void testReplaceDefaultWithStringConstant() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("port_with_default.xml");
		Processor processor_A = null;
		Processor processorStringConstant = null;
		
		assertEquals("There should be 2 processors",2,dataflow.getProcessors().size());
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("Processor_A")) {
				processor_A = p;
			}
			else {
				processorStringConstant = p;
			}
		}
		
		assertNotNull("There should be a processor named processor_A",processor_A);
		assertEquals("processor_A should have 1 input",1,processor_A.getInputPorts().size());
		assertEquals("The should only be 1 activity",1,processorStringConstant.getActivityList().size());
		assertEquals("Processor_A should have 1 iteration stragegy",1,processor_A.getIterationStrategy().getStrategies().size());
		
		assertEquals("There should be a processor that contains a string constant activity",StringConstantActivity.class,processorStringConstant.getActivityList().get(0).getActivity().getClass());
		
		StringConstantActivity activity = (StringConstantActivity) processorStringConstant.getActivityList().get(0).getActivity();
		assertEquals("activity should have value 'Some Data'","Some Data",activity.getStringValue());
		
		assertEquals("The string constant shoudl have 1 output port",1,processorStringConstant.getOutputPorts().size());
		assertEquals("There should only be 1 outgoing link",1,processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().size());
		
		Datalink link = (Datalink)processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().toArray()[0];
		assertEquals("it should be linked to processor_A",processor_A.getInputPorts().get(0),link.getSink());
		
	}
	
	@Test
	public void testDefaultAndUnboundMixed() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("unbound_ports_with_default.xml");
		Processor processor_A = null;
		Processor processorStringConstant = null;
		
		assertEquals("There should be 2 processors",2,dataflow.getProcessors().size());
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("Processor_A")) {
				processor_A = p;
			}
			else {
				processorStringConstant = p;
			}
		}
		
		assertNotNull("There should be a processor named processor_A",processor_A);
		assertEquals("processor_A should have 1 input",1,processor_A.getInputPorts().size());
		assertEquals("The should only be 1 activity",1,processorStringConstant.getActivityList().size());
		assertEquals("There should be a processor that contains a string constant activity",StringConstantActivity.class,processorStringConstant.getActivityList().get(0).getActivity().getClass());
		
		StringConstantActivity activity = (StringConstantActivity) processorStringConstant.getActivityList().get(0).getActivity();
		assertEquals("activity should have value 'Some Data'","Some Data",activity.getStringValue());
		
		assertEquals("The string constant shoudl have 1 output port",1,processorStringConstant.getOutputPorts().size());
		assertEquals("There should only be 1 outgoing link",1,processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().size());
		
		Datalink link = (Datalink)processorStringConstant.getOutputPorts().get(0).getOutgoingLinks().toArray()[0];
		assertEquals("it should be linked to processor_A",processor_A.getInputPorts().get(0),link.getSink());
	}
	
	@Test
	public void testBoundPortOverridesDefault() throws Exception {
		Dataflow dataflow = loadAndTranslateWorkflow("bound_port_overides_default.xml");
		assertEquals("there should only be 2 processors",2,dataflow.getProcessors().size());
		
		Processor processor_A=null;
		Processor processor_B=null;
		
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("Processor_A")) processor_A=p;
			if (p.getLocalName().equals("Processor_B")) processor_B=p;
		}
		
		assertNotNull("Could not find processor A",processor_A);
		assertNotNull("Could not find processor B",processor_B);
		
		assertEquals("processor_A should have no inputs",0,processor_A.getInputPorts().size());
		assertEquals("processor_A should have 1 output",1,processor_A.getOutputPorts().size());
		assertEquals("processor_B should have 1 input",1,processor_B.getInputPorts().size());
		assertEquals("processor_B should have 1 input named input_1","input_1",processor_B.getInputPorts().get(0).getName());
		assertEquals("processor_B should have 1 output",1,processor_B.getOutputPorts().size());
		
		assertEquals("processor_B should have 1 activity",1,processor_B.getActivityList().size());
		Activity<?>activity = processor_B.getActivityList().get(0).getActivity();
		
		assertEquals("activity should have 3 input ports",3,activity.getInputPorts().size());
	}
	
	private Dataflow loadAndTranslateWorkflow(String resourceName) throws Exception {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl(resourceName);
		return WorkflowModelTranslator.doTranslation(model);
	}
}
