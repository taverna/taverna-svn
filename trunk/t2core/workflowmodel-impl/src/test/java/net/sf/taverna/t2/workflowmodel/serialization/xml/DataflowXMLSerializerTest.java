package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.jdom.Element;
import org.junit.Test;



public class DataflowXMLSerializerTest implements XMLSerializationConstants {
	DataflowXMLSerializer serializer = DataflowXMLSerializer.getInstance();
	Edits edits = new EditsImpl();
	
	@Test
	public void testDataflowInputPorts() throws Exception {
		Dataflow df = edits.createDataflow();
		edits.getCreateDataflowInputPortEdit(df, "dataflow_in", 1, 0).doEdit();
		
		Element el = serializer.dataflowInputPorts(df.getInputPorts());
		
		
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
		Element el = serializer.dataflowOutputPorts(df.getOutputPorts());
		
		assertEquals("root name should be outputPorts","outputPorts",el.getName());
		assertEquals("there should be 1 child called port",1,el.getChildren("port",T2_WORKFLOW_NAMESPACE).size());
		Element port=el.getChild("port",T2_WORKFLOW_NAMESPACE);
		assertEquals("name should be dataflow_out","dataflow_out",port.getChild("name",T2_WORKFLOW_NAMESPACE).getText());
	}
	
	@Test
	public void testDataflowNamespace() throws Exception {
		Dataflow df = edits.createDataflow();
		Element el = serializer.serializeDataflow(df);
		assertEquals("Incorrect namespace","http://taverna.sf.net/2008/xml/t2flow",el.getNamespace().getURI());
		
		Element child = el.getChild("inputPorts",T2_WORKFLOW_NAMESPACE);
		assertEquals("Children should also have the correct namespace","http://taverna.sf.net/2008/xml/t2flow",child.getNamespace().getURI());
	}

}
