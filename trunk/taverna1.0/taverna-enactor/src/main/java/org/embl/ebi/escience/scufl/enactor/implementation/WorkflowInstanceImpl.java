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
//      Last commit info    :   $Author: stain $
//                              $Date: 2006-11-02 11:27:17 $
//                              $Revision: 1.5 $
//
///////////////////////////////////////////////////////////////////////////////////////
package org.embl.ebi.escience.scufl.enactor.implementation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.LSIDProvider;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;

import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener;
import uk.ac.soton.itinnovation.freefluo.main.Engine;
import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;
import uk.ac.soton.itinnovation.freefluo.main.UnknownWorkflowInstanceException;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowState;

/**
 * Represents a receipt for a workflow submitted by the client
 * <p>
 * 
 * This class is used as the handle for all client operations on a running or
 * scheduled workflow instance. It includes methods to extract status and result
 * documents and is augemented by Taverna to also include methods to return the
 * inputs and outputs to a specific processor for debug purpopses during the
 * construction of a workflow.
 * 
 * @author Darren Marvin
 * @author Tom Oinn
 * @author Justin Ferris
 * @author Stian Soiland
 */
public class WorkflowInstanceImpl implements WorkflowInstance {
    private static Logger logger = Logger.getLogger(WorkflowInstanceImpl.class);

    private Engine engine;

    private String engineId;

    private Map input;

    private UserContext context;

    private static Map<String, String> internalToLSID = new HashMap<String, String>();

    private static Map<String, String> instanceToDefinitionLSID = new HashMap<String, String>();

    private ScuflModel workflowModel;

    public static WorkflowInstanceImpl getInstance(Engine engine, ScuflModel scuflModel,
            String workflowInstanceId) {
    	return new WorkflowInstanceImpl(engine, scuflModel, workflowInstanceId);
    }
            
    
    /**
     * Constructor for this concrete instance of a flow receipt
     * 
     * @param engine -
     *            the enactment engine to use.
     * @param engineId -
     *            the unique Id for the workflow instance as compiled in the engine
     * @exception WorkflowSubmitInvalidException
     *                thrown by the superclass
     */
    private WorkflowInstanceImpl(Engine engine, ScuflModel scuflModel,
            String engineId) {
        logger.debug("WorkflowInstanceImpl(Engine engine=" + engine
                + ", ScuflModel scuflModel=" + scuflModel
                + ", String workflowInstanceId=" + engineId
                + ") - start");

        this.workflowModel = scuflModel;
        this.engineId = engineId;
        this.engine = engine;

        try {
            this.context = new SimpleUserContext(engine
                    .getFlowContext(engineId));

            // If there's a global LSID provider configured then use
            // it to get an LSID for the workflow instance class and
            // store it.
            if (DataThing.SYSTEM_DEFAULT_LSID_PROVIDER != null) {
                // Check whether we already have an LSID allocated!
                String lsidKey = scuflModel.getDescription().getLSID()
                        + engineId;
                String existingLSID = (String) internalToLSID.get(lsidKey);
                if (existingLSID == null) {
                    LSIDProvider p = DataThing.SYSTEM_DEFAULT_LSID_PROVIDER;
                    String instanceLSID = p.getID(LSIDProvider.WFINSTANCE);
                    internalToLSID.put(lsidKey, instanceLSID);
                    engineId = instanceLSID;
                } else
                    engineId = existingLSID;
            }
        } catch (UnknownWorkflowInstanceException e) {
            String errorMsg = "Error starting to run workflow instance with id "
                    + engineId;
            String msg = errorMsg
                    + ".  The workflow engine didn't recognise the workflow instance id.";
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }
		String definitionLSID = scuflModel.getDescription().getLSID();
		String instanceLSID = getID();
		WorkflowInstanceImpl.instanceToDefinitionLSID.put(instanceLSID,
				definitionLSID);
        logger.debug("WorkflowInstanceImpl(Engine, ScuflModel, String) - end");
    }

    /**
     * Return a reference to the ScuflModel which this workflow was built from
     */
    public ScuflModel getWorkflowModel() {
        return this.workflowModel;
    }

    /**
     * Return the workflow instance ID Modified to look in the global mapping to
     * see whether an LSID has been assigned to this internal ID and if so
     * return the LSID
     */
    private String cachedLSID = null;

    public String getID() {
        logger.debug("getID() - start");

        if (cachedLSID != null) {
            logger.debug("getID() - end at cachedLSID != null");
            return cachedLSID;
        }
        String lsidKey = workflowModel.getDescription().getLSID()
                + engineId;
        logger.debug("getID() - String lsidKey=" + lsidKey);

        String lsid = (String) internalToLSID.get(lsidKey);
        logger.debug("getID() - String lsid=" + lsid);

        if (lsid != null) {
            cachedLSID = lsid;
            logger.debug("getID() - end with lsid != null");
            return lsid;
        }

        logger.debug("getID() - end");
        return engineId;
    }

    public String getDefinitionLSID() {
        String definitionLSID = (String) instanceToDefinitionLSID.get(getID());
        if (definitionLSID != null) {
            return definitionLSID;
        }
        return "";
    }

