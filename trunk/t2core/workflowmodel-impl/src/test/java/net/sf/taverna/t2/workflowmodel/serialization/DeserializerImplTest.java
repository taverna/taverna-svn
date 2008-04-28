package net.sf.taverna.t2.workflowmodel.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DeserializerImplTest{

	private DeserializerImpl deserializer = new DeserializerImpl();

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testActivityDeserialization() throws Exception {
		Element el = loadXMLFragment("activity.xml");
		Activity<?> activity = deserializer.deserializeActivityFromXML(el);
		
		assertNotNull("The activity should not be NULL",activity);
		assertTrue("should be a DummyActivity",activity instanceof DummyActivity);
		assertTrue("bean should be an Integer",activity.getConfiguration() instanceof Integer);
		assertEquals("bean should equal 5",5,((Integer)activity.getConfiguration()).intValue());
		
		assertEquals("there should be 1 input port mapping",1,activity.getInputPortMapping().size());
		assertEquals("there should be 1 output port mapping",1,activity.getOutputPortMapping().size());
		
		assertEquals("input in is mapped to in","in",activity.getInputPortMapping().get("in"));
		assertEquals("output out is mapped to out","out",activity.getOutputPortMapping().get("out"));
	}
	
	@Test
	public void testCreateBeanSimple() throws Exception {
		Element el = new Element("configBean");
		el.setAttribute("encoding","xstream");
		Element elString = new Element("string");
		elString.setText("12345");
		el.addContent(elString);
		
		Object bean = deserializer.createBean(el, DeserializerImpl.class.getClassLoader());
		assertTrue("bean should be a String",bean instanceof String);
		assertEquals("string should equal 12345","12345",((String)bean));
	}
	
	@Test
	public void testCreateBeanComplex() throws Exception {
		String xml="<configBean encoding=\"xstream\"><net.sf.taverna.t2.workflowmodel.serialization.DummyBean><id>1</id><name>bob</name><innerBean><stuff>xyz</stuff></innerBean></net.sf.taverna.t2.workflowmodel.serialization.DummyBean></configBean>";
		Element el = new SAXBuilder().build(new StringReader(xml)).detachRootElement();
		
		Object bean = deserializer.createBean(el, DeserializerImpl.class.getClassLoader());
		assertTrue("bean should be a DummyBean",bean instanceof DummyBean);
		DummyBean dummyBean = (DummyBean)bean;
		
		assertEquals("id should be 1",1,dummyBean.getId());
		assertEquals("namne should be bob","bob",dummyBean.getName());
		assertEquals("stuff should by xyz","xyz",dummyBean.getInnerBean().getStuff());
	}

	protected Element loadXMLFragment(String resourceName) throws Exception {
		InputStream inStream = DeserializerImplTest.class
				.getResourceAsStream("/serialized-fragments/" + resourceName);

		if (inStream==null) throw new IOException("Unable to find resource for serialized fragment :"+resourceName);
		SAXBuilder builder = new SAXBuilder();
		return builder.build(inStream).detachRootElement();
	}

}
