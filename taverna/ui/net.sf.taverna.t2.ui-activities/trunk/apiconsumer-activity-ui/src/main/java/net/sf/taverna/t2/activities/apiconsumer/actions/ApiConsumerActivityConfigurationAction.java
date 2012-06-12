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
package net.sf.taverna.t2.activities.apiconsumer.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JDialog;

import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivityConfigurationBean;
import net.sf.taverna.t2.activities.apiconsumer.views.ApiConsumerConfigView;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

/**
 * Provides a configurable view for {@link ApiConsumerActivity} through its
 * {@link ApiConsumerActivityConfigurationBean}, where users can set local
 * dependencies for the API consumer activity and its classloader sharing
 * policy.
 *
 * @author Alex Nenadic
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class ApiConsumerActivityConfigurationAction extends
		ActivityConfigurationAction<ApiConsumerActivity, ApiConsumerActivityConfigurationBean> {

	// Configuration before any changes have been done in this dialog
	private ApiConsumerActivityConfigurationBean configuration;

	public static final String CONFIGURE_APICONSUMER_ACTIVITY = "Configure Api Consumer";

	private final EditManager editManager;

	private final FileManager fileManager;

	public ApiConsumerActivityConfigurationAction(ApiConsumerActivity activity, Frame owner,
			EditManager editManager, FileManager fileManager,
			ActivityIconManager activityIconManager) {
		super(activity, activityIconManager);
		this.editManager = editManager;
		this.fileManager = fileManager;
		putValue(Action.NAME, CONFIGURE_APICONSUMER_ACTIVITY);
		this.configuration = activity.getConfiguration();

	}

	public void actionPerformed(ActionEvent e) {
		JDialog currentDialog = ActivityConfigurationAction.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		final ApiConsumerConfigView apiConfigView = new ApiConsumerConfigView(
				(ApiConsumerActivity) getActivity());
		final ActivityConfigurationDialog<ApiConsumerActivity, ApiConsumerActivityConfigurationBean> dialog = new ActivityConfigurationDialog<ApiConsumerActivity, ApiConsumerActivityConfigurationBean>(
				getActivity(), apiConfigView, editManager, fileManager);

		ActivityConfigurationAction.setDialog(getActivity(), dialog, fileManager);

	}
}
