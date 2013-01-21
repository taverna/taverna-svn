package uk.ac.soton.mib104.t2.activities.oauth.ui.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivity;
import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivityConfigurationBean;

@SuppressWarnings("serial")
public class OAuthConfigureAction extends ActivityConfigurationAction<OAuthActivity, OAuthActivityConfigurationBean> {

	public OAuthConfigureAction(final OAuthActivity activity, final Frame owner) {
		super(activity);
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(final ActionEvent e) {
		final OAuthActivity activity = this.getActivity();
		
		final ActivityConfigurationDialog<OAuthActivity, OAuthActivityConfigurationBean> currentDialog = ActivityConfigurationAction.getDialog(activity);
		
		if (currentDialog != null) {
			currentDialog.toFront();
			
			return;
		}
		
		final OAuthConfigurationPanel panel = new OAuthConfigurationPanel(activity);
		
		final ActivityConfigurationDialog<OAuthActivity, OAuthActivityConfigurationBean> dialog = new ActivityConfigurationDialog<OAuthActivity, OAuthActivityConfigurationBean>(activity, panel);

		ActivityConfigurationAction.setDialog(activity, dialog);
		
		return;
	}

}
