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
//                              $Date: 2003-09-30 17:11:18 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////

package org.embl.ebi.escience.scuflworkers.wsdl;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.invocation.InvocationDescription;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Output;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;

// Utility Imports
import java.util.HashMap;

// JDOM Imports
import org.jdom.Element;

// Network Imports
import java.net.URL;




public class WSDLInvocationTask extends ProcessorTask implements InvocationDescription {
    private static Logger logger = Logger.getLogger(WSDLInvocationTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    //private Input inputForLog = null;
    private Output outputForLog = null;
    
    public WSDLInvocationTask(String id,Processor proc,LogLevel l, String userID, String userCtx) {
	super(id,proc,l,userID,userCtx);
    }
    
    public java.util.Map execute(java.util.Map inputMap) throws TaskExecutionException {
	
	return new HashMap();
	
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
	return e;
    }
}
