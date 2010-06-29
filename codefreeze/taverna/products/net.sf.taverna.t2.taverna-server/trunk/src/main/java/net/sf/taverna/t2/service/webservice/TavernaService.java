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
package net.sf.taverna.t2.service.webservice;

import net.sf.taverna.t2.service.webservice.resource.DataResource;
import net.sf.taverna.t2.service.webservice.resource.JobResource;
import net.sf.taverna.t2.service.webservice.resource.Jobs;
import net.sf.taverna.t2.service.webservice.resource.WorkflowResource;
import net.sf.taverna.t2.service.webservice.resource.Workflows;

/**
 * Interface defining the operations that can be performed on a Taverna Service.
 * 
 * @author David Withers
 */
public interface TavernaService {

	/**
	 * Adds a new {@link DataResource}. Returns the id for the new DataResource.
	 * 
	 * @param data
	 *            the DataResource to add
	 * @return the id for the DataResource
	 */
	public Long addData(DataResource data);

	/**
	 * Adds a new {@link JobResource}. The JobResource is created from the
	 * specified WorkflowResource and optional input DataResource. The Job will
	 * be scheduled to run as soon as it is created.
	 * 
	 * @param workflowID
	 *            id of the workflow to run
	 * @param dataID
	 *            id of the input {@link DataResource}
	 * @return the id for the JobResource
	 */
	public Long addJob(Long workflowID, Long dataID);

	/**
	 * Adds a new {@link WorkflowResource}. The WorkflowResource is created from
	 * the workflow XML. Returns the id for the new WorkflowResource.
	 * 
	 * @param workflowXML
	 *            the workflow
	 * @return id for the WorkflowResource
	 */
	public Long addWorkflow(String workflowXML);

	/**
	 * Deletes a DataResource with the specified id.
	 * 
	 * @param dataID
	 *            the id of the DataResource to be deleted
	 */
	public void deleteData(Long dataID);

	/**
	 * Deletes a JobResource with the specified id.
	 * 
	 * @param jobID
	 *            the id of the JobResource to be deleted
	 */
	public void deleteJob(Long jobID);

	/**
	 * Deletes a WorkflowResource with the specified id.
	 * 
	 * @param workflowID
	 *            the id of the WorkflowResource to be deleted
	 */
	public void deleteWorkflow(Long workflowID);

	/**
	 * Returns the {@link DataResource} with the specified id.
	 * 
	 * @param dataID
	 *            the id of the
	 * @return the DataResource
	 */
	public DataResource getData(Long dataID);

	/**
	 * Returns the {@link JobResource} with the specified id.
	 * 
	 * @param jobID
	 *            the id of the
	 * @return the JobResource
	 */
	public JobResource getJob(Long jobID);

	/**
	 * Returns all the {@link JobResource}s.
	 * 
	 * @return all the JobResources
	 */
	public Jobs getJobs();

	/**
	 * Returns the status of the job with the specified id.
	 * 
	 * @param jobID
	 *            the id of the JobResource
	 * @return the status of the job
	 */
	public String getJobStatus(Long jobID);

	/**
	 * Returns the {@link WorkflowResource} with the specified id.
	 * 
	 * @param workflowID
	 *            the id of the WorkflowResource
	 * @return
	 */
	public WorkflowResource getWorkflow(Long workflowID);

	/**
	 * Returns all the {@link WorkflowResource}s.
	 * 
	 * @return all the WorkflowResources
	 */
	public Workflows getWorkflows();

	/**
	 * Pauses a running job. Returns true if the job is paused.
	 * 
	 * @param jobID
	 *            the id of the JobResource
	 * @return true if the job is paused
	 */
	public Boolean pauseJob(Long jobID);

	/**
	 * Resumes a paused job. Returns true if the job is resumed.
	 * 
	 * @param jobID
	 *            the id of the JobResource
	 * @return true if the job is resumed
	 */
	public Boolean resumeJob(Long jobID);

	/**
	 * Cancels a running job. Returns true if the job is canceled.
	 * 
	 * @param jobID
	 *            the id of the JobResource
	 * @return true if the job is canceled
	 */
	public Boolean cancelJob(Long jobID);

}