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
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-09 16:38:44 $
 *               by   $Author: sowen70 $
 * Created on 7 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

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
	
	private String receiveEndpointResponse(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		String result="";
		while ((line=reader.readLine())!=null) {
			logger.debug("Received line from endpoint: "+line);
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
		
		String endPointResponse=receiveEndpointResponse(in);
		
		String clientResponse;
		try {
			clientResponse = parseEndpointResponse(id,endPointResponse);
		} catch (JDOMException e) {
			throw new ServletException(e);
		}
		
		logger.debug("Processed response:"+clientResponse);
		
		response.setContentType("text/html");
		response.getWriter().println(clientResponse);
	}

	private String parseEndpointResponse(String id,String endpointResponse) throws JDOMException, IOException {
		String result;
		if (id.equals("11111")) {
			Element root=new SAXBuilder().build(new ByteArrayInputStream(endpointResponse.getBytes())).detachRootElement();
			List<Element> elements= new ArrayList<Element>();
			getChildren(root,"Field",elements);
			for (Element element : elements) {
				element.removeContent();
				element.setName("Field-proxied");
				element.setText("http://a data url");				
			}
			result=new XMLOutputter(Format.getPrettyFormat()).outputString(root);
		}
		else {
			result=endpointResponse;
		}
		
		return result;
		
	}
	
	private void getChildren(Element root,String name,List<Element> collected) {
		if (root.getChildren().size()>0) {
			for (Element child : (List<Element>)root.getChildren()) {
				if (child.getName().equals(name)) collected.add(child);
				getChildren(child, name, collected);
			}
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
