/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.service;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.facade.WorkflowInstanceStatus;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.service.model.Job;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

/**
 *
 *
 * @author David Withers
 */
public class JobMonitorImpl implements JobMonitor {
	
	private Job job;

	private DataManager dataManager;

	private JobManager jobManager;
	
	private Authentication authentication;

	private int outputCount;

	private Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
	
	public JobMonitorImpl(Job job, DataManager dataManager, JobManager jobManager, Dataflow dataflow, Authentication authentication) {
		this.job = job;
		this.dataManager = dataManager;
		this.jobManager = jobManager;
		this.authentication = authentication;
		outputCount = dataflow.getOutputPorts().size();
		jobManager.setJobStatus(job, "INITIALISING");		
	}
	
	public synchronized void resultTokenProduced(WorkflowDataToken token, String portName) {
		if (token.isFinal()) {
			outputCount--;
			outputs.put(portName, token.getData());
			if (outputCount == 0) {
				if (job != null && "RUNNING".equals(job.getStatus())) {
					SecurityContextHolder.getContext().setAuthentication(authentication);
					job.setOutputs(dataManager.createData(outputs).getId());
					SecurityContextHolder.getContext().setAuthentication(null);
//					jobManager.setJobStatus(job, "COMPLETE");
				}
			}
		}
	}

	public void workflowCompleted(ProcessIdentifier owningProcess) {
	}

	public void workflowFailed(ProcessIdentifier failedProcess, InvocationContext context,
			NamedWorkflowEntity workflowEntity, String message, Throwable t) {
	}

	public void workflowStatusChanged(WorkflowInstanceStatus oldStatus, WorkflowInstanceStatus newStatus) {
		if (newStatus.equals(WorkflowInstanceStatus.RUNNING)) {
			jobManager.setJobStatus(job, "RUNNING");
		} else if(newStatus.equals(WorkflowInstanceStatus.COMPLETED)) {
			jobManager.setJobStatus(job, "COMPLETE");
		} else if(newStatus.equals(WorkflowInstanceStatus.FAILED)) {
			if ("CANCELLING".equals(job.getStatus())) {
				jobManager.setJobStatus(job, "CANCELLED");
			} else {
				jobManager.setJobStatus(job, "FAILED");
			}
		} else if(newStatus.equals(WorkflowInstanceStatus.PAUSED)) {
			jobManager.setJobStatus(job, "PAUSED");
		}
	}

}
