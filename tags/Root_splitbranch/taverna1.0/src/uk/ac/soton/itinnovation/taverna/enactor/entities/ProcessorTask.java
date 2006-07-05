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
//      Created Date        :   2003/04/8
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: sowen70 $
//                              $Date: 2006-07-04 14:05:51 $
//                              $Revision: 1.77 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.taverna.enactor.entities;

import java.beans.IntrospectionException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.BaclavaIterator;
import org.embl.ebi.escience.baclava.BaclavaIteratorNode;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.JoinIteratorNode;
import org.embl.ebi.escience.baclava.ResumableIterator;
import org.embl.ebi.escience.baclava.store.BaclavaDataService;
import org.embl.ebi.escience.baclava.store.BaclavaDataServiceFactory;
import org.embl.ebi.escience.baclava.store.DuplicateLSIDException;
import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.AnnotationTemplate;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessFailureEvent;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowEventDispatcher;
import org.embl.ebi.escience.scufl.provenance.process.AlternateProcessScheduled;
import org.embl.ebi.escience.scufl.provenance.process.ConstructingIterator;
import org.embl.ebi.escience.scufl.provenance.process.DataMismatchError;
import org.embl.ebi.escience.scufl.provenance.process.Invoking;
import org.embl.ebi.escience.scufl.provenance.process.InvokingWithIteration;
import org.embl.ebi.escience.scufl.provenance.process.ProcessCancelled;
import org.embl.ebi.escience.scufl.provenance.process.ProcessComplete;
import org.embl.ebi.escience.scufl.provenance.process.ProcessEvent;
import org.embl.ebi.escience.scufl.provenance.process.ProcessPaused;
import org.embl.ebi.escience.scufl.provenance.process.ProcessScheduled;
import org.embl.ebi.escience.scufl.provenance.process.ServiceError;
import org.embl.ebi.escience.scufl.provenance.process.ServiceFailure;
import org.embl.ebi.escience.scufl.provenance.process.WaitingToRetry;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.jdom.Element;
import org.jdom.Namespace;

import uk.ac.soton.itinnovation.freefluo.core.event.RunEvent;
import uk.ac.soton.itinnovation.freefluo.core.flow.Flow;
import uk.ac.soton.itinnovation.freefluo.core.task.AbstractTask;
import uk.ac.soton.itinnovation.freefluo.core.task.Task;
import uk.ac.soton.itinnovation.freefluo.main.Engine;
import uk.ac.soton.itinnovation.freefluo.task.LogLevel;

/**
 * The superclass of all actual task implementations
 */
public class ProcessorTask extends AbstractTask  implements IProcessorTask {
	public static Namespace provNS = Namespace.getNamespace("p",
			"http://org.embl.ebi.escience/xscuflprovenance/0.1alpha");

	public static final String REPORT_NAMESPACE = "http://www.it-innovation.soton.ac.uk/taverna/workflow/enactor/progress";

	private static WorkflowEventDispatcher DISPATCHER = WorkflowEventDispatcher.DISPATCHER;

	int activeWorkers = 0;
		

	static BaclavaDataService STORE = BaclavaDataServiceFactory.getStore();

	// The processor from which the task to be invoked
	// should be derived
	private Processor activeProcessor = null;

	// The task that was invoked
	private ProcessorTaskWorker processorTaskWorker = null;

	// The current input mapping
	private Map activeInputMapping = null;

	// The current output mapping
	private Map activeOutputMapping = null;

	// The list of provenance statements, text for now
	private List provenanceList = new ArrayList();

	// The WorkflowInstance object which can access this
	// workflow instance
	public WorkflowInstance workflowInstance = null;
	
	protected static final Namespace PROVENANCE_NAMESPACE = provNS;

	protected Processor proc = null;

	protected LogLevel logLevel = null;

	private Logger logger = Logger.getLogger(ProcessorTask.class);

	private String userID;

	private String userCtx;

	private List eventList;

	/**
	 * Default Constructor
	 * 	 
	 */
	public ProcessorTask(String id, Flow flow, Processor p, LogLevel l, String userID, String userCtx) {
		super(id, flow);
		proc = p;
		this.logLevel = new LogLevel(l.getLevel());
		this.userID = userID;
		this.userCtx = userCtx;
		this.eventList = Collections.synchronizedList(new ArrayList());
		super.setFailFlowOnTaskFailure(p.getCritical());
		// Add an event to the list for the scheduling operation
		schedule(p);
	}

	public List getProvenanceList() {
		return this.provenanceList;
	}
	
	public WorkflowInstance getWorkflowInstance() {
		return workflowInstance;
	}

