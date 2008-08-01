package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Failover;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;

import org.jdom.Element;
import org.junit.Test;



public class DispatchStackXMLSerializerTest implements XMLSerializationConstants {
	DispatchStackXMLSerializer serializer = DispatchStackXMLSerializer.getInstance();
	Edits edits = EditsRegistry.getEdits();
	
	@Test
	public void testDispatchStackSerialization() throws Exception {
		Processor p = edits.createProcessor("test");
		edits.getAddDispatchLayerEdit(p.getDispatchStack(), new Invoke(), 0).doEdit();
		edits.getAddDispatchLayerEdit(p.getDispatchStack(), new Failover(), 1).doEdit();
		Element el = serializer.dispatchStackToXML(p.getDispatchStack());
		
		
		assertEquals("root name should be dispatchStack","dispatchStack",el.getName());
		assertEquals("there should be 2 inner layer elements",2,el.getChildren("dispatchLayer",T2_WORKFLOW_NAMESPACE).size());
		
		Element firstLayer = (Element)el.getChildren("dispatchLayer",T2_WORKFLOW_NAMESPACE).get(0);
		assertEquals("child for layer define the class for the Invoke layer","net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke",firstLayer.getChild("class",T2_WORKFLOW_NAMESPACE).getText());
	}

}
