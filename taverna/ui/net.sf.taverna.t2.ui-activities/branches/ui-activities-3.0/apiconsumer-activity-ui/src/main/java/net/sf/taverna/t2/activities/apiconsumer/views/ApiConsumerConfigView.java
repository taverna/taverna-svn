/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.activities.apiconsumer.views;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.DependencyConfigurationPanel;
import uk.org.taverna.commons.services.ServiceRegistry;
import uk.org.taverna.configuration.app.ApplicationConfiguration;
import uk.org.taverna.scufl2.api.activity.Activity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Component for configuring an API Consumer activity.
 *
 * @author alanrw
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ApiConsumerConfigView extends ActivityConfigurationPanel {

	private DependencyConfigurationPanel dependencyConfigurationPanel;
	private File libDir;
	private final ServiceRegistry serviceRegistry;

	public ApiConsumerConfigView(Activity activity, ApplicationConfiguration applicationConfiguration,
			ServiceRegistry serviceRegistry) {
		super(activity);
		this.serviceRegistry = serviceRegistry;
		libDir = new File(applicationConfiguration.getApplicationHomeDir(), "lib");
		if (!libDir.exists()) {
			libDir.mkdir();
		}
		initialise();
	}

	@Override
	protected void initialise() {
		super.initialise();
		removeAll();
		add(createDependenciesPanel());
	}

	@Override
	public void noteConfiguration() {
		setProperty("classLoaderSharing", dependencyConfigurationPanel.getClassLoaderSharing());
		ArrayNode localDependenciesArray = getJson().arrayNode();
		for (String localDependency : dependencyConfigurationPanel.getLocalDependencies()) {
			localDependenciesArray.add(localDependency);
		}
		getJson().put("localDependency", localDependenciesArray);
		configureInputPorts(serviceRegistry);
		configureOutputPorts(serviceRegistry);
	}

	@Override
	public boolean checkValues() {
		return true;
	}

	private Component createDependenciesPanel() {
		String classLoaderSharing = getProperty("classLoaderSharing");
		List<String> localDependencies = new ArrayList<>();
		if (getJson().has("localDependency")) {
			for (JsonNode localDependency : getJson().get("localDependency")) {
				localDependencies.add(localDependency.textValue());
			}
		}
		dependencyConfigurationPanel = new DependencyConfigurationPanel(classLoaderSharing,
				localDependencies, libDir);
		return dependencyConfigurationPanel;
	}

}
