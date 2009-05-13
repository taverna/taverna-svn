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

import javax.ws.rs.core.StreamingOutput;

import net.sf.taverna.t2.service.model.Job;

/**
 * Manages workflow jobs.
 *
 * @author David Withers
 */
public interface JobManager {

//	@PreAuthorize("hasRole('ROLE_USER')")
	public void addJob(Job job);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public void deleteJob(Long id);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public Job getJob(Long id);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public Collection<Job> getAllJobs();
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public Job createJob(Long workflow, Long inputs);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public void runJob(Job job, boolean waitForResult);
	
	public boolean pauseJob(Long id);

	public boolean resumeJob(Long id);

	public boolean cancelJob(Long id);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public void setJobStatus(Job job, String status);

	public StreamingOutput getOutput(Long jobID, String port);
	
}
