/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: TestXMLWSDLConfig.java,v $
 * Revision           $Revision: 1.12 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-19 16:30:15 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration.impl;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;

public class TestXMLWSDLConfig {

	@Test
	public void testSimple() throws Exception {
		String xml = "<wsdl><id>1</id><name>wsdl1</name><address>http://address</address><endpoints><endpoint>http://endpoint.cgi</endpoint></endpoints>";
		xml+="<elements><element><name>AnElement</name><namespaceURI>uri</namespaceURI><path>*/path</path><operation>op</operation></element></elements>";
		xml+="</wsdl>";
		
		WSDLConfig config = new XMLWSDLConfig(xmlToElement(xml));
		
		assertEquals("1",config.getWSDLID());
		assertEquals("http://address",config.getAddress());
		assertEquals(1,config.getEndpoints().size());
		assertEquals("http://endpoint.cgi",config.getEndpoints().get(0));
		assertEquals("wsdl1",config.getName());		
		assertEquals(1,config.getElements().size());
		
		ElementDefinition def = config.getElements().get(0);
		
		assertEquals("AnElement",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());				
		assertEquals("*/path",def.getPath());
		assertEquals("op",def.getOperation());
	}
	
	@Test
	public void testMultipleElements() throws Exception {
		String xml = "<wsdl><id>1</id><name>wsdl1</name><address>http://address</address><endpoints><endpoint>http://endpoint.cgi</endpoint></endpoints><elements>";
		xml+="<element><name>AnElement</name><namespaceURI>uri</namespaceURI><path>*/AnElement</path><operation>op</operation></element>";
		xml+="<element><name>AnElement2</name><namespaceURI>uri</namespaceURI><path>*/AnElement2</path><operation>op</operation></element>";
		xml+="<element><name>AnElement3</name><namespaceURI>uri</namespaceURI><path>*/AnElement3</path><operation>op</operation></element>";
		xml+="</elements></wsdl>";
		
		WSDLConfig config = new XMLWSDLConfig(xmlToElement(xml));
		
		assertEquals("1",config.getWSDLID());
		assertEquals("http://address",config.getAddress());
		assertEquals(1,config.getEndpoints().size());
		assertEquals("http://endpoint.cgi",config.getEndpoints().get(0));
		assertEquals(3,config.getElements().size());
		
		ElementDefinition def = config.getElements().get(0);
		assertEquals("AnElement",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());	
		assertEquals("*/AnElement",def.getPath());
		assertEquals("op",def.getOperation());
		
		
		def = config.getElements().get(1);
		assertEquals("AnElement2",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());	
		assertEquals("*/AnElement2",def.getPath());
		assertEquals("op",def.getOperation());
		
		def = config.getElements().get(2);
		assertEquals("AnElement3",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());
		assertEquals("*/AnElement3",def.getPath());
		assertEquals("op",def.getOperation());
	}
	
	@Test
	public void testMissingNamespace() throws Exception {
		String xml = "<wsdl><id>1</id><name>wsdl1</name><address>http://address</address><endpoints><endpoint>http://endpoint.cgi</endpoint></endpoints><elements>";
		xml+="<element><name>AnElement</name><namespaceURI>uri</namespaceURI><path>*/AnElement</path><operation>op</operation></element>";
		xml+="<element><name>AnElement2</name><path>*/AnElement2</path><operation>op</operation></element>";		
		xml+="</elements></wsdl>";
		
		WSDLConfig config = new XMLWSDLConfig(xmlToElement(xml));
				
		assertEquals(2,config.getElements().size());
		
		ElementDefinition def = config.getElements().get(0);
		assertEquals("AnElement",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());	
		assertEquals("*/AnElement",def.getPath());
		assertEquals("op",def.getOperation());
		
		def = config.getElements().get(1);
		assertEquals("AnElement2",def.getElementName());
		assertEquals("",def.getNamespaceURI());	
		assertEquals("*/AnElement2",def.getPath());
		assertEquals("op",def.getOperation());
	}
	
	@Test
	public void testToXML() throws Exception {
		String xml = "<wsdl><id>1</id><name>wsdl1</name><address>http://address</address><endpoints><endpoint>http://endpoint.cgi</endpoint></endpoints>";
		xml+="<elements><element><name>AnElement</name><namespaceURI>uri</namespaceURI><path>*/path</path><operation>op</operation></element></elements>";
		xml+="</wsdl>";
				
		XMLWSDLConfig config = new XMLWSDLConfig(xmlToElement(xml));
		
		String xml2=config.toElement().asXML();
		
		assertEquals("XML is incorrect",xml,xml2);
		
	}
			
	private Element xmlToElement(String xml) throws Exception {
		SAXReader reader = new SAXReader();
		return reader.read(new ByteArrayInputStream(xml.getBytes())).getRootElement();		
	}
}
