package net.sf.taverna.t2.activities.wsdl.xmlsplitter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;

import org.junit.Test;

public class XMLOutputSplitterActivityTest {

	@Test
	public void testGetTypeDescriptorForOutputPort() throws Exception {
		
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"getPersonResponse\" name=\"parameters\" qname=\"{http://testing.org}getPersonResponse\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"getPersonReturn\" qname=\"{http://testing.org}&gt;getPersonResponse&gt;getPersonReturn\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://testing.org}Person&gt;address\"><s:elements><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://testing.org}Address&gt;city\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"int\" name=\"number\" qname=\"{http://testing.org}Address&gt;number\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://testing.org}Address&gt;road\" /></s:elements></s:complextype><s:basetype optional=\"false\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://testing.org}Person&gt;age\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"name\" qname=\"{http://testing.org}Person&gt;name\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>";
		XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder.buildBeanForOutput(xml);
		XMLOutputSplitterActivity a = new XMLOutputSplitterActivity();
		a.configure(bean);
		
		assertNotNull("There should be a descriptor for the port getPersonReturn",a.getTypeDescriptorForOutputPort("getPersonReturn"));
		assertTrue("The descriptor should be complex",a.getTypeDescriptorForOutputPort("getPersonReturn") instanceof ComplexTypeDescriptor);
	
	}
}
