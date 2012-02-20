package net.sf.taverna.t2.activities.interaction.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;
import net.sf.taverna.t2.activities.interaction.view.InteractionActivityConfigView;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;


@SuppressWarnings("serial")
public class InteractionActivityConfigureAction
		extends
		ActivityConfigurationAction<InteractionActivity,
        InteractionActivityConfigurationBean> {

	public InteractionActivityConfigureAction(InteractionActivity activity, Frame owner) {
		super(activity);
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog<InteractionActivity, InteractionActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		InteractionActivityConfigView panel = new InteractionActivityConfigView(
				getActivity());
		ActivityConfigurationDialog<InteractionActivity,
        InteractionActivityConfigurationBean> dialog = new ActivityConfigurationDialog<InteractionActivity, InteractionActivityConfigurationBean>(
				getActivity(), panel);

		ActivityConfigurationAction.setDialog(getActivity(), dialog);

	}

}
