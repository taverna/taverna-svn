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
//      Last commit info    :   $Author: dmarvin $
//                              $Date: 2003-04-13 11:12:01 $
//                              $Revision: 1.2 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.*;

import uk.ac.soton.itinnovation.taverna.enactor.dispatcher.TavernaInvocationDescription;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.invocation.wsdl.WSDLReader;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.invocation.WSDLServiceInvocation;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Output;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Task;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTask;
import uk.ac.soton.itinnovation.taverna.enactor.monitor.TavernaTaskMonitor;

import org.embl.ebi.escience.scufl.Processor;

public class SeqretTask extends ProcessorTask {
	
	private static Logger logger = Logger.getLogger(SeqretTask.class);

	private static final String WSDL_URL = "http://industry.ebi.ac.uk/soaplab/wsdl/edit__seqret__derived.wsdl";
	private static final String PORT_TYPE = "edit__seqret";
	private static final int INVOCATION_TIMEOUT = 0;

	public SeqretTask(String id,Processor proc) {
		super(id,proc);
	}

	public uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage doTask() {
		try {
			//create a suitable empty job for this task
			
			String requestMessageName = "createEmptyJobRequest";
			String responseMessageName = "createEmptyJobResponse";
			String procName = proc.getName();
			String desc_name = procName + ":createEmptyJob";
			String opName = "createEmptyJob";
			TavernaInvocationDescription desc = new TavernaInvocationDescription(desc_name,PORT_TYPE,opName,new URL(WSDL_URL),requestMessageName,responseMessageName,INVOCATION_TIMEOUT);
			WSDLServiceInvocation invoke = new WSDLServiceInvocation(desc,new Input(),WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);
			//have to set up response message parts, only one that is the jobid
			List list = new ArrayList(1);
			list.add(new Part(-1,"createEmptyJobReturn","string",null));
			invoke.setResponseMessageParts(list);
			Output output = invoke.executeOperation();
			String jobID = (String) output.getPartByName("createEmptyJobReturn").getValue();
			
			if(jobID==null) {
				return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED, "No job identifier returned");
				
			}
			
			Part jobIdPart = new Part(-1,"jobId","string",jobID);
			//find out what actual inputs are available
			//look through the input ports for suitable inputs to Seqret calls
			//looking for value for sequence_direct_data and os_format
			Part sequence_direct_dataPart = null;
			Part os_formatPart = null;
			Part sequence_usaPart = null;
			GraphNode[] inputs = getParents();
			for(int i=0;i<inputs.length;i++) {
				PortTask pT = (PortTask) inputs[i];
				//for now going to wait for all data inputs to be available, this must change for the special requirements for taverna.
				//actually want to set data in jobs as it becomes available, so don't block, check if data available every so often
				Input d = pT.waitForData();
				//can have any number of parts
				List partList = d.getPartList();
				Iterator iterator = partList.iterator();
				while(iterator.hasNext()) {
					Part p = (Part) iterator.next();
					if(p.getName().equals("sequence_direct_data"))
						sequence_direct_dataPart = new Part(-1,"value",p.getType(),p.getValue());
					if(p.getName().equals("os_format"))
						os_formatPart = new Part(-1,"value",p.getType(),p.getValue());
					if(p.getName().equals("sequence_usa"))
						sequence_usaPart = new Part(-1,"value",p.getType(),p.getValue());
				}
			}
			
			//set any input data that you have found

			if (sequence_direct_dataPart!=null){
				//set the direct data
				Input input = new Input();
				input.addPart(jobIdPart);
				input.addPart(sequence_direct_dataPart);
				//create the call to set sequence data
				requestMessageName = "set_sequence_direct_dataRequest";
				responseMessageName = "set_sequence_direct_dataResponse";
				desc_name = procName + ":set_sequence_direct_data";
				opName = "set_sequence_direct_data";
				//create input
			    
				desc = new TavernaInvocationDescription(desc_name,PORT_TYPE,opName,new URL(WSDL_URL),requestMessageName,responseMessageName,INVOCATION_TIMEOUT);
				invoke = new WSDLServiceInvocation(desc,input,WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);	
				//have to set up response message parts, there aren't any
				invoke.setResponseMessageParts(new ArrayList(0));
				output = invoke.executeOperation();	//output is empty
			}			
			
			if (sequence_usaPart!=null){
				//set the direct data
				Input input = new Input();
				input.addPart(jobIdPart);
				input.addPart(sequence_usaPart);
				//create the call to set sequence data
				requestMessageName = "set_sequence_usaRequest";
				responseMessageName = "set_sequence_usaResponse";
				desc_name = procName + ":set_sequence_usa";
				opName = "set_sequence_usa";
				//create input
			    
				desc = new TavernaInvocationDescription(desc_name,PORT_TYPE,opName,new URL(WSDL_URL),requestMessageName,responseMessageName,INVOCATION_TIMEOUT);
				invoke = new WSDLServiceInvocation(desc,input,WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);	
				//have to set up response message parts, there aren't any
				invoke.setResponseMessageParts(new ArrayList(0));
				output = invoke.executeOperation();	//output is empty
			}
				
			if (os_formatPart!=null){
				//set the format otherwise accept the default	
				Input input = new Input();
				input.addPart(jobIdPart);
				input.addPart(os_formatPart);				
				//create the call to set os type
				requestMessageName = "set_osformatRequest";
				responseMessageName = "set_osformatResponse";
				desc_name = procName + ":set_osformat";
				opName = "set_osformat";
				   
				desc = new TavernaInvocationDescription(desc_name,PORT_TYPE,opName,new URL(WSDL_URL),requestMessageName,responseMessageName,INVOCATION_TIMEOUT);
				invoke = new WSDLServiceInvocation(desc,input,WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);	
				//have to set up response message parts, there aren't any
				invoke.setResponseMessageParts(new ArrayList(0));
				output = invoke.executeOperation();	//output is empty
			}			

			//create the call to run the job
			requestMessageName = "runRequest";
			responseMessageName = "runResponse";
			desc_name = procName + ":run";
			opName = "run";
			//create input
			Input input = new Input();
			input.addPart(jobIdPart);
           	desc = new TavernaInvocationDescription(desc_name,PORT_TYPE,opName,new URL(WSDL_URL),requestMessageName,responseMessageName,INVOCATION_TIMEOUT);
			invoke = new WSDLServiceInvocation(desc,input,WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);	
			//have to set up response message parts, there aren't any
			invoke.setResponseMessageParts(new ArrayList(0));
			output = invoke.executeOperation();	//output is empty

			//could separate this off as node and therefore enable iterative status reports to be gained in separate thread.
			
			//create waitFor call
			requestMessageName = "waitForRequest";
			responseMessageName = "waitForResponse";
			desc_name = procName + ":waitFor";
			opName = "waitFor";
			//create input
			input = new Input();
			input.addPart(jobIdPart);
           	desc = new TavernaInvocationDescription(desc_name,PORT_TYPE,opName,new URL(WSDL_URL),requestMessageName,responseMessageName,INVOCATION_TIMEOUT);
			invoke = new WSDLServiceInvocation(desc,input,WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);	
			//have to set up response message parts, there aren't any
			invoke.setResponseMessageParts(new ArrayList(0));
			output = invoke.executeOperation();	//output is empty

			//in preparation for output and reports find the relevent ports
			PortTask outSeqPort = null;
			PortTask reportPort = null;
			GraphNode[] outputs = getChildren();
			for(int i=0;i<outputs.length;i++) {
				//look for portTests with the correct portname
				PortTask pT = (PortTask) outputs[i];
				String pTName = pT.getScuflPort().getName();
				if(pTName.equals("outseq"))
					outSeqPort = pT;
				if(pTName.equals("report"))
					reportPort = pT;
			}

			if(outSeqPort!=null) {

				//get the results
				requestMessageName = "get_outseqRequest";
				responseMessageName = "get_outseqResponse";
				desc_name = procName + ":get_outseq";
				opName = "get_outseq";
				//create input
				input = new Input();
				input.addPart(jobIdPart);
				desc = new TavernaInvocationDescription(desc_name,PORT_TYPE,opName,new URL(WSDL_URL),requestMessageName,responseMessageName,INVOCATION_TIMEOUT);
				invoke = new WSDLServiceInvocation(desc,input,WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);	
				//have to set up response message parts, only one that is the jobid
				list = new ArrayList(1);
				list.add(new Part(-1,"get_outseqReturn","string",null));
				invoke.setResponseMessageParts(list);
				output = invoke.executeOperation();
				Part outseqPart = output.getPartByName("get_outseqReturn");
				if(outseqPart==null) {
					return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED, "No out sequence returned");
					
				}
				//becomes input dataset
				Input in = new Input();
				in.addPart(new Part(-1,"outseq",outseqPart.getType(),outseqPart.getValue()));
				outSeqPort.setData(in);
			}
									
