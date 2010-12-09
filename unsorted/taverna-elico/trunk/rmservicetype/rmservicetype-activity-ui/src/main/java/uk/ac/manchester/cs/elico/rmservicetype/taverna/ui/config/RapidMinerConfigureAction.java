package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerParameterDescription;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view.RapidMinerConfigurationView;

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
				
				System.out.println(" Dialog position 1");
				
				
			Iterator myiter = panel.getConfiguration().getParameterDescriptions().iterator();
			
			while (myiter.hasNext()) {
				
				RapidMinerParameterDescription des = (RapidMinerParameterDescription) myiter.next();
				System.out.println("[DEBUG] new parameters to set " + des.getUseParameter() + " " + des.getExecutionValue());
				
			}
				
					ActivityConfigurationDialog.configureActivityStatic(owningDataflow, activity, panel.getConfiguration());
					
				System.out.println(" Dialog position 2");
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
