package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

public class SerializerImplTest implements XMLSerializationConstants{
	
	private static Logger logger = Logger.getLogger(SerializerImplTest.class);
	
	private XMLSerializerImpl serializer=new XMLSerializerImpl();
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
	
	
	private String elementToString(Element element) {
		return new XMLOutputter().outputString(element);
	}
	

}
