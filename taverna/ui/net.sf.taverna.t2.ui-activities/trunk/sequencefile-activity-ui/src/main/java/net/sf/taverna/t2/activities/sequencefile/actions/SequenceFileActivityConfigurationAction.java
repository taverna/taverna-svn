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
package net.sf.taverna.t2.activities.sequencefile.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.activities.sequencefile.SequenceFileActivity;
import net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean;
import net.sf.taverna.t2.activities.sequencefile.servicedescriptions.SequenceFileTemplateService;
import net.sf.taverna.t2.activities.sequencefile.views.SequenceFileConfigurationPanel;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * An Action to configure a SequenceFileActivity.
 * 
 * @author David Withers
 */
public class SequenceFileActivityConfigurationAction extends
		ActivityConfigurationAction<SequenceFileActivity, SequenceFileActivityConfigurationBean> {

	public static final String CONFIGURE = "Configure " + SequenceFileTemplateService.SERVICE_NAME;

	private static final long serialVersionUID = 1L;

	private final Frame owner;

	public SequenceFileActivityConfigurationAction(SequenceFileActivity activity, Frame owner) {
		super(activity);
		putValue(Action.NAME, CONFIGURE);
		this.owner = owner;
	}

	@SuppressWarnings("serial")
	public void actionPerformed(ActionEvent action) {
		final SequenceFileConfigurationPanel spreadsheetConfigView = new SequenceFileConfigurationPanel(
				getActivity());
		final HelpEnabledDialog dialog = new HelpEnabledDialog(owner,
				SequenceFileTemplateService.SERVICE_NAME + " Configuration", true, null);
		final Dataflow owningDataflow = FileManager.getInstance().getCurrentDataflow();
		dialog.add(spreadsheetConfigView);
		dialog.pack();

		spreadsheetConfigView.setOkAction(new AbstractAction("OK") {
			public void actionPerformed(ActionEvent arg0) {
				if (spreadsheetConfigView.isConfigurationChanged()) {
					ActivityConfigurationDialog.configureActivity(owningDataflow, activity,
							spreadsheetConfigView.getConfiguration());
				}
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		spreadsheetConfigView.setCancelAction(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}

		});
		dialog.setVisible(true);
	}

}
