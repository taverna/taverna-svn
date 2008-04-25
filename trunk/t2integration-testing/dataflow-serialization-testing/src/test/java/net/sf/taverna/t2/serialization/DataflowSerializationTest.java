package net.sf.taverna.t2.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.testing.DataflowTranslationHelper;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.Deserializer;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.Serializer;
import net.sf.taverna.t2.workflowmodel.serialization.SerializerImpl;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Ignore;
import org.junit.Test;


public class DataflowSerializationTest extends DataflowTranslationHelper {
	
	private static Serializer serializer = new SerializerImpl();
	private static Deserializer deserializer = new DeserializerImpl();
	
	@Test
	@Ignore("Not fully implemented yet")
	public void testSimpleSerialization() throws Exception {
		Dataflow df = translateScuflFile("simple_workflow_with_input.xml");
		Element el = serializer.serializeDataflowToXML(df);	
		Dataflow df2=deserializer.deserializeDataflowFromXML(el);
		
		System.out.println(new XMLOutputter().outputString(el));
		
		assertNotNull("deserialized dataflow must not be null",df2);
		assertTrue("The deserialized dataflow is not validated",df2.checkValidity().isValid());
		
		Element el2 = serializer.serializeDataflowToXML(df2);
		XMLOutputter outputter = new XMLOutputter();
		assertEquals("XML of for round trip serialized dataflow should match",outputter.outputString(el),outputter.outputString(el2));
	}

	
}
