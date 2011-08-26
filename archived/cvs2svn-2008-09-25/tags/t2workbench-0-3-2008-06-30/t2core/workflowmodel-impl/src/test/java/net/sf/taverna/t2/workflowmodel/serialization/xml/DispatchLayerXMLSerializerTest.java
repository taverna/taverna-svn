package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ParallelizeConfig;

import org.jdom.Element;
import org.junit.Test;


public class DispatchLayerXMLSerializerTest implements XMLSerializationConstants {
	DispatchLayerXMLSerializer serializer = DispatchLayerXMLSerializer.getInstance();
	
	@Test
	public void testDispatchLayerSerialization() throws Exception {
		Parallelize layer = new Parallelize();
		layer.configure(new ParallelizeConfig());
		Element el = serializer.dispatchLayerToXML(layer);
		
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
	
}
