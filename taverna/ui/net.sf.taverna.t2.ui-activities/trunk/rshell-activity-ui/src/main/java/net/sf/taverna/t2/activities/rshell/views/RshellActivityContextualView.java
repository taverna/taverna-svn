/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
package net.sf.taverna.t2.activities.rshell.views;

import java.awt.Frame;
import java.net.URI;

import javax.swing.Action;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.OutputActivityPort;
import uk.org.taverna.scufl2.api.property.PropertyResource;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;

/**
 * A simple non-editable HTML table view over a {@link RshellActivity}.
 *
 * @author Alex Nenadic
 * @author Ian Dunlop
 *
 */
public class RshellActivityContextualView extends HTMLBasedActivityContextualView {

	private static final URI ACTIVITY_TYPE = URI.create("http://ns.taverna.org.uk/2010/activity/rshell");
	private static final long serialVersionUID = -2423232268033935502L;
	private final EditManager editManager;
	private final FileManager fileManager;
	private final ActivityIconManager activityIconManager;
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public RshellActivityContextualView(Activity activity, EditManager editManager,
			FileManager fileManager, ActivityIconManager activityIconManager, ColourManager colourManager, ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super(activity, colourManager);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.activityIconManager = activityIconManager;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		init();
	}

	private void init() {

	}

	@Override
	protected String getRawTableRowsHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<tr><th>Input Port Name</th><th>Semantic Type</th></tr>");
		for (InputActivityPort inputActivityPort : getActivity().getInputPorts()) {
			html.append("<tr><td>" + inputActivityPort.getName() + "</td><td>");
			for (RShellPortSymanticTypeBean inputType : getConfigBean().getInputSymanticTypes()) {
				if (inputActivityPort.getName().equalsIgnoreCase(inputType.getName())) {
					html.append(inputType.getSymanticType().description + "</td></tr>");
					break;
				}
			}
		}
		html.append("<tr><th>Output Port Name</th><th>Depth</th></tr>");
		for (OutputActivityPort outputActivityPort : getActivity().getOutputPorts()) {
			html.append("<tr><td>" + outputActivityPort.getName() + "</td><td>");
			for (RShellPortSymanticTypeBean outputType : getConfigBean().getOutputSymanticTypes()) {
				if (outputActivityPort.getName().equalsIgnoreCase(outputType.getName())) {
					html.append(outputType.getSymanticType().description + "</td></tr>");
					break;
				}
			}
		}

		PropertyResource connection = getConfigBean().getPropertyResource().getPropertyAsResource(ACTIVITY_TYPE.resolve("#connection"));
		if (connection != null) {
			String host = connection.getPropertyAsString(ACTIVITY_TYPE.resolve("#hostname"));
			if (host != null) {
				String port = connection.getPropertyAsString(ACTIVITY_TYPE.resolve("#port"));
				String keepSessionAlive = connection.getPropertyAsString(ACTIVITY_TYPE.resolve("#keepSessionAlive"));
				html.append("<tr><td>Host</td><td>" + host + "</td></tr>");
				html.append("<tr><td>Port</td><td>" + port + "</td></tr>");
				html.append("<tr><td>Keep Session Alive</td><td>" + keepSessionAlive + "</td></tr>");

			}
		}
		return html.toString();
	}

	@Override
	public String getViewTitle() {
		return "Rshell service";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new RshellActivityConfigurationAction(getActivity(), owner,
				editManager, fileManager, activityIconManager, serviceDescriptionRegistry);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
