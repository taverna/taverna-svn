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
//      Created Date        :   2003/04/8
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2004-03-29 14:46:20 $
//                              $Revision: 1.44 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.taverna.enactor.entities;

import java.beans.IntrospectionException;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.BaclavaIterator;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.JoinIterator;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.AnnotationTemplate;
import org.embl.ebi.escience.scufl.provenance.process.*;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.TimePoint;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceSelectionCriteria;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.baclava.store.*;

// Utility Imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

// IO Imports
import java.io.PrintWriter;
import java.io.StringWriter;

// JDOM Imports
import org.jdom.Element;
import org.jdom.Namespace;

import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTask;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;



/**
 * The superclass of all actual task implementations
 */
public class ProcessorTask extends TavernaTask{
    
    static BaclavaDataService STORE = null;
    static {
	String storageClassName = System.getProperty("taverna.datastore.class");
	if (storageClassName!=null) {
	    try {
		Class c = Class.forName(storageClassName);
		STORE = (BaclavaDataService)c.newInstance();
	    }
	    catch (Exception ex) {
		System.out.println("Unable to initialize data store class : "+storageClassName);
		ex.printStackTrace();
	    }
	}
    }

    // The processor from which the task to be invoked
    // should be derived
    private Processor activeProcessor = null;
    // The current input mapping
    private Map activeInputMapping = null;
    // The current output mapping
    private Map activeOutputMapping = null;
    // The list of provenance statements, text for now
    private List provenanceList = new ArrayList();
    
