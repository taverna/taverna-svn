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

import net.sf.taverna.t2.service.model.Workflow;

/**
 *
 *
 * @author David Withers
 */
@XmlRootElement(name = "Workflow")
public class WorkflowResource extends Resource {

	private String xml;
	
	private boolean enabled;
	
	public WorkflowResource() {
	}
	
	public WorkflowResource(Workflow workflow, URI uri) {
		super(workflow, uri);
		setXml(workflow.getXml());
		setEnabled(workflow.isEnabled());
	}

	/**
	 * Returns the workflow xml.
	 *
	 * @return the value of workflow xml
	 */
	public String getXml() {
		return xml;
	}

	/**
	 * Sets the workflow xml.
	 *
	 * @param xml the new value for workflow xml
	 */
	public void setXml(String xml) {
		this.xml = xml;
	}

	/**
	 * Returns the enabled.
	 *
	 * @return the value of enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled.
	 *
	 * @param enabled the new value for enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
