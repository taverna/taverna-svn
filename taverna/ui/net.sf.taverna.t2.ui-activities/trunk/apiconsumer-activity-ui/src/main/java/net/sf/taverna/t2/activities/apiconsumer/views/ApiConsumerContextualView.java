/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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

import java.awt.Frame;
import java.util.Iterator;

import javax.swing.Action;

import net.sf.taverna.t2.activities.apiconsumer.actions.ApiConsumerActivityConfigurationAction;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import uk.org.taverna.commons.services.ServiceRegistry;
import uk.org.taverna.configuration.app.ApplicationConfiguration;
import uk.org.taverna.scufl2.api.activity.Activity;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A simple non editable HTML table view over a {@link ApiConsumerActivity}.
 * Clicking on the 'Configure' button shows the editable {@link ApiConsumerConfigView}
 *
 * @author Alex Nenadic
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ApiConsumerContextualView extends HTMLBasedActivityContextualView {

	private EditManager editManager;
	private FileManager fileManager;
	private final ActivityIconManager activityIconManager;
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;
	private final ApplicationConfiguration applicationConfiguration;
	private final ServiceRegistry serviceRegistry;

	public ApiConsumerContextualView(Activity activity, EditManager editManager,
			FileManager fileManager, ActivityIconManager activityIconManager,
			ColourManager colourManager, ServiceDescriptionRegistry serviceDescriptionRegistry,
			ApplicationConfiguration applicationConfiguration, ServiceRegistry serviceRegistry) {
		super(activity, colourManager);
		this.editManager = editManager;
		this.activityIconManager = activityIconManager;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		this.applicationConfiguration = applicationConfiguration;
		this.serviceRegistry = serviceRegistry;
		init();
	}

	private void init() {
		// CSH.setHelpIDString(this,
		// "net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ApiConsumerContextualView");
	}

	@Override
	protected String getRawTableRowsHtml() {

		JsonNode bean = getConfigBean().getJson();
		String html = "";

		html += "<tr><td>Class name</td>" + "<td>" + bean.get("className").textValue()
				+ "</td></tr>";

		html += "<tr><td>Method name</td>" + "<td>" + bean.get("methodName").textValue()
				+ "</td></tr>";

		html += "<tr><td>Method description</td>" + "<td>" + bean.get("description").textValue()
				+ "</td></tr>";

		html += "<tr><td>Is method constructor?</td>" + "<td>"
				+ bean.get("isMethodConstructor").booleanValue() + "</td></tr>";

		html += "<tr><td>Is method static?</td>" + "<td>"
				+ bean.get("isMethodStatic").booleanValue() + "</td></tr>";

		html += "<tr><td>Method parameters</td><td> ";
		if (bean.has("parameterTypes")) {
			Iterator<JsonNode> parameterNames = bean.get("parameterNames").elements();
			Iterator<JsonNode> parameterTypes = bean.get("parameterTypes").elements();
			Iterator<JsonNode> parameterDimensions = bean.get("parameterDimensions").elements();
			while (parameterTypes.hasNext()) {
				html += parameterNames.next().textValue() + ": "
						+ parameterTypes.next().textValue();
				int parameterDimension = parameterDimensions.next().intValue();
				for (int j = 0; j < parameterDimension; j++) {
					html += "[]";
				}
				html += "<br>";
			}
		}
		html += "</td></tr>";

		html += "<tr><td>Method return type</td><td> ";
		html += bean.get("returnType").textValue();
		int returnDimension = bean.get("returnDimension").intValue();
		for (int j = 0; j < returnDimension; j++) {
			html += "[]";
		}
		html += "</td></tr>";

		return html;
	}

	@Override
	public String getViewTitle() {
		return "API consumer service";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new ApiConsumerActivityConfigurationAction(getActivity(), owner, editManager,
				fileManager, activityIconManager, serviceDescriptionRegistry,
				applicationConfiguration, serviceRegistry);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
