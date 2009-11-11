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
package net.sf.taverna.t2.service.model;

/**
 * A Job is a single run of a Taverna 2 workflow. It has references to the
 * {@link Workflow}, the workflow's input and output {@link Data}. Note that a
 * workflow may not take any inputs or produce any outputs.
 * <p>
 * The Job's status can be one of : CREATED, INITIALISING, RUNNING, COMPLETE,
 * CANCELLING, CANCELLED, FAILED, PAUSED
 * 
 * @author David Withers
 */
public class Job extends IdentifiableImpl {

	private Long workflow, inputs, outputs;

	private String status;

	/**
	 * Returns the id of the workflow.
	 *
	 * @return the id of the workflow
	 */
	public Long getWorkflow() {
		return workflow;
	}

	/**
	 * Sets the id of the workflow.
	 *
	 * @param workflow the id of the workflow
	 */
	public void setWorkflow(Long workflow) {
		this.workflow = workflow;
	}

	/**
	 * Returns the id of the workflow inputs.
	 *
	 * @return the id of the workflow inputs
	 */
	public Long getInputs() {
		return inputs;
	}

	/**
	 * Sets the id of the workflow inputs.
	 *
	 * @param inputs the id of the workflow inputs
	 */
	public void setInputs(Long inputs) {
		this.inputs = inputs;
	}

	/**
	 * Returns the id of the workflow outputs.
	 *
	 * @return the id of the workflow outputs
	 */
	public Long getOutputs() {
		return outputs;
	}

	/**
	 * Sets the id of the workflow outputs
	 *
	 * @param outputs the id of the workflow outputs
	 */
	public void setOutputs(Long outputs) {
		this.outputs = outputs;
	}

	/**
	 * Returns the status of the <code>Job</code>.
	 *
	 * @return
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the <code>Job</code> status. Valid values are :
	 * <p>
	 * CREATED, INITIALISING, RUNNING, COMPLETE, CANCELLING, CANCELLED, FAILED,
	 * PAUSED
	 * 
	 * @param status the current status of the <code>Job</code>
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((workflow == null) ? 0 : workflow.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Job other = (Job) obj;
		if (inputs == null) {
			if (other.inputs != null) {
				return false;
			}
		} else if (!inputs.equals(other.inputs)) {
			return false;
		}
		if (outputs == null) {
			if (other.outputs != null) {
				return false;
			}
		} else if (!outputs.equals(other.outputs)) {
			return false;
		}
		if (status == null) {
			if (other.status != null) {
				return false;
			}
		} else if (!status.equals(other.status)) {
			return false;
		}
		if (workflow == null) {
			if (other.workflow != null) {
				return false;
			}
		} else if (!workflow.equals(other.workflow)) {
			return false;
		}
		return true;
	}

}
