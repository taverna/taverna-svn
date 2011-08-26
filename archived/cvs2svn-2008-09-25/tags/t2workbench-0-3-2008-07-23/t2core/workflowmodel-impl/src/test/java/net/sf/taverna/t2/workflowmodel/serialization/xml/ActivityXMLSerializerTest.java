package net.sf.taverna.t2.workflowmodel.serialization.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.serialization.DummyActivity;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Test;


public class ActivityXMLSerializerTest implements XMLSerializationConstants {
	ActivityXMLSerializer serializer = ActivityXMLSerializer.getInstance();
	Edits edits = new EditsImpl();
	
	@Test
	public void testActivitySerialization() throws Exception
	{
		DummyActivity activity=new DummyActivity();
		activity.getInputPortMapping().put("in", "in");
		activity.getOutputPortMapping().put("out", "out");
		activity.configure(new Integer(5));
		Element el = serializer.activityToXML(activity);
		
		assertEquals("root element should be activity","activity",el.getName());
		Element classChild = el.getChild("class",T2_WORKFLOW_NAMESPACE);
		assertNotNull("there should be a child called class",classChild);
		assertEquals("incorrect activity class name","net.sf.taverna.t2.workflowmodel.serialization.DummyActivity",classChild.getText());
		assertEquals("there should be 1 inputMap child",1,el.getChildren("inputMap",T2_WORKFLOW_NAMESPACE).size());
		assertEquals("there should be 1 outputMap child",1,el.getChildren("outputMap",T2_WORKFLOW_NAMESPACE).size());
		
		Element inputMap=el.getChild("inputMap",T2_WORKFLOW_NAMESPACE);
		Element outputMap=el.getChild("outputMap",T2_WORKFLOW_NAMESPACE);
		
		Element map=inputMap.getChild("map",T2_WORKFLOW_NAMESPACE);
		assertEquals("map to should be 'in'","in",map.getAttribute("to").getValue());
		assertEquals("map from should be 'in'","in",map.getAttribute("from").getValue());
		
		map=outputMap.getChild("map",T2_WORKFLOW_NAMESPACE);
		assertEquals("map to should be 'out'","out",map.getAttribute("to").getValue());
		assertEquals("map from should be 'out'","out",map.getAttribute("from").getValue());
		
		Element bean = el.getChild("configBean",T2_WORKFLOW_NAMESPACE);
		assertNotNull("there should be a child called configBean",bean);
		
		Element intChild = bean.getChild("int");
		assertNotNull("bean should have a child called int",intChild);
		assertEquals("java child should describe an int with value 5","<int>5</int>",elementToString(intChild));
	}
	
	@Test
	public void testDataflowBasedActivity() throws Exception {
		DummyDataflowActivity dummyDataflowActivity = new DummyDataflowActivity();
		Dataflow df = edits.createDataflow();
		((DataflowImpl)df).setLocalName("test");
		dummyDataflowActivity.configure(df);
		
		Element el = serializer.activityToXML(dummyDataflowActivity);
		
		assertEquals("There should be 1 configBean child element",1,el.getChildren("configBean",T2_WORKFLOW_NAMESPACE).size());
		
		Element configBeanEl = el.getChild("configBean",T2_WORKFLOW_NAMESPACE);
		assertEquals("The encoding type should be dataflow","dataflow",configBeanEl.getAttribute("encoding").getValue());
		assertEquals("There should be 1 child called dataflow",1,configBeanEl.getChildren("dataflow",T2_WORKFLOW_NAMESPACE).size());
		Element dataflowEl = configBeanEl.getChild("dataflow",T2_WORKFLOW_NAMESPACE);
		assertNotNull("there should be the attribute ref",dataflowEl.getAttribute("ref"));
	}
	
	private String elementToString(Element element) {
		return new XMLOutputter().outputString(element);
	}
}