    public List getProvenanceList() {
	return this.provenanceList;
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
	    String key = (String)i.next();
	    String value = (String)theAlternate.getOutputMapping().get(key);
	    activeOutputMapping.put(value,key);
	}
	eventList.add(new AlternateProcessScheduled(activeProcessor));
    }
    
    /**
     * Run the task
     */
    public TaskStateMessage doTask() {
	try {
	    // The default processor will have been scheduled by the
	    // constructor to this class so we can get on and do stuff.
	    for (int i = -1; i < proc.getAlternatesArray().length; i++) {
		// If i>-1 then reschedule an alternate.
		if (i>-1) {
		    schedule(proc.getAlternatesArray()[i]);
		}
		try {
		    invoke();
		    return new TaskStateMessage(getParentFlow().getID(), 
						getID(), 
						TaskStateMessage.COMPLETE,"Task completed successfully");
		}
		catch (TaskExecutionException tee) {
		    // If there are alternates left then just loop
		    // otherwise rethrow
		    if (i>=proc.getAlternatesArray().length) {
			throw tee;
		    }
		    else {
			// loop
		    }
		}
	    }
	}
	catch (TaskExecutionException ex) {
	    ex.printStackTrace();
	    eventList.add(new ServiceFailure());
	    endTime = new TimePoint();
	    faultCausingException = ex;
	    logger.error(ex);
	    return new TaskStateMessage(getParentFlow().getID(),getID(), TaskStateMessage.FAILED,ex.getMessage());
	}
	catch (Exception ex){
	    eventList.add(new ServiceFailure());
	    endTime = new TimePoint();
	    faultCausingException = ex;
	    logger.error(ex,ex);
	    return new TaskStateMessage(getParentFlow().getID(), 
					getID(), 
					TaskStateMessage.FAILED, 
					"Unrecognised dispatch failure for a task within the workflow.");
	}
	return new TaskStateMessage(getParentFlow().getID(), 
				    getID(), 
				    TaskStateMessage.FAILED, 
				    "Unrecognised dispatch failure for a task within the workflow.");	
    }

    /**
     * Invoke, checking for iteration or not first
     */
    private synchronized void invoke() throws TaskExecutionException {
	// Gather data from the inputs to build the input map
	Map inputMap = new HashMap();
	Map outputMap = null;
	for (int i=0; i<getParents().length; i++) {
	    if (getParents()[i] instanceof PortTask) {
		PortTask inputPortTask = (PortTask)getParents()[i];
		DataThing dataThing = inputPortTask.getData();
		String portName = inputPortTask.getScuflPort().getName();
		inputMap.put(portName, dataThing);
	    }
	}
	if (iterationRequired()) {
	    outputMap = invokeWithIteration(inputMap);
	}
	else {
	    outputMap = invokeWithoutIteration(inputMap);
	}
	Set alreadyStoredThings = new HashSet();
	// Iterate over the children, pushing data into the port tasks
	// as appropriate.
	for (int i = 0; i < getChildren().length; i++) {
	    if (getChildren()[i] instanceof PortTask) {
		PortTask outputPortTask = (PortTask)getChildren()[i];
		String portName = outputPortTask.getScuflPort().getName();
		DataThing resultDataThing = (DataThing)outputMap.get(portName);
		if (resultDataThing != null) {
		    outputPortTask.setData(resultDataThing);
		    // If the data store is configured then
		    // push the datathing into it
		    if (ProcessorTask.STORE != null) {
			try {
			    // Avoid duplicates
			    if (alreadyStoredThings.contains(outputPortTask.getData())==false) {
				STORE.storeDataThing(outputPortTask.getData(),true);
				alreadyStoredThings.add(outputPortTask.getData());
			    }
			}
			catch (DuplicateLSIDException dple) {
			    //
			}
			catch (Exception e) {
			    System.out.println("Exception thrown while trying to store a datathing,\n"+
					       "disabling further stores.");
			    e.printStackTrace();
			    STORE = null;
			}
		    }
		}
		else {
		    // Datathing was null, so the processor didn't produce an output that it
		    // declared it did in its operational contract. This is an error condition
		    // and should be reported as such to the user
		    eventList.add(new ServiceError(new RuntimeException("Output '"+portName+"' was declared but never created!")));
		    throw new TaskExecutionException("Output port '"+portName+"' not populated by service instance");
		}
	    }
	}
	eventList.add(new ProcessComplete());
    }


    /**
     * Method that actually undertakes a service action. Should be implemented by concrete processors.
     * @return output map containing String->DataThing named pairs, with the key
     * being the name of the output port. This implementation also includes the logic
     * to remap the port names in the event of an alternate processor being used with
     * differing names for the inputs and outputs.
     */
    private synchronized Map invokeOnce(Map inputMap) throws TaskExecutionException {
	ProcessorTaskWorker worker = ProcessorHelper.getTaskWorker(activeProcessor);
	Map output = null;
	
	if (activeInputMapping == null) {
	    // If no input mapping is defined then we just invoke directly
	    output = doInvocationWithRetryLogic(worker, inputMap);	
	}
	else {
	    // Otherwise have to remap the input map
	    Map taskInput = new HashMap();
	    for (Iterator i = inputMap.keySet().iterator(); i.hasNext();) {
		String originalInputName = (String)i.next();
		DataThing inputItem = (DataThing)inputMap.get(originalInputName);
		String targetInputName = (String)activeInputMapping.get(originalInputName);
		if (targetInputName == null) {
		    targetInputName = originalInputName;
		}
		System.out.println("Mapping input name '"+originalInputName+"' to processor port '"+targetInputName+"'");
		taskInput.put(targetInputName, inputItem);
	    }
	    output = doInvocationWithRetryLogic(worker, taskInput);
	}
	
	// Now do the same for the output mapping
	if (activeOutputMapping == null) {
	    System.out.println("No mapping, returning output straight");
	    return output;
	}
	else {
	    Map taskOutput = new HashMap();
	    for (Iterator i = output.keySet().iterator(); i.hasNext();) {
		String realOutputName = (String)i.next();
		String targetOutputName = (String)activeOutputMapping.get(realOutputName);
		if (targetOutputName == null) {
		    targetOutputName = realOutputName;
		}
		System.out.println("Storing result from '"+realOutputName+"' as '"+targetOutputName+"'");
		DataThing outputItem = (DataThing)output.get(realOutputName);
		taskOutput.put(targetOutputName, outputItem);
	    }
	    return taskOutput;
	}
    }

    /**
     * Given a map of DataThing objects, call the fillLSIDValues()
     * method on each object within the map.
     */
    private void fillAllLSIDs(Map inputMap) {
	for (Iterator i = inputMap.values().iterator(); i.hasNext();) {
	    ((DataThing)i.next()).fillLSIDValues();
	}
    }
    
    /**
     * Actually call the service instance, handles the retry logic.
     */
    private Map doInvocationWithRetryLogic(ProcessorTaskWorker worker, Map inputMap) throws TaskExecutionException {
	try {
	    // If the first invocation works then great, return it.
	    return runAndGenerateTemplates(worker, inputMap);
	}
	catch (TaskExecutionException tee) {
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
		int waitTime = (int)((double)baseTimeout * Math.pow(backoff,(double)i));
		try {
		    eventList.add(new WaitingToRetry(waitTime, i+1, maxRetries));
		    Thread.sleep(waitTime);
		    return runAndGenerateTemplates(worker, inputMap);
		}
		catch (InterruptedException ie) {
		    TaskExecutionException t = new TaskExecutionException("Interrupted during wait to retry");
		    t.initCause(ie);
		    eventList.add(new ServiceError(t));
		    throw t;
		}
		catch (TaskExecutionException t) {
		    eventList.add(new ServiceError(t));
		}
	    }
	}
	throw new TaskExecutionException("No retries remaining");
    }

    /**
     * Call the service worker, populate LSID and generate any available templates
     */
    private Map runAndGenerateTemplates(ProcessorTaskWorker worker, Map inputMap) 
	throws TaskExecutionException {
	// Populate all LSIDs in the output map
	Map outputMap = worker.execute(inputMap);
	fillAllLSIDs(outputMap);
	AnnotationTemplate[] templates = activeProcessor.getAnnotationTemplates();	    
	AnnotationTemplate[] defaultTemplates = activeProcessor.defaultAnnotationTemplates();
	if (templates.length > 0 || defaultTemplates.length > 0) {
	    // For the inputs and outputs get the LSIDs of all the
	    // data objects, and apply these to the processor's
	    // annotation templates.
	    Map templateInputs = new HashMap();
	    Map templateOutputs = new HashMap();
	    for (Iterator i = inputMap.keySet().iterator(); i.hasNext();) { 
		String inputName = (String)i.next();
		DataThing inputValue = (DataThing)inputMap.get(inputName);
		String objectLSID = inputValue.getLSID(inputValue.getDataObject());
		templateInputs.put(inputName, objectLSID);
	    }
	    for (Iterator i = outputMap.keySet().iterator(); i.hasNext();) {
		String outputName = (String)i.next();
		DataThing outputValue = (DataThing)outputMap.get(outputName);
		String objectLSID = outputValue.getLSID(outputValue.getDataObject());
		templateOutputs.put(outputName, objectLSID);
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
	for (int i = 0; i < getParents().length; i++) {
	    if (getParents()[i] instanceof PortTask) {
		// For each input port task check the types
		PortTask inputPortTask = (PortTask)getParents()[i];
		String portName = inputPortTask.getScuflPort().getName();
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
	// Build the iterator
	eventList.add(new ConstructingIterator());
	String[] inputNames = new String[inputMap.keySet().size()];
	BaclavaIterator[] inputIterators = new BaclavaIterator[inputNames.length];
	int j=0;
	for (int i=0; i<getParents().length; i++) {
	    if (getParents()[i] instanceof PortTask) {
		PortTask inputPortTask = (PortTask)getParents()[i];
		inputNames[j] = inputPortTask.getScuflPort().getName();
		DataThing dataThing = inputPortTask.getData();
		try {
		    inputIterators[j] = dataThing.iterator(inputPortTask.getScuflPort().getSyntacticType());
		}
		catch (IntrospectionException ie) {
		    eventList.add(new DataMismatchError());
		    throw new TaskExecutionException("Unable to reconcile iterator types");
		}
		j++;
	    }
	}

	// Create the output container
	Map outputMap = new HashMap();
	for (int i = 0; i < getChildren().length; i++) {
	    if (getChildren()[i] instanceof PortTask) {
		PortTask outputPortTask = (PortTask)getChildren()[i];
		// Create data things with array lists inside them.
		outputMap.put(outputPortTask.getScuflPort().getName(), new DataThing(new ArrayList()));
	    }
	}

	// Build the join iterator
	JoinIterator i = new JoinIterator(inputIterators);
	int totalIterations = i.size();
	int currentIteration = 0;
		
	// Do the iteration
	for (; i.hasNext(); ) {
	    currentIteration++;
	    Object[] inputs = (Object[])i.next();
	    Map splitInput = new HashMap();
	    for (int k = 0; k < inputs.length; k++) {
		splitInput.put(inputNames[k], inputs[k]);
	    }
	    // Have now populated the input map for the service invocation
	    eventList.add(new InvokingWithIteration(currentIteration, totalIterations));
	    Map singleResultMap = execute(splitInput);
	    
	    // Iterate over the outputs
	    for (Iterator l = singleResultMap.keySet().iterator(); l.hasNext(); ) {
		String outputName = (String)l.next();
		DataThing outputValue = (DataThing)singleResultMap.get(outputName);
		Object dataObject = outputValue.getDataObject();
		// addition of a fix here by Chris Wroe
		// Before it tried to map all results from the service call into the subsequent
		// data flow causing a null pointer exception if no such data flow existed.
		if (outputMap.containsKey(outputName)) {
		    List targetList = ((List)((DataThing)outputMap.get(outputName)).getDataObject());
		    targetList.add(dataObject);
		}
		/// fix ends
	    }
	}
	
	// Return the output map
	return outputMap;
    }


    
    //protected static final String PROVENANCE_NAMESPACE = "http://www.it-innovation.soton.ac.uk/taverna/workflow/enactor/provenance";
    protected static final Namespace PROVENANCE_NAMESPACE = TavernaFlowReceipt.provNS;
    protected Processor proc = null;
    protected LogLevel logLevel = null;	
    private Logger logger = Logger.getLogger(ProcessorTask.class);
    private String userID;
    private String userCtx;
    private List eventList;

    /**
     * Default Constructor
     * @param id
     */
    public ProcessorTask(String id,Processor p,LogLevel l,String userID, String userCtx) {
        super(id);
	proc = p;
	this.logLevel = new LogLevel(l.getLevel());
	this.userID = userID;
	this.userCtx = userCtx;
	this.eventList = new ArrayList();
	// Add an event to the list for the scheduling operation
	schedule(p);
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
	return (ProcessEvent[])this.eventList.toArray(new ProcessEvent[0]);
    }

    /**
     * Retrieve the user context for the parent workflow
     */
    protected String getUserNamespaceContext() {
	return userCtx;
    }
    
    /**
     * Undertakes any special cancel processing required by Processor tasks
     */
    public void cancelConcreteTask() {
	//no additional processing is undertaken, any running invocations are allowed to
        //complete normally, any scheduled tasks are cancelled normally in core framework
    }  
    
    /**
     * Retrieves the XScufl Processor object for this task
     * @return the Processor object that this task is an execution shim for
     */
    public Processor getProcessor() {
	return proc;
    }
    
    public ServiceSelectionCriteria getServiceSelectionCriteria() {
	return null;
    }
    
    /**
     * Retrieve provenance information for this task, concrete tasks should
     * overide this method and provide this information as an XML JDOM element
     */
    public Element getProvenance() {
	return new Element("NotImplementedHere",PROVENANCE_NAMESPACE);
    }
    
    private Exception faultCausingException = null;
    /**
     * If the processor invocation throws an exception causing it
     * to fail, this class will populate an XML element with as much
     * information as possible about the exception that was thrown.
     */
    public Element getFaultElement() {
	if (faultCausingException == null) {
	    return null;
	}
	Element faultElement = new Element("failureDescription",PROVENANCE_NAMESPACE);
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
    
    public void cleanUpConcreteTask() {
	//
    }

    /**
     * Method that actually undertakes a service action. Should be implemented by concrete processors.
     * @return output map containing String->DataThing named pairs, with the key
     * being the name of the output port.
     */
    private Map execute(Map inputMap) throws TaskExecutionException {
	ProcessorTaskWorker worker = ProcessorHelper.getTaskWorker(proc);
	return worker.execute(inputMap);
    }

}
