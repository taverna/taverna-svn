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
//                              $Revision: 1.31 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.broker;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.provenance.process.ProcessEvent;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.broker.WSFlowReceipt;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.broker.WorkflowSubmitInvalidException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowCallback;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowMessage;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Flow;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.FlowStates;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Task;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.TimePoint;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.taskstate.TaskState;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.FlowEvent;
import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;

// Utility Imports
import java.util.*;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.CDATA;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;

import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import java.lang.Exception;
import java.lang.String;
import java.lang.StringBuffer;



/**
 * Represents a receipt for a workflow submitted by the client<p>
 *
 * This receipt is used as the handle for all client operations on a
 * running or scheduled workflow instance. It includes methods to extract
 * status and result documents and is augemented by Taverna to also include
 * methods to return the inputs and outputs to a specific processor for
 * debug purpopses during the construction of a workflow.
 * 
 * @author Darren Marvin
 * @author Tom Oinn
 */
public class TavernaFlowReceipt extends WSFlowReceipt implements org.embl.ebi.escience.scufl.enactor.WorkflowInstance {
    
    private static final String REPORT_NAMESPACE = "http://www.it-innovation.soton.ac.uk/taverna/workflow/enactor/progress";
    
    public static Namespace provNS = Namespace.getNamespace("p","http://org.embl.ebi.escience/xscuflprovenance/0.1alpha");
    
    private Logger logger = Logger.getLogger(getClass());
    private String userID = null;
    private LogLevel modelLogLevel;
    private String flowDefnString;
    private Map input;
    private Flow flow;
    
    /**
     * Constructor for this concrete instance of a flow receipt
     * @param flow - the flow to which this receipt applies.
     * @param flowDefnString - the original XScufl file that defined the workflow
     * @param input - a map of String->DataThing objects defining the workflow input
     * @param userID - string representing the user
     * @param modelLogLevel - the logging level for the scufl model
     * @exception WorkflowSubmitInvalidException thrown by the superclass
     */
    public TavernaFlowReceipt(Flow flow,
			      String flowDefnString,
			      Map input,
			      String userID,
			      LogLevel modelLogLevel) 
	throws  WorkflowSubmitInvalidException  {
        super(flow);
	this.flow = flow;
	this.userID = userID;
	this.modelLogLevel = modelLogLevel;
	this.flowDefnString = flowDefnString;
	this.input = input;
    }
    
