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
//      Last commit info    :   $Author: sowen70 $
//                              $Date: 2006-03-22 15:19:34 $
//                              $Revision: 1.11 $
//
///////////////////////////////////////////////////////////////////////////////////////

package org.embl.ebi.escience.scuflworkers.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowInstanceImpl;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowState;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class WorkflowTask implements ProcessorTaskWorker {
	private static EnactorProxy defaultEnactor = new FreefluoEnactorProxy();

	private static Logger logger = Logger.getLogger(WorkflowTask.class);	

	private WorkflowInstance workflowInstance = null;

	private Processor proc;	

	public WorkflowTask(Processor p) {
		this.proc = p;
	}

	public WorkflowInstance getWorkflowInstance() {
		return workflowInstance;
	}

	/**
	 * Invoke a nested workflow, the input map being a map of string port names
	 * to DataThing objects containing the current values.
	 */
	public Map execute(Map inputMap, ProcessorTask parentTask) throws TaskExecutionException {
		WorkflowProcessor theProcessor = (WorkflowProcessor) proc;
		ScuflModel theNestedModel = theProcessor.getInternalModel();

		// The inputMap is already in the form we need for a submission
		try {
			// Get the parent workflow instance
			WorkflowInstance parentInstance = parentTask.workflowInstance;
			UserContext context = parentInstance.getUserContext();
			workflowInstance = defaultEnactor.compileWorkflow(theNestedModel, inputMap, context);
		} catch (WorkflowSubmissionException e) {
			String msg = "Error executing workflow task.  Error compiling the nested workflow.";
			logger.error(msg, e);
			throw new TaskExecutionException(msg);
		}

		try {
			final Thread taskThread = Thread.currentThread();
			((WorkflowInstanceImpl) workflowInstance).addWorkflowStateListener(new WorkflowStateListener() {
				public void workflowStateChanged(WorkflowStateChangedEvent event) {
					WorkflowState state = event.getWorkflowState();
					if (state.isFinal()) {
						taskThread.interrupt();
					}
				}
			});
			workflowInstance.run();
		} catch (Exception e) {
			String msg = "Nested workflow failed in task, error message was: " + e.getMessage();
			logger.error(msg, e);
			throw new TaskExecutionException(msg);
		}

		try {
			while (true) {
				Thread.sleep(10000);
			}
		} catch (InterruptedException ie) {
			//
		}

		WorkflowState workflowState = WorkflowState.getState(workflowInstance.getStatus());

		// Did we finish okay?
		if (workflowState.equals(WorkflowState.COMPLETE)) {
			return workflowInstance.getOutput();
		} else if (workflowState.equals(WorkflowState.FAILED)) {
			throw new TaskExecutionException("Nested workflow failed in task, error message was : "
					+ workflowInstance.getErrorMessage());
		} else if (workflowState.equals(WorkflowState.CANCELLED)) {
			return new HashMap();
		} else {
			throw new TaskExecutionException("Unknown flow state in task, failing.");
		}
	}
}