	/**
	 * Set up the next invocation to use the specified processor
	 */
	private synchronized void schedule(Processor theProcessor) {
		activeProcessor = theProcessor;
		activeInputMapping = null;
		activeOutputMapping = null;
		eventList.add(new ProcessScheduled(theProcessor));
	}

	/**
	 * Set up the next invocation to use the specified alternate processor
	 */
	private synchronized void schedule(AlternateProcessor theAlternate) {
		activeProcessor = theAlternate.getProcessor();
		activeInputMapping = theAlternate.getInputMapping();
		activeOutputMapping = new HashMap();
		// invert the mapping contained by the alternates object!
		for (Iterator i = theAlternate.getOutputMapping().keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = (String) theAlternate.getOutputMapping().get(key);
			activeOutputMapping.put(value, key);
		}
		eventList.add(new AlternateProcessScheduled(activeProcessor));
	}

	/**
	 * Add paused event to the event list.
	 */
	protected synchronized void taskPaused() {
		eventList.add(new ProcessPaused(activeProcessor));
	}

	/**
	 * Add cancel event to the event list.
	 */
	protected synchronized void taskCancelled() {
		eventList.add(new ProcessCancelled(activeProcessor));
	}

	/**
	 * Add resume (invoking) event to the event list.
	 */
	protected synchronized void taskResumed() {
		eventList.add(new Invoking());
	}

	/**
	 * Add complete event to the event list.
	 */
	protected synchronized void taskComplete() {
		eventList.add(new ProcessComplete());
	}

	/**
	 * Run the task
	 */
	public void handleRun(RunEvent runEvents) {
		try {
			// Get the workflow instance object
			Flow flow = getFlow();
			String flowID = flow.getFlowId();
			Engine e = flow.getEngine();
			this.workflowInstance = (WorkflowInstance) new org.embl.ebi.escience.scufl.enactor.implementation.WorkflowInstanceImpl(
					e, activeProcessor.getModel(), flowID);
			// //System.out.println("Invoking processor task for
			// "+activeProcessor.getName());
			// The default processor will have been scheduled by the
			// constructor to this class so we can get on and do stuff.
			for (int i = -1; i < proc.getAlternatesArray().length; i++) {
				// If i>-1 then reschedule an alternate.
				if (i > -1) {
					schedule(proc.getAlternatesArray()[i]);
				}
				try {
					invoke();
					// "Task completed successfully"
					break;
				} catch (TaskExecutionException tee) {
					// If there are alternates left then just loop
					// otherwise rethrow
					if (i == proc.getAlternatesArray().length - 1) {
						throw tee;
					} else {
						// loop
					}
				}
			}

			// "Task " + getTaskId() + " in flow " + getFlow().getFlowId() + "
			// completed successfully"
			complete();
		} catch (Exception ex) {
			eventList.add(new ServiceFailure());					
			faultCausingException = ex;
			logger.error("Failure while executing task " + getTaskId(), ex);
			fail("Task " + getTaskId() + " in flow " + getFlow().getFlowId() + " failed.  " + ex.getMessage());
			Map inputMap = new HashMap();
			for (Iterator i = getParents().iterator(); i.hasNext();) {
				Task task = (Task) i.next();
				if (task instanceof PortTask) {
					PortTask inputPortTask = (PortTask) task;
					DataThing dataThing = inputPortTask.getData();
					String portName = inputPortTask.getScuflPort().getName();
					inputMap.put(portName, dataThing);
				}
			}
			if (processorTaskWorker instanceof EnactorWorkflowTask) {								
				WorkflowInstance nestedWorkflow = (processorTaskWorker != null ? ((EnactorWorkflowTask)processorTaskWorker).getWorkflowInstance()
						: null);
				DISPATCHER.fireProcessFailed(new NestedWorkflowFailureEvent(workflowInstance, activeProcessor, ex,
						inputMap, nestedWorkflow));
			} else {
				DISPATCHER.fireProcessFailed(new ProcessFailureEvent(workflowInstance, activeProcessor, ex, inputMap));
			}
		}
	}

