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
 * Filename           $RCSfile: WSDLProxyImpl.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-19 16:30:16 $
 *               by   $Author: sowen70 $
 * Created on 20 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.wsdl.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.WSDLProxy;

/**
 * An implementation of the WSDLProxy that rewrites any imported schemas to be proxied via the 
 * ViewSchemaServlet, and also rewrites the endpoint to use the ProxyServlet.
 * 
 * @see uk.org.mygrid.dataproxy.web.servlet.ViewSchemaServlet
 * @see uk.org.mygrid.dataproxy.web.servlet.ProxyServlet
 * 
 * @author Stuart Owen
 */

public class WSDLProxyImpl implements WSDLProxy {
	
	private static Logger logger = Logger.getLogger(WSDLProxyImpl.class);
	
	private WSDLConfig config;	

	/**
	 * Constructor requiring the WSDLConfig for the wsdl to be proxied.
	 * 
	 * @param config
	 */
	public WSDLProxyImpl(WSDLConfig config) {
		this.config = config;
	}

	public InputStream getStream() throws JaxenException, DocumentException, IOException {
		URL wsdlURL = new URL(config.getAddress());
		logger.info("Proxying wsdl for address:"+wsdlURL.toExternalForm());
		Document doc = changeEndpoints(new SAXReader().read(wsdlURL.openStream()));
		changeImports(doc);
		changeIncludes(doc);
		
		StringWriter stringWriter = new StringWriter();
		XMLWriter writer = new XMLWriter(stringWriter,OutputFormat.createPrettyPrint());
		writer.write(doc);
		writer.close();
		
		ByteArrayInputStream result = new ByteArrayInputStream(stringWriter.getBuffer().toString().getBytes());
		return result;
	}
	
	@SuppressWarnings("unchecked")
	protected void changeIncludes(Document doc) throws JaxenException
	{
		Dom4jXPath path = new Dom4jXPath("//wsdl:types/s:schema/s:include");	
		path.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		path.addNamespace("s", "http://www.w3.org/2001/XMLSchema");
		
		List<Element> nodes=path.selectNodes(doc);
		rewriteSchemaLocations(nodes);
	}
	
	@SuppressWarnings("unchecked")
	protected void changeImports(Document doc) throws JaxenException {
		Dom4jXPath path = new Dom4jXPath("//wsdl:types/s:schema/s:import");		
		path.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		path.addNamespace("s", "http://www.w3.org/2001/XMLSchema");
		
		List<Element> nodes=path.selectNodes(doc);
		rewriteSchemaLocations(nodes);		
	}

	protected void rewriteSchemaLocations(List<Element> nodes) {
		for (Element element : nodes) {
			String schemaLocation = element.attributeValue("schemaLocation");
			if (schemaLocation!=null) {
				element.attribute("schemaLocation").setValue("schema?wsdlid="+config.getWSDLID()+"&xsd="+schemaLocation);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private Document changeEndpoints(Document doc) throws JaxenException {			
		Dom4jXPath path = new Dom4jXPath("//wsdl:service/wsdl:port/soap:address");		
		path.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		path.addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
		List<Element> locations = (List<Element>)path.selectNodes(doc);		
		for (Element el : locations) {
			String originalEndpoint=el.attributeValue("location");
			int i=endpointIndex(originalEndpoint);
			el.attribute("location").setValue(ProxyConfigFactory.getInstance().getContextPath()+"proxy?id="+config.getWSDLID()+"&ep="+i);			
		}
		
		return doc;
	}
	
	private int endpointIndex(String endpoint) {		
		List<String> endpoints = config.getEndpoints();
		int i=0;
		for (String ep : endpoints) {
			if (ep.equals(endpoint)) {
				return i;
			}
			i++;
		}
		logger.error("Endpoint: '"+endpoint+"' not found in wsdl config (ID="+config.getWSDLID()+"), using an index of 0");
		return 0;
	}
		
	protected WSDLConfig getConfig() {
		return config;
	}
	
}
