package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Test;


public class ConditionXMLSerializerTest implements XMLSerializationConstants {
	
	private ConditionXMLSerializer serializer = ConditionXMLSerializer.getInstance();
	private Edits edits = EditsRegistry.getEdits();
	
	private String elementToString(Element element) {
		return new XMLOutputter().outputString(element);
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
		
		assertEquals("root name should be conditions","conditions",el.getName());
		assertEquals("there should be 1 child condition",1,el.getChildren("condition",T2_WORKFLOW_NAMESPACE).size());
		Element condition = el.getChild("condition",T2_WORKFLOW_NAMESPACE);
		condition.setNamespace(null); //remove the default namespace
		assertEquals("incorrect condition xml","<condition control=\"control\" target=\"target\" />",elementToString(condition));
		
	}
}
