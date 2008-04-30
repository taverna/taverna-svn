package net.sf.taverna.t2.workflowmodel.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ErrorBounce;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Failover;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ParallelizeConfig;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

public class DeserializerImplTest{

	private DeserializerImpl deserializer = new DeserializerImpl();
	private Edits edits = new EditsImpl();

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testActivityDeserialization() throws Exception {
		Element el = loadXMLFragment("activity.xml");
		Activity<?> activity = deserializer.deserializeActivityFromXML(el);
		
		assertNotNull("The activity should not be NULL",activity);
		assertTrue("should be a DummyActivity",activity instanceof DummyActivity);
		assertTrue("bean should be an Integer",activity.getConfiguration() instanceof Integer);
		assertEquals("bean should equal 5",5,((Integer)activity.getConfiguration()).intValue());
		
		assertEquals("there should be 1 input port mapping",1,activity.getInputPortMapping().size());
		assertEquals("there should be 1 output port mapping",1,activity.getOutputPortMapping().size());
		
		assertEquals("input in is mapped to in","in",activity.getInputPortMapping().get("in"));
		assertEquals("output out is mapped to out","out",activity.getOutputPortMapping().get("out"));
	}
	
	@Test
	public void testMerge() throws Exception {
		Element el = loadXMLFragment("dataflow_with_merge.xml");
		Dataflow df = deserializer.deserializeDataflowFromXML(el);
		
		assertEquals("There should be 2 processors",2,df.getProcessors().size());
		Processor top=df.getProcessors().get(0);
		Processor bottom=df.getProcessors().get(1);
		
		assertEquals("Top processor should be called top","top",top.getLocalName());
		assertEquals("Bottom processor should be called top","bottom",bottom.getLocalName());
		
		assertEquals("Top should have 1 output port",1,top.getOutputPorts().size());
		
		assertEquals("There should be 1 outgoing link",1,top.getOutputPorts().get(0).getOutgoingLinks().size());
		
		Datalink link = top.getOutputPorts().get(0).getOutgoingLinks().iterator().next();
		
		assertTrue("Link sink should be Merge port",link.getSink() instanceof MergeInputPort);
	}
	
	@Test
	public void testDispatchStack() throws Exception {
		Element el = loadXMLFragment("dispatchStack.xml");
		Processor p = edits.createProcessor("p");
		deserializer.deserializeDispatchStack(p, el);
		assertEquals("there should be 5 layers",5,p.getDispatchStack().getLayers().size());
		assertTrue("first layer should be parallelize, but was "+p.getDispatchStack().getLayers().get(0),p.getDispatchStack().getLayers().get(0) instanceof Parallelize);
		assertTrue("2nd layer should be ErrorBounce, but was "+p.getDispatchStack().getLayers().get(1),p.getDispatchStack().getLayers().get(1) instanceof ErrorBounce);
		assertTrue("3rd layer should be Failover, but was "+p.getDispatchStack().getLayers().get(2),p.getDispatchStack().getLayers().get(2) instanceof Failover);
		assertTrue("4th layer should be Retry, but was "+p.getDispatchStack().getLayers().get(3),p.getDispatchStack().getLayers().get(3) instanceof Retry);
		assertTrue("5th layer should be Invoke, but was "+p.getDispatchStack().getLayers().get(4),p.getDispatchStack().getLayers().get(4) instanceof Invoke);
	}
	
	@Test
	public void testDataflowPorts() throws Exception {
		Element element = loadXMLFragment("empty_dataflow_with_ports.xml");
		Dataflow df = deserializer.deserializeDataflowFromXML(element);
		
		assertEquals("there should be 2 input ports",2,df.getInputPorts().size());
		assertEquals("there should be 1 output port",1,df.getOutputPorts().size());
		
		DataflowInputPort port = df.getInputPorts().get(0);
		assertEquals("Name should be input1","input1",port.getName());
		assertEquals("depth should be 0",0,port.getDepth());
		assertEquals("granular depth should be 0",0,port.getGranularInputDepth());
		
		port = df.getInputPorts().get(1);
		assertEquals("Name should be input2","input2",port.getName());
		assertEquals("depth should be 1",1,port.getDepth());
		assertEquals("granular depth should be 1",1,port.getGranularInputDepth());
		
		DataflowOutputPort outputPort = df.getOutputPorts().get(0);
		assertEquals("Name should be output","output",outputPort.getName());
	}
	
