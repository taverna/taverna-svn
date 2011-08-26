package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;

import org.jdom.Element;
import org.junit.Test;


public class ProcessorXMLDeserializerTest extends DeserializerTestsHelper {
	ProcessorXMLDeserializer deserializer = ProcessorXMLDeserializer.getInstance();
	
	@Test
	public void testProcessor() throws Exception {
		Element el = loadXMLFragment("processor.xml");
		Processor p = deserializer.deserializeProcessor(el,new HashMap<String, Element>());
		
		assertEquals("Local name should be george","george",p.getLocalName());
		assertEquals("there should be 1 input port",1,p.getInputPorts().size());
		assertEquals("there should be 0 activities",0,p.getActivityList().size());
		assertEquals("there should be 1 output port",1,p.getOutputPorts().size());
		assertNotNull("there should be an iteration strategy",p.getIterationStrategy());
		assertEquals("there should be no dispatch stack layers",0,p.getDispatchStack().getLayers().size());
		
		ProcessorInputPort processorInputPort = p.getInputPorts().get(0);
		assertEquals("name should be input","input",processorInputPort.getName());
		assertEquals("depth should be 0",0,processorInputPort.getDepth());
		
		ProcessorOutputPort processorOutputPort = p.getOutputPorts().get(0);
		assertEquals("name should be output","output",processorOutputPort.getName());
		assertEquals("depth should be 1",1,processorOutputPort.getDepth());
		assertEquals("granular depth should be 0",0,processorOutputPort.getGranularDepth());
		
		IterationStrategyStack stack=p.getIterationStrategy();
		assertEquals("There should be 1 iteration strategy defined",1,stack.getStrategies().size());
	}

}
