package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;

@SuppressWarnings("serial")
public class RapidMinerConfigureAction
		extends
		ActivityConfigurationAction<RapidMinerExampleActivity, RapidMinerActivityConfigurationBean> {

	public RapidMinerConfigureAction(RapidMinerExampleActivity activity, Frame owner) {
		super(activity);
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
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

	}

}