    /**
     * Register the specified listener with the engine for this instance and use
     * the internal workflow ID
     */
    public void addWorkflowStateListener(WorkflowStateListener listener) {
        try {
            engine.addWorkflowStateListener(engineId, listener);
        } catch (UnknownWorkflowInstanceException e) {
            String errorMsg = "Cannot add listener";
            String msg = errorMsg
                    + ".  The workflow engine didn't recognise the workflow instance id.";
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Remove a workflow state listener
     */
    public void removeWorkflowStateListener(WorkflowStateListener listener) {
        try {
            engine.removeWorkflowStateListener(engineId, listener);
        } catch (UnknownWorkflowInstanceException e) {
            String errorMsg = "Cannot remove listener";
            String msg = errorMsg
                    + ".  The workflow engine didn't recognise the workflow instance id.";
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }
    }

    public void setInputs(Map inputMap) {
        this.input = inputMap;
    }

    public void run() throws InvalidInputException {
        String errorMsg = "Error starting to run workflow instance with id "
                + engineId;

        try {
            // Populate the input map with LSIDs in case they don't already have
            // them
            for (Iterator i = input.keySet().iterator(); i.hasNext();) {
                String inputName = (String) i.next();
                DataThing inputValue = (DataThing) input.get(inputName);
                inputValue.fillLSIDValues();
            }
            engine.setInput(engineId, input);
            engine.run(engineId);
            WorkflowEventDispatcher.DISPATCHER
                    .fireWorkflowCreated(new WorkflowCreationEvent(this, input,
                            this.getDefinitionLSID()));
            // Add a new listener to handle and emit the workflow completion
            // events
            final Map lsidMap = internalToLSID;
            addWorkflowStateListener(new WorkflowStateListener() {
                public void workflowStateChanged(WorkflowStateChangedEvent event) {
                    WorkflowEventDispatcher dispatcher = WorkflowEventDispatcher.DISPATCHER;
                    WorkflowState state = event.getWorkflowState();
                    if (state.isFinal()) {
                        // Force caching of the LSID
                        getID();
                        // Have either completion, cancellation or failure
                        if (state.equals(WorkflowState.CANCELLED)
                                || state.equals(WorkflowState.FAILED)) {
                            dispatcher
                                    .fireWorkflowFailed(new WorkflowFailureEvent(
                                            WorkflowInstanceImpl.this));
                        } else if (state.equals(WorkflowState.COMPLETE)) {
                            dispatcher
                                    .fireWorkflowCompleted(new WorkflowCompletionEvent(
                                            WorkflowInstanceImpl.this));
                        }
                        // Remove the LSID mapping from the global map as the
                        // internal
                        // ID will be recycled, don't want the LSID to be picked
                        // up as
                        // well.
                        WorkflowInstanceImpl.instanceToDefinitionLSID
                                .remove(lsidMap
                                        .get(WorkflowInstanceImpl.this.engineId));
                        lsidMap
                                .remove(WorkflowInstanceImpl.this.engineId);
                    }
                }
            });
        } catch (InvalidInputException e) {
            logger.error(errorMsg
                    + ".  The inputs don't map to sources in the dataflow.");
            throw e;
        } catch (UnknownWorkflowInstanceException e) {
            String msg = errorMsg
                    + ".  The workflow engine didn't recognise the workflow instance id.";
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }
    }

    public String getStatus() {
        try {
            return engine.getStatus(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error getting status for workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Returns two Map objects of port name -> dataThing. The input document is
     * at position 0, the output at position 1 and the result array has exactly
     * two slots, the documents are in the format defined by the Baclava package
     * and used elsewhere - this allows reuse of the display code from the main
     * workbench.
     * 
     * @exception UnknownProcessorException
     *                if a ProcessorTask with the supplied name cannot be found
     *                within the DiGraph that this FlowReceipt is associated
     *                with.
     */
    public Map[] getIntermediateResultsForProcessor(String processorName)
            throws UnknownProcessorException {
        try {
            return engine.getIntermediateResultsForProcessor(
                    engineId, processorName);

        } catch (uk.ac.soton.itinnovation.freefluo.main.UnknownProcessorException e) {
            String msg = "Error getting intermediate results for workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise in the workflow instance the processor with name "
                    + processorName;
            logger.error(msg, e);
            throw new UnknownProcessorException(msg);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error getting intermediate results for workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Updated progress report code to use the event lists
     */
    public String getProgressReportXMLString() {
        try {
            return engine.getProgressReportXML(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error getting progress report xml string for workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public Map getOutput() {
        try {
            return engine.getOutput(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error getting progress the output for workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public String getErrorMessage() {
        try {
            return engine.getErrorMessage(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error getting the error message for workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public String getProvenanceXMLString() {
        try {
            return engine.getProvenanceXML(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error getting provenance xml string for workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public void pauseExecution() {
        try {
            engine.pauseExecution(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error pausing workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public void resumeExecution() {
        try {
            engine.resumeExecution(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error resuming workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public boolean isPaused() {
        try {
            return engine.isPaused(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error determining if the workflow is paused for workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }

    }

    public void cancelExecution() {
        try {
            engine.cancelExecution(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error cancelling workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public void destroy() {
        try {
            engine.destroy(engineId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error destroying workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public void pause(String processorId) {
        try {
            engine.pause(engineId, processorId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error destroying workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public boolean isDataNonVolatile(String processorId) {
        try {
            return engine.isDataNonVolatile(engineId, processorId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error destroying workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public boolean changeOutputPortTaskData(String processorId,
            String OutputPortName, Object newData) {
        try {
            return engine.changeOutputPortTaskData(engineId,
                    processorId, OutputPortName, newData);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error destroying workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public void resume(String processorId) {
        try {
            engine.resume(engineId, processorId);
        } catch (UnknownWorkflowInstanceException e) {
            String msg = "Error destroying workflow instance with id "
                    + engineId
                    + ".  The workflow engine didn't recognise the workflow instance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public UserContext getUserContext() {
        return this.context;
    }
}
