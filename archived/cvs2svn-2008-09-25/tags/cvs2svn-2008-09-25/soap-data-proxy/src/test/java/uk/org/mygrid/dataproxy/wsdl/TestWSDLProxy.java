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
 * Filename           $RCSfile: TestWSDLProxy.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-19 16:30:16 $
 *               by   $Author: sowen70 $
 * Created on 20 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.wsdl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.impl.NewWSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.impl.WSDLProxyImpl;

public class TestWSDLProxy {

	@Test
	public void testEndpointReWritten() throws Exception 
	{
		ProxyConfigFactory.getInstance().setContextPath("http://localhost:8080/dataproxy/");
		NewWSDLConfig config = new NewWSDLConfig();
		config.setAddress("http://www.cs.man.ac.uk/~sowen/proxytests/wsdls/gominer/GMService.wsdl");
		config.setWSDLID("1");
		WSDLProxy proxy = new WSDLProxyImpl(config);
		
		Document doc = new SAXReader().read(proxy.getStream());
		Element service=doc.getRootElement().element("service");
		Element port=service.element("port");
		Element address=port.element("address");		
		String endpoint=address.attributeValue("location").trim();
		
		assertEquals("Incorrect endpoint","http://localhost:8080/dataproxy/proxy?id=1&ep=0",endpoint);		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testImportReWritten() throws Exception
	{
		NewWSDLConfig config = new NewWSDLConfig();
		config.setAddress("http://www.cs.man.ac.uk/~sowen/proxytests/wsdls/eutils/eutils_lite.wsdl");
		config.setWSDLID("1");
		WSDLProxy proxy = new WSDLProxyImpl(config);
		
		ProxyConfigFactory.getInstance().setContextPath("http://localhost:8080/dataproxy/");
		
		Document doc = new SAXReader().read(proxy.getStream());
		
		Element types = doc.getRootElement().element("types");
		Element schema = types.element("schema");
		List<Element> imports = schema.elements("import");
		Element egquery = imports.get(0);
		assertEquals("Incorrect schemaLocation - should be rewritten to feed from servlet",egquery.attribute("schemaLocation").getText(),"schema?wsdlid=1&xsd=egquery.xsd");
		
		Element einfo = imports.get(1);
		assertEquals("Incorrect schemaLocation - should be rewritten to feed from servlet",einfo.attribute("schemaLocation").getText(),"schema?wsdlid=1&xsd=einfo.xsd");
	}
	
}
