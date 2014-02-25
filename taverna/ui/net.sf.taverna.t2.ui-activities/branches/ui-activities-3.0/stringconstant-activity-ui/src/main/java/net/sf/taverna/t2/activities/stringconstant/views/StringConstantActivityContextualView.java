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
package net.sf.taverna.t2.activities.stringconstant.views;

import java.awt.Frame;

import javax.swing.Action;

import net.sf.taverna.t2.activities.stringconstant.actions.StringConstantActivityConfigurationAction;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import uk.org.taverna.commons.services.ServiceRegistry;
import uk.org.taverna.scufl2.api.activity.Activity;

import com.fasterxml.jackson.databind.JsonNode;

public class StringConstantActivityContextualView extends HTMLBasedActivityContextualView {

	private static final long serialVersionUID = -553974544001808511L;

	private final EditManager editManager;
	private final FileManager fileManager;
	private final ActivityIconManager activityIconManager;
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;
	private final ServiceRegistry serviceRegistry;
	private static final int MAX_LENGTH = 100;

	public StringConstantActivityContextualView(Activity activity, EditManager editManager,
			FileManager fileManager, ActivityIconManager activityIconManager,
			ColourManager colourManager, ServiceDescriptionRegistry serviceDescriptionRegistry, ServiceRegistry serviceRegistry) {
		super(activity, colourManager);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.activityIconManager = activityIconManager;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public String getViewTitle() {
		return "Text constant";
	}

	@Override
	protected String getRawTableRowsHtml() {
		JsonNode json = getConfigBean().getJson();
		String value = json.get("string").textValue();
		value = StringUtils.abbreviate(value, MAX_LENGTH);
		value = StringEscapeUtils.escapeHtml(value);
		String html = "<tr><td>Value</td><td>" + value + "</td></tr>";
		return html;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new StringConstantActivityConfigurationAction(
				getActivity(), owner, editManager, fileManager,
				activityIconManager, serviceDescriptionRegistry, serviceRegistry);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
