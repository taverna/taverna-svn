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
package net.sf.taverna.t2.service.webservice.resource;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.taverna.t2.service.model.Job;

/**
 * A Job is a single run of a workflow.
 *
 * @author David Withers
 */
@XmlRootElement(name = "Job")
public class JobResource extends Resource {

	private Long workflow;
	
	private Long inputs;
	
	private Long outputs;
	
	private String status;

	public JobResource() {
	}

	public JobResource(Job job, URI uri) {
		super(job, uri);
		setWorkflow(job.getWorkflow());
		setInputs(job.getInputs());
		setOutputs(job.getOutputs());
		setStatus(job.getStatus());
	}

	public Long getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Long workflow) {
		this.workflow = workflow;
	}

	public Long getInputs() {
		return inputs;
	}

	public void setInputs(Long inputs) {
		this.inputs = inputs;
	}

	public Long getOutputs() {
		return outputs;
	}

	public void setOutputs(Long outputs) {
		this.outputs = outputs;
	}

	/**
	 * CREATED, INITIALISING, RUNNING, COMPLETE, CANCELLING, CANCELLED, FAILED, PAUSED
	 *
	 * @return
	 */
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("---------------------------------------");
		sb.append(lineSeparator);
		sb.append("JobResource");
		sb.append(lineSeparator);
		sb.append(super.toString());
		sb.append("Status : ");
		sb.append(getStatus());
		sb.append(lineSeparator);
		sb.append("Inputs : ");
		sb.append(getInputs());
		sb.append(lineSeparator);
		sb.append("Outputs : ");
		sb.append(getOutputs());
		sb.append(lineSeparator);
		sb.append("---------------------------------------");
		sb.append(lineSeparator);
		return sb.toString();
	}
	
}
