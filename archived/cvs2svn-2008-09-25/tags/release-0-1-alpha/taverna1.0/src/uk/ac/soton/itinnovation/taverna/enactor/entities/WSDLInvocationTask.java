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
//                              $Date: 2003-05-13 13:03:45 $
//                              $Revision: 1.3 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.WSDLBasedProcessor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.invocation.InvocationDescription;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.invocation.WSDLServiceInvocation;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Output;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;

// Utility Imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Network Imports
import java.net.URL;

import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import java.lang.Exception;
import java.lang.String;



public class WSDLInvocationTask extends ProcessorTask implements InvocationDescription {
	private static Logger logger = Logger.getLogger(WSDLInvocationTask.class);
	private static final int INVOCATION_TIMEOUT = 0;

	public WSDLInvocationTask(String id,Processor proc) {
		super(id,proc);
	}
	
	public uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage doTask() {
		try{
			//want to siffle through the input ports and get input parts  
			GraphNode[] inputs = getParents();
			//want to create suitable input parts
			Input input = new Input();			
			for(int i=0;i<inputs.length;i++) {
			    if(inputs[i] instanceof PortTask){
			    PortTask pT = (PortTask) inputs[i];				
				//for now going to wait for all data inputs to be available, this must change for the special requirements for taverna.
				//actually want to set data in jobs as it becomes available, so don't block, check if data available every so often
			    input.addPart(pT.getData());
			    }					
			}

			WSDLServiceInvocation serviceInvocation = new WSDLServiceInvocation(this,input,WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);
			//have to configure invocation for the output parts
			List outParts = new ArrayList();
			GraphNode[] children = getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof PortTask) {
					PortTask pT = (PortTask) children[i];
					Port pt = pT.getScuflPort();
					Part prt = new Part(-1,pt.getName(),null,null);	//not worried that type and value unknown at present since will be filled in by invocation api
					pT.setData(prt);
					outParts.add(prt);
                }
			}
			serviceInvocation.setResponseMessageParts(outParts);

			//execute the call
            serviceInvocation.executeOperation();
            //group together the output parts and write to child partchecks
            Output output = serviceInvocation.getServiceOutput();
            List outputParts = output.getPartList();

            Iterator iterator = outputParts.iterator();
            while (iterator.hasNext()) {
		//match with child part by name and set the part value
		
                Part part = (Part) iterator.next();
                String partName = part.getName();
                Iterator iter2 = outParts.iterator();

                while (iter2.hasNext()) {
		     Part prt = (Part) iter2.next();

                    if (prt.getName().equals(partName)) {
			prt.setValue(part.getValue());
                        prt.setType(part.getType());
                        prt.setID(part.getID());
                    }
                }
            }
		
			//success
			return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.COMPLETE, "Task finished successfully");
		}
		catch(Exception ex) {
			logger.error("Error invoking soaplab service for task " +getID() ,ex);
			return new TaskStateMessage(getParentFlow().getID(),getID(),TaskStateMessage.FAILED,"Task " + getID() + " failed due to problem invoking soaplab service");
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
}
