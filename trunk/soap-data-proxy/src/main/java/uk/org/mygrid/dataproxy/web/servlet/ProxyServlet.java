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
 * Filename           $RCSfile: ProxyServlet.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-03-16 10:14:33 $
 *               by   $Author: sowen70 $
 * Created on 7 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.xml.ElementDef;
import uk.org.mygrid.dataproxy.xml.XMLStreamParser;
import uk.org.mygrid.dataproxy.xml.impl.FileInterceptorReaderFactory;
import uk.org.mygrid.dataproxy.xml.impl.FileInterceptorWriterFactory;
import uk.org.mygrid.dataproxy.xml.impl.RequestTagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.RequestXMLStreamParserImpl;
import uk.org.mygrid.dataproxy.xml.impl.ResponseTagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.ResponseXMLStreamParserImpl;

@SuppressWarnings("serial")
public class ProxyServlet extends HttpServlet {
		
	private static Logger logger = Logger.getLogger(ProxyServlet.class);		

	public ProxyServlet() {		
		logger.info("Instantiating Proxy Servlet.");					
	}
	
	private ProxyConfig getConfig() {		
		return ProxyConfigFactory.getInstance();
	}	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getConfig();		
		super.doGet(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String id=request.getParameter("id");				
		logger.info("Proxying request for wsdlID="+id);
		
		WSDLConfig wsdlConfig = getConfig().getWSDLConfigForID(id);
		if (wsdlConfig==null) {
			logger.error("Identifier "+id+" not recognised when looking for WSDL configuration details");
			throw new ServletException("Identifier "+id+" not recognised when looking for WSDL configuration details");
		}

		HttpURLConnection connection = createEndpointConnection(wsdlConfig.getEndpoint(), request.getHeader("SOAPAction"));
		
		XMLStreamParser requestParser = new RequestXMLStreamParserImpl();
		for (ElementDef elementDef : wsdlConfig.getElements()) {
			requestParser.addTagInterceptor(new RequestTagInterceptorImpl(elementDef,new FileInterceptorReaderFactory()));
		}
		
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		requestParser.setOutputStream(outstream);
		
		try {
			requestParser.read(request.getInputStream());
		} catch (SAXException e2) {
			logger.error("Error parsing request SOAP message",e2);
		}					
		
		String message = outstream.toString();		
		
		
		connection.getOutputStream().write(message.getBytes());
		InputStream in = connection.getInputStream();		
		
		URL dataStoreLocation;
		try {
			dataStoreLocation = getDataStoreLocation(wsdlConfig.getWSDLID());
		} catch (Exception e1) {
			logger.error("An error occurred creating the data store location");
			throw new ServletException("An error occurred creating the data store location",e1);
		}
		
		XMLStreamParser responseParser = new ResponseXMLStreamParserImpl();	
		for (ElementDef elementDef : wsdlConfig.getElements()) {
			responseParser.addTagInterceptor(new ResponseTagInterceptorImpl(elementDef,new FileInterceptorWriterFactory(dataStoreLocation,elementDef.getElementName())));
		}
			
		responseParser.setOutputStream(response.getOutputStream());
						
		response.setContentType("text/html");
		
		try {
			responseParser.read(in);
		} catch (SAXException e) {
			logger.error("Error parsing SOAP response",e);
		}		
	}	
	
	private URL getDataStoreLocation(String wsdlID) throws Exception {
		URL result = null;
		
		URL base = getConfig().getStoreBaseURL();
		File dir = new File(base.toURI());
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		while(true) {
			String uuid=UUID.randomUUID().toString();
			File invocationDir = new File(dir,uuid);
			if (!invocationDir.exists()) {
				invocationDir.mkdir();
				result=invocationDir.toURL();
				break;
			}
		}
		
		return result;
	}
	
	private HttpURLConnection createEndpointConnection(String endPoint, String soapAction) throws MalformedURLException, IOException, ProtocolException {
		URL endpointUrl=new URL(endPoint);
		HttpURLConnection connection = (HttpURLConnection)endpointUrl.openConnection();
		connection.setRequestMethod("POST");		
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setAllowUserInteraction(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type","text/xml");		
		connection.setRequestProperty("SOAPAction", soapAction);
		connection.connect();
		return connection;
	}
	
	

}