	@Test
	public void testDataflowConditionLink() throws Exception {
		Element element = loadXMLFragment("dataflow_with_condition.xml");
		Dataflow df = deserializer.deserializeDataflowFromXML(element);
		
		assertEquals("There should be 2 processors",2,df.getProcessors().size());
		Processor pA = df.getProcessors().get(0);
		Processor pB = df.getProcessors().get(1);
		if (!pB.getLocalName().equals("b_processor")) {
			pB=df.getProcessors().get(0);
			pA=df.getProcessors().get(1);
		}
		assertEquals("There should be 1 precondition",1,pB.getPreconditionList().size());
		Condition con = pB.getPreconditionList().get(0);
		assertSame("the control processor shoudl be a_processor",pA, con.getControl());
	}
	
	@Test
	public void testDataflowProcessor() throws Exception {
		Element element = loadXMLFragment("dataflow_with_unlinked_processor.xml");
		Dataflow df = deserializer.deserializeDataflowFromXML(element);
		assertEquals("There should be 1 processor",1,df.getProcessors().size());
		assertEquals("Processor name should be a_processor","a_processor",df.getProcessors().get(0).getLocalName());	
	}
	
	@Test
	public void testCreateBeanSimple() throws Exception {
		Element el = new Element("configBean");
		el.setAttribute("encoding","xstream");
		Element elString = new Element("string");
		elString.setText("12345");
		el.addContent(elString);
		
		Object bean = deserializer.createBean(el, DeserializerImpl.class.getClassLoader());
		assertTrue("bean should be a String",bean instanceof String);
		assertEquals("string should equal 12345","12345",((String)bean));
	}
	
	@Test
	public void testCreateBeanComplex() throws Exception {
		String xml="<configBean encoding=\"xstream\"><net.sf.taverna.t2.workflowmodel.serialization.DummyBean><id>1</id><name>bob</name><innerBean><stuff>xyz</stuff></innerBean></net.sf.taverna.t2.workflowmodel.serialization.DummyBean></configBean>";
		Element el = new SAXBuilder().build(new StringReader(xml)).detachRootElement();
		
		Object bean = deserializer.createBean(el, DeserializerImpl.class.getClassLoader());
		assertTrue("bean should be a DummyBean",bean instanceof DummyBean);
		DummyBean dummyBean = (DummyBean)bean;
		
		assertEquals("id should be 1",1,dummyBean.getId());
		assertEquals("namne should be bob","bob",dummyBean.getName());
		assertEquals("stuff should by xyz","xyz",dummyBean.getInnerBean().getStuff());
	}
	
	@Test
	public void testDispatchLayer() throws Exception {
		Element el = loadXMLFragment("dispatchLayer.xml");
		DispatchLayer<?> layer = deserializer.deserializeDispatchLayer(el);
		assertTrue("Should be a Parallelize layer",layer instanceof Parallelize);
		Parallelize para = (Parallelize)layer;
		assertTrue("config should be ParellizeConfig",para.getConfiguration() instanceof ParallelizeConfig);
		assertEquals("max jobs should be 7",7,((ParallelizeConfig)para.getConfiguration()).getMaximumJobs());
	}
	
	@Test
	public void testProcessor() throws Exception {
		Element el = loadXMLFragment("processor.xml");
		Processor p = deserializer.deserializeProcessorFromXML(el);
		
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
	
	@Test
	public void testDataflowDataLinks() throws Exception {
		Element el = loadXMLFragment("dataflow_datalinks.xml");
		Dataflow df = deserializer.deserializeDataflowFromXML(el);
		
		assertEquals("There should be 2 processors",2,df.getProcessors().size());
		assertEquals("There should be 2 datalinks",2,df.getLinks().size());
	}

	protected Element loadXMLFragment(String resourceName) throws Exception {
		InputStream inStream = DeserializerImplTest.class
				.getResourceAsStream("/serialized-fragments/" + resourceName);

		if (inStream==null) throw new IOException("Unable to find resource for serialized fragment :"+resourceName);
		SAXBuilder builder = new SAXBuilder();
		return builder.build(inStream).detachRootElement();
	}

}
