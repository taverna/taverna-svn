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
//                              $Date: 2003-04-25 14:57:08 $
//                              $Revision: 1.4 $
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
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.taskstate.TaskState;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.FlowEvent;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Output;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;

// Utility Imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.lang.Exception;
import java.lang.String;
import java.lang.StringBuffer;



/**
 * Represents a receipt for a workflow submitted by the client
 */
public class TavernaFlowReceipt extends WSFlowReceipt {
    
    private Logger logger = Logger.getLogger(getClass());

    /**
     * Constructor for this concrete instance of a flow receipt
     * @ Flow to which this receipt applies.
     */
    public TavernaFlowReceipt(Flow flow) throws  WorkflowSubmitInvalidException  {
        super(flow);        
    }

    /**
     * Process an event associated with a Flow
     *
     * @param FlowEvent describing the event.
     */
    public void processFlowEvent(FlowEvent flowEvent) {        
		try	{
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
            logger.error(ex);
		}
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

	public String getOutputString() {
		Output output = new Output();
		try	{
		
			//get the output nodes and generate single output composed of all node parts
			
			GraphNode[] outNodes = flow.getDiGraph().getOutputNodes();
			
			int count = 0;
			for(int i=0;i<outNodes.length;i++) {
				PortTask pT = (PortTask) outNodes[i];
				if(!pT.dataAvailable()) {
					output.addPart(new Part(count + 1,"UNKNOWN","UNKNOWN","NO_DATA"));
				}
				else {
						output.addPart(pT.getData());
				}
				
			
			}
			return output.toXML();
		}
		catch(Exception ex) {
			return output.toXML();
		}
	}    

}