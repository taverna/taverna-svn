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
//      Created Date        :   2003/6/4
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2004-04-06 12:05:48 $
//                              $Revision: 1.5 $
//
///////////////////////////////////////////////////////////////////////////////////////

package org.embl.ebi.escience.scuflworkers.workflow;

import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.scufl.enactor.implementation.*;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBrokerFactory;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowCallback;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowMessage;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaBinaryWorkflowSubmission;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.lang.NullPointerException;
import java.lang.String;
import java.lang.Thread;



public class WorkflowTask implements ProcessorTaskWorker {

    private Processor proc;

    public WorkflowTask(Processor p) {
	this.proc = p;
    }
    
    /**
     * Invoke a nested workflow, the input map being a map of string port names
     * to DataThing objects containing the current values.
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
	WorkflowProcessor theProcessor = (WorkflowProcessor)proc;
	ScuflModel theNestedModel = theProcessor.getInternalModel();
	
	EnactorProxy theEnactor = new FreefluoEnactorProxy();
	WorkflowInstance flowReceipt = null;
	try {
	    flowReceipt = theEnactor.submitWorkflow(theNestedModel, inputMap);
	}
	catch (WorkflowSubmissionException wse) {
	    TaskExecutionException tee = new TaskExecutionException("Error submitting nested workflow");
	    tee.initCause(wse);
	    throw tee;
	}
	try {
	    String results;
	    boolean gotResults = false;
	    while (!gotResults) {
		results = flowReceipt.getOutputXMLString();
		if (results.equals("") == false) {
		    gotResults = true;
		} else {
		    Thread.sleep(1000);
		}
	    }
	} catch (InterruptedException ie) {
	    TaskExecutionException tee = new TaskExecutionException("Nested workflow failure");
	    tee.initCause(ie);
	    throw tee;
	}
	return flowReceipt.getOutput();
    }
        
}
