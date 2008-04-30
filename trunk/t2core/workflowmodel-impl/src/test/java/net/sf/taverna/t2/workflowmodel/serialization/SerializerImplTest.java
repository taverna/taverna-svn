package net.sf.taverna.t2.workflowmodel.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.MergeImpl;
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

public class SerializerImplTest implements SerializationConstants{
	
	private static Logger logger = Logger.getLogger(SerializerImplTest.class);
	
	private SerializerImpl serializer=new SerializerImpl();
	private EditsImpl edits = new EditsImpl();
	
	@Test
	public void testDataflowSerialization() throws Exception {
		//FIXME: Update to include name - a Dataflow has a LocalName
		Dataflow df = edits.createDataflow();
		Element el = serializer.serializeDataflow(df);
		
		logger.info("workflow serialization xml = "+elementToString(el));
		
		assertEquals("root should be workflow","workflow",el.getName());
		assertEquals("there should be 1 child 'dataflow'",1,el.getChildren("dataflow",T2_WORKFLOW_NAMESPACE).size());
		Element dfElement = el.getChild("dataflow",T2_WORKFLOW_NAMESPACE);
		assertEquals("the inner dataflow should have a role as 'top'","top",dfElement.getAttribute("role").getValue());
	}
	
	@Test
	public void testDatalinks() throws Exception {
		Processor p = edits.createProcessor("top");
		Processor p2 = edits.createProcessor("bottom");
		edits.getCreateProcessorInputPortEdit(p2, "input", 0).doEdit();
		edits.getCreateProcessorOutputPortEdit(p, "output", 0, 0).doEdit();
		Datalink link = edits.createDatalink(p.getOutputPorts().get(0), p2.getInputPorts().get(0));
		List<Datalink> links = new ArrayList<Datalink>();
		links.add(link);
		
		Element el = serializer.datalinksToXML(links);
		
		logger.info("datalinks serialization xml = "+elementToString(el));
		
		assertEquals("Root name should be datalinks","datalinks",el.getName());
		assertEquals("there should be 1 child named datalink",1,el.getChildren("datalink",T2_WORKFLOW_NAMESPACE).size());
		
	}
	
	@Test
	public void testDatalinkMerge() throws Exception {
		Dataflow df = edits.createDataflow();
		Processor p = edits.createProcessor("top");
		Processor p2 = edits.createProcessor("bottom");
		edits.getAddProcessorEdit(df, p).doEdit();
		edits.getAddProcessorEdit(df, p2).doEdit();
		edits.getCreateProcessorInputPortEdit(p2, "input", 0).doEdit();
		edits.getCreateProcessorOutputPortEdit(p, "output", 0, 0).doEdit();
		Merge m =edits.createMerge(p2.getInputPorts().get(0));
		edits.getAddMergeEdit(df, m).doEdit();

		edits.getConnectMergedDatalinkEdit(m, p.getOutputPorts().get(0), p2.getInputPorts().get(0)).doEdit();
		
		Element el = serializer.datalinksToXML(df.getLinks());
		
		logger.info("datalinks serialization with merge: "+elementToString(el));
		assertEquals("root element should be datalinks","datalinks",el.getName());
		assertEquals("there should be 1 child datalink",1,el.getChildren("datalink",T2_WORKFLOW_NAMESPACE).size());
		
		String xml = elementToString(el);
		String expected="<datalinks xmlns=\""+T2_WORKFLOW_NAMESPACE.getURI()+"\"><datalink><sink type=\"merge\"><processor>bottom</processor><port>input</port></sink><source type=\"processor\"><processor>top</processor><port>output</port></source></datalink></datalinks>";
		assertEquals("Unexpected xml generated",expected,xml);
	}
	
