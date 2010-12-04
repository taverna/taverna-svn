package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
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
		RapidMinerConfigurationView panel = new RapidMinerConfigurationView(getActivity());
		
		//ActivityConfigurationDialog<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean> dialog = new ActivityConfigurationDialog<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean>(
		//		getActivity(), panel);
		
				
		//ActivityConfigurationAction.setDialog(getActivity(), dialog);
		final HelpEnabledDialog dialog = new HelpEnabledDialog(owner, "Operator Configuration", true, null);
		dialog.add(panel);
		dialog.pack();
		dialog.setVisible(true);
	}

}

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
