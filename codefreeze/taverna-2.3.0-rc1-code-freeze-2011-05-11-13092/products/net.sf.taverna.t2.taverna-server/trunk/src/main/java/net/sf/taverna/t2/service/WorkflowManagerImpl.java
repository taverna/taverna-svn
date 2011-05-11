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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.platform.taverna.WorkflowParser;
import net.sf.taverna.t2.service.controller.WorkflowUpload;
import net.sf.taverna.t2.service.model.Workflow;
import net.sf.taverna.t2.service.store.WorkflowDao;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class WorkflowManagerImpl implements WorkflowManager, InitializingBean {

	private WorkflowDao workflowDao;

	private WorkflowParser workflowParser;

	private AuthorizationManager authorizationManager;

	private static Map<Long, Dataflow> dataflowMap = new HashMap<Long, Dataflow>();

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(workflowDao, "workflowDao required");
		Assert.notNull(workflowParser, "workflowParser required");
	}

	public void addWorkflow(Workflow workflow) {
		workflowDao.save(workflow);
		if (authorizationManager != null) {
			authorizationManager.createAclEntry(workflow);
		}
	}

	public void deleteWorkflow(Long id) {
		workflowDao.delete(id);
		if (authorizationManager != null) {
			authorizationManager.deleteAclEntry(Workflow.class, id);
		}
	}

	public Workflow getWorkflow(Long id) {
		return workflowDao.get(id);
	}

	public Collection<Workflow> getAllWorkflows() {
		return workflowDao.getAll();
	}

	public Workflow createWorkflow(WorkflowUpload workflowUpload)
			throws IOException {
		String workflow = IOUtils.toString(workflowUpload.getFile()
				.getInputStream());
		return createWorkflow(workflow);
	}

	public Workflow createWorkflow(String workflowXML) {
		Workflow workflow = new Workflow();
		Dataflow dataflow = parseDataflow(workflowXML);
		workflow.setXml(workflowXML);
		workflow.setEnabled(true);
		addWorkflow(workflow);
		dataflowMap.put(workflow.getId(), dataflow);
		return workflow;
	}

	public Dataflow getDataflow(Long workflowID) {
		if (!dataflowMap.containsKey(workflowID)) {
			dataflowMap.put(workflowID, parseDataflow(getWorkflow(workflowID)
					.getXml()));
		}
		return dataflowMap.get(workflowID);
	}

	private Dataflow parseDataflow(String xml) {
		Dataflow dataflow = null;
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		try {
			dataflow = workflowParser.createDataflow(inputStream);
		} catch (DeserializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataflow;
	}

	public void setWorkflowDao(WorkflowDao workflowDao) {
		this.workflowDao = workflowDao;
	}

	public void setWorkflowParser(WorkflowParser workflowParser) {
		this.workflowParser = workflowParser;
	}

	public void setAuthorizationManager(
			AuthorizationManager authorizationManager) {
		this.authorizationManager = authorizationManager;
	}

	public void disableWorkflow(Long id) {
		Workflow workflow = getWorkflow(id);
		if (workflow.isEnabled()) {
			workflow.setEnabled(false);
			workflowDao.save(workflow);
		}
	}

	public void enableWorkflow(Long id) {
		Workflow workflow = getWorkflow(id);
		if (!workflow.isEnabled()) {
			workflow.setEnabled(true);
			workflowDao.save(workflow);
		}
	}

}
