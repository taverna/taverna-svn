package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;

@SuppressWarnings("serial")
public class RapidMinerConfigureAction
		extends
		ActivityConfigurationAction<RapidMinerActivity, RapidMinerActivityConfigurationBean> {

	public RapidMinerConfigureAction(RapidMinerActivity activity, Frame owner) {
		super(activity);
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog<RapidMinerActivity, RapidMinerActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		RapidMinerConfigurationPanel panel = new RapidMinerConfigurationPanel(
				getActivity());
		ActivityConfigurationDialog<RapidMinerActivity, RapidMinerActivityConfigurationBean> dialog = new ActivityConfigurationDialog<RapidMinerActivity, RapidMinerActivityConfigurationBean>(
				getActivity(), panel);

		ActivityConfigurationAction.setDialog(getActivity(), dialog);

	}

}
