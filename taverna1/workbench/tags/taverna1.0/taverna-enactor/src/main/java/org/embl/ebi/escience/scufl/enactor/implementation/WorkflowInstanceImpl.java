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
//      Last commit info    :   $Author: sowen70 $
//                              $Date: 2007-05-18 16:26:22 $
//                              $Revision: 1.11 $
//
///////////////////////////////////////////////////////////////////////////////////////
package org.embl.ebi.escience.scufl.enactor.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.LSIDProvider;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventAdapter;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowDestroyedEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowToBeDestroyedEvent;

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

	private String instanceLSID = null;

	private UserContext context;
	
	private boolean toBeDestroyed = false;

	/**
	 * Cache of instances as used by getInstance()
	 */
	private static Map<List<Object>, WorkflowInstanceImpl> instanceCache = new HashMap<List<Object>, WorkflowInstanceImpl>();
	
	private ScuflModel workflowModel;
	
	/**
	 * Get the workflow as compiled to the given engine with the given engineId.
	 * <p>
	 * Instances are cached until the instance is destroyed by destroy()
	 * 
	 * @see destroy()
	 * 
	 * @param engine Engine the workflow was compiled on
	 * @param workflowModel Workflow that was compiled
	 * @param engineId Identifier returned when compiling
	 * 
	 * @return A WorkflowInstanceImpl representing given workflow compilation. 
	 * Instances are cached, so that each call to getInstance() with
	 * the same parameters will return the same instance.
	 */
	public synchronized static WorkflowInstanceImpl getInstance(Engine engine,
			ScuflModel workflowModel, String engineId) {
		List<Object> cacheKey = cacheKey(engine, engineId);
		WorkflowInstanceImpl instance = instanceCache.get(cacheKey);
		if (instance == null) {
			instance = new WorkflowInstanceImpl(engine, workflowModel, engineId);
			instanceCache.put(cacheKey, instance);
		} 
		return instance;
	}

	private static List<Object> cacheKey(Engine engine, String engineId) {
        List<Object> cacheKey = new ArrayList<Object>(2);
        cacheKey.add(engine);
        cacheKey.add(engineId);
        return cacheKey;
    }

    /**
	 * Constructor for this concrete instance of a flow receipt
	 * 
	 * @param engine -
	 *            the enactment engine to use.
	 * @param workflowModel -
	 *            the ScuflModel that has been submitted
	 * @param engineId -
	 *            the unique Id for the workflow instance as compiled in the
	 *            engine
	 * @exception IllegalStateException
	 *                if the engineId is not valid
	 */
	private WorkflowInstanceImpl(Engine engine, ScuflModel workflowModel,
			String engineId) {
		this.workflowModel = workflowModel;
		this.engineId = engineId;
		this.engine = engine;
		try {
			this.context = new SimpleUserContext(engine
					.getFlowContext(engineId));
		} catch (UnknownWorkflowInstanceException e) {
			String errorMsg = "Error starting to run workflow instance with id "
					+ engineId;
			String msg = errorMsg
					+ ".  The workflow engine didn't recognise the workflow instance id.";
			logger.warn(msg);
			throw new IllegalStateException(msg);
		}
	}
	
	public String toString() {
		return workflowModel + " instance " + engineId + "@" + engine; 
	}

	/**
	 * Return a reference to the ScuflModel which this workflow was built from
	 */
	public ScuflModel getWorkflowModel() {
		return workflowModel;
	}


	// FIXME: Rename to getInstanceLSID() ?
	/**
	 * Return a unique identifier for this workflow run. If the system
	 * default LSID provider is set in DataThing.SYSTEM_DEFAULT_LSID_PROVIDER,
	 * a fresh LSID will be assigned. Otherwise, a semi-unique string will
	 * be generated from the internal engine representation.
	 * 
	 */
	public synchronized String getID() {
		if (instanceLSID == null && DataThing.SYSTEM_DEFAULT_LSID_PROVIDER != null) {
			// If there's a global LSID provider configured then use
			// it to get an LSID for the workflow instance class and
			// store it.
			LSIDProvider provider = DataThing.SYSTEM_DEFAULT_LSID_PROVIDER;
			instanceLSID = provider.getID(LSIDProvider.WFINSTANCE);
		}
		if (instanceLSID == null) {
			// Last resort, make up some kind of id
			return engine.toString() + engineId;
		}
		return instanceLSID;
	}

	public String getDefinitionLSID() {
		String lsid = workflowModel.getDescription().getLSID();
		if (lsid == null) {
			return "";
		}
		return lsid;
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
		input = inputMap;
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
					.fireEvent(new WorkflowCreationEvent(this, input,
							getDefinitionLSID()));
			// Add a new listener to handle and emit the workflow completion
			// events
			addWorkflowStateListener(new WorkflowStateListener() {
				public void workflowStateChanged(WorkflowStateChangedEvent event) {
					WorkflowEventDispatcher dispatcher = WorkflowEventDispatcher.DISPATCHER;
					WorkflowState state = event.getWorkflowState();
					if (state.isFinal()) {
						// Have either completion, cancellation or failure
						if (state.equals(WorkflowState.CANCELLED)
								|| state.equals(WorkflowState.FAILED)) {
							dispatcher
									.fireEvent(new WorkflowFailureEvent(
											WorkflowInstanceImpl.this));
						} else if (state.equals(WorkflowState.COMPLETE)) {
							dispatcher
									.fireEvent(new WorkflowCompletionEvent(
											WorkflowInstanceImpl.this));
						}
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
	@SuppressWarnings("unchecked")
	public Map<String, DataThing>[] getIntermediateResultsForProcessor(String processorName)
			throws UnknownProcessorException {
		try {
			return engine.getIntermediateResultsForProcessor(engineId,
					processorName);

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

	@SuppressWarnings("unchecked")
	public Map<String, DataThing> getOutput() {
		try {
			// It's <String, DataThing> because we have a Taverna-based engine
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

	/**
	 * Ask for the the workflow instance to be destroyed. 
	 * <p>
	 * This happens in two stages. First, an WorkflowToBeDestroyedEvent is
	 * sent to each of the registered WorkflowEventListener. A mini-listener has
	 * been added by this method, which will be the latest listener to receive 
	 * that event, uppon when the real destruction (through doDestroy()) will occur. 
	 * After that, a WorkflowDestroyedEvent is sent out to confirm the destruction.
	 * Finally, cleanup() is called.
	 * 
	 * @see doDestroy()
	 * @see cleanup()
	 * @see WorkflowToBeDestroyedEvent
	 * @see WorkflowDestroyedEvent
	 * @see WorkflowEventDispatcher
	 */
	public synchronized void destroy() {
		if (toBeDestroyed) {
			// Only one destruction can be initiated
			return;
		}
		// We'll add a listener here so that we can assume it is
		// the latest listener 
		WorkflowEventDispatcher.DISPATCHER.addListener(new WorkflowEventAdapter(){
			public void workflowDestroyed(WorkflowDestroyedEvent event) {
				if (! event.getWorkflowInstanceID().equals(getID())) {
					return; // someone else
				}
				// This listener has done it's job now, but we can't remove ourself
				// from this thread, as it will have the synchronized lock on 
				// 
				WorkflowEventDispatcher.DISPATCHER.removeListener(this);
				// And we'll clean up some other stuff
				cleanup();
			}
			public void workflowToBeDestroyed(WorkflowToBeDestroyedEvent event) {
				if (event.getWorkflowInstance() != WorkflowInstanceImpl.this) {
					return; // someone else
				}
				if (! toBeDestroyed) {
					// Maybe one of our listeners decided to cancel the destroy
					logger.info("destroy() cancelled");
					// TODO: Implement a cancelDestroy() method - if needed
					return;
				}
				// We are the last listener, so we'll get started on the destruction
				String id = getID();
				doDestroy();
				// We'll pop off this event, and do the final cleanup in 
				// workflowDestroyed()
				WorkflowEventDispatcher.DISPATCHER.fireEvent(new WorkflowDestroyedEvent(id));
			}	
		});
		toBeDestroyed = true;
		WorkflowEventDispatcher.DISPATCHER.fireEvent(new WorkflowToBeDestroyedEvent(this));
	}
	
	
	/**
	 * Perform the destruction of this workflow instance in the
	 * workflow engine.
	 * <p>
	 * This method is called by destroy() after all listeners have received
	 * an WorkflowToBeDestroyedEvent.
	 * 
	 * @see destroy()
	 */
	// Yes, circular references are possible in Java because there can
	// be threads referring to the circle.
	private synchronized void doDestroy() {
		if (! toBeDestroyed) {
			logger.error("Attempted to call doDestroy() without calling destroy()");
			return;
		}
		logger.debug("Destroying " + this);
		try {
			engine.destroy(engineId);
		} catch (UnknownWorkflowInstanceException e) {
			String msg = "Error destroying workflow instance with id "
					+ engineId
					+ ".  The workflow engine didn't recognise the workflow instance id";
			logger.error(msg, e);
			throw new IllegalStateException(msg);
		}
		
		logger.debug("Destroyed " + this);		
	}
	
	/**
	 * Clear out our own references to avoid any circular references.
	 * This involves destroying it at the engine, removing it 
	 * from instanceCache, and resetting internal fields to null.
	 * <p>
	 * This method is called by destroy() after the WorkflowDestroyedEvent
	 * has been received by all listeners.
	 *
	 */
	private synchronized void cleanup() {
		if (! toBeDestroyed) {
			logger.error("Attempted to call cleanup() without calling destroy()");
			return;
		}
        List<Object> cacheKey = cacheKey(engine, engineId);
		instanceCache.remove(cacheKey);
		engine = null;
		engineId = null;
		context = null;
		input = null;
		workflowModel = null;
		instanceLSID = null;
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
			return engine.changeOutputPortTaskData(engineId, processorId,
					OutputPortName, newData);
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
