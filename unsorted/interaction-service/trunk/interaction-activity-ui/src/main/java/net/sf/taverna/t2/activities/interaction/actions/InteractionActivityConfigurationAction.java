/**
 *
 */
package net.sf.taverna.t2.activities.interaction.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;
import net.sf.taverna.t2.activities.interaction.view.InteractionActivityConfigView;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

/**
 * @author alanrw
 * 
 */
public class InteractionActivityConfigurationAction
		extends
		ActivityConfigurationAction<InteractionActivity, InteractionActivityConfigurationBean> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static final String EDIT_INTERACTION = "Edit interaction";

	public InteractionActivityConfigurationAction(
			final InteractionActivity activity, final Frame owner) {
		super(activity);
		this.putValue(NAME, EDIT_INTERACTION);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final ActivityConfigurationDialog<?, ?> currentDialog = ActivityConfigurationAction
				.getDialog(this.getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		final InteractionActivityConfigView interactionConfigView = new InteractionActivityConfigView(
				this.getActivity());
		final ActivityConfigurationDialog<InteractionActivity, InteractionActivityConfigurationBean> dialog = new ActivityConfigurationDialog<InteractionActivity, InteractionActivityConfigurationBean>(
				this.getActivity(), interactionConfigView);

		ActivityConfigurationAction.setDialog(this.getActivity(), dialog);
	}

}
