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
//                              $Date: 2003-11-14 14:11:29 $
//                              $Revision: 1.25 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.TimePoint;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.serviceprovidermanager.ServiceSelectionCriteria;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;

import org.embl.ebi.escience.baclava.*;
import java.beans.*;

// Utility Imports
import java.util.HashMap;
import java.util.Map;
import java.util.*;

// IO Imports
import java.io.PrintWriter;
import java.io.StringWriter;

// JDOM Imports
import org.jdom.Element;
import org.jdom.Namespace;




/**
 * The superclass of all actual task implementations
 */
public abstract class ProcessorTask extends TavernaTask{
    
    //protected static final String PROVENANCE_NAMESPACE = "http://www.it-innovation.soton.ac.uk/taverna/workflow/enactor/provenance";
    protected static final Namespace PROVENANCE_NAMESPACE = TavernaFlowReceipt.provNS;
    protected Processor proc = null;
    protected LogLevel logLevel = null;	
    private Logger logger = Logger.getLogger(ProcessorTask.class);
    private String userID;
    private String userCtx;
    
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
    }
    
    /**
     * Retrieve the user identifier for the parent workflow
     */
    protected String getUserID() {
	return userID;
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
     * @return 
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
    public abstract Element getProvenance();
    
    /**
     * Wrapper method to enable pre and post processing for actual service invocations
     */
    public TaskStateMessage doTask() {
	try {
	    TaskStateMessage result;
	    startTime =  new TimePoint();
	    //do any pre-processing here
	    
	    // The input map, contains String->DataThing
	    // where the string keys are the input port
	    // names of the processor
	    Map inputMap = new HashMap();
	    // Get all the inputs, which are all PortTasks
	    boolean implicitIteration = false;
	    int numberOfParameters = 0;
	    for (int i = 0; i < getParents().length; i++) {
		if (getParents()[i] instanceof PortTask) {
		    numberOfParameters++;
		    PortTask inputPortTask = (PortTask)getParents()[i];
		    String portName = inputPortTask.getScuflPort().getName();
		    DataThing dataThing = inputPortTask.getData();
		    
		    String dataType = dataThing.getSyntacticType().split("\\'")[0];
		    String portType = inputPortTask.getScuflPort().getSyntacticType().split("\\'")[0];
		    if (dataType.equals(portType) == false) {
			implicitIteration = true;
			System.out.println("Found a type mismatch, will need the implicit iteration.");
		    }	    
		    
		    inputMap.put(portName, dataThing);
		}
	    }
	    
	    Map outputMap = null;
	    
	    if (!implicitIteration) {
		// No implicit iteration required.
		outputMap = execute(inputMap);
	    }
	    else {
		// Do the iteration here. First get the iterators for each
		// input datathing
		String[] inputNames = new String[numberOfParameters];
		BaclavaIterator[] inputIterators = new BaclavaIterator[numberOfParameters];
		int j = 0;
		for (int i = 0; i < getParents().length; i++) {
		    if (getParents()[i] instanceof PortTask) {
			PortTask inputPortTask = (PortTask)getParents()[i];
			inputNames[j] = inputPortTask.getScuflPort().getName();
			DataThing dataThing = inputPortTask.getData();
			try {
			    inputIterators[j] = dataThing.iterator(inputPortTask.getScuflPort().getSyntacticType());
			}
			catch (IntrospectionException ie) {
			    throw new TaskExecutionException("Unable to reconcile iterator types");
			}
			j++;
		    }
		}
		// Create a new DataThing object for each output
		outputMap = new HashMap();
		for (int i = 0; i < getChildren().length; i++) {
		    if (getChildren()[i] instanceof PortTask) {
			PortTask outputPortTask = (PortTask)getChildren()[i];
			outputMap.put(outputPortTask.getScuflPort().getName(), new DataThing(new ArrayList()));
		    }
		}
		// Now have an array of iterators covering all the inputs needed, need to repeatedly invoke
		// the task on each possible combination
		for (Iterator i = new JoinIterator(inputIterators); i.hasNext(); ) {
		    Object[] inputs = (Object[])i.next();
		    Map splitInput = new HashMap();
		    for (int k = 0; k < inputs.length; k++) {
			splitInput.put(inputNames[k], inputs[k]);
		    }
		    // Have now populated the input map for the service invocation
		    
		    Map singleResultMap = execute(splitInput);
		    
		    // Iterate over the outputs
		    for (Iterator l = singleResultMap.keySet().iterator(); l.hasNext(); ) {
			String outputName = (String)l.next();
			DataThing outputValue = (DataThing)singleResultMap.get(outputName);
			Object dataObject = outputValue.getDataObject();
			List targetList = ((List)((DataThing)outputMap.get(outputName)).getDataObject());
			targetList.add(dataObject);
		    }
		}
		

	    }

	    // Iterate over the children, pushing data into the port tasks
	    // as appropriate.
	    for (int i = 0; i < getChildren().length; i++) {
		if (getChildren()[i] instanceof PortTask) {
		    PortTask outputPortTask = (PortTask)getChildren()[i];
		    String portName = outputPortTask.getScuflPort().getName();
		    DataThing resultDataThing = (DataThing)outputMap.get(portName);
		    if (resultDataThing != null) {
			outputPortTask.setData(resultDataThing);
		    }
		}
	    }
	    
	    endTime = new TimePoint();
	    
	    return new TaskStateMessage(getParentFlow().getID(), 
					getID(), 
					TaskStateMessage.COMPLETE,"Task completed successfully");
	}
	catch (TaskExecutionException ex) {
	    endTime = new TimePoint();
	    faultCausingException = ex;
	    logger.error(ex);
	    return new TaskStateMessage(getParentFlow().getID(),getID(), TaskStateMessage.FAILED,ex.getMessage());
	}
	catch (Exception ex){
	    endTime = new TimePoint();
	    faultCausingException = ex;
	    logger.error(ex);
	    return new TaskStateMessage(getParentFlow().getID(), 
					getID(), 
					TaskStateMessage.FAILED, 
					"Unrecognised dispatch failure for a task within the workflow.");
	}	
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
    
    /**
     * Method that actually undertakes a service action. Should be implemented by concrete processors.
     * @return output map containing String->DataThing named pairs, with the key
     * being the name of the output port.
     */
    protected abstract Map execute(Map inputMap) throws TaskExecutionException;

}
