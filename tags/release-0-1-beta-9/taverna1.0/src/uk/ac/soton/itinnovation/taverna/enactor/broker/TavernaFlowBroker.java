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
//      Created Date        :   2003/04/08
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2004-03-11 17:20:37 $
//                              $Revision: 1.26 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.broker;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.view.XScuflView;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowReceipt;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.WorkflowCommandException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.dispatcher.Dispatcher; // ambiguous with: org.apache.log4j.Dispatcher 
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Flow;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.DiGraph;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.FlowCommandEvent;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.FlowCommandHandler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.registry.FlowRegistry;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.scheduler.NoReservationScheduler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.scheduler.Scheduler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.NoReservationSPManager;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceProviderManager;
import uk.ac.soton.itinnovation.taverna.enactor.dispatcher.TavernaDispatcher;

// Utility Imports
import java.util.Map;

// Network Imports
import java.net.MalformedURLException;

import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;
import uk.ac.soton.itinnovation.taverna.enactor.broker.XScuflDiGraphGenerator;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.String;



/**
 * A flowbroker designed to allow submission of workflows from
 * taverna into the freefluo enactment engine
 */
public class TavernaFlowBroker implements FlowBroker {

    private static Logger logger = Logger.getLogger(TavernaFlowBroker.class);
    
    public FlowReceipt submitFlow(Object o) {
	return null;
    }

    /**
     * Submit a new flow to the workflow enactor
     * @param theModel a ScuflModel instance representing this workflow
     * @param theInputMap a Map of String->DataThing objects representing the workflow input
     * @param userID a String containing the user identifier
     * @param userContext a String containing the user context
     * @return a receipt for the workflow.
     */
    public FlowReceipt submitFlow(ScuflModel theModel,
				  Map theInputMap,
				  String userID,
				  String userContext) 
	throws WorkflowCommandException {
        
	LogLevel modelLogLevel = null;
	
	String originalFlowDefinition = new XScuflView(theModel).getXMLText();
	String flowID = theModel.toString()+":"+userID+":"+":";
	
	// Build the DiGraph object
	DiGraph theDiGraph = null;
	try {
	    theDiGraph = XScuflDiGraphGenerator.build(flowID, theModel, theInputMap, userID, userContext);
	}
	catch (XScuflFormatException ex) {
	    //
	}
	catch (MalformedURLException mue) {
	    // huh?
	}
	
	// Register the dataflow
	FlowRegistry registry = null;
	Flow flow = null;
	registry = FlowRegistry.getInstance();
	flow = registry.createFlow(flowID, theDiGraph);
	
	// Get the service provider manager and schedule
	ServiceProviderManager spm = new NoReservationSPManager();
	try {
	    Scheduler scheduler = new NoReservationScheduler(spm);
	    scheduler.addDiGraph(theDiGraph);
	    scheduler.reschedule();
	}
	catch (Exception ex) {
	    //
	}
	
	// add the handler as a listener on the tasks as well 
	// and set the dispatcher for each task.
	for (int i = 0; i < flow.getTasks().length; i++) {
	    Dispatcher dispatcher = new TavernaDispatcher();
	    flow.getTasks()[i].setDispatcher(dispatcher);
	}	
	flow.update();
	
	// Get the receipt and register it as a callback for the flow
	try {
	    TavernaFlowReceipt receipt = new TavernaFlowReceipt(flow,originalFlowDefinition,theInputMap,userID,modelLogLevel);
	    flow.addListener(receipt);
	    FlowCommandHandler commandHandler = FlowCommandHandler.getInstance();
	    commandHandler.put(new FlowCommandEvent("New flow submitted with ID: " + flow.getID(), 
						    flow, 
						    FlowCommandEvent.FLOW_SUBMIT));
	    return receipt;
	}
	catch (Exception ex) {
	    throw new WorkflowCommandException("Error submitting flow");
	}
    }
    /**
       try {
       
       
       flow.addListener(receipt);
       
       FlowCommandHandler commandHandler = FlowCommandHandler.getInstance();
       
       //create flowcommandevent and fire it
       commandHandler.put(new FlowCommandEvent("New flow submitted with ID: " + flow.getID(), flow, FlowCommandEvent.FLOW_SUBMIT));
       boolean notifyConfigSetting = false;
       
       //return a suitable receipt
       return receipt;
       
       } catch (WorkflowSubmitInvalidException ex) {
       logger.error(ex);
       throw new WorkflowCommandException(ex.getMessage());
       } catch (WorkflowCommandException ex) {
       if (registry != null) {
       if (flow != null)
       registry.removeFlow(flow.getID());
       }
       throw ex;
       } catch (Exception ex) {
       logger.error(ex);
       if (registry != null) {
       if (flow != null)
       registry.removeFlow(flow.getID());
       }
       throw new WorkflowCommandException(ex.getMessage());
       }
       }
    */

    /**
     * Provides for cancelling a workflow previously submitted to the flow engine
     *
     * @param identifier String identifier for the workflow to cancel.
     */
    public void cancelFlow(String identifier) throws WorkflowCommandException {
        try {
            FlowRegistry  registry = FlowRegistry.getInstance();
            Flow flow = registry.getFlow(identifier);

            logger.info("Cancelling workflow with ID:" + flow.getID());
            FlowCommandHandler commandHandler = FlowCommandHandler.getInstance();

            //create flowcommandevent and fire it
            commandHandler.put(new FlowCommandEvent("Flow with ID: " + flow.getID() + "cancelled", flow, FlowCommandEvent.FLOW_CANCEL));
        } catch (IllegalArgumentException ex) {
            logger.error("Flow Cancel Failure");
            throw new WorkflowCommandException(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Flow Cancel Failure");
            throw new WorkflowCommandException(ex.getMessage());
        }
    }

    /**
     * Provides for suspending a workflow previously submitted to the flow engine - Not yet implemented.
     *
     * @param identifier String identifier for the workflow to suspend 
     */
    public void suspendFlow(String identifier) throws WorkflowCommandException {
        throw new WorkflowCommandException("Sorry, this functionality is not yet available.");
    }

    /**
     * Provides for resuming a workflow previously suspended - Not yet implemented.
     *
     * @param identifier String identifier for the workflow to resume.
     */
    public void resumeFlow(String identifier) throws WorkflowCommandException {
        throw new WorkflowCommandException("Sorry, this functionality is not yet available.");
    }

    /**
     * Signals to the flow engine that a flow can be released.
     *
     * @param receipt
     * @throws WorkflowCommandException
     */
    public void releaseFlow(FlowReceipt receipt) throws WorkflowCommandException {
		try{	
			//get the flow
			Flow flow = FlowRegistry.getInstance().getFlow(receipt.getID());
			FlowCommandHandler commandHandler = FlowCommandHandler.getInstance();
			commandHandler.put(new FlowCommandEvent("Release flow with ID: " + receipt.getID(), flow, FlowCommandEvent.FLOW_REMOVE));
		  
		} catch (Exception ex) {
		  logger.error(ex);
		}	
	}    

}
