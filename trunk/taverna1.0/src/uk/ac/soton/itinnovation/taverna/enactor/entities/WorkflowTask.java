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
//      Created Date        :   2003/6/4
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2003-06-09 11:13:03 $
//                              $Revision: 1.2 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.WorkflowProcessor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBrokerFactory;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowCallback;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Output;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaBinaryWorkflowSubmission;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;

// Utility Imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// JDOM Imports
import org.jdom.Element;
import org.jdom.Text;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.lang.NullPointerException;
import java.lang.String;
import java.lang.Thread;



public class WorkflowTask extends ProcessorTask {
	private static Logger logger = Logger.getLogger(WorkflowTask.class);
	private static final int INVOCATION_TIMEOUT = 0;
	private static final long WAITTIME = 10000;
	private Input inputForLog = null;
	private Output outputForLog = null;
	private TavernaFlowReceipt receipt = null;
	private String subWorkflowID = null;
	private FlowBroker broker = null;
	private Thread thisThread = null;
	private int state = FlowMessage.NEW;
		
	public WorkflowTask(String id,Processor proc,LogLevel l,String userID, String userCtx) {
		super(id,proc,l,userID,userCtx);
	}
	
	public java.util.Map execute(java.util.Map inputMap) throws TaskExecutionException {
		try{
			
			Map outputMap = null;
			
			//want to siffle through the input ports and get input parts  
			Input input = new Input();
			if(logLevel.getLevel()>=LogLevel.HIGH)
				inputForLog = input;
			Iterator iterator = inputMap.keySet().iterator();
			while(iterator.hasNext()) {
				input.addPart((Part) inputMap.get(iterator.next()));
			}
			
			//the intput object should be serialized ready for use with the nested submission
			//generate the TavernaSubmission Object ready for the TavernaFlowBroker
			TavernaBinaryWorkflowSubmission submit = new TavernaBinaryWorkflowSubmission(((WorkflowProcessor) proc).getInternalModel(), input, getUserID() , getUserNamespaceContext());
			//retrieve a flow broker
			broker = FlowBrokerFactory.createFlowBroker("uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
			receipt = (TavernaFlowReceipt) broker.submitFlow(submit);
			
			subWorkflowID = receipt.getID();
			WorkflowMonitor monitor = new WorkflowMonitor();
			//registry this as listener
			receipt.registerFlowCallback(monitor);
						
			monitor.waitFor();
						
			if(state==FlowMessage.COMPLETE) {				
				Output output = receipt.getOutput();
				if(logLevel.getLevel()>=LogLevel.HIGH) 
					outputForLog = output;
				List outputParts = output.getPartList();
						
				outputMap = new HashMap();
				iterator = outputParts.iterator();
				while (iterator.hasNext()) {
					//match with child part by name and set the part value
	            	Part part = (Part) iterator.next();
					String partName = part.getName();
					outputMap.put(partName,part);							
				}							
			}
			else if(state==FlowMessage.FAILED) {
				try{					
					throw new TaskExecutionException("Subworkflow failed, error message: " + receipt.getErrorMessage());
				}
				catch(Exception ex) {
					logger.error(ex);
					throw new TaskExecutionException("Subworkflow failed, unable to discover reason");
				}
			}
			else if(state==FlowMessage.CANCELLED) {						
					outputMap = new HashMap();
			}
			else {
					throw new TaskExecutionException("Could not establish state of the subworkflow so it is considered failed");
			}			
			//success
			return outputMap;
		}
		catch(Exception ex) {			
			logger.error("Error invoking subworkflow for task " +getID() ,ex);
			throw new TaskExecutionException("Task " + getID() + " failed due to problem invoking subworkflow, could not obtain any further information");
		}
	}

	public void cleanUpConcreteTask() {
		try{
			if(receipt!=null) {
				broker.releaseFlow(receipt);
			}
		}
		catch(Exception ex) {
			//we tried
		}
	}

	/**
	 * Retrieve provenance information for this task, concrete tasks should
	 * overide this method and provide this information as an XML JDOM element
	 */
	public org.jdom.Element getProvenance() {
		Element e = new Element("Workflow",PROVENANCE_NAMESPACE);
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
			
			
		}

		if(logLevel.getLevel()>=LogLevel.NORMAL) {
			//add the sub workflow id
			if(subWorkflowID!=null) {
				Element uri = new Element("workflowID",PROVENANCE_NAMESPACE);
				uri.addContent(new Text(subWorkflowID));
				e.addContent(uri);
			}		
		}

		if(logLevel.getLevel()>=LogLevel.HIGH) {
			//add the input and output data
			//required retrieving of it
			if(inputForLog!=null)
				e.addContent(inputForLog.toXMLElement());
			if(outputForLog!=null)
				e.addContent(outputForLog.toXMLElement());
			if(receipt!=null) {
				//grab the provenance record 
				e.addContent(receipt.getProvenanceXML());
			}
		}		
		return e;
	}

	/**
     * Undertakes any special cancel processing required by Taverna tasks
     */
    public void cancelConcreteTask() {
			try{
				broker.cancelFlow(subWorkflowID);
			}
			catch(uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.WorkflowCommandException ex) {
				ex.printStackTrace();
				logger.error(ex);
			}
			catch(NullPointerException ex) {
				logger.error(ex);
				//we have tried
			}
    }

	

	private class WorkflowMonitor implements FlowCallback {
		private boolean stop = false;
		public WorkflowMonitor() {};			
		public void waitFor() {
				thisThread = Thread.currentThread();
					while(!stop) {
						try{
							Thread.sleep(WAITTIME);
						} catch(InterruptedException ex) {
							//not a problem
						}
					}	
		}
		
		/**
		 * Implementation method for FlowCallback interface
		 */
		public void notify(FlowMessage msg) {
			//only interested in FAILED, COMPLETE and CANCELLED
			try{
				state = msg.getNewState();
				switch(state) {
					case 3:	//COMPLETE
					case 4:	//FAILED
					case 5: //CANCELLED
						stop = true;
						thisThread.interrupt();
						break;
					
				}					
			}
			catch (NullPointerException ex){
				logger.error(ex);
			}

		}
	};

}
