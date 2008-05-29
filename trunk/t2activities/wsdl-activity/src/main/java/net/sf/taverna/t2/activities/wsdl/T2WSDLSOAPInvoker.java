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
 */

package net.sf.taverna.t2.activities.wsdl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import net.sf.taverna.security.SecurityAgent;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;
import net.sf.taverna.wsdl.soap.SOAPResponseParser;
import net.sf.taverna.wsdl.soap.SOAPResponseParserFactory;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.configuration.XMLStringProvider;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.log4j.Logger;


/**
 * Invokes SOAP based Web Services from T2.
 * 
 * Subclasses WSDLSOAPInvoker used for invoking Web Services from Taverna 1.x and overrides the 
 * getCall(EngineConfiguration config) method to enable invocation of 
 * secure Web Services using the T2 Security Agents.
 * 
 * @author Stuart Owen
 * @author Alex Nenadic
 * 
 */
public class T2WSDLSOAPInvoker extends WSDLSOAPInvoker{
	
	private static Logger logger = Logger.getLogger(WSDLSOAPInvoker.class);

	public T2WSDLSOAPInvoker(WSDLParser parser, String operationName,
			List<String> outputNames) {
		super(parser, operationName, outputNames);
	}
	
	/**
	 * Returns an Axis-based Call, initialised for the operation that needs to be invoked. 
	 * 
	 * @param config - Axis engine configuration containing settings for the transport and WSS4J handlers 
	 * that will add WS-Security headers to the SOAP envelope in order to make secure WSs invocation.
	 * @return Call object initialised for the operation that needs to be invoked.
	 * @throws ServiceException
	 * @throws UnknownOperationException
	 * @throws MalformedURLException 
	 */
	protected Call getCall(EngineConfiguration config)  throws ServiceException, UnknownOperationException, MalformedURLException {

		Call call = super.getCall(config);

		// Call's USERNAME_PROPERTY is here simply used to pass the credential's alias to fetch it  
		// from the Keystore. As alias value we use wsdlLocation so that the credential 
		// is tied to a particular service. Once Security Agent picks up the alias, it will 
		// set the USERNAME_PROPERTY to null or to a proper username.
		// Note that WSS4J's handler WSDoAllSender expects (which is invoked before our T2DoAllSender takes over) 
		// the USERNAME_PROPERTY to be set to whatever non-empty value for almost all security operations (even for signing, 
		// except for encryption), otherwise it raises an Exception.
		call.setProperty(Call.USERNAME_PROPERTY, super.getParser().getWSDLLocation()); 
		
		/* 
		 * We also need to pass the security agent(s) (or some kind of a reference to the Peer groupof agents) 
		 * that will do the actual work on setting WS-SEcurity headers on the SOAP request.
		 * 
		*/
		// Create and initialise a security agent 
    	String ksPassword = "uber"; //Keystore master password
    	String ksFileName = "/Users/alex/Documents/workspace/credential-manager/bin/net/sf/taverna/credentialmanager/keystore/t2keystore.ubr"; //Keystore file name
    	SecurityAgent sa = new SecurityAgent(ksPassword, ksFileName);
        try{
        	sa.init();
        }
        catch(Exception ex){

        	if ((ex instanceof IOException) || (ex instanceof NoSuchAlgorithmException) || (ex instanceof CertificateException)){
            	logger.error("T2WSDLSOAPInvoker: Security Agent could not be initiated - failed to load the Keystore. " +
       			"Possible reason: Keystore has been corrupted or the master password supplied was incorrect.");
            	//fix me: change exception
            	throw new ServiceException("T2WSDLSOAPInvoker: Security Agent could not be initiated - failed to load the Keystore. " +
               			"Possible reason: Keystore has been corrupted or the master password supplied was incorrect.");
        	}
        	else {
            	logger.error("T2WSDLSOAPInvoker: Security Agent could not be initiated - failed to load the Keystore. " +
               			"Reason: Bouncy Castle provider could not be loaded.");

            	throw new ServiceException("T2WSDLSOAPInvoker: Security Agent could not be initiated - failed to load the Keystore. " +
               			"Reason: Bouncy Castle provider could not be loaded.");
        	}
        }
		call.setProperty("security_agent", sa);	
		
		return call;
	}	
	
}
