package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.*;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

public class SerializerImplTest implements XMLSerializationConstants{
	
	private static Logger logger = Logger.getLogger(SerializerImplTest.class);
	
	private XMLSerializerImpl serializer=new XMLSerializerImpl();
	private Edits edits = EditsRegistry.getEdits();
	
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
	
	@Test
	public void testWithNested() throws Exception {
		DataflowImpl df = (DataflowImpl)edits.createDataflow();
		df.setLocalName("main_dataflow");
		DataflowImpl innerDf = (DataflowImpl)edits.createDataflow();
		innerDf.setLocalName("inner_dataflow");
		DummyDataflowActivity a = new DummyDataflowActivity();
		a.configure(innerDf);
		Processor p = edits.createProcessor("proc");
		edits.getAddActivityEdit(p, a).doEdit();
		edits.getAddProcessorEdit(df, p).doEdit();
		
		Element el = serializer.serializeDataflow(df);
		assertEquals("root should be workflow","workflow",el.getName());
		assertEquals("there should be 2 child 'dataflow'",2,el.getChildren("dataflow",T2_WORKFLOW_NAMESPACE).size());
		
		Element elOuterDf = (Element)el.getChildren("dataflow",T2_WORKFLOW_NAMESPACE).get(0);
		Element elInnerDf = (Element)el.getChildren("dataflow",T2_WORKFLOW_NAMESPACE).get(1);
		
		assertEquals("first df should be role=top","top",elOuterDf.getAttribute("role").getValue());
		assertEquals("inner df should be role=nested","nested",elInnerDf.getAttribute("role").getValue());
		assertNotNull("inner df should have attribute id=inner_dataflow",elInnerDf.getAttributeValue("id"));	
	}
	
	@Test
	//test for df that has a df activity that itself contains a df activity
	public void testWithNestedNested() throws Exception
	{
		DataflowImpl df = (DataflowImpl)edits.createDataflow();
		df.setLocalName("main_dataflow");
		DataflowImpl innerDf = (DataflowImpl)edits.createDataflow();
		innerDf.setLocalName("inner_dataflow");
		DummyDataflowActivity a = new DummyDataflowActivity();
		a.configure(innerDf);
		Processor p = edits.createProcessor("proc");
		edits.getAddActivityEdit(p, a).doEdit();
		edits.getAddProcessorEdit(df, p).doEdit();
		
		DataflowImpl innerInnerDf = (DataflowImpl)edits.createDataflow();
		innerInnerDf.setLocalName("inner_inner_dataflow");
		DummyDataflowActivity a2 = new DummyDataflowActivity();
		a2.configure(innerInnerDf);
		Processor p2 = edits.createProcessor("proc2");
		edits.getAddActivityEdit(p2, a2).doEdit();
		edits.getAddProcessorEdit(innerDf, p2).doEdit();
		
		Element el = serializer.serializeDataflow(df);
		assertEquals("root should be workflow","workflow",el.getName());
		assertEquals("there should be 3 child 'dataflow'",3,el.getChildren("dataflow",T2_WORKFLOW_NAMESPACE).size());
		
		Element elOuterDf = (Element)el.getChildren("dataflow",T2_WORKFLOW_NAMESPACE).get(0);
		Element elInnerDf = (Element)el.getChildren("dataflow",T2_WORKFLOW_NAMESPACE).get(1);
		Element elInnerInnerDf = (Element)el.getChildren("dataflow",T2_WORKFLOW_NAMESPACE).get(2);
		
		assertEquals("first df should be role=top","top",elOuterDf.getAttribute("role").getValue());
		assertEquals("inner df should be role=nested","nested",elInnerDf.getAttribute("role").getValue());
		assertNotNull("inner df should have attribute id=inner_dataflow",elInnerDf.getAttributeValue("id"));
		
		assertEquals("inner inner df should be role=nested","nested",elInnerInnerDf.getAttribute("role").getValue());
		assertNotNull("inner inner df should have attribute id=inner_dataflow",elInnerInnerDf.getAttributeValue("id"));
		
	}
}
