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

import java.net.MalformedURLException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Call;
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
	
	private static Logger logger = Logger.getLogger(T2WSDLSOAPInvoker.class);

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
		
		if (config != null) {
					
			configureSecurity(call);
		}
		
		return call;
	}

	private void configureSecurity(Call call) {
		// Call's USERNAME_PROPERTY is here simply used to pass the credential's alias to fetch it  
		// from the Keystore. As alias value we use wsdlLocation so that the credential 
		// is tied to a particular service. Once Security Agent picks up the alias, it will 
		// set the USERNAME_PROPERTY to null or to a proper username.
		// Note that WSS4J's handler WSDoAllSender expects (which is invoked before our T2DoAllSender takes over) 
		// the USERNAME_PROPERTY to be set to whatever non-empty value for almost all security operations (even for signing, 
		// except for encryption), otherwise it raises an Exception.
		
		//call.setProperty(Call.USERNAME_PROPERTY, getParser().getWSDLLocation()); 
		
		// Get the appropriate security agent
//		CredentialManager credManager;
//		try {
//			credManager = CredentialManager.getInstance();
//			SecurityAgentManager saManager = credManager.getSecurityAgentManager();
//			WSSecurityRequest wsSecReq = new WSSecurityRequest(getParser().getWSDLLocation(), null);
//
//			WSSecurityAgent sa = (WSSecurityAgent) saManager.getSecurityAgent((SecurityRequest) wsSecReq);
//			call.setProperty("security_agent", sa);
//			
//		} catch (CMException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CMNotInitialisedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}	
	
}
