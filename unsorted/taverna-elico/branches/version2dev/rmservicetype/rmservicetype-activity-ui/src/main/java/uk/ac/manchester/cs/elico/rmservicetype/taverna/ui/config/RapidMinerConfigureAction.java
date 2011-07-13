package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view.RapidMinerConfigurationView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

@SuppressWarnings("serial")
public class RapidMinerConfigureAction
		extends
		ActivityConfigurationAction<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean> {

	Frame owner;
	
	public RapidMinerConfigureAction(RapidMinerExampleActivity activity, Frame owner) {
		super(activity);
		this.owner = owner;
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		
		ActivityConfigurationDialog<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		
		if (currentDialog != null) {
			
			currentDialog.toFront();
			return;
			
		}
		
		//RapidMinerConfigurationPanel panel = new RapidMinerConfigurationPanel(
		//		getActivity());
		final Dataflow owningDataflow = FileManager.getInstance().getCurrentDataflow();
		
		final RapidMinerConfigurationView panel = new RapidMinerConfigurationView(getActivity());
		final HelpEnabledDialog dialog = new HelpEnabledDialog(owner, "Operator Configuration", true, null);
		dialog.add(panel);
		dialog.pack();
		
		
		panel.setOkAction(new AbstractAction("Finish") {

			public void actionPerformed(ActionEvent arg0) {
				
				//[debug]System.out.println(" Dialog position 1");
									
//				Iterator myiter = panel.getConfiguration().getParameterDescriptions().iterator();
//
//				while (myiter.hasNext()) {
//
//					RapidMinerParameterDescription des = (RapidMinerParameterDescription) myiter.next();
//					System.out.println("[DEBUG] new parameters to set " + des.getUseParameter() + " " + des.getExecutionValue());
//
//				}
					
				ActivityConfigurationDialog.configureActivityStatic(owningDataflow, activity, panel.getConfiguration());
					
				//[debug]System.out.println(" Dialog position 2");
				dialog.setVisible(false);
				dialog.dispose();
				
			}

		});
		
		//ActivityConfigurationDialog<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean> dialog = new ActivityConfigurationDialog<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean>(
		//		getActivity(), panel);
			
		//ActivityConfigurationAction.setDialog(getActivity(), dialog);
		
		dialog.setVisible(true);
	
	}

}

/*
 		spreadsheetConfigView.setOkAction(new AbstractAction(SpreadsheetImportUIText
				.getString("SpreadsheetImportActivityConfigurationAction.okButton")) {
			public void actionPerformed(ActionEvent arg0) {
				if (spreadsheetConfigView.isConfigurationChanged()) {
					ActivityConfigurationDialog.configureActivityStatic(owningDataflow, activity, spreadsheetConfigView.getConfiguration());
				}
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
 */

/*
 ActivityConfigurationDialog<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		RapidMinerConfigurationPanel panel = new RapidMinerConfigurationPanel(
				getActivity());
		ActivityConfigurationDialog<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean> dialog = new ActivityConfigurationDialog<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean>(
				getActivity(), panel);
		
				
		ActivityConfigurationAction.setDialog(getActivity(), dialog);
 */