    /**
     * Process an event associated with a Flow
     *
     * @param flowEvent FlowEvent describing the event.
     */
    public void processFlowEvent(FlowEvent flowEvent) {        
	try {
	    //construct the task summary
            Task[] tasks = flow.getTasks();
	    
            List processorSummaries = new ArrayList();
	    
            //For map the tasks are stacked which is the wrong way round, so correct ;)
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i] instanceof ProcessorTask) {
                    processorSummaries.add("Processor {" + ((ProcessorTask) tasks[i]).getProcessor().getName() + "} has status {" + tasks[i].getStateString() + "}");
		}
	    }
	    String[] taskSummary = (String[])processorSummaries.toArray(new String[0]);

            String message = "Workflow active.";
	    if (flow.getStatus() == FlowStates.FAILED) {
                //find a failed task, should only be one, but otherwise deal with one error at a time.
                message = "Unknown error.";
		for (int i = 0; i < tasks.length; i++) {
                    if (tasks[i].getCurrentState() == TaskState.FAILED) {
                        message = "Reason for failure: " + tasks[i].getClientMessage();
		    }
		}
	    }
            else if (flow.getStatus() == FlowStates.COMPLETE) {
                message = "Workflow completed successfully.";
	    }
	    else {
		// Nothing interesting happened
		return;
	    }
	    
	    //not interested in other events here
	    if (callbacks != null) {
		for (Iterator i = callbacks.iterator(); i.hasNext();) {
		    FlowCallback cb = (FlowCallback)i.next();
		    cb.notify(new FlowMessage(getID(), flow.getStatus(), taskSummary, message));
		}
	    }
	    
	}
	catch (Exception ex){
	    //log the exception
	    //ex.printStackTrace();
	    logger.error(ex);
	}
    } 
    
    /**
     * Returns two Map objects of port name -> dataThing. The input document
     * is at position 0, the output at position 1 and the result array has
     * exactly two slots, the documents are in the format defined by the
     * Baclava package and used elsewhere - this allows reuse of the display
     * code from the main workbench.
     * @exception UnknownProcessorException if a ProcessorTask with the supplied
     * name cannot be found within the DiGraph that this FlowReceipt is associated
     * with.
     */
    public Map[] getIntermediateResultsForProcessor(String processorName) 
	throws UnknownProcessorException {
	// Create a new array, just used to return a pair of strings,
	// have to do this due to Java's inadequate type system.
	Map[] results = new Map[2];
	// Locate a ProcessorTask with the appropriate name
	Task[] tasks = flow.getTasks();
	ProcessorTask targetTask = null;
	for (int i = 0; i < tasks.length && targetTask == null; i++) {
	    if (tasks[i] instanceof ProcessorTask) {
		Processor p = ((ProcessorTask)tasks[i]).getProcessor();
		if (p.getName().equalsIgnoreCase(processorName)) {
		    targetTask = (ProcessorTask)tasks[i];
		}
	    }
	}
	if (targetTask == null) {
	    throw new UnknownProcessorException("Unable to find a task with name '"+processorName+"' within the current workflow instance.");
	}

	// Configure an XML Outputter to 'print' the documents to the output
	// string array later.
	XMLOutputter xo = new XMLOutputter();
	xo.setIndent(" ");
	xo.setNewlines(true);
	xo.setTextNormalize(false);

	// If we reach this point then there is a processor task with the appropriate
	// name, so find all children (which should be PortTask entities, and, for all
	// those that have data available - which could be none - add their data to the
	// map for inputs or outputs respectively.
	GraphNode[] inputs = targetTask.getParents();
	Map inputMap = new HashMap();
	for (int i = 0; i < inputs.length; i++) {
	    if (inputs[i] instanceof PortTask) {
		String portName = ((PortTask)inputs[i]).getScuflPort().getName();
		if (((PortTask)inputs[i]).dataAvailable()) {
		    inputMap.put(portName, ((PortTask)inputs[i]).getData());
		}
	    }
	}
	results[0] = inputMap;
	// Repeat for the outputs
	GraphNode[] outputs = targetTask.getChildren();
	Map outputMap = new HashMap();
	for (int i = 0; i < outputs.length; i++) {
	    if (outputs[i] instanceof PortTask) {
		String portName = ((PortTask)outputs[i]).getScuflPort().getName();
		if (((PortTask)outputs[i]).dataAvailable()) {
		    outputMap.put(portName, ((PortTask)outputs[i]).getData());
		}
	    }
	}
	results[1] = outputMap;
	return results;
    }

    public void releaseConcrete() {
	flowDefnString = null;
	input = null;
    }
    
    public String getStatusString() {
	int status = flow.getStatus();
        String msg = "UNKNOWN";
	switch (status) {
        case 0:
            msg = "NEW";
            break;
	case 1:
            msg = "SCHEDULED";
            break;
	case 2:
            msg = "RUNNING";
            break;
        case 3:
            msg = "COMPLETE";
            break;
        case 4:
            msg = "FAILED";
            break;
        case 5:
            msg = "CANCELLED";
        }
        return msg;
    }

    public String getErrorMessage() {
	StringBuffer buf = new StringBuffer("");
	Task[] nodes = flow.getTasks();
	for(int i=0;i<nodes.length;i++) {
	    if(nodes[i].getCurrentState()==TaskState.FAILED) {
		buf.append("(");
		buf.append(nodes[i].getID());
		buf.append("[");
		buf.append(nodes[i].getClientMessage());
		buf.append("]");
		buf.append(")");
	    }
	}
	return buf.toString();
    }
    
    public String getStatusXMLString() {
        StringBuffer buf = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        String status = getStatusString();
	buf.append("<status>");
        buf.append(status + "</status>");
        return buf.toString();
    }
    
    /**
     * Updated progress report code to use the event lists
     */
    public String getProgressReportXMLString() {
	try {
	    Element report = new Element("workflowReport");
	    report.setAttribute("workflowID",flow.getID());
	    report.setAttribute("workflowStatus",translateFlowState(flow.getStatus()));	
	    Document doc = new Document(report);
	    Element processorListElement = new Element("processorList");
	    report.addContent(processorListElement);
	    // Iterate over all tasks, filter for processor tasks and
	    // get their event lists, using the methods in ProcessEvent
	    // to create XML fragments and add them to the report
	    Task[] tasks = flow.getTasks();
	    for (int i = 0; i < tasks.length; i++) {
		if (tasks[i] instanceof ProcessorTask) {
		    ProcessorTask theTask = (ProcessorTask)tasks[i];
		    Element processorElement = new Element("processor");
		    processorListElement.addContent(processorElement);
		    processorElement.setAttribute("name",theTask.getProcessor().getName());
		    ProcessEvent[] eventList = theTask.getEventList();
		    // Add all events into the log
		    for (int j = eventList.length-1; j>=0; j--) {
			processorElement.addContent(eventList[j].eventElement());
		    }		    
		}
	    }
	    XMLOutputter xmlout = new XMLOutputter();
	    xmlout.setIndent(" ");
	    xmlout.setNewlines(true);
	    xmlout.setTextNormalize(false);
	    return (xmlout.outputString(doc));
	}
	catch (Exception ex) {
	    logger.error("Unable to create progress report for workflow : "+ flow.getID(),ex);
	}
	return "";
    }

    /**
     * The old version of the progress report code
     */
    public String getProgressReportXMLString_old() {
	try {
	    Element report = new Element("workflowReport",REPORT_NAMESPACE);
	    Document doc = new Document(report);
	    Element activityReport = new Element("processorList",REPORT_NAMESPACE);
	    //set the flow id
	    report.setAttribute("workflowID",flow.getID());
	    String statString = translateFlowState(flow.getStatus());
	    report.setAttribute("workflowStatus",statString);			
	    report.addContent(activityReport);
	    //get the activity list
	    Task[] tasks = flow.getTasks();
	    for(int i=0;i<tasks.length;i++) {
		if(tasks[i] instanceof ProcessorTask) {
		    ProcessorTask task = (ProcessorTask) tasks[i];
		    Element a = new Element("processor",REPORT_NAMESPACE);
		    a.setAttribute("name",task.getProcessor().getName());
		    a.setAttribute("status",translateStateString(task.getStateString()));
		    TimePoint startTp = task.getStartTime();
		    if(startTp!=null) {
			a.setAttribute("startTime",startTp.getShortString());
		    }
		    TimePoint endTp = task.getEndTime();
		    if(endTp!=null) {
			a.setAttribute("endTime",endTp.getShortString());
		    }
		    String clientMessage = task.getClientMessage();
		    if(clientMessage!=null) {
			Element msg = new Element("executionMessage",REPORT_NAMESPACE);
			msg.addContent(new Text(clientMessage));
			a.addContent(msg);
		    }
		    activityReport.addContent(a);
		}	
	    }
	    XMLOutputter xmlout = new XMLOutputter();
	    xmlout.setIndent(" ");
	    xmlout.setNewlines(true);
	    xmlout.setTextNormalize(false);
	    return (xmlout.outputString(doc));
	}
	catch(Exception ex) {
	    logger.error("Unable to create progress report for workflow : "+ flow.getID(),ex);
	    //generate best attempt at xml
	    StringBuffer buf = new StringBuffer("<workflowReport workflowID=\"");
	    buf.append(flow.getID());
	    buf.append("\" workflowStatus=\"");
	    buf.append("UNKNOWN\"");
	    buf.append("><ProcessorList/></workflowReport>");
	    return buf.toString();
	}
    }
    
    public String getOutputXMLString() {
	return getOutputString();
    }

    public String getOutputString() {
	Document doc = DataThingXMLFactory.getDataDocument(getOutput());
	XMLOutputter xo = new XMLOutputter();
	xo.setIndent("  ");
	xo.setNewlines(true);
	return xo.outputString(doc);
    }
    
    public Map getOutput() {
	Map results = new HashMap();
	// Iterate over all output port tasks
	// and collect data from them if they
	// have anything to show
	GraphNode[] outputNodes = flow.getDiGraph().getOutputNodes();
	for (int i = 0; i < outputNodes.length; i++) {
	    if (outputNodes[i] instanceof PortTask) {
		PortTask thePortTask = (PortTask)outputNodes[i];
		String portName = thePortTask.getScuflPort().getName();
		DataThing theDataThing = thePortTask.getData();
		if (theDataThing != null) {
		    results.put(portName, theDataThing);
		}
	    }
	}
	return results;
    }
    
    public String getProvenanceXMLString() {
	XMLOutputter xo = new XMLOutputter();
	xo.setIndent(" ");
	xo.setNewlines(true);
	xo.setTextNormalize(false);
	return xo.outputString(getProvenanceXML());
    }
    
    
    public Element getProvenanceXML() {
	Element prov = new Element("dataProvenance", provNS);
	Task[] tasks = flow.getTasks();
	for (int i = 0; i < tasks.length; i++) {
	    if (tasks[i] instanceof ProcessorTask) {
		for (Iterator j = ((ProcessorTask)tasks[i]).getProvenanceList().iterator(); j.hasNext();) {
		    String provenanceItem = (String)j.next();
		    System.out.println(provenanceItem);
		    Element item = new Element("item", provNS);
		    item.setText(provenanceItem);
		    prov.addContent(item);
		}
	    }
	}
	return prov;
    }
    
    private String translateStateString(String s) {
	String newString = null;
	if(s.equals("CANCELLED"))
	    newString = "ABORTED";
	else
	    newString = s;
	return newString;
    }
    
    private String translateFlowState(int status) {
	String msg = "UNKNOWN";
	switch (status) {
        case 0:
            msg = "NEW";
            break;
	    
        case 1:
            msg = "SCHEDULED";
            break;
	    
        case 2:
            msg = "RUNNING";
            break;
	    
        case 3:
            msg = "COMPLETE";
            break;
	    
        case 4:
            msg = "FAILED";
            break;
	case 5:
            msg = "CANCELLED";
        }
        return msg;
    }

    public boolean pauseExecution() {
	return true;
    }
    
    public boolean resumeExecution() {
	return true;
    }

    public boolean isPaused() {
	return false;
    }
    
    public void cancel() {
	//
    }
    
    public void setInputs(Map inputMap) {
	//
    }

}
