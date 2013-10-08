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
		ActivityConfigurationAction<InteractionActivity, InteractionActivityConfigurationBean> {

	public InteractionActivityConfigureAction(
			final InteractionActivity activity, final Frame owner) {
		super(activity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void actionPerformed(final ActionEvent e) {
		final ActivityConfigurationDialog<InteractionActivity, InteractionActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(this.getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		final InteractionActivityConfigView panel = new InteractionActivityConfigView(
				this.getActivity());
		final ActivityConfigurationDialog<InteractionActivity, InteractionActivityConfigurationBean> dialog = new ActivityConfigurationDialog<InteractionActivity, InteractionActivityConfigurationBean>(
				this.getActivity(), panel);

		ActivityConfigurationAction.setDialog(this.getActivity(), dialog);

	}

}
