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
//                              $Date: 2003-09-30 17:11:18 $
//                              $Revision: 1.19 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.broker;

import org.apache.log4j.Logger;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;
import java.util.*;

import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.factory.*;



/**
 * Represents a receipt for a workflow submitted by the client
 */
public class TavernaFlowReceipt extends WSFlowReceipt {
    
    private static final String REPORT_NAMESPACE = "http://www.it-innovation.soton.ac.uk/taverna/workflow/enactor/progress";
    private static final String PROVENANCE_NAMESPACE = "http://www.it-innovation.soton.ac.uk/taverna/workflow/enactor/provenance";
    
    private Logger logger = Logger.getLogger(getClass());
    private String userID = null;
    private LogLevel modelLogLevel;
    private String flowDefnString;
    private Map input;
    
    /**
     * Constructor for this concrete instance of a flow receipt
     * @param flow - the flow to which this receipt applies.
     * @param flowDenString - the original XScufl file that defined the workflow
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
	this.userID = userID;
	this.modelLogLevel = modelLogLevel;
	this.flowDefnString = flowDefnString;
	this.input = input;
    }
    
    /**
     * Process an event associated with a Flow
     *
     * @param FlowEvent describing the event.
     */
    public void processFlowEvent(FlowEvent flowEvent) {        
	try {
	    //construct the task summary
            Task[] tasks = flow.getTasks();
	    
            List processorSummaries = new ArrayList();
	    
            //For map the tasks are stacked which is the wrong way round, so correct ;)
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i] instanceof ProcessorTask)
                    processorSummaries.add("Processor {" + ((ProcessorTask) tasks[i]).getProcessor().getName() + "} has status {" + tasks[i].getStateString() + "}");
            }
            Iterator iterator = processorSummaries.iterator();
            String[] taskSummary = new String[processorSummaries.size()];
            int count = 0;
	    
            while (iterator.hasNext()) {
                taskSummary[count] = (String) iterator.next();
                count++;
            }
            String message = "Workflow active";
            boolean report = false;
	    if (flow.getStatus() == FlowStates.FAILED) {
                //find a failed task, should only be one, but otherwise deal with one error at a time.
                message = "Unknown error.";
		for (int i = 0; i < tasks.length; i++) {
                    if (tasks[i].getCurrentState() == TaskState.FAILED)
                        message = "Reason for failure: " + tasks[i].getClientMessage();
                }
                report = true;
            }
            if (flow.getStatus() == FlowStates.COMPLETE) {
                message = "Dataflow completed successfully";
		report = true;
            }
            if (report) {
                //not interested in other events here
                if (callbacks != null && !callbacks.isEmpty()) {
                    iterator = callbacks.iterator();
                    while (iterator.hasNext()) {
                        FlowCallback cb = (FlowCallback) iterator.next();
			
                        cb.notify(new FlowMessage(getID(), flow.getStatus(), taskSummary, message));
                    }
                }
            }
	}
	catch (Exception ex){
	    //log the exception
	    //ex.printStackTrace();
	    logger.error(ex);
	}
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
    
    public String getProgressReportXMLString() {
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
	return "No...";
    }
	    

    public org.jdom.Element getProvenanceXML() {
	//get the end provenance task or return empty string
	Element prov = new Element("workflowProvenance",PROVENANCE_NAMESPACE);
	
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

}
