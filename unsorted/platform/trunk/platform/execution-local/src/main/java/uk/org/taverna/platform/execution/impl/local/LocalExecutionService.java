/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package uk.org.taverna.platform.execution.impl.local;

import java.util.Map;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Edits;
import uk.org.taverna.platform.activity.ActivityService;
import uk.org.taverna.platform.execution.api.AbstractExecutionService;
import uk.org.taverna.platform.execution.api.Execution;
import uk.org.taverna.platform.execution.api.InvalidWorkflowException;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.profiles.Profile;

/**
 * An Execution Service for executing Taverna workflows using a local Taverna Dataflow Engine.
 * 
 * @author David Withers
 */
public class LocalExecutionService extends AbstractExecutionService {

	private Edits edits;
	
	private ActivityService activityService;

	/**
	 * Constructs an execution service that executes workflows using the T2 dataflow engine.
	 */
	public LocalExecutionService() {
		super(LocalExecutionService.class.getName(), "Taverna Local Execution Service",
				"Execution Service for executing Taverna workflows using a local Taverna Dataflow Engine");
	}

	/**
	 * Sets the Edits Service for creating Taverna Dataflows.
	 * 
	 * @param edits
	 *            the Edits Service for creating Taverna Dataflows
	 */
	public void setEdits(Edits edits) {
		this.edits = edits;
	}

	/**
	 * Sets the ActivityService for creating activities.
	 * 
	 * @param activityService the ActivityService for creating activities
	 */
	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}

	@Override
	protected Execution createExecutionImpl(WorkflowBundle workflowBundle, Workflow workflow,
			Profile profile, Map<String, T2Reference> inputs, ReferenceService referenceService)
			throws InvalidWorkflowException {
		return new LocalExecution(workflowBundle, workflow, profile, inputs, referenceService,
				edits, activityService);
	}

}
