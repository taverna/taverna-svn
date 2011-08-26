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
//                              $Date: 2003-05-23 12:36:00 $
//                              $Revision: 1.10 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.broker;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.broker.WorkflowSubmitInvalidException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowReceipt;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.WorkflowCommandException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.dispatcher.Dispatcher; // ambiguous with: org.apache.log4j.Dispatcher 
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Flow;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Task;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.DiGraph;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.FlowCommandEvent;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.FlowCommandHandler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.registry.FlowRegistry;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.scheduler.NoReservationScheduler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.scheduler.Scheduler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.NoReservationSPManager;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceProviderManager;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Input;
import uk.ac.soton.itinnovation.taverna.enactor.dispatcher.TavernaDispatcher;

// IO Imports
import java.io.ByteArrayInputStream;

// JDOM Imports
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaWorkflowSubmission;
import uk.ac.soton.itinnovation.taverna.enactor.broker.XScuflDiGraphGenerator;
import uk.ac.soton.itinnovation.taverna.enactor.broker.XScuflInvalidException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.String;
import java.lang.StringBuffer;
import java.lang.System;



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
		LogLevel modelLogLevel = null;
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
					//model.addListener(new ScuflModelEventPrinter(null)); 
					System.out.println("About to try to populate ScuflModel");
					XScuflParser.populate(doc,model,null);
					logger.debug("Loaded ScuflModel from xml file");
					modelLogLevel = new LogLevel(model.getLogLevel());
					
                } catch(org.embl.ebi.escience.scufl.parser.XScuflFormatException ex) {
					logger.error(ex);
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.MalformedNameException ex) {
					logger.error(ex);
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.DuplicateProcessorNameException ex) {
					logger.error(ex);
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.DataConstraintCreationException ex) {
					logger.error(ex);
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.UnknownPortException ex) {
					logger.error(ex);
					throw new WorkflowCommandException(ex.getMessage());
				} catch(org.embl.ebi.escience.scufl.UnknownProcessorException ex) {
					logger.error(ex);
					throw new WorkflowCommandException(ex.getMessage());				
				} catch (org.embl.ebi.escience.scufl.ProcessorCreationException ex) {
                    logger.error(ex);
					throw new WorkflowCommandException(ex.getMessage());
                }
                if (model == null)
                    throw new WorkflowCommandException("Unable to obtain model representing the XScufl definition");

				if(model.getProcessors().length==0)
					throw new WorkflowCommandException("Could not resolve any processors from the submitted workflow, please check the workflow definition is correct");
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
                logger.error(ex);
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
            TavernaFlowReceipt receipt = new TavernaFlowReceipt(flow,userID,modelLogLevel);

            flow.addListener(receipt);

            FlowCommandHandler commandHandler = FlowCommandHandler.getInstance();

            //create flowcommandevent and fire it
            commandHandler.put(new FlowCommandEvent("New flow submitted with ID: " + flow.getID(), flow, FlowCommandEvent.FLOW_SUBMIT));
            //notifyFlowCommandEventListeners(new FlowCommandEvent("New flow submitted with ID: " + flow.getID(),flow,FlowCommandEvent.FLOW_SUBMIT));
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
     * Provides for suspending a workflow previously submitted to the flow engine - Not yet implemented.
     *
     * @param String identifier for the workflow to suspend 
     */
    public void suspendFlow(String identifier) throws WorkflowCommandException {
        throw new WorkflowCommandException("Sorry, this functionality is not yet available.");
    }

    /**
     * Provides for resuming a workflow previously suspended - Not yet implemented.
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
