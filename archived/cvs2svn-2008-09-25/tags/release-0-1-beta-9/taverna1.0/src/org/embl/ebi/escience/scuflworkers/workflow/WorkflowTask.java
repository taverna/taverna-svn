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
//                              $Date: 2004-02-04 11:21:21 $
//                              $Revision: 1.4 $
//
///////////////////////////////////////////////////////////////////////////////////////

package org.embl.ebi.escience.scuflworkers.workflow;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBrokerFactory;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowCallback;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowMessage;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaBinaryWorkflowSubmission;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.lang.NullPointerException;
import java.lang.String;
import java.lang.Thread;



public class WorkflowTask implements ProcessorTaskWorker {

    private static Logger logger = Logger.getLogger(WorkflowTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    private static final long WAITTIME = 10000;
    private String subWorkflowID = null;
    private TavernaFlowReceipt receipt = null;
    private FlowBroker broker = null;
    private int flowState = FlowMessage.NEW;
    private Processor proc;

    public WorkflowTask(Processor p) {
	this.proc = p;
    }
    
    /**
     * Invoke a nested workflow, the input map being a map of string port names
     * to DataThing objects containing the current values.
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
	WorkflowProcessor theProcessor = (WorkflowProcessor)proc;
	ScuflModel theNestedModel = theProcessor.getInternalModel();
	//String userID = getUserID();
	//String userContext = getUserNamespaceContext();
	String userID = "";
	String userContext = "";
	// The inputMap is already in the form we need for a submission
	TavernaBinaryWorkflowSubmission theSubmission = new TavernaBinaryWorkflowSubmission(theNestedModel,
											    inputMap,
											    userID,
											    userContext);
	try {
	    broker = FlowBrokerFactory.createFlowBroker("uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
	    receipt = (TavernaFlowReceipt)broker.submitFlow(theSubmission);
	}
	catch (Exception ex) {
	    //
	}
	subWorkflowID = receipt.getID();
	// The flowState is manipulated by the new FlowCallBack
	EnhancedFlowCallback monitor = new EnhancedFlowCallback() {
		private boolean running = true;
		private Thread myThread = null;
		public void waitFor() {
		    myThread = Thread.currentThread();
		    while (running) {
			try {
			    Thread.sleep(WAITTIME);
			}
			catch (InterruptedException ie) {
			    //
			}
		    }
		}
		public void notify(FlowMessage msg) {
		    try {
			flowState = msg.getNewState();
			switch(flowState) {
			case 3: // Complete
			case 4: // Failed
			case 5: // Cancelled
			    this.running = false;
			    myThread.interrupt();
			    break;
			}
		    }
		    catch (NullPointerException ex) {
			logger.error(ex);
		    }
		}
	    };
	receipt.registerFlowCallback(monitor);
	monitor.waitFor();
	
	// Did we finish okay?
	if (flowState == FlowMessage.COMPLETE) {
	    return receipt.getOutput();
	}
	else if (flowState == FlowMessage.FAILED) {
	    throw new TaskExecutionException("Nested workflow failed in task, error message was : "+receipt.getErrorMessage());
	}
	else if (flowState == FlowMessage.CANCELLED) {
	    return new HashMap();
	}
	else {
	    throw new TaskExecutionException("Unknown flow state in task, failing.");
	}
    }
    
    private interface EnhancedFlowCallback extends FlowCallback {
	public void waitFor();
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
        
}
