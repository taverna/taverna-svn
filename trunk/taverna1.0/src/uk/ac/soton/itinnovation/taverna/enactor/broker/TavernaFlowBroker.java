////////////////////////////////////////////////////////////////////////////////
//
// � University of Southampton IT Innovation Centre, 2002
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
//      Last commit info    :   $Author: dmarvin $
//                              $Date: 2003-04-12 13:18:07 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.broker;


import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Date;

import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.DiGraph;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.WorkflowCommandException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowReceipt;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.registry.FlowRegistry;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Flow;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceProviderManager;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.NoReservationSPManager;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.scheduler.NoReservationScheduler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.scheduler.Scheduler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.dispatcher.Dispatcher;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.FlowCommandHandler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.FlowCommandEvent;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Task;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.FlowStates;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskScheduledStateMessage;

import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.User;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;

import uk.ac.soton.itinnovation.taverna.enactor.dispatcher.TavernaDispatcher;

import org.embl.ebi.escience.scufl.ScuflModel;

import org.apache.log4j.Logger;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.embl.ebi.escience.scufl.parser.XScuflParser; 



/**
 * Negotiates on behalf of a taverna client to submit a flow to the flow engine
 */
public class TavernaFlowBroker implements FlowBroker {

    private static Logger logger = Logger.getLogger(TavernaFlowBroker.class);

    /**
     * Provides for submitting a workflow to the flow engine.
     *
     * @param Taverna specific workflow object.
     * @return a receipt for the workflow.
     */
    public FlowReceipt submitFlow(Object o) throws WorkflowCommandException {
        FlowRegistry registry = null;
        Flow flow = null;
        DiGraph dGrph = null;
        TavernaWorkflowSubmission submit = null;
        String inputData = null;
		String userID = null;
		Input input = null;
        try {
            //for Taverna the passed object is a holder for the XScufl represention of the workflow and other important bits???
            ScuflModel model = null;

            if (o instanceof TavernaWorkflowSubmission) {
                submit = (TavernaWorkflowSubmission) o;
                String workflowDefn = submit.getXScuflDefinition();
				
				inputData = submit.getInputData();
                userID = submit.getUserID();
                
                try {
                    
					byte[] scuflSpecB = workflowDefn.getBytes();
                    ByteArrayInputStream stream = new ByteArrayInputStream(scuflSpecB);
					//convert to Document object
					SAXBuilder sb = new SAXBuilder();
					Document doc = sb.build(stream);
                    //obtain a scuflmodel
					model = new ScuflModel();
					XScuflParser.populate(doc,model,userID);                    
                } catch(org.embl.ebi.escience.scufl.parser.XScuflFormatException ex) {
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.MalformedNameException ex) {
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.DuplicateProcessorNameException ex) {
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.DataConstraintCreationException ex) {
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.UnknownPortException ex) {
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.UnknownProcessorException ex) {
					throw new WorkflowCommandException(ex.getMessage());				
				} catch (org.embl.ebi.escience.scufl.ProcessorCreationException ex) {
                    throw new WorkflowCommandException(ex.getMessage());
                }
                if (model == null)
                    throw new WorkflowCommandException("Unable to obtain model representing the XScufl definition");

            } else
                throw new WorkflowCommandException("Sorry unsupported format for submitted flow");

            //generate the digraph representation
            byte[] inputBytes = inputData.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBytes);

            input = new Input(inputStream);
            StringBuffer buf = new StringBuffer("Taverna:Workflow:");

            buf.append(model.toString());
            buf.append(":");
			buf.append(userID);
			buf.append(":");
            try {
                dGrph = XScuflDiGraphGenerator.build(buf.toString(), model, input, userID);
            } catch (XScuflInvalidException ex) {
                throw new WorkflowCommandException(ex.getMessage());
            }
            //register the dataflow
            //FlowRegistry registry = new TransientRegistry();
            registry = FlowRegistry.getInstance();
            flow = registry.createFlow(buf.toString(), dGrph);

            //Get the no reservation service provider manager
            ServiceProviderManager spm = new NoReservationSPManager();
            //schedule associated digraph
            Scheduler scheduler = new NoReservationScheduler(spm);

            scheduler.addDiGraph(dGrph);
            scheduler.reschedule();

            //add the handler as a listener on the tasks as well and set the dispatcher for each task.
            Task[] tasks = flow.getTasks();

            for (int i = 0; i < tasks.length; i++) {
                //better approach would allow plug-in by using config to do dynamic class loading.
                Dispatcher dispatcher = new TavernaDispatcher();

                tasks[i].setDispatcher(dispatcher);
            }
            //update the flow for this
            flow.update();
            TavernaFlowReceipt receipt = new TavernaFlowReceipt(flow);

            flow.addListener(receipt);

            FlowCommandHandler commandHandler = FlowCommandHandler.getInstance();

            //create flowcommandevent and fire it
            commandHandler.put(new FlowCommandEvent("New flow submitted with ID: " + flow.getID(), flow, FlowCommandEvent.FLOW_SUBMIT));
            //notifyFlowCommandEventListeners(new FlowCommandEvent("New flow submitted with ID: " + flow.getID(),flow,FlowCommandEvent.FLOW_SUBMIT));
            boolean notifyConfigSetting = false;

            //return a suitable receipt
            return receipt;

        } catch (WorkflowSubmitException ex) {
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

    /**
     * Provides for cancelling a workflow previously submitted to the flow engine
     *
     * @param String identifier for the workflow to cancel.
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
     * Provides for suspending a workflow previously submitted to the flow engine
     *
     * @param String identifier for the workflow to suspend.
     */
    public void suspendFlow(String identifier) throws WorkflowCommandException {
        throw new WorkflowCommandException("Sorry, this functionality is not yet available.");
    }

    /**
     * Provides for resuming a workflow previously suspended.
     *
     * @param String identifier for the workflow to resume.
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