			if(reportPort!=null) {

				//get the results
				requestMessageName = "ger_reportRequest";
				responseMessageName = "get_reportResponse";
				desc_name = procName + ":get_report";
				opName = "get_report";
				//create input
				input = new Input();
				input.addPart(jobIdPart);
				desc = new TavernaInvocationDescription(desc_name,PORT_TYPE,opName,new URL(WSDL_URL),requestMessageName,responseMessageName,INVOCATION_TIMEOUT);
				invoke = new WSDLServiceInvocation(desc,input,WSDLServiceInvocation.OPERATION_TYPE_REQUEST_RESPONSE);	
				//have to set up response message parts, only one that is the jobid
				list = new ArrayList(1);
				list.add(new Part(-1,"get_reportReturn","string",null));
				invoke.setResponseMessageParts(list);
				output = invoke.executeOperation();
				Part reportPart = output.getPartByName("get_reportReturn");
				if(reportPart==null) {
					return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED, "No report returned");
					
				}
				//becomes input dataset
				Input in = new Input();
				in.addPart(new Part(-1,"report",reportPart.getType(),reportPart.getValue()));
				reportPort.setData(in);
			}
			//destroy the job

		//success
		return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.COMPLETE, "Task finished successfully");
		
		}
		catch (org.jdom.JDOMException ex){
			logger.error("Task " + getID() + " failed",ex);
			return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED, ex.getMessage()); 
		}
		catch (java.net.MalformedURLException ex){
			logger.error("Task " + getID() + " failed",ex);
			return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED, "Could not read supplied URL:" + WSDL_URL); 
		}
		catch(uk.ac.soton.itinnovation.mygrid.workflow.enactor.invocation.InvocationException ex) {
			logger.error("Task " + getID() + " failed",ex);
			return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED, ex.getMessage()); 
		}
		catch(uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.DataParseException ex) {
			logger.error("Task " + getID() + " failed",ex);
			return new TaskStateMessage(getParentFlow().getID(), getID(), TaskStateMessage.FAILED, ex.getMessage()); 
		}
		
	}
	
	public void cleanUpConcreteTask() {
		//nothing at mo, but should call destroy on job if job id available
	}
}