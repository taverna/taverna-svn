/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.interaction.workflow.impl;

import net.sf.taverna.interaction.workflow.*;
import java.net.*;
import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.apache.log4j.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * Implementation of InteractionService based on a remote set
 * of Servlets using the HTTP transport.<p>
 * This client proxy assumes that there is the following structure
 * beneath the specified base URL :
 * <ul>
 * <li>/patterns.xml - <em>XML file containing all the patterns this
 * interaction server supports</em></li>
 * <li>/submit - <em>Submission handler servlet to which interaction
 * request data and metadata can be POSTed to in multipart form</em></li>
 * <li>/status - <em>Status handler servlet used to fetch interaction
 * request status for a previously submitted request</em></li>
 * </ul>
 * @author Tom Oinn
 */
public class HTTPInteractionServiceProxy implements InteractionService {
    
    static Logger log = Logger.getLogger(HTTPInteractionServiceProxy.class);
    static Map proxyCache = new HashMap();
    
    private InteractionPattern[] patterns = null;
    private URL baseURL = null;

    /**
     * Get a connection to the interaction service at the specified
     * URL
     */
    public static InteractionService connectTo(URL targetURL) {
	synchronized (proxyCache) {
	    if (proxyCache.containsKey(targetURL)) {
		return (InteractionService)proxyCache.get(targetURL);
	    }
	    else {
		InteractionService i = new HTTPInteractionServiceProxy(targetURL);
		proxyCache.put(targetURL, i);
		return i;
	    }
	}
    }

    /**
     * Private constructor, this class should never be directly
     * constructed, use the 'connectTo' static method instead
     */
    private HTTPInteractionServiceProxy(URL targetURL) {
	baseURL = targetURL;
    }

    /**
     * Get the XML document from the server describing all possible
     * interaction patterns, parse it and create InteractionPattern
     * implementations for each pattern
     */
    public InteractionPattern[] getInteractionPatterns() {
	if (this.patterns != null) {
	    return this.patterns;
	}
	else {
	    List patternElements = null;
	    try {
		URL metadataURL = new URL(baseURL, "patterns.xml");
		InputStream is = metadataURL.openStream();
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);
		patternElements = doc.getRootElement().getChildren();
	    }
	    catch (MalformedURLException mue) {
		log.error(mue);
	    }
	    catch (JDOMException jde) {
		log.error(jde);
	    }
	    catch (IOException ioe){
		log.error(ioe);
	    }
	    // Fail silently, return no interaction patterns if anything
	    // goes wrong. Arguably this isn't the best way to cope here..
	    if (patternElements == null) {
		return new InteractionPattern[0];
	    }
	    List patternObjects = new ArrayList();
	    for (Iterator i = patternElements.iterator(); i.hasNext();) {
		try {
		    patternObjects.add(new XMLBasedInteractionPattern((Element)i.next()));
		}
		catch (Exception ex) {
		    //
		}
	    }
	    this.patterns = (InteractionPattern[])patternObjects.toArray(new InteractionPattern[0]);
	    return this.patterns;
	}
    }

    public InteractionReceipt submitRequest(InteractionRequest request) 
	throws SubmissionException {
	try {
	    // URL to submit the request to by POST
	    URL submitURL = new URL(baseURL, "submit");
	    
	    // Build the request metadata document
	    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	    String requestMetadata = xo.outputString(elementForRequest(request));
	    
	    // Build the data document
	    
	    // Post data to the URL
	    PostMethod post = new PostMethod(submitURL.toString());
	    Part[] parts = { new StringPart("metadata", requestMetadata),
			     new FilePart("data", new ByteArrayPartSource("data",request.getData())) };
	    post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
	    HttpClient client = new HttpClient();
	    client.getHttpConnectionManager().
		getParams().setConnectionTimeout(5000);
	    int status = client.executeMethod(post);
	    if (status == HttpStatus.SC_OK) {
		log.info("Submitted request...");
	    }
	    else {
		SubmissionException se = new SubmissionException();
		se.setMessage("Upload failed, response was " + HttpStatus.getStatusText(status));
		throw se;
	    }
	    return null;
	    
	}
	catch (Exception ex) {
	    if (ex instanceof SubmissionException) {
		throw (SubmissionException)ex;
	    }
	    SubmissionException se = new SubmissionException();
	    se.setMessage("Failed to submit interaction request "+ex.getMessage());
	    se.initCause(ex);
	    throw se;
	}	
    }
    
    public String getHostName() {
	return this.baseURL.getHost();
    }

    static Element elementForRequest(InteractionRequest r) {
	Element requestElement = new Element("request");
	Element patternElement = new Element("pattern");
	patternElement.setAttribute("name", r.getPattern().getName());
	Element emailElement = new Element("to");
	emailElement.setAttribute("email", r.getEmail());
	Element expiryElement = new Element("expires");
	expiryElement.setAttribute("date",Long.toString(r.getExpiryTime().getTime()));
	requestElement.addContent(patternElement);
	requestElement.addContent(emailElement);
	requestElement.addContent(expiryElement);	
	return requestElement;
    }

    

}

class XMLBasedInteractionPattern implements InteractionPattern {

    private String name, description;
    private String[] inputTypes, outputTypes, inputNames, outputNames;

    XMLBasedInteractionPattern(Element e) {
	this.name = e.getAttributeValue("name","No name!");
	Element descriptionElement = e.getChild("description");
	this.description = descriptionElement.getTextTrim();
	List inputs = e.getChildren("input");
	inputTypes = new String[inputs.size()];
	inputNames = new String[inputs.size()];
	List outputs = e.getChildren("output");
	outputTypes = new String[outputs.size()];
	outputNames = new String[outputs.size()];
	for (int i = 0; i < inputTypes.length; i++) {
	    Element input = (Element)inputs.get(i);
	    inputTypes[i] = input.getAttributeValue("type");
	    inputNames[i] = input.getAttributeValue("name");
	}
	for (int i = 0; i < outputTypes.length; i++) {
	    Element output = (Element)outputs.get(i);
	    outputTypes[i] = output.getAttributeValue("type");
	    outputNames[i] = output.getAttributeValue("name");
	}
    }
    
    public String getName() {
	return this.name;
    }

    public String getDescription() {
	return this.description;
    }

    public String[] getInputTypes() {
	return this.inputTypes;
    }

    public String[] getOutputTypes() {
	return this.outputTypes;
    }

    public String[] getInputNames() {
	return this.inputNames;
    }
    
    public String[] getOutputNames() {
	return this.outputNames;
    }

}
