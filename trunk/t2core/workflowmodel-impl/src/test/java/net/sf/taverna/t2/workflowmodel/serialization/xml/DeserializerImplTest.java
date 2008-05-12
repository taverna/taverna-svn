package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;

import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;

public class DeserializerImplTest extends DeserializerTestsHelper {

	private XMLDeserializerImpl deserializer = new XMLDeserializerImpl();

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDeserialize() throws Exception {
		
			Element element = new Element("workflow",Namespace.getNamespace("http://taverna.sf.net/2008/xml/t2flow"));
			Element innerDataflow = loadXMLFragment("empty_dataflow_with_ports.xml");
			innerDataflow.setAttribute("role","top");
			element.addContent(innerDataflow);
			Dataflow df = deserializer.deserializeDataflow(element);
			
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
	
	
	

}
