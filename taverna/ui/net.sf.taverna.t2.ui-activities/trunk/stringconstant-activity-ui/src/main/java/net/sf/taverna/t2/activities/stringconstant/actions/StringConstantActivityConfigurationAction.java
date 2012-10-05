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
package net.sf.taverna.t2.activities.stringconstant.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
<<<<<<< .working
import net.sf.taverna.t2.activities.stringconstant.servicedescriptions.StringConstantActivityIcon;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
=======
import net.sf.taverna.t2.activities.stringconstant.views.StringConstantConfigView;
>>>>>>> .merge-right.r15044
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

public class StringConstantActivityConfigurationAction extends
		ActivityConfigurationAction<StringConstantActivity, StringConstantConfigurationBean> {

	public static final String CONFIGURE_STRINGCONSTANT = "Edit value";

	private static final long serialVersionUID = 2518716617809186972L;
	private final Frame owner;

	private final EditManager editManager;

	private final FileManager fileManager;

	public StringConstantActivityConfigurationAction(StringConstantActivity activity, Frame owner,
			EditManager editManager, FileManager fileManager,
			ActivityIconManager activityIconManager) {
		super(activity, activityIconManager);
		this.editManager = editManager;
		this.fileManager = fileManager;
		putValue(Action.NAME, CONFIGURE_STRINGCONSTANT);
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent e) {

		ActivityConfigurationDialog currentDialog = ActivityConfigurationAction.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		final StringConstantConfigView stringConstantConfigView = new StringConstantConfigView((StringConstantActivity)getActivity());
		final ActivityConfigurationDialog<StringConstantActivity, StringConstantConfigurationBean> dialog =
			new ActivityConfigurationDialog<StringConstantActivity, StringConstantConfigurationBean>(getActivity(), stringConstantConfigView);

		ActivityConfigurationAction.setDialog(getActivity(), dialog);	
		
	}

}
