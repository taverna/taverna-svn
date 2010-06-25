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
package net.sf.taverna.t2.service.webservice.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.jws.WebService;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.service.DataManager;
import net.sf.taverna.t2.service.JobManager;
import net.sf.taverna.t2.service.WorkflowManager;
import net.sf.taverna.t2.service.model.Data;
import net.sf.taverna.t2.service.model.Job;
import net.sf.taverna.t2.service.model.Workflow;
import net.sf.taverna.t2.service.webservice.resource.DataResource;
import net.sf.taverna.t2.service.webservice.resource.DataValue;
import net.sf.taverna.t2.service.webservice.resource.JobResource;
import net.sf.taverna.t2.service.webservice.resource.Jobs;
import net.sf.taverna.t2.service.webservice.resource.WorkflowResource;
import net.sf.taverna.t2.service.webservice.resource.Workflows;

/**
 * Server implementation of the {@link TavernaRESTService}.
 *
 * @author David Withers
 */
@WebService
@Path("")
@ProduceMime("application/xml")
public class TavernaRESTServer implements TavernaRESTService {

	private WorkflowManager workflowManager;

	private JobManager jobManager;

	private DataManager dataManager;

	@POST
	@ProduceMime("text/plain")
	@Path(DATA_PATH)
	public Long addData(DataResource data) {
		return dataManager.createData(dataManager.registerData(data.getDataMap()))
				.getId();
	}

	public Long addJob(Long workflowID, Long dataID) {
		Job job = jobManager.createJob(workflowID, dataID);
		jobManager.runJob(job, false);
		return job.getId();
	}

	@POST
	@ConsumeMime("application/xml")
	@ProduceMime("text/plain")
	@Path(JOB_PATH)
	public Long addJob(JobResource job) {
		return addJob(job.getWorkflow(), job.getInputs());
	}

	@POST
	@ConsumeMime(WORKFLOW_MIME)
	@ProduceMime("text/plain")
	@Path(WORKFLOW_PATH)
	public Long addWorkflow(String workflowXML) {
		return workflowManager.createWorkflow(workflowXML).getId();
	}

	@DELETE
	@Path(DATA_PATH + "{id}")
	public void deleteData(@PathParam("id") Long id) {
		dataManager.deleteData(id);
	}

	@DELETE
	@Path(JOB_PATH + "{id}")
	public void deleteJob(@PathParam("id") Long id) {
		jobManager.deleteJob(id);
	}

	@DELETE
	@Path(WORKFLOW_PATH + "{id}")
	public void deleteWorkflow(@PathParam("id") Long id) {
		workflowManager.deleteWorkflow(id);
	}

	@GET
	@Path(DATA_PATH + "{id}")
	public DataResource getData(@PathParam("id") Long id) {
		DataResource dataResource = null;
		Data data = dataManager.getData(id);
		if (data != null) {
			Map<String, T2Reference> referenceMap = data.getReferenceMap();
			Map<String, DataValue> dataMap = dataManager.dereferenceData(referenceMap);
			dataResource = new DataResource(data, null);
			dataResource.setDataMap(dataMap);
		}
		return dataResource;
	}

	@GET
	@Path(JOB_PATH + "{id}")
	public JobResource getJob(@PathParam("id") Long id) {
		JobResource jobResource = null;
		Job job = jobManager.getJob(id);
		if (job != null) {
			jobResource = new JobResource(job, null);
		}
		return jobResource;
	}

	@GET
	@Path(JOB_PATH)
	public Jobs getJobs() {
		Collection<JobResource> jobs = new ArrayList<JobResource>();
		for (Job job : jobManager.getAllJobs()) {
			jobs.add(new JobResource(job, null));
		}
		return new Jobs(jobs);
	}

	@GET
	@ProduceMime("text/plain")
	@Path(JOB_PATH + "{id}/status")
	public String getJobStatus(@PathParam("id") Long jobID) {
		return jobManager.getJob(jobID).getStatus();
	}

	@GET
	@ProduceMime("text/plain")
	@Path(JOB_PATH + "{id}/cancel")
	public Boolean cancelJob(@PathParam("id") Long jobID) {
		return jobManager.cancelJob(jobID);
	}

	@GET
	@ProduceMime("text/plain")
	@Path(JOB_PATH + "{id}/pause")
	public Boolean pauseJob(@PathParam("id") Long jobID) {
		return jobManager.pauseJob(jobID);
	}

	@GET
	@ProduceMime("text/plain")
	@Path(JOB_PATH + "{id}/resume")
	public Boolean resumeJob(@PathParam("id") Long jobID) {
		return jobManager.resumeJob(jobID);
	}

	@GET
	@Path(JOB_PATH + "{id}/{port}")
	public StreamingOutput getOutput(@PathParam("id") Long jobID,
			@PathParam("port") String port) {
		return jobManager.getOutput(jobID, port);
	}

	@GET
	@Path(WORKFLOW_PATH + "{id}")
	public WorkflowResource getWorkflow(@PathParam("id") Long id) {
		WorkflowResource workflowResource = null;
		Workflow workflow = workflowManager.getWorkflow(id);
		if (workflow != null) {
			workflowResource = new WorkflowResource(workflow, null);
		}
		return workflowResource;
	}

	@GET
	@Path(WORKFLOW_PATH)
	public Workflows getWorkflows() {
		Collection<WorkflowResource> workflows = new ArrayList<WorkflowResource>();
		for (Workflow workflow : workflowManager.getAllWorkflows()) {
			workflows.add(new WorkflowResource(workflow, null));
		}
		return new Workflows(workflows);
	}

	public void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}

	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	public void setWorkflowManager(WorkflowManager workflowManager) {
		this.workflowManager = workflowManager;
	}

//	@GET
//	@ProduceMime("text/plain")
//	@Path("test")
//	public String test() {
//		return "this is a taverna server test";
//	}
//
//	@GET
//	@ProduceMime("text/plain")
//	@Path("teststreaming/{lines}/{delay}")
//	public StreamingOutput testStreaming(@PathParam("lines") final int lines, @PathParam("delay") final int delay) {
//		StreamingOutput stream = new StreamingOutput() {
//			public void write(OutputStream out) throws IOException,
//					WebApplicationException {
//				try {
//					for (int i = 0; i < lines; i++) {
//						out.write(("This is result line " + i + " from the CXF streaming output test operation \n").getBytes());
//						out.flush();
//						Thread.sleep(delay);
//					}
//				} catch (InterruptedException e) {
//				} catch (IOException e) {
//				}
//			}
//
//		};
//		return stream;
//	}

}
