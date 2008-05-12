package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.jdom.Element;
import org.junit.Test;



public class ProcessorXMLSerializerTest implements XMLSerializationConstants {
	Edits edits = new EditsImpl();
	
	ProcessorXMLSerializer serializer = ProcessorXMLSerializer.getInstance();
	
	@Test
	public void testProcessorSerialization() throws Exception {
		Processor p = edits.createProcessor("fred");
		edits.getCreateProcessorInputPortEdit(p, "input", 0).doEdit();
		edits.getCreateProcessorOutputPortEdit(p, "output", 1, 0).doEdit();
		
		Element el = serializer.processorToXML(p);

		
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

}
