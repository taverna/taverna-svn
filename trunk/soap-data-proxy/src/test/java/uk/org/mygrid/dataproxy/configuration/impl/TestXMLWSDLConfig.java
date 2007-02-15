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
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-15 14:34:23 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration.impl;

import java.io.ByteArrayInputStream;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import static org.junit.Assert.*;

import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.xml.ElementDef;

public class TestXMLWSDLConfig {

	@Test
	public void testSimple() throws Exception {
		String xml = "<wsdl><id>1</id><address>http://address</address><endpoint>http://endpoint.cgi</endpoint>";
		xml+="<elements><element><name>AnElement</name><namespaceURI>uri</namespaceURI></element></elements>";
		xml+="</wsdl>";
		
		WSDLConfig config = new XMLWSDLConfig(xmlToElement(xml));
		
		assertEquals("1",config.getWSDLID());
		assertEquals("http://address",config.getAddress());
		assertEquals("http://endpoint.cgi",config.getEndpoint());
		assertEquals(1,config.getElements().size());
		
		ElementDef def = config.getElements().get(0);
		
		assertEquals("AnElement",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());				
	}
	
	@Test
	public void testMultipleElements() throws Exception {
		String xml = "<wsdl><id>1</id><address>http://address</address><endpoint>http://endpoint.cgi</endpoint><elements>";
		xml+="<element><name>AnElement</name><namespaceURI>uri</namespaceURI></element>";
		xml+="<element><name>AnElement2</name><namespaceURI>uri</namespaceURI></element>";
		xml+="<element><name>AnElement3</name><namespaceURI>uri</namespaceURI></element>";
		xml+="</elements></wsdl>";
		
		WSDLConfig config = new XMLWSDLConfig(xmlToElement(xml));
		
		assertEquals("1",config.getWSDLID());
		assertEquals("http://address",config.getAddress());
		assertEquals("http://endpoint.cgi",config.getEndpoint());
		assertEquals(3,config.getElements().size());
		
		ElementDef def = config.getElements().get(0);
		assertEquals("AnElement",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());		
		
		def = config.getElements().get(1);
		assertEquals("AnElement2",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());		
		
		def = config.getElements().get(2);
		assertEquals("AnElement3",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());		
	}
	
	@Test
	public void testMissingNamespace() throws Exception {
		String xml = "<wsdl><id>1</id><address>http://address</address><endpoint>http://endpoint.cgi</endpoint><elements>";
		xml+="<element><name>AnElement</name><namespaceURI>uri</namespaceURI><replacement>AnElement-replaced</replacement></element>";
		xml+="<element><name>AnElement2</name><replacement>AnElement2-replaced</replacement></element>";		
		xml+="</elements></wsdl>";
		
		WSDLConfig config = new XMLWSDLConfig(xmlToElement(xml));
				
		assertEquals(2,config.getElements().size());
		
		ElementDef def = config.getElements().get(0);
		assertEquals("AnElement",def.getElementName());
		assertEquals("uri",def.getNamespaceURI());		
		
		def = config.getElements().get(1);
		assertEquals("AnElement2",def.getElementName());
		assertEquals("",def.getNamespaceURI());		
	}
	
	
	
	private Element xmlToElement(String xml) throws Exception {
		SAXReader reader = new SAXReader();
		return reader.read(new ByteArrayInputStream(xml.getBytes())).getRootElement();		
	}
}
