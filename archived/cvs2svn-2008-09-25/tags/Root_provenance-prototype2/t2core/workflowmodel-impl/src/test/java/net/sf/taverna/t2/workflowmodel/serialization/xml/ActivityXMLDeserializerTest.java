package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.serialization.DummyActivity;

import org.jdom.Element;
import org.junit.Test;



public class ActivityXMLDeserializerTest extends DeserializerTestsHelper {
		ActivityXMLDeserializer deserializer = ActivityXMLDeserializer.getInstance();

		@Test
		public void testActivityDeserialization() throws Exception {
			Element el = loadXMLFragment("activity.xml");
			Activity<?> activity = deserializer.deserializeActivity(el,new HashMap<String,Element>());
			
			assertNotNull("The activity should not be NULL",activity);
			assertTrue("should be a DummyActivity",activity instanceof DummyActivity);
			assertTrue("bean should be an Integer",activity.getConfiguration() instanceof Integer);
			assertEquals("bean should equal 5",5,((Integer)activity.getConfiguration()).intValue());
			
			assertEquals("there should be 1 input port mapping",1,activity.getInputPortMapping().size());
			assertEquals("there should be 1 output port mapping",1,activity.getOutputPortMapping().size());
			
			assertEquals("input in is mapped to in","in",activity.getInputPortMapping().get("in"));
			assertEquals("output out is mapped to out","out",activity.getOutputPortMapping().get("out"));
		}

}
