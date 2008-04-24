package net.sf.taverna.t2.workflowmodel.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Failover;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ParallelizeConfig;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

public class SerializerImplTest {
	
	private static Logger logger = Logger.getLogger(SerializerImplTest.class);
	
	private SerializerImpl serializer=new SerializerImpl();
	private EditsImpl edits = new EditsImpl();
	
	@Test
	public void testActivitySerialization() throws Exception
	{
		DummyActivity activity=new DummyActivity();
		activity.getInputPortMapping().put("in", "in");
		activity.getOutputPortMapping().put("out", "out");
		activity.configure(new Integer(5));
		Element el = serializer.activityToXML(activity);
		
		logger.info("processor serialization xml = "+elementToString(el));
		
		assertEquals("root element should be activity","activity",el.getName());
		Element classChild = el.getChild("class");
		assertNotNull("there should be a child called class",classChild);
		assertEquals("incorrect activity class name","net.sf.taverna.t2.workflowmodel.serialization.DummyActivity",classChild.getText());
		assertEquals("there should be 1 inputMap child",1,el.getChildren("inputMap").size());
		assertEquals("there should be 1 outputMap child",1,el.getChildren("outputMap").size());
		
		Element inputMap=el.getChild("inputMap");
		Element outputMap=el.getChild("outputMap");
		assertEquals("input map should define the map for 'in'","<map from=\"in\" to=\"in\" />",elementToString(inputMap.getChild("map")));
		assertEquals("output map should define the map for 'out'","<map from=\"out\" to=\"out\" />",elementToString(outputMap.getChild("map")));
		
		Element java = el.getChild("java");
		assertNotNull("there should be a child called java",java);
		assertEquals("java child should describe an int with value 5","<int>5</int>",elementToString(java.getChild("int")));
	}
	
	@Test
	public void testProcessorSerialization() throws Exception {
		Processor p = edits.createProcessor("fred");
		Element el = serializer.processorToXML(p);
		
		logger.info("processor serialization xml = "+elementToString(el));
		
		assertNotNull("Element should not be null",el);
		
		assertEquals("root element should be processor","processor",el.getName());
		Element name=el.getChild("name");
		assertNotNull("There should be a child called name",name);
		assertEquals("name should be fred","fred",name.getText());
		
		assertEquals("there should be an annotations child (even if its empty)",1,el.getChildren("annotations").size());
		assertEquals("there should be an activities child (even if its empty)",1,el.getChildren("activities").size());
		assertEquals("there should be an dispatch statck child (even if its empty)",1,el.getChildren("dispatchStack").size());
		assertEquals("there should be an iteration strategy stack child (even if its empty)",1,el.getChildren("iterationStrategyStack").size());
		assertEquals("there should be an input ports child (even if its empty)",1,el.getChildren("inputPorts").size());
		assertEquals("there should be an output ports child (even if its empty)",1,el.getChildren("outputPorts").size());
	}
	
	@Test
	public void testDispatchLayerSerialization() throws Exception {
		Parallelize layer = new Parallelize();
		layer.configure(new ParallelizeConfig());
		Element el = serializer.dispatchLayerToXML(layer);
		
		logger.info("layer serialization xml = "+elementToString(el));
		
		assertEquals("element should have name dispatchLayer","dispatchLayer",el.getName());
		Element classChild = el.getChild("class");
		
		assertNotNull("There should be a child called class",classChild);
		assertEquals("Incorrect class name for Parellalize","net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize",classChild.getText());
		assertEquals("There should be a child called java (that described the config bean)",1,el.getChildren("java").size());
		
		Element javaElement = el.getChild("java");
		assertEquals("the java child element,object, should have a class attribute that describes the ParellizeConfig bean","net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ParallelizeConfig",javaElement.getChild("object").getAttribute("class").getValue());
	}
	
	@Test
	public void testDispatchStackSerialization() throws Exception {
		Processor p = edits.createProcessor("test");
		edits.getAddDispatchLayerEdit(p.getDispatchStack(), new Invoke(), 0).doEdit();
		edits.getAddDispatchLayerEdit(p.getDispatchStack(), new Failover(), 1).doEdit();
		Element el = serializer.dispatchStackToXML(p.getDispatchStack());
		
		assertEquals("root name should be dispatchStack","dispatchStack",el.getName());
		assertEquals("there should be 2 inner layer elements",2,el.getChildren("dispatchLayer").size());
		
		Element firstLayer = (Element)el.getChildren("dispatchLayer").get(0);
		assertEquals("child for layer define the class for the Invoke layer","net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke",firstLayer.getChild("class").getText());
		
		logger.info("stack serialization xml = "+elementToString(el));
	}
	
	@Test
	public void testIterationStrategyStack() throws Exception {
		NamedInputPortNode nipn1 = new NamedInputPortNode("a", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("b", 0);
		IterationStrategyImpl strat = new IterationStrategyImpl();
		
		DotProduct dp = new DotProduct();
		nipn1.setParent(dp);
		nipn2.setParent(dp);
		dp.setParent(strat.getTerminal());
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);
		
		IterationStrategyStackImpl stack = new IterationStrategyStackImpl();
		stack.addStrategy(strat);
		
		Element el = serializer.iterationStrategyStackToXML(stack);
		
		logger.info("Iteration strategy stack xml = "+elementToString(el));
		
		assertEquals("root name should be iterationStrategyStack","iterationStrategyStack",el.getName());
		assertEquals("child name should be iteration","iteration",el.getChild("iteration").getName());
		Element iteration=el.getChild("iteration");
		assertEquals("there should be 1 child named strategy",1,iteration.getChildren("strategy").size());
		Element strategy=iteration.getChild("strategy");
		assertEquals("there should be 1 child named dot",1,strategy.getChildren("dot").size());
		assertEquals("there should be no child named cross",0,strategy.getChildren("cross").size());
		Element dot=strategy.getChild("dot");
		assertEquals("wrong xml for dot","<dot><port name=\"a\" depth=\"0\" /><port name=\"b\" depth=\"0\" /></dot>",elementToString(dot));
		
	}
	
	@Test
	public void testConditions() throws Exception {
		Processor control = edits.createProcessor("control");
		Processor target = edits.createProcessor("target");
		edits.getCreateConditionEdit(control, target).doEdit();
		
		
		List<Processor> processors = new ArrayList<Processor>();
		processors.add(control);
		processors.add(target);
		
		Element el = serializer.conditionsToXML(processors);
		
		logger.info("condition serialized xml="+elementToString(el));
		
		assertEquals("root name should be conditions","conditions",el.getName());
		assertEquals("there should be 1 child condition",1,el.getChildren("condition").size());
		Element condition = el.getChild("condition");
		assertEquals("incorrect condition xml","<condition control=\"control\" target=\"target\" />",elementToString(condition));
		
	}
	
	private String elementToString(Element element) {
		return new XMLOutputter().outputString(element);
	}

}
