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
//      Last commit info    :   $Author: ferris $
//                              $Date: 2004-06-03 11:59:20 $
//                              $Revision: 1.2 $
//
///////////////////////////////////////////////////////////////////////////////////////
package org.embl.ebi.escience.scufl.enactor.implementation;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.provenance.process.ProcessEvent;
import org.embl.ebi.escience.scufl.enactor.*;
import uk.ac.soton.itinnovation.freefluo.main.Engine;
import org.embl.ebi.escience.scufl.UnknownProcessorException;

import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;



// Utility Imports
import java.util.*;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;

import uk.ac.soton.itinnovation.freefluo.task.LogLevel;
import uk.ac.soton.itinnovation.freefluo.main.Engine;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowState;
import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;
import uk.ac.soton.itinnovation.freefluo.main.UnknownWorkflowInstanceException;

import java.lang.Exception;
import java.lang.String;
import java.lang.StringBuffer;



/**
 * Represents a receipt for a workflow submitted by the client<p>
 *
 * This class is used as the handle for all client operations on a
 * running or scheduled workflow instance. It includes methods to extract
 * status and result documents and is augemented by Taverna to also include
 * methods to return the inputs and outputs to a specific processor for
 * debug purpopses during the construction of a workflow.
 * 
 * @author Darren Marvin
 * @author Tom Oinn
 * @author Justin Ferris
 */
public class WorkflowInstanceImpl implements WorkflowInstance {    
    private Logger logger = Logger.getLogger(getClass());
   
    private Engine engine;
    private String workflowInstanceId;
    private Map input;
    private WorkflowState state;
    private HashSet stateListeners = new HashSet();
    
    /**
     * Constructor for this concrete instance of a flow receipt
     * @param engine - the enactment engine to use.
     * @param workflowInstanceId - the unique Id for the workflow instance
     * @exception WorkflowSubmitInvalidException thrown by the superclass
     */
    public WorkflowInstanceImpl(Engine engine, String workflowInstanceId) {
	      this.engine = engine;
        this.workflowInstanceId = workflowInstanceId;
    }

    public void setInputs(Map inputMap) {
        this.input = inputMap;
    }

    public void run() throws InvalidInputException {
        String errorMsg = "Error starting to run workflow instance with id " + workflowInstanceId; 

        try {
            engine.run(workflowInstanceId, input);
        }
        catch(InvalidInputException e) {
            logger.error(errorMsg + ".  The inputs don't map to sources in the dataflow.");
            throw e;
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = errorMsg + ".  The workflow engine didn't recognise the workflow instance id.";
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }
    }
    
    public String getStatus() {
        try {
            return engine.getStatus(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error getting status for workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
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
	      try {
            return engine.getIntermediateResultsForProcessor(workflowInstanceId, processorName);
        
        }
        catch(uk.ac.soton.itinnovation.freefluo.main.UnknownProcessorException e) {
            String msg = "Error getting intermediate results for workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise in the workflow instance the processor with name " + processorName;
            logger.error(msg, e);
            throw new UnknownProcessorException(msg);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error getting intermediate results for workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }
    
    /**
     * Updated progress report code to use the event lists
     */
    public String getProgressReportXMLString() {
        try {
	          return engine.getProgressReportXML(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error getting progress report xml string for workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public String getOutputXMLString() {
        try {
	          return engine.getOutputXML(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error getting output report xml string for workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }
    
    public Map getOutput() {
        try {
	          return engine.getOutput(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error getting progress the output for workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public String getErrorMessage() {
        try {
            return engine.getErrorMessage(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error getting the error message for workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }
    
    public String getProvenanceXMLString() {
        try {
            return engine.getProvenanceXML(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error getting provenance xml string for workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }
 
    public boolean pauseExecution() {
        try {
	          return engine.pauseExecution(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error pausing workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }
    
    public boolean resumeExecution() {
        try {
            return engine.resumeExecution(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error resuming workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public boolean isPaused() {
        try {
	          return engine.isPaused(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error determining if the workflow is paused for workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
        
    }
    
    public void cancel() {
        try {
	          engine.cancel(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error cancelling workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    public void destroy() {
        try {    
            engine.destroy(workflowInstanceId);
        }
        catch(UnknownWorkflowInstanceException e) {
            String msg = "Error destroying workflow instance with id " + workflowInstanceId +
                    ".  The workflow engine didn't recognise the workflow isntance id";
            logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }
}
