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
 * Revision           $Revision: 1.20 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-20 11:33:59 $
 *               by   $Author: sowen70 $
 * Created on 7 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;
import uk.org.mygrid.dataproxy.xml.InterceptingXMLStreamParser;
import uk.org.mygrid.dataproxy.xml.impl.DataURLInterceptor;
import uk.org.mygrid.dataproxy.xml.impl.FileInterceptorWriterFactory;
import uk.org.mygrid.dataproxy.xml.impl.RequestXMLStreamParserImpl;
import uk.org.mygrid.dataproxy.xml.impl.ResponseTagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.ResponseXMLStreamParserImpl;

/**
 * The servlet responsible for acting as a proxying webservice endpoint. It pipes the incoming SOAP stream, de-referencing any data urls, and forwarding to the original endpoint.
 * The received SOAP message response from the 'real' webservice is parsed, and if any elements match those that have been marked for referencing then 
 * the incoming data stream is redirectred to a file until the matching closing tag is met. The data itself is replaced with a URL to the data, using the
 * DataServlet.
 * 
 * @see uk.org.mygrid.dataproxy.web.servlet.DataServlet
 * 
 * @author Stuart Owen
 */

@SuppressWarnings("serial")
public class ProxyServlet extends ProxyBaseServlet {
		
	private static Logger logger = Logger.getLogger(ProxyServlet.class);		

	public ProxyServlet() {		
		logger.info("Instantiating Proxy Servlet.");					
	}	

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setContextOnServerInfo();
		
		String id=request.getParameter("id");						
		String ep=request.getParameter("ep");
		logger.info("Proxying request for wsdlID="+id+", ep="+ep);
				
		WSDLConfig wsdlConfig = ProxyConfigFactory.getInstance().getWSDLConfigForID(id);
		if (wsdlConfig==null) {
			logger.error("Identifier "+id+" not recognised when looking for WSDL configuration details");
			throw new ServletException("Identifier "+id+" not recognised when looking for WSDL configuration details");
		}
		
		String endpoint = determineEndpoint(ep, wsdlConfig);

		HttpURLConnection connection = createEndpointConnection(endpoint, request.getHeader("SOAPAction"));		
		
		InterceptingXMLStreamParser requestParser = new RequestXMLStreamParserImpl();
		requestParser.addContentInterceptor(new DataURLInterceptor());
				
		requestParser.setOutputStream(connection.getOutputStream());		
		
		try {
			requestParser.read(request.getInputStream());
		} catch (SAXException e2) {
			logger.error("Error parsing request SOAP message",e2);
		}					
		
		InputStream in;
		try {
			in = connection.getInputStream();
		}
		catch(IOException e) {
			//use error stream to get soap:fault.
			in = connection.getErrorStream();
		}
		
		String invocationID;
		URL dataStoreLocation;
		try {
			invocationID=generateInvocationID(wsdlConfig.getWSDLID());
			dataStoreLocation = getDataStoreLocation(wsdlConfig.getWSDLID(), invocationID);
		} catch (Exception e1) {
			logger.error("An error occurred creating the data store location");
			throw new ServletException("An error occurred creating the data store location",e1);
		}
		
		String baseReference = ProxyConfigFactory.getInstance().getContextPath()+"data?id="+wsdlConfig.getWSDLID()+"-"+invocationID;
		
		InterceptingXMLStreamParser responseParser = new ResponseXMLStreamParserImpl();	
		for (ElementDefinition elementDef : wsdlConfig.getElements()) {
			String prefix=elementDef.getElementName();
			if (prefix.equals("*")) {
				prefix = createPrefixForWildcard(elementDef);
			}
			responseParser.addTagInterceptor(new ResponseTagInterceptorImpl(elementDef,new FileInterceptorWriterFactory(dataStoreLocation,baseReference,prefix)));
		}
			
		responseParser.setOutputStream(response.getOutputStream());
						
		response.setContentType("text/html");
		
		try {
			responseParser.read(in);
		} catch (SAXException e) {
			logger.error("Error parsing SOAP response",e);
		}		
		
		clearEmptyDirectories(dataStoreLocation);
	}

	private String determineEndpoint(String ep, WSDLConfig wsdlConfig) {
		String endpoint=null;
		try {
			int endpointIndex=Integer.valueOf(ep);
			if (endpointIndex >=0 && endpointIndex < wsdlConfig.getEndpoints().size()) {
				endpoint=wsdlConfig.getEndpoints().get(endpointIndex);
			}
			else {
				logger.error("Endpoint index is out of range, using the first endpoint");
			}
		}
		catch(NumberFormatException e) {
			logger.error("ep is not a number",e);
		}
		
		if (endpoint==null) endpoint=wsdlConfig.getEndpoints().get(0);
		return endpoint;
	}

	private String createPrefixForWildcard(ElementDefinition elementDef) {
		String prefix=null;
		String path=elementDef.getPath();
		String bits[]=path.split("/"); 
		for (int i=(bits.length-1);i>=0;i--) { //use an earlier part of the path as the data prefix, appended with _item
			if (!bits[i].equals("*")) {
				prefix=bits[i]+"_item";
				break;
			}			
		}
		
		//failings this use part of a UUID
		if (prefix==null) prefix=UUID.randomUUID().toString().split("-")[0];
		return prefix;
	}	
	
	private void clearEmptyDirectories(URL dataDirectory) {		
		try {
			File dir = new File(dataDirectory.toURI());
			if (dir.exists()) {
				if (dir.isDirectory()) {
					if (dir.listFiles().length==0) {
						dir.delete();
					}
				}
			}
		} catch (URISyntaxException e) {
			logger.error("Error deleting empty data directory");
		}		
	}
	
	private String generateInvocationID(String wsdlID) throws URISyntaxException {
		URL base = ProxyConfigFactory.getInstance().getStoreBaseURL();
		File dir = new File(base.toURI());
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		dir=new File(dir,wsdlID);
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		String invID=UUID.randomUUID().toString().split("-")[0];
		while (true) {
			if (new File(dir,invID).exists()) {
				invID=UUID.randomUUID().toString().split("-")[0];
			}
			else {
				break;
			}			
		}
		return invID;
	}
	
	private URL getDataStoreLocation(String wsdlID, String invocationID) throws Exception {		
		
		URL base = ProxyConfigFactory.getInstance().getStoreBaseURL();
		File dir = new File(base.toURI());
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		dir=new File(dir,wsdlID);
		if (!dir.exists()) {
			dir.mkdir();
		}
					
		dir = new File(dir,invocationID);
		if (!dir.exists()) {
			dir.mkdir();
		}
				
		return dir.toURL();
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
