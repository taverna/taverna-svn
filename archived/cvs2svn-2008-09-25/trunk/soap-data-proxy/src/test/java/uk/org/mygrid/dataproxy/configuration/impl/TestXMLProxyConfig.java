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
 * Filename           $RCSfile: TestXMLProxyConfig.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-19 16:30:15 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;

public class TestXMLProxyConfig {

	@Test
	public void testSimple() throws Exception {
		String xml="<config><contextPath>context</contextPath><store><baseURL>file:/tmp/</baseURL></store></config>";
		Element el = xmlToElement(xml);
		ProxyConfig config = new XMLProxyConfig(el);
		
		assertEquals("file:/tmp/",config.getStoreBaseURL().toExternalForm());
		assertEquals("context",config.getContextPath());
	}
	
	@Test
	public void testOneWSDLDefined() throws Exception {
		String wsdlXML="<wsdl><id>1</id><name>test wsdl</name><address>http://wsdl</address><endpoints><endpoint>http://endpoint</endpoint></endpoints><elements></elements></wsdl>";
		String xml="<config><contextPath>context</contextPath><store><baseURL>file:/url</baseURL></store><wsdls>"+wsdlXML+"</wsdls></config>";
		
		ProxyConfig config = new XMLProxyConfig(xmlToElement(xml));
		
		assertEquals("context",config.getContextPath());
		assertEquals("file:/url",config.getStoreBaseURL().toExternalForm());
		assertNotNull(config.getWSDLConfigForID("1"));
		
		WSDLConfig wsdlConfig = config.getWSDLConfigForID("1");
		
		assertEquals("1",wsdlConfig.getWSDLID());
		assertEquals("http://wsdl",wsdlConfig.getAddress());
		assertEquals(1,wsdlConfig.getEndpoints().size());
		assertEquals("http://endpoint",wsdlConfig.getEndpoints().get(0));
		assertEquals("test wsdl",wsdlConfig.getName());		
		assertEquals(0,wsdlConfig.getElements().size());
	}
	
	@Test
	public void testDeleteWSDL() throws Exception {
		String wsdlXML="<wsdl><id>1</id><name>test wsdl</name><address>http://wsdl</address><endpoints><endpoint>http://endpoint</endpoint></endpoints><elements></elements></wsdl>";
		String xml="<config><contextPath>context</contextPath><store><baseURL>file:/url</baseURL></store><wsdls>"+wsdlXML+"</wsdls></config>";
		
		ProxyConfig config = new XMLProxyConfig(xmlToElement(xml));
		
		WSDLConfig wsdlConfig = config.getWSDLConfigForID("1");
		config.deleteWSDLConfig(wsdlConfig);
		
		assertNull(config.getWSDLConfigForID("1"));
		
		xml = config.toStringForm();
		
		assertEquals("Incorrect xml",xml,"<config><contextPath>context</contextPath><store><baseURL>file:/url</baseURL></store></config>");
	}
	
	@Test
	public void testToStringForm() throws Exception {
		String xml="<config><contextPath>context</contextPath><store><baseURL>file:/tmp/</baseURL></store></config>";
		Element el = xmlToElement(xml);
		ProxyConfig config = new XMLProxyConfig(el);
		
		xml = config.toStringForm();
		config = new XMLProxyConfig(xmlToElement(xml));
		
		assertEquals("file:/tmp/",config.getStoreBaseURL().toExternalForm());
		assertEquals("context",config.getContextPath());
	}
	
	@Test
	public void testToStringFormWithOneWSDL() throws Exception {
		String wsdlXML="<wsdl><id>1</id><name>test wsdl</name><address>http://wsdl</address><endpoints><endpoint>http://endpoint</endpoint></endpoints><elements></elements></wsdl>";
		String xml="<config><contextPath>context</contextPath><store><baseURL>file:/url</baseURL></store><wsdls>"+wsdlXML+"</wsdls></config>";
		
		ProxyConfig config = new XMLProxyConfig(xmlToElement(xml));
		
		xml = config.toStringForm();
		config = new XMLProxyConfig(xmlToElement(xml));
		
		assertEquals("context",config.getContextPath());
		assertEquals("file:/url",config.getStoreBaseURL().toExternalForm());
		assertNotNull(config.getWSDLConfigForID("1"));
		
		WSDLConfig wsdlConfig = config.getWSDLConfigForID("1");
		
		assertEquals("1",wsdlConfig.getWSDLID());
		assertEquals("http://wsdl",wsdlConfig.getAddress());
		assertEquals(1,wsdlConfig.getEndpoints().size());
		assertEquals("http://endpoint",wsdlConfig.getEndpoints().get(0));
		assertEquals("test wsdl",wsdlConfig.getName());		
		assertEquals(0,wsdlConfig.getElements().size());
	}
	
	@Test
	public void testToStringWithNewWSDL() throws Exception {		
		String xml="<config><contextPath>context</contextPath><store><baseURL>file:/url</baseURL></store></config>";
		
		ProxyConfig config = new XMLProxyConfig(xmlToElement(xml));
		
		NewWSDLConfig newWsdl = new NewWSDLConfig();
		newWsdl.setWSDLID("1");
		newWsdl.setAddress("http://wsdl");
		newWsdl.addEndpoint("http://endpoint");
		newWsdl.setName("test wsdl");		
		config.addWSDLConfig(newWsdl);
		
		xml = config.toStringForm();
		config = new XMLProxyConfig(xmlToElement(xml));
		
		assertEquals("context",config.getContextPath());
		assertEquals("file:/url",config.getStoreBaseURL().toExternalForm());
		assertNotNull(config.getWSDLConfigForID("1"));
		
		WSDLConfig wsdlConfig = config.getWSDLConfigForID("1");
		
		assertEquals("1",wsdlConfig.getWSDLID());
		assertEquals("http://wsdl",wsdlConfig.getAddress());
		assertEquals(1,wsdlConfig.getEndpoints().size());
		assertEquals("http://endpoint",wsdlConfig.getEndpoints().get(0));
		assertEquals("test wsdl",wsdlConfig.getName());		
		assertEquals(0,wsdlConfig.getElements().size());
	}
		
	private Element xmlToElement(String xml) throws Exception {
		SAXReader reader = new SAXReader();
		return reader.read(new ByteArrayInputStream(xml.getBytes())).getRootElement();		
	}
	
	
	
}
