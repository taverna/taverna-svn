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
 * Filename           $RCSfile: SchemaProxyImpl.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
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

import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.SchemaProxy;

/**
 * An implementation of a proxy that provides a stream to a modified schema. The schema is modified to
 * rewrite any additional schema includes or imports to also be proxied using the ViewSchemaServlet.
 *  
 *  @see uk.org.mygrid.dataproxy.web.servlet.ViewSchemaServlet
 *  @see uk.org.mygrid.dataproxy.wsdl.SchemaProxy
 *  
 *  @author Stuart Owen
 */

public class SchemaProxyImpl extends WSDLProxyImpl implements SchemaProxy {
	
	private static Logger logger = Logger.getLogger(SchemaProxyImpl.class);
		
	private String xsd;
	
	public SchemaProxyImpl(WSDLConfig config, String xsd) {		
		super(config);
		this.xsd = xsd;
	}

	public InputStream getStream() throws JaxenException, IOException, DocumentException {
		URL schemaUrl = new URL(getConfig().getAddress());
		schemaUrl=new URL(schemaUrl,xsd);
		logger.info("Proxying schema for address:"+schemaUrl.toExternalForm());
		Document doc = new SAXReader().read(schemaUrl.openStream());
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
		Dom4jXPath path = new Dom4jXPath("//s:schema/s:include");	
		path.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		path.addNamespace("s", "http://www.w3.org/2001/XMLSchema");
		
		List<Element> nodes=path.selectNodes(doc);
		rewriteSchemaLocations(nodes);
	}
	
	@SuppressWarnings("unchecked")
	protected void changeImports(Document doc) throws JaxenException {
		Dom4jXPath path = new Dom4jXPath("//s:schema/s:import");		
		path.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		path.addNamespace("s", "http://www.w3.org/2001/XMLSchema");
		
		List<Element> nodes=path.selectNodes(doc);
		rewriteSchemaLocations(nodes);		
	}		
}
