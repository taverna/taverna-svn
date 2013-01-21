package uk.ac.soton.mib104.t2.activities.oauth.ui.menu;

import javax.swing.Action;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivity;
import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivityConfigurationBean;
import uk.ac.soton.mib104.t2.activities.oauth.ui.config.OAuthConfigureAction;
import uk.ac.soton.mib104.t2.activities.oauth.util.ApiDescImpl;

public class OAuthConfigureMenuAction extends AbstractConfigureActivityMenuAction<OAuthActivity> {

	public OAuthConfigureMenuAction() {
		super(OAuthActivity.class);
	}

	@Override
	protected Action createAction() {
		final OAuthActivity activity = this.findActivity();
		
		final OAuthActivityConfigurationBean configBean = activity.getConfiguration();
		
		final ApiDescImpl apiDesc = configBean.isRequestTokenRequired() ? ApiDescImpl.OAuth10a : ApiDescImpl.OAuth20_draft10;
		
		final Action result = new OAuthConfigureAction(activity, this.getParentFrame());
		
		result.putValue(Action.NAME, String.format("Configure %s Access Token service", apiDesc.getPreferredName()));
		
		this.addMenuDots(result);
		
		return result;
	}

}
