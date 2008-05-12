package net.sf.taverna.t2.workflowmodel.serialization.xml;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import net.sf.taverna.t2.workflowmodel.serialization.DummyBean;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

public class AbstractXMLDeserializerTest {
	AbstractXMLDeserializer deserializer = new AbstractXMLDeserializer() {};
	
	@Test
	public void testCreateBeanSimple() throws Exception {
		Element el = new Element("configBean");
		el.setAttribute("encoding","xstream");
		Element elString = new Element("string");
		elString.setText("12345");
		el.addContent(elString);
		
		Object bean = deserializer.createBean(el, XMLDeserializerImpl.class.getClassLoader());
		assertTrue("bean should be a String",bean instanceof String);
		assertEquals("string should equal 12345","12345",((String)bean));
	}
	
	@Test
	public void testCreateBeanComplex() throws Exception {
		String xml="<configBean encoding=\"xstream\"><net.sf.taverna.t2.workflowmodel.serialization.DummyBean><id>1</id><name>bob</name><innerBean><stuff>xyz</stuff></innerBean></net.sf.taverna.t2.workflowmodel.serialization.DummyBean></configBean>";
		Element el = new SAXBuilder().build(new StringReader(xml)).detachRootElement();
		
		Object bean = deserializer.createBean(el, XMLDeserializerImpl.class.getClassLoader());
		assertTrue("bean should be a DummyBean",bean instanceof DummyBean);
		DummyBean dummyBean = (DummyBean)bean;
		
		assertEquals("id should be 1",1,dummyBean.getId());
		assertEquals("namne should be bob","bob",dummyBean.getName());
		assertEquals("stuff should by xyz","xyz",dummyBean.getInnerBean().getStuff());
	}

}
