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

import java.io.IOException;
import java.util.Collection;

import net.sf.taverna.t2.service.controller.WorkflowUpload;
import net.sf.taverna.t2.service.model.Workflow;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Manages storing and parsing of workflows.
 *
 * @author David Withers
 */
public interface WorkflowManager {

//	@PreAuthorize("hasRole('ROLE_USER')")
	public void addWorkflow(Workflow workflow);

//	@PreAuthorize("hasRole('ROLE_USER')")
	public void deleteWorkflow(Long id);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public Workflow getWorkflow(Long id);
	
//	@PreAuthorize("hasRole('ROLE_USER')")
//	@PostFilter("hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public Collection<Workflow> getAllWorkflows();
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public void enableWorkflow(Long id);

//	@PreAuthorize("hasRole('ROLE_USER')")
	public void disableWorkflow(Long id);

//	@PreAuthorize("hasRole('ROLE_USER')")
	public Workflow createWorkflow(WorkflowUpload workflowUpload) throws IOException;
	
//	@PreAuthorize("hasRole('ROLE_USER')")
	public Workflow createWorkflow(String workflowXML);
	
	public Dataflow getDataflow(Long id);

}
