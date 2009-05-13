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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.StreamingOutput;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowInstanceStatus;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.platform.taverna.Enactor;
import net.sf.taverna.t2.platform.taverna.InvocationContextFactory;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.service.model.Job;
import net.sf.taverna.t2.service.store.JobDao;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.util.Assert;

public class JobManagerImpl implements JobManager, InitializingBean {

	private JobDao jobDao;

	private Enactor enactor;

	private WorkflowManager workflowManager;

	private DataManager dataManager;

	private AuthorizationManager authorizationManager;

	private InvocationContextFactory invocationContextFactory;

	private Map<Long, WorkflowInstanceFacade> workflowInstances = new HashMap<Long, WorkflowInstanceFacade>();

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jobDao, "jobDao required");
		Assert.notNull(enactor, "enactor required");
		Assert.notNull(workflowManager, "workflowManager required");
		Assert.notNull(dataManager, "dataManager required");
		Assert.notNull(invocationContextFactory,
				"invocationContextFactory required");
	}

	public void addJob(Job job) {
		jobDao.save(job);
		if (authorizationManager != null) {
			authorizationManager.createAclEntry(job);
		}
	}

	public void deleteJob(Long id) {
		Job job = jobDao.get(id);
		if (job != null) {
			String status = job.getStatus();
			if ("COMPLETE".equals(status) || "CANCELLED".equals(status)
					|| "FAILED".equals(status)) {
				jobDao.delete(id);
				if (authorizationManager != null) {
					authorizationManager.deleteAclEntry(Job.class, id);
				}
			} else {
				throw new JobStateException("Job running");
			}
		}
	}

	public Job getJob(Long id) {
		return jobDao.get(id);
	}

	public Collection<Job> getAllJobs() {
		return jobDao.getAll();
	}

	public Job createJob(Long workflow, Long inputs) {
		Job job = new Job();
		job.setInputs(inputs);
		job.setStatus("CREATED");
		job.setWorkflow(workflow);
		addJob(job);
		return job;
	}

	public StreamingOutput getOutput(Long jobID, String port) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean pauseJob(Long id) {
		Job job = jobDao.get(id);
		if (job != null && "RUNNING".equals(job.getStatus())) {
			WorkflowInstanceFacade instance = workflowInstances
					.get(job.getId());
			synchronized (instance) {
				if (instance.getStatus().equals(WorkflowInstanceStatus.RUNNING)) {
					instance.setPaused(true);
					return true;
				}
			}
		}
		return false;
	}

	public boolean resumeJob(Long id) {
		Job job = jobDao.get(id);
		if (job != null && "PAUSED".equals(job.getStatus())) {
			WorkflowInstanceFacade instance = workflowInstances
					.get(job.getId());
			synchronized (instance) {
				if (instance.getStatus().equals(WorkflowInstanceStatus.PAUSED)) {
					instance.setPaused(false);
					return true;
				}
			}
		}
		return false;
	}

	public boolean cancelJob(Long id) {
		Job job = jobDao.get(id);
		if (job != null && "RUNNING".equals(job.getStatus())) {
			WorkflowInstanceFacade instance = workflowInstances
					.get(job.getId());
			synchronized (instance) {
				if (instance.getStatus().equals(WorkflowInstanceStatus.RUNNING)) {
					setJobStatus(job, "CANCELLING");
					instance.cancel();
					return true;
				}
			}
		}
		return false;
	}

	public void runJob(Job job, boolean waitForResult) {
		Dataflow dataflow = workflowManager.getDataflow(job.getWorkflow());
		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();

		JobMonitor jobMonitor = new JobMonitorImpl(job, dataManager, this,
				dataflow, authentication);
		InvocationContext invocationContext = invocationContextFactory
				.createInvocationContext();
		invocationContext.addEntity(jobMonitor);

		WorkflowInstanceFacade instance = enactor.createFacade(dataflow,
				invocationContext);
		workflowInstances.put(job.getId(), instance);
		instance.addWorkflowInstanceListener(jobMonitor);

		Map<String, T2Reference> inputs = null;
		if (job.getInputs() != null) {
			inputs = dataManager.getData(job.getInputs()).getReferenceMap();
		}

		run(instance, inputs);
	}

	public void setJobStatus(Job job, String status) {
		job.setStatus(status);
		jobDao.save(job);
	}

	public void run(WorkflowInstanceFacade instance,
			Map<String, T2Reference> inputs) {
		instance.fire();
		if (inputs != null && !inputs.isEmpty()) {
			for (Map.Entry<String, T2Reference> entry : inputs.entrySet()) {
				enactor.pushData(instance, entry.getKey(), entry.getValue());
			}
		}
	}

	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}

	public void setWorkflowManager(WorkflowManager workflowManager) {
		this.workflowManager = workflowManager;
	}

	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	public void setEnactor(Enactor enactor) {
		this.enactor = enactor;
	}

	public void setInvocationContextFactory(
			InvocationContextFactory invocationContextFactory) {
		this.invocationContextFactory = invocationContextFactory;
	}

	public void setAuthorizationManager(
			AuthorizationManager authorizationManager) {
		this.authorizationManager = authorizationManager;
	}

}
