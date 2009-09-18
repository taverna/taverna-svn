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
package net.sf.taverna.t2.activities.rshell.views;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JDialog;

import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

/**
 * Pops up a config view for an {@link RshellActivity} and then re-configs the
 * activity with new settings when the user clicks OK on the view
 * 
 * @author Ian Dunlop
 * 
 */
public class RshellActivityConfigurationAction
		extends
		ActivityConfigurationAction<RshellActivity, RshellActivityConfigurationBean> {

	private Frame owner;
	public static final String EDIT_RSHELL_SCRIPT = "Edit Rshell script";

	public RshellActivityConfigurationAction(RshellActivity activity,
			Frame owner) {
		super(activity);
		putValue(Action.NAME, EDIT_RSHELL_SCRIPT);
		this.owner = owner;
	}

	/**
	 * Pops up a {@link JDialog} with the {@link RshellActivityConfigView} and
	 * provides a way to re-config the {@link RshellActivity} when the user
	 * clicks OK
	 */
	public void actionPerformed(ActionEvent e) {
		final RshellActivityConfigView rshellConfigView = new RshellActivityConfigView(
				(RshellActivity) getActivity());
		final HelpEnabledDialog dialog =
			new HelpEnabledDialog(owner, getRelativeName(), true, null);
		dialog.add(rshellConfigView);
		dialog.setSize(500, 500);
		// the action that will happen when the OK button is clicked
		rshellConfigView.setButtonClickedListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (rshellConfigView.isConfigurationChanged()) {
					configureActivity(rshellConfigView.getConfiguration());
				}
				dialog.setVisible(false);
			}

		});
		dialog.setVisible(true);

	}

}