	/**
	 * Invoke, checking for iteration or not first
	 */
	private synchronized void invoke() throws TaskExecutionException {
		// Gather data from the inputs to build the input map
		Map inputMap = new HashMap();
		Map outputMap = null;
		for (Iterator i = getParents().iterator(); i.hasNext();) {
			Task task = (Task) i.next();

			if (task instanceof PortTask) {
				PortTask inputPortTask = (PortTask) task;
				DataThing dataThing = inputPortTask.getData();
				String portName = inputPortTask.getScuflPort().getName();
				inputMap.put(portName, dataThing);
			}
		}
		if (iterationRequired()) {
			outputMap = invokeWithIteration(inputMap);
		} else {
			outputMap = invokeWithoutIteration(inputMap);
			// Fire a new ProcessCompletionEvent
			if (processorTaskWorker instanceof EnactorWorkflowTask) {
				WorkflowInstance nestedWorkflow = (processorTaskWorker != null ? ((EnactorWorkflowTask)processorTaskWorker).getWorkflowInstance()
						: null);
				NestedWorkflowCompletionEvent event = new NestedWorkflowCompletionEvent(false, inputMap, outputMap,
						activeProcessor, workflowInstance, nestedWorkflow);
				DISPATCHER.fireProcessCompleted(event);
			} else {
				DISPATCHER.fireProcessCompleted(new ProcessCompletionEvent(false, inputMap, outputMap, activeProcessor,
						workflowInstance));
			}
		}
		Set alreadyStoredThings = new HashSet();
		// Iterate over the children, pushing data into the port tasks
		// as appropriate.
		for (Iterator i = getChildren().iterator(); i.hasNext();) {
			Task task = (Task) i.next();
			if (task instanceof PortTask) {
				PortTask outputPortTask = (PortTask) task;
				String portName = outputPortTask.getScuflPort().getName();
				DataThing resultDataThing = (DataThing) outputMap.get(portName);
				if (resultDataThing != null) {
					outputPortTask.setData(resultDataThing);
					// If the data store is configured then
					// push the datathing into it
					if (ProcessorTask.STORE != null) {
						try {
							// Avoid duplicates
							if (alreadyStoredThings.contains(outputPortTask.getData()) == false) {
								STORE.storeDataThing(outputPortTask.getData(), true);
								alreadyStoredThings.add(outputPortTask.getData());
							}
						} catch (DuplicateLSIDException dple) {
							//
						} catch (Exception e) {
							logger.error("Exception thrown while trying to store a datathing,\n"
									+ "disabling further stores.",e);							
							STORE = null;
						}
					}
				} else {
					// Datathing was null, so the processor didn't produce an
					// output that it
					// declared it did in its operational contract. This is an
					// error condition
					// and should be reported as such to the user
					eventList.add(new ServiceError(new RuntimeException("Output '" + portName
							+ "' was declared but never created!")));
					throw new TaskExecutionException("Output port '" + portName + "' not populated by service instance");
				}
			}
		}
		eventList.add(new ProcessComplete());
	}

	/**
	 * Method that actually undertakes a service action. Should be implemented
	 * by concrete processors.
	 * 
	 * @return output map containing String->DataThing named pairs, with the key
	 *         being the name of the output port. This implementation also
	 *         includes the logic to remap the port names in the event of an
	 *         alternate processor being used with differing names for the
	 *         inputs and outputs.
	 * 
	 * CHANGE - this method was synchronized, was there a reason for this?
	 */
	private Map invokeOnce(Map inputMap) throws TaskExecutionException {
		processorTaskWorker = ProcessorHelper.getTaskWorker(activeProcessor);
		Map output = null;

		if (activeInputMapping == null) {
			// If no input mapping is defined then we just invoke directly
			output = doInvocationWithRetryLogic(inputMap);
		} else {
			// Otherwise have to remap the input map
			Map taskInput = new HashMap();
			for (Iterator i = inputMap.keySet().iterator(); i.hasNext();) {
				String originalInputName = (String) i.next();
				DataThing inputItem = (DataThing) inputMap.get(originalInputName);
				String targetInputName = (String) activeInputMapping.get(originalInputName);
				if (targetInputName == null) {
					targetInputName = originalInputName;
				}
				
				if (inputItem != null) {
					taskInput.put(targetInputName, inputItem);
				}
			}
			output = doInvocationWithRetryLogic(taskInput);
		}

		// Now do the same for the output mapping
		if (activeOutputMapping == null) { 
			return output;
		} else {
			Map taskOutput = new HashMap();
			for (Iterator i = output.keySet().iterator(); i.hasNext();) {
				String realOutputName = (String) i.next();
				String targetOutputName = (String) activeOutputMapping.get(realOutputName);
				if (targetOutputName == null) {
					targetOutputName = realOutputName;
				}
				// //System.out.println("Storing result from
				// '"+realOutputName+"' as '"+targetOutputName+"'");
				DataThing outputItem = (DataThing) output.get(realOutputName);
				taskOutput.put(targetOutputName, outputItem);
			}
			return taskOutput;
		}
	}

