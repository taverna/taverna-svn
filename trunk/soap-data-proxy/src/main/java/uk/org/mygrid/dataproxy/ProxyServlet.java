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
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-12 17:16:18 $
 *               by   $Author: sowen70 $
 * Created on 7 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import uk.org.mygrid.dataproxy.xml.TagInterceptor;
import uk.org.mygrid.dataproxy.xml.XMLStreamParser;
import uk.org.mygrid.dataproxy.xml.impl.FileInterceptorWriterFactory;
import uk.org.mygrid.dataproxy.xml.impl.TagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.XMLStreamParserImpl;

@SuppressWarnings("serial")
public class ProxyServlet extends HttpServlet {
	
	private Map<String,String> endPointMap=null;	
	private static Logger logger = Logger.getLogger(ProxyServlet.class);	
	
	
	public ProxyServlet() {	
		logger.info("Instantiating Proxy Servlet");
		if (endPointMap==null) {
			endPointMap = Collections.synchronizedMap(new HashMap<String,String>());
			endPointMap.put("11111", "http://www.ncbi.nlm.nih.gov/entrez/eutils/soap/soap_adapter_1_5.cgi");
			endPointMap.put("22222", "http://localhost:8080/testwebservices/services/MyService");
			endPointMap.put("33333", "http://localhost:8080/testwebservices/services/MyService");
		}
	}
	
	private String getEndpoint(String id) {
		return endPointMap.get(id);
	}
	
	private String receiveIncomingMessage(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		String result="";
		while ((line=reader.readLine())!=null) {
			logger.debug("Read Line from request stream:"+line);
			result+=line;
		}
		
		return result;
	}	

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String id=request.getParameter("id");
		String endPoint = getEndpoint(id);				
		logger.info("Proxying request for wsdlID="+id);
		
		String incomingMessage = receiveIncomingMessage(request.getInputStream());					

		HttpURLConnection connection = createEndpointConnection(endPoint, request.getHeader("SOAPAction"));		
		OutputStream out = connection.getOutputStream();
		out.write(incomingMessage.getBytes("UTF-8"));		
		
		InputStream in = connection.getInputStream();
		
		XMLStreamParser parser = new XMLStreamParserImpl();
		if (id.equals("11111")) {
			File tmpFile = File.createTempFile("FieldListProxy", "");
			tmpFile.delete();
			tmpFile.mkdir();
			
			TagInterceptor interceptor = new TagInterceptorImpl("FieldList","FieldList-proxied",new FileInterceptorWriterFactory(tmpFile.toURL()));
			parser.addTagInterceptor(interceptor);
		}
			
			
		parser.setOutputStream(response.getOutputStream());
						
		response.setContentType("text/html");
		
		try {
			parser.read(in);
		} catch (SAXException e) {
			logger.error("Error parsing SOAP response",e);
		}
		
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
