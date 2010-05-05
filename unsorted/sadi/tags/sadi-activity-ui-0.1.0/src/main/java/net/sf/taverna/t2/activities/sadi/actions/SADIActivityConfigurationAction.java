/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.sadi.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.activities.sadi.views.SADIConfigurationPanel;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

/**
 * 
 *
 * @author David Withers
 */
public class SADIActivityConfigurationAction extends ActivityConfigurationAction<SADIActivity, SADIActivityConfigurationBean> {

	public static final String CONFIGURE = "Configure SADI";

	private static final long serialVersionUID = 1L;

//	private final Frame owner;

	public SADIActivityConfigurationAction(SADIActivity activity, Frame owner) {
		super(activity);
		putValue(Action.NAME, CONFIGURE);
//		this.owner = owner;
	}

	public void actionPerformed(ActionEvent action) {
		ActivityConfigurationDialog<?, ?> currentDialog = ActivityConfigurationAction.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}

		final SADIConfigurationPanel configurationPanel = new SADIConfigurationPanel(getActivity());
		final ActivityConfigurationDialog<SADIActivity, SADIActivityConfigurationBean> dialog =
			new ActivityConfigurationDialog<SADIActivity, SADIActivityConfigurationBean>(getActivity(), configurationPanel);
		
		ActivityConfigurationAction.setDialog(getActivity(), dialog);	
	}

}