	/**
	 * Given a map of DataThing objects, call the fillLSIDValues() method on
	 * each object within the map.
	 */
	private void fillAllLSIDs(Map inputMap) {
		for (Iterator i = inputMap.values().iterator(); i.hasNext();) {
			((DataThing) i.next()).fillLSIDValues();
		}
	}

	/**
	 * Do any post-processor transformations required on output data things.
	 * Currently just special-case handling of replacelsid... (insert SPI here).
	 */
	private void transformOutputDataThings(Map inputMap, Map outputMap) {
		if (outputMap == null)
			return;
		HashMap newMap = new HashMap();
		for (Iterator i = outputMap.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			DataThing dataThing = (DataThing) outputMap.get(name);
			if (dataThing != null && name.startsWith("replacelsid")) {
				newMap.put(name, TransformDataThing.replacelsid(dataThing, inputMap, outputMap));
			}
		}
		for (Iterator i = newMap.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			outputMap.remove(name);
			outputMap.put(name, newMap.get(name));
		}
	}

	/**
	 * Actually call the service instance, handles the retry logic.
	 */
	private Map doInvocationWithRetryLogic(Map inputMap) throws TaskExecutionException {
		try {
			// If the first invocation works then great, return it.
			return runAndGenerateTemplates(inputMap);
		} catch (TaskExecutionException tee) {
			eventList.add(new ServiceError(tee));
			// First invocation has failed, see if there are retries.
			int maxRetries = activeProcessor.getRetries();
			if (maxRetries == 0) {
				// No retries, rethrow the task execution exception.
				throw tee;
			}
			// Retries available, so loop, sleeping first (we've already
			// done one invocation so the first thing to do is to wait.
			int baseTimeout = activeProcessor.getRetryDelay();
			double backoff = activeProcessor.getBackoff();
			for (int i = 0; i < maxRetries; i++) {
				int waitTime = (int) ((double) baseTimeout * Math.pow(backoff, (double) i));
				try {
					logger.info(Thread.currentThread() + " About to sleep for " + waitTime + ", retry "
							+ (i + 1) + " of " + maxRetries);
					eventList.add(new WaitingToRetry(waitTime, i + 1, maxRetries));
					Thread.currentThread().sleep(waitTime);
					logger.info(Thread.currentThread() + " Done sleeping, attempting to rerun operation");
					return runAndGenerateTemplates(inputMap);
				} catch (InterruptedException ie) {
					TaskExecutionException t = new TaskExecutionException("Interrupted during wait to retry");
					t.initCause(ie);
					eventList.add(new ServiceError(t));
					throw t;
				} catch (TaskExecutionException t) {
					eventList.add(new ServiceError(t));
				} catch (Exception ex) {
					logger.error(ex);					
				}
			}
		}
		throw new TaskExecutionException("No retries remaining");
	}

	/**
	 * Call the service worker, populate LSID and generate any available
	 * templates
	 */
	private Map runAndGenerateTemplates(Map inputMap) throws TaskExecutionException {
		// Insert any input defaults with no bound values in the map
		InputPort[] ip = activeProcessor.getInputPorts();
		for (int i = 0; i < ip.length; i++) {
			if (ip[i].hasDefaultValue() && inputMap.containsKey(ip[i].getName()) == false) {
				inputMap.put(ip[i].getName(), new DataThing(ip[i].getWrappedDefaultValue()));
			}
		}
		// Populate all LSIDs in the output map
		Map outputMap = processorTaskWorker.execute(inputMap, this);
		fillAllLSIDs(outputMap);
		transformOutputDataThings(inputMap, outputMap);
		AnnotationTemplate[] templates = activeProcessor.getAnnotationTemplates();
		AnnotationTemplate[] defaultTemplates = activeProcessor.defaultAnnotationTemplates();
		if (templates.length > 0 || defaultTemplates.length > 0) {
			// For the inputs and outputs get the LSIDs of all the
			// data objects, and apply these to the processor's
			// annotation templates.
			Map templateInputs = new HashMap();
			Map templateOutputs = new HashMap();
			for (Iterator i = inputMap.keySet().iterator(); i.hasNext();) {
				String inputName = (String) i.next();
				DataThing inputValue = (DataThing) inputMap.get(inputName);
				String objectLSID = inputValue.getLSID(inputValue.getDataObject());
				if (objectLSID != null && objectLSID.equals("") == false) {
					templateInputs.put(inputName, objectLSID);
				}
			}
			for (Iterator i = outputMap.keySet().iterator(); i.hasNext();) {
				String outputName = (String) i.next();
				DataThing outputValue = (DataThing) outputMap.get(outputName);
				String objectLSID = outputValue.getLSID(outputValue.getDataObject());
				if (objectLSID != null && objectLSID.equals("") == false) {
					templateOutputs.put(outputName, objectLSID);
				}
			}
			// Iterate over each of the templates, getting their
			// textual provenance back for the moment and adding it to the
			// provenance list
			for (int i = 0; i < templates.length; i++) {
				String annotation = templates[i].getTextAnnotation(templateInputs, templateOutputs);
				if (annotation != null) {
					provenanceList.add(AnnotationTemplate.convert(annotation));
					if (ProcessorTask.STORE != null) {
						STORE.storeMetadata(annotation);
					}
				}
			}
			for (int i = 0; i < defaultTemplates.length; i++) {
				String annotation = defaultTemplates[i].getTextAnnotation(templateInputs, templateOutputs);
				if (annotation != null) {
					provenanceList.add(AnnotationTemplate.convert(annotation));
					if (ProcessorTask.STORE != null) {
						STORE.storeMetadata(annotation);
					}
				}
			}
		}

		return outputMap;
	}

