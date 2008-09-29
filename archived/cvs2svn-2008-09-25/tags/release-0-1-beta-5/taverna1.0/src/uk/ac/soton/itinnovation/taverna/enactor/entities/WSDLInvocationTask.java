////////////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2002
//
// Copyright in this library belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Darren Marvin
//      Created Date        :   2003/4/9
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2003-09-26 12:22:15 $
//                              $Revision: 1.22 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.invocation.InvocationDescription;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.invocation.WSDLServiceInvocation;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Output;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;

// Utility Imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// JDOM Imports
import org.jdom.Element;
import org.jdom.Text;

// Network Imports
import java.net.URL;

import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import java.lang.Exception;
import java.lang.String;



public class WSDLInvocationTask extends ProcessorTask implements InvocationDescription {
	private static Logger logger = Logger.getLogger(WSDLInvocationTask.class);
	private static final int INVOCATION_TIMEOUT = 0;
	private Input inputForLog = null;
	private Output outputForLog = null;

	public WSDLInvocationTask(String id,Processor proc,LogLevel l, String userID, String userCtx) {
		super(id,proc,l,userID,userCtx);
	}
	
	public java.util.Map execute(java.util.Map inputMap) throws TaskExecutionException {
		
		try{
			
			//want to siffle through the input ports and get input parts  
			//GraphNode[] inputs = getParents();
			//want to create suitable input parts
			
			Input input = new Input();
			if(logLevel.getLevel()>=LogLevel.HIGH)
				inputForLog = input;
			Iterator iterator = inputMap.keySet().iterator();
			while(iterator.hasNext()) {
				input.addPart((Part) inputMap.get(iterator.next()));
			}

			WSDLServiceInvocation serviceInvocation = new WSDLServiceInvocation(this,input,WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);
			//have to configure invocation for the output parts
			
			
				List outParts = new ArrayList();
			GraphNode[] children = getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof PortTask) {
					PortTask pT = (PortTask) children[i];
					Port pt = pT.getScuflPort();
					Part prt = new Part(-1,pt.getName(),"org.w3c.dom.Element",null);	//not worried that type and value unknown at present since will be filled in by invocation api
					pT.setData(prt);
					outParts.add(prt);
          }
			}
			
			serviceInvocation.setResponseMessageParts(outParts);

			//execute the call
            serviceInvocation.executeOperation();
            //group together the output parts and write to child partchecks
            Output output = serviceInvocation.getServiceOutput();

            if(logLevel.getLevel()>=LogLevel.HIGH) 
							outputForLog = output;
						List outputParts = output.getPartList();
						
						Map outputMap = new HashMap();
            iterator = outputParts.iterator();
            while (iterator.hasNext()) {
							//match with child part by name and set the part value
	            
							Part part = (Part) iterator.next();
              String partName = part.getName();
              outputMap.put(partName,part);
							
						}
          
			
			//success
			return outputMap;
		}
		catch(Exception ex) {
			logger.error("Error invoking soaplab service for task " +getID() ,ex);
			throw new TaskExecutionException("WSDL-based service invocation failed due to '" + ex.getMessage() + "'");
		}

	}

	public void cleanUpConcreteTask() {
		//nothing at mo, but should call destroy on job if job id available
	}

	//following required to implement the invocation description interface
	
	public String getName() {
		return proc.getName();
	}


    /**
     * Obtain the WSDL portType identifier required for this invocation. - required for InvocationDescription
     * @return identifier
     */
    public String getPortType() {
		return ((WSDLBasedProcessor) proc).getPortTypeName();
	}

    /**
     * Obtain the WSDL Operation identifier required for this invocation - required for InvocationDescription
     * @return identifier
     */
    public String getOperation() {
		return ((WSDLBasedProcessor) proc).getOperationName();
	}

    /**
     * Obtain the WSDL selected for invocation against - required for InvocationDescription
     * @return URL location
     */
    public URL getSelectedServiceWSDLURL(){
		try{
			return new URL(((WSDLBasedProcessor) proc).getWSDLLocation());
		}catch(java.net.MalformedURLException ex) {
			return null;
		}
	}

    /**
     * Obtain the WSDL RequestMessageName for the operation - required for InvocationDescription
     * @return identifier
     */
    public String getRequestMessageName() {
		return ((WSDLBasedProcessor) proc).getRequestMessageName();
	}

    /**
     * Obtain the WSDL ResponseMessageName for the operation - required for InvocationDescription
     * @return identifier
     */
    public String getResponseMessageName() {
		return ((WSDLBasedProcessor) proc).getResponseMessageName();
	}

	/**
	 * Get the socket timeout period for the invocation - required for InvocationDescription
	 * @return time period
	 */
	public int getSocketTimeOut() {
		//use 0 to allow endless wait, can't assume anything at present about the length of time for an invocation.
		return 0;
	}

	/**
	 * Retrieve provenance information for this task, concrete tasks should
	 * overide this method and provide this information as an XML JDOM element
	 */
	public org.jdom.Element getProvenance() {
		Element e = new Element("WSDLInvocation",PROVENANCE_NAMESPACE);
		if(logLevel.getLevel()>=LogLevel.LOW) {
			org.jdom.Element status = new org.jdom.Element("status",PROVENANCE_NAMESPACE);
			status.addContent(new org.jdom.Text(getStateString()));
			e.addContent(status);
			//add start and end time
			if(startTime!=null) {
				Element sT = new Element("startTime",PROVENANCE_NAMESPACE);
				sT.addContent(new Text(startTime.getString()));
				e.addContent(sT);
			}
			if(endTime!=null) {
				Element eT = new Element("endTime",PROVENANCE_NAMESPACE);
				eT.addContent(new Text(endTime.getString()));
				e.addContent(eT);
			}
			if(getClientMessage()!=null) {
				Element eT = new Element("executionMessage",PROVENANCE_NAMESPACE);
				eT.addContent(new Text(getClientMessage()));
				e.addContent(eT);
			}
			
			
		}

		if(logLevel.getLevel()>=LogLevel.NORMAL) {
			//add the wsdl service invoked
			String wsdlURL = getSelectedServiceWSDLURL().toExternalForm();
			if(wsdlURL!=null) {
				Element uri = new Element("WSDLURI",PROVENANCE_NAMESPACE);
				uri.addContent(new Text(wsdlURL));
				e.addContent(uri);
			}
			//add the portType
			String portType = getPortType();
			if(portType!=null) {
				Element pT = new Element("PortType",PROVENANCE_NAMESPACE);
				pT.addContent(new Text(portType));
				e.addContent(pT);
			}
			//add the operation
			String operation = getOperation();
			if(operation!=null) {
				Element op = new Element("Operation",PROVENANCE_NAMESPACE);
				op.addContent(new Text(operation));
				e.addContent(op);
			}	
		}

		if(logLevel.getLevel()>=LogLevel.HIGH) {
			//add the input and output data
			//required retrieving of it
			if(inputForLog!=null)
				e.addContent(inputForLog.toXMLElement());
			if(outputForLog!=null)
				e.addContent(outputForLog.toXMLElement());
		}		
		return e;
	}
}
