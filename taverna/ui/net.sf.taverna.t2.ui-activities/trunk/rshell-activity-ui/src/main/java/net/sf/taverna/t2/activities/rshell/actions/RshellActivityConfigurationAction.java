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
package net.sf.taverna.t2.activities.rshell.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JDialog;

import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.views.RshellConfigurationPanel;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;
import uk.org.taverna.scufl2.api.activity.Activity;

/**
 * Pops up a config view for an {@link RshellActivity} and then re-configs the activity with new
 * settings when the user clicks OK on the view
 *
 * @author Ian Dunlop
 *
 */
@SuppressWarnings("serial")
public class RshellActivityConfigurationAction extends ActivityConfigurationAction {

	public static final String EDIT_RSHELL_SCRIPT = "Edit Rshell script";
	private final EditManager editManager;
	private final FileManager fileManager;

	public RshellActivityConfigurationAction(Activity activity, Frame owner,
			EditManager editManager, FileManager fileManager,
			ActivityIconManager activityIconManager, ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super(activity, activityIconManager, serviceDescriptionRegistry);
		this.editManager = editManager;
		this.fileManager = fileManager;
		putValue(Action.NAME, EDIT_RSHELL_SCRIPT);
	}

	/**
	 * Pops up a {@link JDialog} with the {@link RshellActivityConfigView} and provides a way to
	 * re-config the {@link RshellActivity} when the user clicks OK
	 */
	public void actionPerformed(ActionEvent e) {
		JDialog currentDialog = ActivityConfigurationAction.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		final ActivityConfigurationPanel rshellConfigView = new RshellConfigurationPanel(getActivity());
		final ActivityConfigurationDialog dialog = new ActivityConfigurationDialog(getActivity(), rshellConfigView, editManager);

		ActivityConfigurationAction.setDialog(getActivity(), dialog, fileManager);

	}

}