	/**
	 * Wrapper to invoke without iteration
	 */
	private synchronized Map invokeWithoutIteration(Map inputMap) throws TaskExecutionException {
		eventList.add(new Invoking());
		return invokeOnce(inputMap);
	}

	/**
	 * Check whether iteration is required
	 */
	private synchronized boolean iterationRequired() {
		for (Iterator i = getParents().iterator(); i.hasNext();) {
			Task task = (Task) i.next();
			if (task instanceof PortTask) {
				// For each input port task check the types
				PortTask inputPortTask = (PortTask) task;
				DataThing dataThing = inputPortTask.getData();
				String dataType = dataThing.getSyntacticType().split("\\'")[0];
				String portType = inputPortTask.getScuflPort().getSyntacticType().split("\\'")[0];
				if (dataType.equals(portType) == false) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Invoke with iteration
	 */
	private synchronized Map invokeWithIteration(Map inputMap) throws TaskExecutionException {
		if (getProcessor().getIterationStrategy() == null) {
			// No explicit strategy specified
			eventList.add(new ConstructingIterator());
		} else {
			// Strategy defined so pass strategy information into
			// process provenance
			eventList.add(new ConstructingIterator(getProcessor().getIterationStrategy()));
		}

		// Create a map of input name -> BaclavaIteratorNode instance
		Map iteratorNodeMap = new HashMap();
		// Iterate over all parent tasks to get the original port
		// types, use this to determine how deeply into the collection
		// we need to drill
		for (Iterator i = getParents().iterator(); i.hasNext();) {
			Task task = (Task) i.next();
			if (task instanceof PortTask) {
				// Found an input port
				String portName = ((PortTask) task).getScuflPort().getName();
				String portType = ((PortTask) task).getScuflPort().getSyntacticType();
				DataThing portData = (DataThing) inputMap.get(portName);
				try {
					BaclavaIterator iterator = portData.iterator(portType);
					BaclavaIteratorNode iteratorNode = new BaclavaIteratorNode(iterator, portName);
					iteratorNodeMap.put(portName, iteratorNode);
				} catch (IntrospectionException ie) {
					eventList.add(new DataMismatchError());
					throw new TaskExecutionException("Unable to reconcile iterator types");
				}
			}
		}

		ResumableIterator rootNode = null;

		if (getProcessor().getIterationStrategy() == null) {
			// Default behaviour is to create a cross join across all input
			// iterators
			rootNode = new JoinIteratorNode();
			for (Iterator i = iteratorNodeMap.values().iterator(); i.hasNext();) {
				BaclavaIteratorNode iteratorNode = (BaclavaIteratorNode) i.next();
				((JoinIteratorNode) rootNode).add(iteratorNode);
			}
		} else {
			// Use the IterationStrategy object to construct the iterator
			try {
				rootNode = getProcessor().getIterationStrategy().buildIterator(iteratorNodeMap);
			} catch (IntrospectionException ie) {
				eventList.add(new DataMismatchError());
				throw new TaskExecutionException("Unable to reconcile iterator types");
			}
		}

		// Create the mapping of collection -> set of component LSIDs, this can
		// then
		// be passed on to the iteration completion event. The keys are LSIDs of
		// the
		// output collections, the values are sets of the LSIDs of the
		// individual
		// result items which have been used to compose these collections.
		final Map collectionStructure = new HashMap();
		int totalIterations = rootNode.size();
		final Map lsidForNamedOutput = new HashMap();

		// Create the output container
		final Map outputMap = new HashMap();
		for (Iterator i = getChildren().iterator(); i.hasNext();) {
			Task task = (Task) i.next();
			if (task instanceof PortTask) {
				PortTask outputPortTask = (PortTask) task;
				// Create data things with array lists inside them.
				DataThing outputThing = new DataThing(new ArrayList(totalIterations));
				// Assign an LSID to the root collection object
				outputThing.fillLSIDValues();
				String collectionLSID = outputThing.getLSID(outputThing.getDataObject());
				// Hack - remove the LSID from this datathing and remember it,
				// will reinsert
				// after the iteration has completed. This is a workaround for a
				// potentially
				// expensive remove operation within the iteration loop
				outputThing.getLSIDMap().remove(outputThing.getDataObject());
				lsidForNamedOutput.put(outputPortTask.getScuflPort().getName(), collectionLSID);
				outputMap.put(outputPortTask.getScuflPort().getName(), outputThing);
				// Create an entry in the collectionStructure map containing
				// a set, initially empty
				collectionStructure.put(collectionLSID, new HashSet());
			}
		}

		// Create a similar mapping for the input LSIDs, indicate where the
		// collections
		// have been shredded down to allow for the iteration composition
		// Keys are strings containing the LSIDs of the input objects with
		// values
		// being the sets of decomposed datathing LSID values which the
		// iteration has
		// split them into. At the end of this sets with only one item (i.e.
		// self iteration
		// where no splitting has taken place) are removed to avoid redundant
		// information
		// in the provenance logs.
		final Map inputNameToLSID = new HashMap();
		final Map inputShredding = new HashMap();
		for (Iterator i = getParents().iterator(); i.hasNext();) {
			Task task = (Task) i.next();
			if (task instanceof PortTask) {
				PortTask inputPortTask = (PortTask) task;
				DataThing inputThing = inputPortTask.getData();
				String inputLSID = inputThing.getLSID(inputThing.getDataObject());
				String inputName = inputPortTask.getScuflPort().getName();
				inputNameToLSID.put(inputName, inputLSID);
				inputShredding.put(inputLSID, new HashSet());
			}
		}

		currentIteration = 0;
		final List finalEventList = eventList;
		final List completionEvents = new ArrayList();

		// Different code paths for processors which allow multiple workers...
		if (activeProcessor.getMaximumWorkers() == 1) {
			activeWorkers++;
			while (doSingleIteration(rootNode, finalEventList, completionEvents, outputMap, inputShredding, collectionStructure,
					lsidForNamedOutput, inputNameToLSID))
				;
			activeWorkers--;
		} else {
			// Create multiple worker threads
			int workers = activeProcessor.getWorkers();
			if (workers > rootNode.size()) {
				workers = rootNode.size();
			}
			final boolean[] active = new boolean[workers];
			final Thread manager = Thread.currentThread();
			final TaskExecutionException[] exception = new TaskExecutionException[1];
			for (int i = 0; i < workers; i++) {
				final int position = i;
				final ResumableIterator irootNode = rootNode;
				new Thread() {
					public void run() {
						try {
							logger.info(Thread.currentThread() + " started");
							active[position] = true;
							activeWorkers++;
							while (doSingleIteration(irootNode, finalEventList, completionEvents, outputMap, inputShredding,
									collectionStructure, lsidForNamedOutput, inputNameToLSID))
								;
							activeWorkers--;
							active[position] = false;
						} catch (TaskExecutionException tee) {
							logger.error(tee);							
							exception[0] = tee;
						} catch (Throwable e) {
							logger.error(e);							
						} finally {
							logger.info(Thread.currentThread() + " completed");
							active[position] = false;
							manager.interrupt();
						}
					}
				}.start();
			}
			boolean finished = false;
			while (finished == false) {
				try {
					manager.sleep(1000);
				} catch (InterruptedException ie) {
					if (exception[0] != null) {
						throw exception[0];
					}
				}
				finished = true;
				for (int i = 0; i < workers; i++) {
					if (active[i] == true) {
						finished = false;
					}
				}
			}
		}
				 
		// Fix up the LSIDs in the outputs
		for (Iterator i = outputMap.keySet().iterator(); i.hasNext();) {
			String outputName = (String) i.next();
			DataThing outputThing = (DataThing) outputMap.get(outputName);
			String collectionLSID = (String) lsidForNamedOutput.get(outputName);
			outputThing.getLSIDMap().put(outputThing.getDataObject(), collectionLSID);
		}

		// Iterate over the inputShredding map and remove any keys which map to
		// sets with
		// only a single item where the item in the set is the same as the key
		// (i.e. where
		// the iteration structure has iterated over a single item repeatedly)
		Set removeKeys = new HashSet();
		for (Iterator i = inputShredding.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			Set shredSet = (Set) inputShredding.get(key);
			if (shredSet.size() == 1 && shredSet.contains(key)) {
				removeKeys.add(key);
			}
		}
		for (Iterator i = removeKeys.iterator(); i.hasNext();) {
			inputShredding.remove(i.next());
		}
		IterationCompletionEvent iterationCompletionEvent=new IterationCompletionEvent(collectionStructure, inputShredding,
				workflowInstance, activeProcessor,completionEvents, inputMap, outputMap);
		DISPATCHER.fireIterationCompleted(iterationCompletionEvent);

		return outputMap;
	}

	private int currentIteration = 0;

	private boolean doSingleIteration(ResumableIterator jobQueue, List eventList, List completionEvents, Map outputMap, Map inputShredding,
			Map collectionStructure, Map lsidForNamedOutput, Map inputNameToLSID) throws TaskExecutionException {
		Map inputSet = null;
		int[] currentLocation = null;
		int queueSize = 0;
		InvokingWithIteration event;
		synchronized (jobQueue) {
			if (jobQueue.hasNext() == false) {
				return false;
			} else {
				inputSet = (Map) jobQueue.next();
				currentLocation = jobQueue.getCurrentLocation();
				queueSize = jobQueue.size();
				int eventListSize = eventList.size();
				Object o = eventList.get(eventListSize - 1);
				if (o instanceof InvokingWithIteration) {
					event = (InvokingWithIteration) o;
				} else {
					event = new InvokingWithIteration(currentIteration, queueSize);
					eventList.add(event);
				}
			}
		}		

		// Iterate over the set and add the LSIDs of inputs to the
		// inputShredding map...
		for (Iterator i = inputSet.keySet().iterator(); i.hasNext();) {
			String inputName = (String) i.next();
			DataThing inputThing = (DataThing) inputSet.get(inputName);
			String inputThingLSID = inputThing.getLSID(inputThing.getDataObject());
			String primaryLSID = (String) inputNameToLSID.get(inputName);
			Set shredding = (Set) inputShredding.get(primaryLSID);
			shredding.add(inputThingLSID);
		}
		
		// Mark the current iteration
		event.setIterationNumber("" + (++currentIteration));
		event.setActiveWorkers("" + activeWorkers);
		
		// Run the process		
		Map singleResultMap = invokeOnce(inputSet);
		
		
		// Fire a new ProcessCompletionEvent
		ProcessCompletionEvent completionEvent;
		if (processorTaskWorker instanceof EnactorWorkflowTask) {
			WorkflowInstance nestedWorkflow = (processorTaskWorker != null ? ((EnactorWorkflowTask)processorTaskWorker).getWorkflowInstance()
					: null);
			completionEvent = new NestedWorkflowCompletionEvent(true, inputSet,
					outputMap, activeProcessor, workflowInstance, nestedWorkflow);
			
		} else {
			completionEvent=new ProcessCompletionEvent(true, inputSet, singleResultMap,
					activeProcessor, workflowInstance);
		}
		DISPATCHER.fireProcessCompleted(completionEvent);
		completionEvents.add(completionEvent);
		
		// Iterate over the outputs
		for (Iterator l = singleResultMap.keySet().iterator(); l.hasNext();) {
			String outputName = (String) l.next();
			DataThing outputValue = (DataThing) singleResultMap.get(outputName);
			Object dataObject = outputValue.getDataObject();
			// addition of a fix here by Chris Wroe
			// Before it tried to map all results from the service call into the
			// subsequent
			// data flow causing a null pointer exception if no such data flow
			// existed.
			if (outputMap.containsKey(outputName)) {
				DataThing targetThing = (DataThing) outputMap.get(outputName);
				
				List targetList = (List) targetThing.getDataObject();				
				// Store the result into the appropriate output collection
				
				insertObjectInto(dataObject, targetList, currentLocation, targetThing);
				
				// Copy metadata from the original output into the new one,
				// preserve LSID hopefully!				
				targetThing.copyMetadataFrom(outputValue);
				
				// Get the LSID of the original output item				
				String originalLSID = outputValue.getLSID(dataObject);
				
				String collectionLSID = (String) lsidForNamedOutput.get(outputName);

				synchronized (collectionStructure) {
					((Set) collectionStructure.get(collectionLSID)).add(originalLSID);
				}
			}
			// / fix ends
		}
		return true;
	}

	/**
	 * A private method to insert an object into a root list at the position
	 * specified by an array of integer indices as returned from the
	 * ResumableIterator getCurrentLocation method. This method must be called
	 * with at least one element in the position array, but I'm fairly sure that
	 * this is implied by the calling context anyway. The DataThing is supplied
	 * so that the LSID map can be correctly maintained
	 */
	private void insertObjectInto(Object o, List l, int[] position, DataThing theThing) {
		synchronized (l) {
			List currentList = l;
			// Does this get and set LSID logic slow the thing down? Seems like
			// it shouldn't
			// but we don't really need to do this once per iteration, should
			// really be once
			// per processor surely? Even if this isn't the source of the non
			// constant time
			// cost for this method it should be moved out, has to help
			// performance a bit.
			// Should investigate what the costs are for the remove and
			// containskey operations
			// on the hashes.
			// String oldLSID = theThing.getLSID(l);
			// if (oldLSID != null) {
			// theThing.getLSIDMap().remove(l);
			// }
			// Walk over the index array to find the enclosing collection
			for (int i = 0; i < position.length - 1; i++) {
				int index = position[i];
				if (index < currentList.size()) {
					currentList = (List) currentList.get(index);
				} else {
					// How many lists are needed to pad this?
					int numberShort = (index - currentList.size()) + 1;
					for (int j = 0; j < numberShort; j++) {
						currentList.add(new ArrayList());
					}
					currentList = (List) currentList.get(index);
				}
			}
			// The leaf index is the last in the position array
			int objectIndex = position[position.length - 1];
			int listSize = currentList.size();
			if (objectIndex + 1 > listSize) {
				// Grow the list and populate it with nulls to fit
				// to at least the size required
				for (int i = listSize; i < objectIndex + 1; i++) {
					currentList.add("");
				}
			}
			// By definition can just set the object at list index to the
			// correct value
			currentList.set(objectIndex, o);
			/**
			 * Check whether it's safe to just insert this item at the given
			 * position if (currentList.size() < objectIndex) {
			 * currentList.add(o); } else { currentList.add(objectIndex, o); }
			 */
			/**
			 * try { currentList.add(objectIndex, o); } catch
			 * (IndexOutOfBoundsException ioobe) { // Just add onto the end
			 * currentList.add(o); }
			 */
			// if (oldLSID!=null) {
			// theThing.setLSID(l, oldLSID);
			// }
		}
	}
		

	/**
	 * Retrieve the user identifier for the parent workflow
	 */
	protected String getUserID() {
		return userID;
	}

	/**
	 * Get the list of events thus far
	 */
	public ProcessEvent[] getEventList() {
		return (ProcessEvent[]) this.eventList.toArray(new ProcessEvent[0]);
	}

	/**
	 * Retrieve the user context for the parent workflow
	 */
	protected String getUserNamespaceContext() {
		return userCtx;
	}

	/**
	 * Retrieves the XScufl Processor object for this task
	 * 
	 * @return the Processor object that this task is an execution shim for
	 */
	public Processor getProcessor() {
		return proc;
	}

	/**
	 * Retrieve provenance information for this task, concrete tasks should
	 * overide this method and provide this information as an XML JDOM element
	 */
	public Element getProvenance() {
		return new Element("NotImplementedHere", PROVENANCE_NAMESPACE);
	}

	private Exception faultCausingException = null;

	/**
	 * If the processor invocation throws an exception causing it to fail, this
	 * class will populate an XML element with as much information as possible
	 * about the exception that was thrown.
	 */
	public Element getFaultElement() {
		if (faultCausingException == null) {
			return null;
		}
		Element faultElement = new Element("failureDescription", PROVENANCE_NAMESPACE);
		String faultClass = faultCausingException.getClass().getName();
		String faultMessage = faultCausingException.getMessage();
		StringWriter sw = new StringWriter();
		faultCausingException.printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();
		Element faultClassElement = new Element("exceptionClass", PROVENANCE_NAMESPACE);
		faultClassElement.setText(faultClass);
		faultElement.addContent(faultClassElement);
		Element faultMessageElement = new Element("exceptionMessage", PROVENANCE_NAMESPACE);
		faultMessageElement.setText(faultMessage);
		faultElement.addContent(faultMessageElement);
		Element faultTraceElement = new Element("exceptionTrace", PROVENANCE_NAMESPACE);
		faultTraceElement.setText(stackTrace);
		faultElement.addContent(faultTraceElement);
		return faultElement;
	}

	/**
	 * Method that actually undertakes a service action. Should be implemented
	 * by concrete processors.
	 * 
	 * @return output map containing String->DataThing named pairs, with the key
	 *         being the name of the output port.
	 */
	// private Map execute(Map inputMap) throws TaskExecutionException {
	// ProcessorTaskWorker worker = ProcessorHelper.getTaskWorker(proc);
	// return worker.execute(inputMap);
	// }
}