	@Test
	public void testLinkedDataflowInputPort() throws Exception {
		Dataflow df = edits.createDataflow();
		edits.getCreateDataflowInputPortEdit(df, "dataflow_in", 0, 0).doEdit();
		Processor p=edits.createProcessor("p");
		edits.getCreateProcessorInputPortEdit(p, "p_in", 0).doEdit();
		Datalink link=edits.createDatalink(df.getInputPorts().get(0).getInternalOutputPort(), p.getInputPorts().get(0));
		edits.getConnectDatalinkEdit(link).doEdit();
		List<Datalink> links = new ArrayList<Datalink>();
		links.add(link);
		
		Element el = serializer.datalinksToXML(links);
		logger.info("datalinks serialization xml = "+elementToString(el));
		assertEquals("Root name should be datalinks","datalinks",el.getName());
		assertEquals("there should be 1 child named datalink",1,el.getChildren("datalink",T2_WORKFLOW_NAMESPACE).size());
		Element elLink=el.getChild("datalink",T2_WORKFLOW_NAMESPACE);
		assertEquals("There should be 1 sink",1,elLink.getChildren("sink",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("There should be 1 source",1,elLink.getChildren("source",T2_WORKFLOW_NAMESPACE).size());
		
		Element elSink=elLink.getChild("sink",T2_WORKFLOW_NAMESPACE);
		assertEquals("type should be processor","processor",elSink.getAttribute("type").getValue());
		assertEquals("processor name should be p","p",elSink.getChildText("processor",T2_WORKFLOW_NAMESPACE));
		assertEquals("port name should be p_in","p_in",elSink.getChildText("port",T2_WORKFLOW_NAMESPACE));
		
		Element elSource=elLink.getChild("source",T2_WORKFLOW_NAMESPACE);
		assertEquals("type should be dataflow","dataflow",elSource.getAttribute("type").getValue());
		assertEquals("port name should be dataflow_in","dataflow_in",elSource.getChildText("port",T2_WORKFLOW_NAMESPACE));
		
	}
	
	@Test
	public void testLinkedDataflowOutputPort() throws Exception {
		Dataflow df = edits.createDataflow();
		edits.getCreateDataflowOutputPortEdit(df, "dataflow_out").doEdit();
		Processor p=edits.createProcessor("p");
		edits.getCreateProcessorOutputPortEdit(p, "p_out", 0,0).doEdit();
		Datalink link=edits.createDatalink(p.getOutputPorts().get(0), df.getOutputPorts().get(0).getInternalInputPort());
		edits.getConnectDatalinkEdit(link).doEdit();
		List<Datalink> links = new ArrayList<Datalink>();
		links.add(link);
		
		Element el = serializer.datalinksToXML(links);
		logger.info("datalinks serialization xml = "+elementToString(el));
		assertEquals("Root name should be datalinks","datalinks",el.getName());
		assertEquals("there should be 1 child named datalink",1,el.getChildren("datalink",T2_WORKFLOW_NAMESPACE).size());
		Element elLink=el.getChild("datalink",T2_WORKFLOW_NAMESPACE);
		assertEquals("There should be 1 sink",1,elLink.getChildren("sink",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("There should be 1 source",1,elLink.getChildren("source",T2_WORKFLOW_NAMESPACE).size());
		
		Element elSink=elLink.getChild("sink",T2_WORKFLOW_NAMESPACE);
		assertEquals("type should be dataflow","dataflow",elSink.getAttribute("type").getValue());
		assertEquals("port name should be dataflow_out","dataflow_out",elSink.getChildText("port",T2_WORKFLOW_NAMESPACE));
		
		Element elSource=elLink.getChild("source",T2_WORKFLOW_NAMESPACE);
		assertEquals("type should be processor","processor",elSource.getAttribute("type").getValue());
		assertEquals("processor name should be p","p",elSource.getChildText("processor",T2_WORKFLOW_NAMESPACE));
		assertEquals("port name should be p_in","p_out",elSource.getChildText("port",T2_WORKFLOW_NAMESPACE));
	}
	
	@Test
	public void testDataflowInputPorts() throws Exception {
		Dataflow df = edits.createDataflow();
		edits.getCreateDataflowInputPortEdit(df, "dataflow_in", 1, 0).doEdit();
		
		Element el = serializer.dataflowInputPortsToXML(df.getInputPorts());
		
		logger.info("dataflow input ports xml = "+elementToString(el));
		
		assertEquals("root name should be inputPorts","inputPorts",el.getName());
		assertEquals("there should be 1 child called port",1,el.getChildren("port",T2_WORKFLOW_NAMESPACE).size());
		Element port=el.getChild("port",T2_WORKFLOW_NAMESPACE);
		assertEquals("name should be dataflow_in","dataflow_in",port.getChild("name",T2_WORKFLOW_NAMESPACE).getText());
		assertEquals("depth should be 1","1",port.getChild("depth",T2_WORKFLOW_NAMESPACE).getText());
		assertEquals("granular depth should be 0","0",port.getChild("granularDepth",T2_WORKFLOW_NAMESPACE).getText());
		
	}
	
	@Test
	public void testDataflowOutputPorts() throws Exception {
		Dataflow df = edits.createDataflow();
		edits.getCreateDataflowOutputPortEdit(df, "dataflow_out").doEdit();
		Element el = serializer.dataflowOutputPortsToXML(df.getOutputPorts());
		
		logger.info("dataflow output ports xml = "+elementToString(el));
		
		assertEquals("root name should be outputPorts","outputPorts",el.getName());
		assertEquals("there should be 1 child called port",1,el.getChildren("port",T2_WORKFLOW_NAMESPACE).size());
		Element port=el.getChild("port",T2_WORKFLOW_NAMESPACE);
		assertEquals("name should be dataflow_out","dataflow_out",port.getChild("name",T2_WORKFLOW_NAMESPACE).getText());
	}
	
	@Test
	public void testDatalink() throws Exception {
		Processor p = edits.createProcessor("top");
		Processor p2 = edits.createProcessor("bottom");
		edits.getCreateProcessorInputPortEdit(p2, "input", 0).doEdit();
		edits.getCreateProcessorOutputPortEdit(p, "output", 0, 0).doEdit();
		Datalink link = edits.createDatalink(p.getOutputPorts().get(0), p2.getInputPorts().get(0));
		edits.getConnectDatalinkEdit(link).doEdit();
		
		Element el = serializer.datalinkToXML(link);
		
		logger.info("Serialized datalink xml = "+elementToString(el));
		
		assertEquals("root name should be datalink","datalink",el.getName());
		assertEquals("there should be 1 child called source",1,el.getChildren("source",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("there should be 1 child called sink",1,el.getChildren("sink",T2_WORKFLOW_NAMESPACE).size());
		Element sink=el.getChild("sink",T2_WORKFLOW_NAMESPACE);
		Element source=el.getChild("source",T2_WORKFLOW_NAMESPACE);
		
		assertEquals("source processor should be called 'top'","top",source.getChild("processor",T2_WORKFLOW_NAMESPACE).getText());
		assertEquals("sink processor should be called 'bottom'","bottom",sink.getChild("processor",T2_WORKFLOW_NAMESPACE).getText());
		
		assertEquals("source port should be called 'output'","output",source.getChild("port",T2_WORKFLOW_NAMESPACE).getText());
		assertEquals("sink port should be called 'input'","input",sink.getChild("port",T2_WORKFLOW_NAMESPACE).getText());
	}
	
	@Test
	public void testActivitySerialization() throws Exception
	{
		DummyActivity activity=new DummyActivity();
		activity.getInputPortMapping().put("in", "in");
		activity.getOutputPortMapping().put("out", "out");
		activity.configure(new Integer(5));
		Element el = serializer.activityToXML(activity);
		
		logger.info("activity serialization xml = "+elementToString(el));
		
		assertEquals("root element should be activity","activity",el.getName());
		Element classChild = el.getChild("class",T2_WORKFLOW_NAMESPACE);
		assertNotNull("there should be a child called class",classChild);
		assertEquals("incorrect activity class name","net.sf.taverna.t2.workflowmodel.serialization.DummyActivity",classChild.getText());
		assertEquals("there should be 1 inputMap child",1,el.getChildren("inputMap",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("there should be 1 outputMap child",1,el.getChildren("outputMap",T2_WORKFLOW_NAMESPACE).size());
		
		Element inputMap=el.getChild("inputMap",T2_WORKFLOW_NAMESPACE);
		Element outputMap=el.getChild("outputMap",T2_WORKFLOW_NAMESPACE);
		
		Element map=inputMap.getChild("map",T2_WORKFLOW_NAMESPACE);
		assertEquals("map to should be 'in'","in",map.getAttribute("to").getValue());
		assertEquals("map from should be 'in'","in",map.getAttribute("from").getValue());
		
		map=outputMap.getChild("map",T2_WORKFLOW_NAMESPACE);
		assertEquals("map to should be 'out'","out",map.getAttribute("to").getValue());
		assertEquals("map from should be 'out'","out",map.getAttribute("from").getValue());
		
		Element bean = el.getChild("configBean",T2_WORKFLOW_NAMESPACE);
		assertNotNull("there should be a child called configBean",bean);
		
		Element intChild = bean.getChild("int");
		assertNotNull("bean should have a child called int",intChild);
		assertEquals("java child should describe an int with value 5","<int>5</int>",elementToString(intChild));
	}
	
	@Test
	public void testProcessorSerialization() throws Exception {
		Processor p = edits.createProcessor("fred");
		edits.getCreateProcessorInputPortEdit(p, "input", 0).doEdit();
		edits.getCreateProcessorOutputPortEdit(p, "output", 1, 0).doEdit();
		
		Element el = serializer.processorToXML(p);
		
		logger.info("processor serialization xml = "+elementToString(el));
		
		assertNotNull("Element should not be null",el);
		
		assertEquals("root element should be processor","processor",el.getName());
		Element name=el.getChild("name",T2_WORKFLOW_NAMESPACE);
		assertNotNull("There should be a child called name",name);
		assertEquals("name should be fred","fred",name.getText());
		
		assertEquals("there should be an annotations child (even if its empty)",1,el.getChildren("annotations",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("there should be an activities child (even if its empty)",1,el.getChildren("activities",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("there should be an dispatch statck child (even if its empty)",1,el.getChildren("dispatchStack",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("there should be an iteration strategy stack child (even if its empty)",1,el.getChildren("iterationStrategyStack",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("there should be an input ports child (even if its empty)",1,el.getChildren("inputPorts",T2_WORKFLOW_NAMESPACE).size());
		Element inputPorts = el.getChild("inputPorts",T2_WORKFLOW_NAMESPACE);
		assertEquals("there should be 1 port element",1,inputPorts.getChildren("port",T2_WORKFLOW_NAMESPACE).size());
		Element port = inputPorts.getChild("port",T2_WORKFLOW_NAMESPACE);
		assertEquals("name should be input","input",port.getChild("name",T2_WORKFLOW_NAMESPACE).getText());
		assertEquals("depth should be 0","0",port.getChild("depth",T2_WORKFLOW_NAMESPACE).getText());
		Element outputPorts = el.getChild("outputPorts",T2_WORKFLOW_NAMESPACE);
		assertEquals("there should be an output ports child (even if its empty)",1,el.getChildren("outputPorts",T2_WORKFLOW_NAMESPACE).size());
		port = outputPorts.getChild("port",T2_WORKFLOW_NAMESPACE);
		assertEquals("name should be output","output",port.getChild("name",T2_WORKFLOW_NAMESPACE).getText());
		assertEquals("depth should be 1","1",port.getChild("depth",T2_WORKFLOW_NAMESPACE).getText());
		assertEquals("granularDepth should be 0","0",port.getChild("granularDepth",T2_WORKFLOW_NAMESPACE).getText());
	}
	
	@Test
	public void testDataflowNamespace() throws Exception {
		Dataflow df = edits.createDataflow();
		Element el = serializer.serializeDataflowToXML(df);
		assertEquals("Incorrect namespace","http://taverna.sf.net/2008/xml/t2flow",el.getNamespace().getURI());
		
		Element child = el.getChild("inputPorts",T2_WORKFLOW_NAMESPACE);
		assertEquals("Children should also have the correct namespace","http://taverna.sf.net/2008/xml/t2flow",child.getNamespace().getURI());
	}
	
	@Test
	public void testDispatchLayerSerialization() throws Exception {
		Parallelize layer = new Parallelize();
		layer.configure(new ParallelizeConfig());
		Element el = serializer.dispatchLayerToXML(layer);
		
		logger.info("layer serialization xml = "+elementToString(el));
		
		assertEquals("element should have name dispatchLayer","dispatchLayer",el.getName());
		Element classChild = el.getChild("class",T2_WORKFLOW_NAMESPACE);
		
		assertNotNull("There should be a child called class",classChild);
		assertEquals("Incorrect class name for Parellalize","net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize",classChild.getText());
		
		Element bean = el.getChild("configBean",T2_WORKFLOW_NAMESPACE);
		assertNotNull("there should be a child called configBean",bean);
		assertEquals("the type should be xstream","xstream",bean.getAttribute("encoding").getValue());
		assertEquals("there should be 1 child that describes the class",1,bean.getChildren().size());
		
		classChild=(Element)bean.getChildren().get(0);
		assertEquals("the element name should describe the Parallelize child","net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ParallelizeConfig",classChild.getName());
	}
	
	@Test
	public void testBeanAsElementSimple() throws Exception {
		String helloWorld="hello world";
		Element el = serializer.beanAsElement(helloWorld);
		assertEquals("root should be configBean","configBean",el.getName());
		assertEquals("the type should be xstream","xstream",el.getAttribute("encoding").getValue());
		assertEquals("there should be 1 string",1,el.getChildren("string").size());
		String innerXML=elementToString(el.getChild("string"));
		assertEquals("Unexpected xml for the string","<string>hello world</string>",innerXML);
	}
	
	@Test
	public void testBeanAsElementComplex() throws Exception {
		DummyBean bean = new DummyBean();
		InnerBean bean2 = new InnerBean();
		bean.setName("name");
		bean.setId(5);
		bean2.setStuff("stuff");
		bean.setInnerBean(bean2);
		Element el = serializer.beanAsElement(bean);
		assertEquals("root should be configBean","configBean",el.getName());
		assertEquals("the type should be xstream","xstream",el.getAttribute("encoding").getValue());
		assertEquals("there should be 1 DummyBean child",1,el.getChildren("net.sf.taverna.t2.workflowmodel.serialization.DummyBean").size());
		Element beanElement = el.getChild("net.sf.taverna.t2.workflowmodel.serialization.DummyBean");
		assertNotNull("there should be a child id",beanElement.getChild("id"));
		assertEquals("id child should have value 5","5",beanElement.getChild("id").getText());
		assertNotNull("there should be a child name",beanElement.getChild("name"));
		assertEquals("name child should have text name","name",beanElement.getChild("name").getText());
		assertNotNull("there should be a child innerBean",beanElement.getChild("innerBean"));
		Element innerBeanElement = beanElement.getChild("innerBean");
		assertNotNull("innerBean should have a child stuff",innerBeanElement.getChild("stuff"));
		assertEquals("stuff child should have text stuff","stuff",innerBeanElement.getChild("stuff").getText());
		
	}
	
	@Test
	public void testElementBean() throws Exception {
		Element person = new Element("person");
		person.addContent(new Element("name"));
		person.getChild("name").setText("fred smith");
		
		Element el = serializer.beanAsElement(person);
		assertEquals("root should be configBean","configBean",el.getName());
		assertEquals("the type should be jdomxml","jdomxml",el.getAttribute("encoding").getValue());
		assertEquals("there should be 1 person",1,el.getChildren("person").size());
		
		Element person2=el.getChild("person");
		
		assertEquals("XML for person should match",elementToString(person),elementToString(person2));
		
	}
	
	@Test
	public void testDispatchStackSerialization() throws Exception {
		Processor p = edits.createProcessor("test");
		edits.getAddDispatchLayerEdit(p.getDispatchStack(), new Invoke(), 0).doEdit();
		edits.getAddDispatchLayerEdit(p.getDispatchStack(), new Failover(), 1).doEdit();
		Element el = serializer.dispatchStackToXML(p.getDispatchStack());
		
		logger.info("stack serialization xml = "+elementToString(el));
		
		assertEquals("root name should be dispatchStack","dispatchStack",el.getName());
		assertEquals("there should be 2 inner layer elements",2,el.getChildren("dispatchLayer",T2_WORKFLOW_NAMESPACE).size());
		
		Element firstLayer = (Element)el.getChildren("dispatchLayer",T2_WORKFLOW_NAMESPACE).get(0);
		assertEquals("child for layer define the class for the Invoke layer","net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke",firstLayer.getChild("class",T2_WORKFLOW_NAMESPACE).getText());
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
		assertEquals("child name should be iteration","iteration",el.getChild("iteration",T2_WORKFLOW_NAMESPACE).getName());
		Element iteration=el.getChild("iteration",T2_WORKFLOW_NAMESPACE);
		assertEquals("there should be 1 child named strategy",1,iteration.getChildren("strategy",T2_WORKFLOW_NAMESPACE).size());
		Element strategy=iteration.getChild("strategy",T2_WORKFLOW_NAMESPACE);
		assertEquals("there should be 1 child named dot",1,strategy.getChildren("dot",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("there should be no child named cross",0,strategy.getChildren("cross",T2_WORKFLOW_NAMESPACE).size());
		Element dot=strategy.getChild("dot",T2_WORKFLOW_NAMESPACE);
		dot.setNamespace(null);
		for (Object child : dot.getChildren()) {
			((Element)child).setNamespace(null);
		}
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
		assertEquals("there should be 1 child condition",1,el.getChildren("condition",T2_WORKFLOW_NAMESPACE).size());
		Element condition = el.getChild("condition",T2_WORKFLOW_NAMESPACE);
		condition.setNamespace(null); //remove the default namespace
		assertEquals("incorrect condition xml","<condition control=\"control\" target=\"target\" />",elementToString(condition));
		
	}
	
	private String elementToString(Element element) {
		return new XMLOutputter().outputString(element);
	}

}
