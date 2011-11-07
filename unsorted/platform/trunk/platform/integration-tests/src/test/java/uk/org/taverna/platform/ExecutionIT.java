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
package uk.org.taverna.platform;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import uk.org.taverna.platform.activity.ActivityConfigurationException;
import uk.org.taverna.platform.activity.ActivityNotFoundException;
import uk.org.taverna.platform.execution.api.AbstractExecutionEnvironment;
import uk.org.taverna.platform.execution.api.AbstractExecutionService;
import uk.org.taverna.platform.execution.api.Execution;
import uk.org.taverna.platform.execution.api.ExecutionEnvironment;
import uk.org.taverna.platform.execution.api.ExecutionEnvironmentService;
import uk.org.taverna.platform.execution.api.InvalidWorkflowException;
import uk.org.taverna.scufl2.api.configurations.ConfigurationDefinition;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.profiles.Profile;

public class ExecutionIT extends PlatformIT {

	private ExecutionEnvironmentService executionEnvironmentService;

	protected void setup() throws InvalidSyntaxException {
		super.setup();
		ServiceReference[] executionServiceReferences = bundleContext.getServiceReferences(
				"uk.org.taverna.platform.execution.api.ExecutionEnvironmentService", null);
		assertEquals(1, executionServiceReferences.length);
		executionEnvironmentService = (ExecutionEnvironmentService) bundleContext
				.getService(executionServiceReferences[0]);

	}

	public void testGetExecutionEnvironments() throws Exception {
		setup();

		Set<ExecutionEnvironment> executionEnvironments = executionEnvironmentService
				.getExecutionEnvironments();
		assertEquals(1, executionEnvironments.size());

		bundleContext.registerService("uk.org.taverna.platform.execution.api.ExecutionService",
				new AbstractExecutionService("test id", "test name", "test description") {
					public Set<ExecutionEnvironment> getExecutionEnvivonments() {
						return Collections
								.<ExecutionEnvironment> singleton(new AbstractExecutionEnvironment(
										"test id", "test name", "test description", this, null) {
									public List<URI> getDispatchLayerURIs() {
										return Collections.singletonList(URI
												.create("http://ns.taverna.org.uk/2010/dispatchlayer/testDispatchLayer"));
									}

									public List<URI> getActivityURIs() {
										return Collections.singletonList(URI
												.create("http://ns.taverna.org.uk/2010/activity/testActivity"));
									}

									public boolean dispatchLayerExists(URI uri) {
										return false;
									}

									public boolean activityExists(URI uri) {
										return false;
									}

									public ConfigurationDefinition getActivityConfigurationDefinition(
											URI uri) throws ActivityNotFoundException,
											ActivityConfigurationException {
										return null;
									}
								});
					}

					protected Execution createExecutionImpl(WorkflowBundle workflowBundle,
							Workflow workflow, Profile profile, Map<String, T2Reference> inputs,
							ReferenceService referenceService) throws InvalidWorkflowException {
						return null;
					}
				}, null);


		executionEnvironments = executionEnvironmentService
				.getExecutionEnvironments();
		assertEquals(2, executionEnvironments.size());

		for (ExecutionEnvironment executionEnvironment : executionEnvironments) {
			System.out.println(executionEnvironment);
		}
	}

	public void testGetExecutionEnvironmentsProfile() throws Exception {
		setup();

		WorkflowBundle workflowBundle = loadWorkflow("/t2flow/beanshell.t2flow");

		Set<ExecutionEnvironment> executionEnvironments = executionEnvironmentService
				.getExecutionEnvironments(workflowBundle.getMainProfile());
		assertEquals(1, executionEnvironments.size());

		System.out.println(executionEnvironments.iterator().next());
	}

}
